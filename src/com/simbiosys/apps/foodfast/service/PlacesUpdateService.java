package com.simbiosys.apps.foodfast.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.HttpsURLConnection;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.app.IntentService;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;

import com.simbiosys.apps.foodfast.provider.PlacesContract;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;
import com.simbiosys.apps.foodfast.receiver.ConnectivityReceiver;
import com.simbiosys.apps.foodfast.receiver.LocationChangedReceiver;
import com.simbiosys.apps.foodfast.receiver.PassiveLocationChangedReceiver;
import com.simbiosys.apps.foodfast.utils.AppConstants;

/**
 * Service which handles updating the list of nearest places.
 */
public class PlacesUpdateService extends IntentService {
	private static String TAG = "PlacesUpdateService";

	// update succeeded
	public static final int RESULT_OK = 0;
	// skipped update, we have relevant data already
	public static final int RESULT_SKIPPED_UPDATE = 1;
	// background data setting disabled and we're in background
	public static final int RESULT_ABORTED_BG_DATA_DISABLED = 2;
	// low battery
	public static final int RESULT_ABORTED_LOW_BATTERY = 3;
	// no data connection
	public static final int RESULT_ABORTED_NO_CONNECTION = 4;
	// http error
	public static final int RESULT_FAILED_HTTP_ERROR = 5;

	protected ContentResolver contentResolver;
	protected ConnectivityManager connectivityManager;
	protected SharedPreferences prefs;
	protected Editor prefsEditor;

	public PlacesUpdateService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		contentResolver = getContentResolver();
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		prefs = getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
	}

	/**
	 * Checks the background data enabled setting on the phone.
	 */
	protected boolean getBackgroundEnabledSetting() {
		return connectivityManager.getBackgroundDataSetting();
	}

	/**
	 * Returns true if we're on low battery (less than 15%)
	 */
	protected boolean getIsLowBattery(Intent battery) {
		int batteryLevel = battery.getIntExtra(BatteryManager.EXTRA_LEVEL, 1);
		int batteryScale = battery.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
		float percentLevel = batteryLevel / (float) batteryScale;
		return percentLevel < 0.15;

	}

	/*
	 * Removes old places from the database and requests new ones from the
	 * server using the newest location.
	 * 
	 * @see android.app.IntentService#onHandleIntent(android.content.Intent)
	 */
	@Override
	protected void onHandleIntent(Intent intent) {
		// If we're in the background and background data isn't enabled, we
		// can't do anything.
		boolean inBackground = prefs.getBoolean(AppConstants.IN_BACKGROUND, true);
		if (inBackground && !getBackgroundEnabledSetting()) {
			onFinishUpdate(RESULT_ABORTED_BG_DATA_DISABLED);
			return;
		}

		// Get the location around which to search
		Location location = new Location(AppConstants.CONSTRUCTED_PROVIDER);
		int radius = AppConstants.DEFAULT_RADIUS;

		Bundle extras = intent.getExtras();
		if (extras.containsKey(AppConstants.EXTRA_LOCATION)) {
			location = (Location) extras.get(AppConstants.EXTRA_LOCATION);
			radius = extras.getInt(AppConstants.EXTRA_RADIUS, AppConstants.DEFAULT_RADIUS);
		}

		// Check if we're connected to a data network
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		if (isConnected) {
			// If we're connected, check to see if it's a forced update. If it's
			// not, check to see how far we've moved or how long it's been since
			// the last update.
			boolean performUpdate = extras.getBoolean(AppConstants.EXTRA_FORCE_UPDATE, false);

			if (!performUpdate) {
				// The update isn't forced. If battery is low, we'll skip it.
				IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				Intent batteryStatus = registerReceiver(null, batteryFilter);
				if (getIsLowBattery(batteryStatus)) {
					onFinishUpdate(RESULT_ABORTED_LOW_BATTERY);
					return;
				}

				// Alternatively, if we haven't updated in a long time
				// or we've moved far enough from our last update, we should
				// update.
				long lastTime = prefs.getLong(AppConstants.LAST_UPDATE_TIME, Long.MIN_VALUE);
				float lastLatitude = prefs.getFloat(AppConstants.LAST_UPDATE_LAT, Float.MIN_VALUE);
				float lastLongitude = prefs
						.getFloat(AppConstants.LAST_UPDATE_LONG, Float.MIN_VALUE);
				Location lastLocation = new Location(AppConstants.CONSTRUCTED_PROVIDER);
				lastLocation.setLatitude(lastLatitude);
				lastLocation.setLongitude(lastLongitude);

				long minTime = System.currentTimeMillis() - AppConstants.MAX_INTERVAL;

				int locationSensitivity = prefs.getInt(AppConstants.PREF_LOCATION_SENSITIVITY,
					AppConstants.DEFAULT_SENSITIVITY);

				if ((lastTime < minTime)
						|| (location.distanceTo(lastLocation) > locationSensitivity)) {
					performUpdate = true;
				}
			}

			if (performUpdate) {
				Log.d(TAG, "performing update");
				// Remove old location details from database
				removeOldPlaces();

				// Get new list of places
				searchPlaces(location, radius);
			} else {
				onFinishUpdate(RESULT_SKIPPED_UPDATE);
			}
		} else {
			// If we're not connected, enable the connectivity receiver and
			// disable the location receivers. The connectivity receiver will
			// reverse these operations when we're connected again.
			PackageManager packageManager = getPackageManager();

			ComponentName connectivityReceiver = new ComponentName(this, ConnectivityReceiver.class);
			ComponentName locationReceiver = new ComponentName(this, LocationChangedReceiver.class);
			ComponentName passiveLocationReceiver = new ComponentName(this,
					PassiveLocationChangedReceiver.class);

			packageManager.setComponentEnabledSetting(connectivityReceiver,
				PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			packageManager.setComponentEnabledSetting(locationReceiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
			packageManager.setComponentEnabledSetting(passiveLocationReceiver,
				PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

			// Notify that an update could not complete because of no data
			// connection.
			onFinishUpdate(RESULT_ABORTED_NO_CONNECTION);
		}
	}

	/**
	 * Removes place details from the database if they're older than the max
	 * refresh delay period.
	 */
	protected void removeOldPlaces() {
		long minTime = System.currentTimeMillis() - AppConstants.MAX_REFRESH_DELAY;
		String where = PlaceDetails.PLACE_LAST_UPDATED + " < " + minTime;
		getContentResolver().delete(PlacesContract.PlaceDetails.CONTENT_URI, where, null);
	}

	/**
	 * Call the Places server to get new places around the current location.
	 */
	protected void searchPlaces(Location location, int radius) {
		long currentTime = System.currentTimeMillis();

		// Build request url
		final String HTTP_PARAM_LOCATION = "&location=" + location.getLatitude() + ","
				+ location.getLongitude();
		final String HTTP_PARAM_RADIUS = "&radius=" + radius;

		StringBuilder urlBuilder = new StringBuilder(AppConstants.PLACES_SEARCH_BASE_URI);
		urlBuilder.append(AppConstants.HTTP_PARAM_API_KEY);
		urlBuilder.append(AppConstants.HTTP_PARAM_TYPE);
		urlBuilder.append(HTTP_PARAM_LOCATION);
		urlBuilder.append(HTTP_PARAM_RADIUS);

		// Make web service call
		try {
			URL url = new URL(urlBuilder.toString());
			URLConnection connection = url.openConnection();
			HttpsURLConnection httpConnection = (HttpsURLConnection) connection;

			if (httpConnection.getResponseCode() == HttpsURLConnection.HTTP_OK) {
				// Response OK, parse the response
				Log.d(TAG, "Places search HTTP response OK");
				XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
				xppFactory.setNamespaceAware(true);
				XmlPullParser xpp = xppFactory.newPullParser();
				xpp.setInput(httpConnection.getInputStream(), null);

				int eventType = xpp.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("result")) {
						// Started a "result" tag
						eventType = xpp.next();
						String id = "";
						String name = "";
						String latitude = "";
						String longitude = "";
						String icon = "";
						String types = "";
						String reference = "";
						String vicinity = "";
						while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals(
							"result"))) {
							// Process child tags until we get to the end tag
							// for this result.
							if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("id")) {
								id = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("name")) {
								name = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("lat")) {
								latitude = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("lng")) {
								longitude = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("icon")) {
								icon = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("reference")) {
								reference = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("type")) {
								types = types == "" ? xpp.nextText() : types + " " + xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("vicinity")) {
								vicinity = xpp.nextText();
							}
							eventType = xpp.next();
						} // end while not end of result tag
						Location resultLocation = new Location(AppConstants.CONSTRUCTED_PROVIDER);
						resultLocation.setLatitude(Double.parseDouble(latitude));
						resultLocation.setLongitude(Double.parseDouble(longitude));

						// Add place to the database
						addPlace(location, resultLocation, id, name, icon, reference, types,
							vicinity, currentTime);

					} // end if start result tag
					eventType = xpp.next();
				} // end while not end of document

				// Remove from database any places not inserted by this update
				String where = Places.PLACE_LAST_UPDATED + " < " + currentTime;
				contentResolver.delete(Places.CONTENT_URI, where, null);

				// Save last update information to shared preferences
				prefsEditor.putLong(AppConstants.LAST_UPDATE_TIME, currentTime);
				prefsEditor.putFloat(AppConstants.LAST_UPDATE_LAT, (float) location.getLatitude());
				prefsEditor
						.putFloat(AppConstants.LAST_UPDATE_LONG, (float) location.getLongitude());
				prefsEditor.commit();

				onFinishUpdate(RESULT_OK);
			} else {
				Log.e(TAG, httpConnection.getResponseCode() + ":"
						+ httpConnection.getResponseMessage());
				onFinishUpdate(RESULT_FAILED_HTTP_ERROR);
			}

		} catch (XmlPullParserException e) {
			Log.e(TAG, e.getMessage());
			onFinishUpdate(RESULT_FAILED_HTTP_ERROR);
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
			onFinishUpdate(RESULT_FAILED_HTTP_ERROR);
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			onFinishUpdate(RESULT_FAILED_HTTP_ERROR);
		}

	}

	/**
	 * Adds a place to the database.
	 * 
	 * @return true if the place was inserted successfully
	 */
	protected boolean addPlace(Location currentLocation, Location resultLocation, String placeId,
			String name, String icon, String reference, String types, String vicinity,
			long currentTime) {
		boolean result = false;

		// Calculate distance to result location
		float distance = 0;
		if (currentLocation != null && resultLocation != null) {
			distance = currentLocation.distanceTo(resultLocation);
		}

		// Create content values to insert/update
		ContentValues values = new ContentValues();
		values.put(Places.PLACE_ID, placeId);
		values.put(Places.PLACE_NAME, name);
		values.put(Places.PLACE_LATITUDE, resultLocation.getLatitude());
		values.put(Places.PLACE_LONGITUDE, resultLocation.getLongitude());
		values.put(Places.PLACE_DISTANCE, distance);
		values.put(Places.PLACE_ICON, icon);
		values.put(Places.PLACE_REFERENCE, reference);
		values.put(Places.PLACE_TYPES, types);
		values.put(Places.PLACE_VICINITY, vicinity);
		values.put(Places.PLACE_LAST_UPDATED, currentTime);

		try {
			// Database table uses ON CONFLICT REPLACE, so we can just call
			// insert
			if (contentResolver.insert(Places.CONTENT_URI, values) != null) {
				result = true;
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}

		return result;
	}

	/**
	 * Sends a broadcast that this update finished.
	 */
	private void onFinishUpdate(int resultCode) {
		Log.d(TAG, "Update service finished with result: " + resultCode);

		Intent intent = new Intent(AppConstants.ACTION_UPDATE_SERVICE_RESULT);
		intent.putExtra(AppConstants.EXTRA_UPDATE_RESULT, resultCode);
		sendBroadcast(intent);
	}

}
