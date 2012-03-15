package com.simbiosys.apps.foodfast.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.simbiosys.apps.foodfast.receiver.LocationChangedReceiver;
import com.simbiosys.apps.foodfast.receiver.PassiveLocationChangedReceiver;
import com.simbiosys.apps.foodfast.service.PlacesUpdateService;
import com.simbiosys.apps.foodfast.service.PlacesUpdateServiceEclair;
import com.simbiosys.apps.foodfast.service.PlacesUpdateServiceICS;

/**
 * Helper class to manage all the location-based plumbing behind the scenes.
 * 
 */
public class LocationHelper {
	private static final String TAG = "LocationHelper";

	protected Context context;
	protected boolean activeUpdatesOn;

	protected LocationManager locationManager;
	protected Criteria criteria;

	protected SharedPreferences prefs;
	protected Editor prefsEditor;

	protected PendingIntent activeListenerPendingIntent;
	protected PendingIntent passiveListenerPendingIntent;

	protected ILastLocationFinder lastLocationFinder;
	protected LocationUpdateRequester locationUpdateRequester;

	public LocationHelper(Context context) {
		this.context = context;
		activeUpdatesOn = false;

		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		prefs = context.getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE,
			Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();

		// Specify criteria to use for location updates
		criteria = new Criteria();

		// Set up location pending intents
		Intent activeListenerIntent = new Intent(context, LocationChangedReceiver.class);
		activeListenerPendingIntent = PendingIntent.getBroadcast(context, 0, activeListenerIntent,
			PendingIntent.FLAG_UPDATE_CURRENT);

		Intent passiveListenerIntent = new Intent(context, PassiveLocationChangedReceiver.class);
		passiveListenerPendingIntent = PendingIntent.getBroadcast(context, 0,
			passiveListenerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

		// Make a last location finder
		lastLocationFinder = AppConstants.SUPPORTS_GINGERBREAD ? new LastLocationFinderGingerbread(
				context) : new LastLocationFinder(context);
		lastLocationFinder.setLocationListener(oneTimeUpdateListener);

		// Make a LocationUpdateRequester, used to request location updates
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		locationUpdateRequester = AppConstants.SUPPORTS_FROYO ? new LocationUpdateRequesterFroyo(
				locationManager)
				: new LocationUpdateRequesterCupcake(locationManager, alarmManager);

	}

	private void setCriteria() {
		if (prefs.getBoolean(AppConstants.PREF_USE_GPS, AppConstants.DEFAULT_USE_GPS)) {
			criteria.setAccuracy(Criteria.ACCURACY_FINE);
		} else {
			criteria.setAccuracy(Criteria.POWER_LOW);
		}
	}

	/**
	 * Start listening for passive location updates based on preferences.
	 */
	public void startPassiveUpdates() {
		if (prefs.getBoolean(AppConstants.PREF_PASSIVE_UPDATES_ALLOWED,
			AppConstants.DEFAULT_PASSIVE_UPDATES_ALLOWED)) {
			int distance = prefs.getInt(AppConstants.PREF_LOCATION_SENSITIVITY, AppConstants.DEFAULT_SENSITIVITY);
			locationUpdateRequester.requestPassiveLocationUpdates(AppConstants.MAX_INTERVAL,
				distance, passiveListenerPendingIntent);
		}
	}

	/**
	 * Stop listening for passive location updates based on preferences.
	 */
	public void stopPassiveUpdates() {
		// Cancel the LastLocationFinder in case it's waiting for an update.
		lastLocationFinder.cancel();

		// Conditionally disable passive listener based on preferences.
		if (prefs.getBoolean(AppConstants.PREF_DISABLE_PASSIVE_ON_EXIT,
			AppConstants.DEFAULT_DISABLE_PASSIVE_ON_EXIT)) {
			locationManager.removeUpdates(passiveListenerPendingIntent);
		}
	}

	public void togglePassiveUpdates(boolean enableUpdates) {
		if (enableUpdates) {
			startPassiveUpdates();
		} else {
			stopPassiveUpdates();
		}
	}

	/**
	 * Gets the last location and starts listening for active location updates
	 * if enabled in preferences.
	 */
	public void startActiveUpdates() {
		getLocation();
		if (!activeUpdatesOn
				&& prefs.getBoolean(AppConstants.PREF_ACTIVE_UPDATES_ALLOWED,
					AppConstants.DEFAULT_ACTIVE_UPDATES_ALLOWED)) {
			enableActiveLocationUpdates();
		}
	}

	/**
	 * Stop listening for active location updates
	 */
	public void stopActiveUpdates() {
		if (activeUpdatesOn) {
			// Disable active updates
			locationManager.removeUpdates(activeListenerPendingIntent);
			// Stop listening for provider to become disabled
			context.unregisterReceiver(providerDisabledReceiver);
			// Stop listening for better provider to become available
			locationManager.removeUpdates(inactiveProviderListener);
			activeUpdatesOn = false;
		}
	}

	/**
	 * Toggle whether we want to receive location updates while in the
	 * foreground.
	 */
	public void toggleActiveUpdates(boolean enableUpdates) {
		if (enableUpdates) {
			enableActiveLocationUpdates();
		} else {
			stopActiveUpdates();
		}
	}

	/**
	 * Called when the application comes to the foreground to find the current
	 * location.
	 */
	protected void getLocation() {
		// Use a worker thread to get our last location
		AsyncTask<Void, Void, Void> getLocationTask = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... params) {
				// Find last known location. May trigger a one-time update
				Location lastLocation = lastLocationFinder.getLastKnownLocation(prefs.getInt(
					AppConstants.PREF_LOCATION_SENSITIVITY, AppConstants.DEFAULT_SENSITIVITY), System
						.currentTimeMillis()
						- AppConstants.MAX_INTERVAL);

				// Update places around the last known location. The
				// LocationUpaterService will determine whether we can skip the
				// update or not.
				updatePlaces(lastLocation, prefs.getInt(AppConstants.PREF_SEARCH_RADIUS,
					AppConstants.DEFAULT_RADIUS), false);
				return null;
			}
		};
		getLocationTask.execute();
	}

	/**
	 * Start listening for location updates
	 */
	public void enableActiveLocationUpdates() {
		setCriteria();
		// Request active updates while we're in the foreground
		locationUpdateRequester.requestLocationUpdates(criteria, AppConstants.MAX_INTERVAL,
			AppConstants.DEFAULT_SENSITIVITY, activeListenerPendingIntent);

		// Register to listen for when the location provider has become disabled
		IntentFilter intentFilter = new IntentFilter(AppConstants.ACTION_LOCATION_PROVIDER_DISABLED);
		context.registerReceiver(providerDisabledReceiver, intentFilter);

		// Register to listen for when a better provider becomes enabled
		String provider = locationManager.getBestProvider(criteria, true);
		String bestProvider = locationManager.getBestProvider(criteria, false);
		if (provider != null && bestProvider != null && !provider.equals(bestProvider)) {
			Log.d(TAG, "Listening for " + bestProvider + " provider to be enabled");
			locationManager.requestLocationUpdates(bestProvider, 0, 0, inactiveProviderListener,
				context.getMainLooper());
		}
		activeUpdatesOn = true;
		Log.d(TAG, "Active updates on? " + activeUpdatesOn);
	}

	/**
	 * A listener to receive a single update from the LastLocationFinder if it
	 * forces a location update.
	 */
	protected LocationListener oneTimeUpdateListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updatePlaces(location, prefs.getInt(AppConstants.PREF_SEARCH_RADIUS,
				AppConstants.DEFAULT_RADIUS), true);
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onProviderDisabled(String provider) {
		}
	};

	/**
	 * A listener to receive updates when the best possible inactive location
	 * provider becomes enabled, so that we can register again with the best
	 * provider available.
	 */
	protected LocationListener inactiveProviderListener = new LocationListener() {
		public void onLocationChanged(Location location) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
			// Re-register location listeners
			Log.d(TAG, "Better provider enabled, re-registering for updates");
			enableActiveLocationUpdates();
		}
	};

	/**
	 * A receiver to be notified when the current location provider becomes
	 * disabled, so that we can register again with the another available
	 * provider.
	 */
	protected BroadcastReceiver providerDisabledReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			boolean providerEnabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED,
				false);
			if (!providerEnabled) {
				// Re-register location listeners
				Log.d(TAG, "Provider disabled, re-registering for updates");
				enableActiveLocationUpdates();
			}

		}
	};

	/**
	 * A receiver to be notified when an update fails due to lack of data
	 * connection.
	 */
	protected BroadcastReceiver updateFailedReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			// TODO Notify user that update failed?
			Log.d(TAG, "updateFailedReceiver received broadcast");
		}
	};

	/**
	 * Forces an update using the last known location.
	 */
	public void performRefresh() {
		// Get last known location.
		Location lastLocation = lastLocationFinder.getLastKnownLocation(prefs.getInt(
			AppConstants.PREF_LOCATION_SENSITIVITY, AppConstants.DEFAULT_SENSITIVITY), System
				.currentTimeMillis()
				- AppConstants.MAX_INTERVAL);

		// Update places around the last known location.
		updatePlaces(lastLocation, prefs.getInt(AppConstants.PREF_SEARCH_RADIUS,
			AppConstants.DEFAULT_RADIUS), true);
	}

	/**
	 * Starts the update service to refresh the list of nearby places.
	 */
	protected void updatePlaces(Location location, int radius, boolean forceUpdate) {
		if (location != null) {
			Intent updateIntent = new Intent(context,
					AppConstants.SUPPORTS_ICS ? PlacesUpdateServiceICS.class
							: AppConstants.SUPPORTS_ECLAIR ? PlacesUpdateServiceEclair.class
									: PlacesUpdateService.class);
			updateIntent.putExtra(AppConstants.EXTRA_LOCATION, location);
			updateIntent.putExtra(AppConstants.EXTRA_RADIUS, AppConstants.DEFAULT_RADIUS);
			updateIntent.putExtra(AppConstants.EXTRA_FORCE_UPDATE, forceUpdate);
			context.startService(updateIntent);
		} else {
			// Can't perform update. Send an update finished broadcast.
			Log.e(TAG, "Couldn't update; no known location.");
			Intent intent = new Intent(AppConstants.ACTION_UPDATE_SERVICE_RESULT);
			context.sendBroadcast(intent);
		}
	}
}
