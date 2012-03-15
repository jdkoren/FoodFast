package com.simbiosys.apps.foodfast.service;

import java.io.IOException;
import java.net.HttpURLConnection;
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
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.util.Log;

import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.receiver.ConnectivityReceiver;
import com.simbiosys.apps.foodfast.receiver.LocationChangedReceiver;
import com.simbiosys.apps.foodfast.receiver.PassiveLocationChangedReceiver;
import com.simbiosys.apps.foodfast.utils.AppConstants;

/**
 * Service which handles updating the place details for a given place.
 */
public class PlaceDetailsUpdateService extends IntentService {
	private static final String TAG = "PlaceDetailsUpdateService";

	protected ContentResolver contentResolver;
	protected ConnectivityManager connectivityManager;
	protected PowerManager powerManager;
	protected SharedPreferences prefs;

	public PlaceDetailsUpdateService() {
		super(TAG);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		contentResolver = getContentResolver();
		connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
		prefs = getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
	}

	/**
	 * Checks background data enabled setting on the phone.
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

	@Override
	protected void onHandleIntent(Intent intent) {
		// If we're in the background and background data isn't enabled, we
		// can't do anything.
		boolean inBackground = prefs.getBoolean(AppConstants.IN_BACKGROUND, true);
		if (inBackground && !getBackgroundEnabledSetting()) {
			return;
		}

		// Get reference and id for this place
		String id = intent.getStringExtra(AppConstants.EXTRA_ID);
		String reference = intent.getStringExtra(AppConstants.EXTRA_REFERENCE);

		// Check if we're connected to a data network
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

		if (isConnected) {
			// Determine whether to force the update. This will usually be the
			// case when displaying place details to the user.
			boolean forceUpdate = intent.getBooleanExtra(AppConstants.EXTRA_FORCE_UPDATE, false);
			boolean performUpdate = (id == null) || forceUpdate;

			if (!performUpdate) {
				// The update isn't forced. If battery is low, we'll skip it.
				IntentFilter batteryFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
				Intent batteryStatus = registerReceiver(null, batteryFilter);
				if (getIsLowBattery(batteryStatus)) {
					performUpdate = false;
				}
			}

			if (!performUpdate) {
				// The update isn't forced. Check if we have data about this
				// place and whether that data is fresh enough.

				Uri uri = PlaceDetails.buildPlaceDetailUri(id);
				Cursor cursor = contentResolver.query(uri, new String[] { PlaceDetails.PLACE_ID,
						PlaceDetails.PLACE_LAST_UPDATED }, null, null, null);

				try {
					performUpdate = true;
					// If the cursor found a record and the last update time
					// isn't too far in the past, we can skip this update.
					if (cursor.moveToFirst()) {
						long minTime = System.currentTimeMillis() - AppConstants.MAX_REFRESH_DELAY;
						if (cursor.getLong(cursor.getColumnIndex(PlaceDetails.PLACE_LAST_UPDATED)) > minTime) {
							performUpdate = false;
						}
					}
				} finally {
					cursor.close();
				}
			}

			if (performUpdate) {
				getPlaceDetails(reference);
			}
		} else {
			// Notify that an update could not complete because of no data
			// connection.
			onFinishUpdate();

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
		}
	}

	protected void getPlaceDetails(String reference) {
		// Build place search URL
		final String HTTP_PARAM_REFERENCE = "&reference=" + reference;
		StringBuilder urlBuilder = new StringBuilder(AppConstants.PLACE_DETAILS_SEARCH_BASE_URI);
		urlBuilder.append(AppConstants.HTTP_PARAM_API_KEY);
		urlBuilder.append(HTTP_PARAM_REFERENCE);

		// Make web service call
		try {
			URL url = new URL(urlBuilder.toString());
			URLConnection connection = url.openConnection();
			HttpsURLConnection httpConnection = (HttpsURLConnection) connection;

			if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				Log.d(TAG, "HTTP response OK");
				// Response OK, parse response using XML Pull Parser
				XmlPullParserFactory xppFactory = XmlPullParserFactory.newInstance();
				xppFactory.setNamespaceAware(true);
				XmlPullParser xpp = xppFactory.newPullParser();
				xpp.setInput(httpConnection.getInputStream(), null);

				int eventType = xpp.getEventType();
				while (eventType != XmlPullParser.END_DOCUMENT) {
					if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("result")) {
						// Started "result" tag
						eventType = xpp.next();
						String id = "";
						String name = "";
						String address = "";
						String phone = "";
						String latitude = "";
						String longitude = "";
						String icon = "";
						String types = "";
						String vicinity = "";
						float rating = 0;
						String placeUrl = "";
						String website = "";
						// Process child tags until we get to the end tag for
						// this result.
						while (!(eventType == XmlPullParser.END_TAG && xpp.getName().equals(
							"result"))) {
							if (eventType == XmlPullParser.START_TAG && xpp.getName().equals("id")) {
								id = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("name")) {
								name = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("formatted_address")) {
								address = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("formatted_phone_number")) {
								phone = xpp.nextText();
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
									&& xpp.getName().equals("type")) {
								types = types == "" ? xpp.nextText() : types + " " + xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("vicinity")) {
								vicinity = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("rating")) {
								rating = Float.parseFloat(xpp.nextText());
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("url")) {
								placeUrl = xpp.nextText();
							} else if (eventType == XmlPullParser.START_TAG
									&& xpp.getName().equals("website")) {
								website = xpp.nextText();
							}
							eventType = xpp.next();
						} // end while not end of result tag
						Location resultLocation = new Location(AppConstants.CONSTRUCTED_PROVIDER);
						resultLocation.setLatitude(Double.parseDouble(latitude));
						resultLocation.setLongitude(Double.parseDouble(longitude));

						addPlaceDetail(resultLocation, id, name, address, phone, icon, reference,
							types, vicinity, rating, placeUrl, website);
					} // end if start of result tag

					eventType = xpp.next();
				} // end while not end of document
			}
		} catch (NumberFormatException e) {
			Log.e(TAG, e.getMessage());
			// onFinishUpdate();
		} catch (MalformedURLException e) {
			Log.e(TAG, e.getMessage());
			// onFinishUpdate();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage());
			// onFinishUpdate();
		} catch (XmlPullParserException e) {
			Log.e(TAG, e.getMessage());
			// onFinishUpdate();
		} 
	}

	protected boolean addPlaceDetail(Location location, String id, String name, String address,
			String phone, String icon, String reference, String types, String vicinity,
			float rating, String placeUrl, String website) {
		boolean result = false;

		// Create content values to insert/update
		ContentValues values = new ContentValues();
		values.put(PlaceDetails.PLACE_ID, id);
		values.put(PlaceDetails.PLACE_NAME, name);
		values.put(PlaceDetails.PLACE_ADDRESS, address);
		values.put(PlaceDetails.PLACE_PHONE, phone);
		values.put(PlaceDetails.PLACE_LATITUDE, location.getLatitude());
		values.put(PlaceDetails.PLACE_LONGITUDE, location.getLongitude());
		values.put(PlaceDetails.PLACE_ICON, icon);
		values.put(PlaceDetails.PLACE_REFERENCE, reference);
		values.put(PlaceDetails.PLACE_TYPES, types);
		values.put(PlaceDetails.PLACE_VICINITY, vicinity);
		values.put(PlaceDetails.PLACE_RATING, rating);
		values.put(PlaceDetails.PLACE_URL, placeUrl);
		values.put(PlaceDetails.PLACE_WEBSITE, website);
		values.put(PlaceDetails.PLACE_LAST_UPDATED, System.currentTimeMillis());

		try {
			// Database table uses ON CONFLICT REPLACE, so we can just call
			// insert
			if (contentResolver.insert(PlaceDetails.CONTENT_URI, values) != null) {
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
	private void onFinishUpdate() {
		Log.d(TAG, "Update service finished");

		Intent intent = new Intent(AppConstants.ACTION_UPDATE_SERVICE_RESULT);
		sendBroadcast(intent);
	}
}
