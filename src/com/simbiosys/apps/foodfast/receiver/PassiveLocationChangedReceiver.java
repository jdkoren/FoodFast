package com.simbiosys.apps.foodfast.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.simbiosys.apps.foodfast.service.PlacesUpdateService;
import com.simbiosys.apps.foodfast.service.PlacesUpdateServiceEclair;
import com.simbiosys.apps.foodfast.utils.AppConstants;
import com.simbiosys.apps.foodfast.utils.LastLocationFinder;

/**
 * Receiver to listen for location updates from the passive provider.
 * 
 */
public class PassiveLocationChangedReceiver extends BroadcastReceiver {
	private static final String TAG = "PassiveLocationChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Location location = null;
		Log.d(TAG, "Passive location receiver triggered");

		SharedPreferences prefs = context.getSharedPreferences(
			AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

		if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
			// This update came from a passive provider and contains a location
			location = (Location) intent.getExtras().get(LocationManager.KEY_LOCATION_CHANGED);
		} else {
			// This update came from a repeating alarm and has no location
			// extra. Get the last location from all providers.
			LastLocationFinder finder = new LastLocationFinder(context);

			location = finder.getLastKnownLocation(AppConstants.DEFAULT_SENSITIVITY, System
					.currentTimeMillis()
					- AppConstants.MAX_INTERVAL);

			// Get the last location we stored
			long lastTime = prefs.getLong(AppConstants.LAST_UPDATE_TIME, Long.MIN_VALUE);
			float lastLatitude = prefs.getFloat(AppConstants.LAST_UPDATE_LAT, Float.MIN_VALUE);
			float lastLongitude = prefs.getFloat(AppConstants.LAST_UPDATE_LONG, Float.MIN_VALUE);
			Location lastLocation = new Location(AppConstants.CONSTRUCTED_PROVIDER);
			lastLocation.setLatitude(lastLatitude);
			lastLocation.setLongitude(lastLongitude);

			// If the last location from the providers is close to the stored
			// one, or if not enough time has passed between them, we can skip
			// this update.
			if ((lastTime > System.currentTimeMillis() - AppConstants.MAX_INTERVAL)
					|| (lastLocation.distanceTo(location) < AppConstants.DEFAULT_SENSITIVITY)) {
				location = null;
			}
		}

		// Start the update service
		if (location != null) {
			Intent updateIntent = new Intent(context,
					AppConstants.SUPPORTS_ECLAIR ? PlacesUpdateServiceEclair.class
							: PlacesUpdateService.class);
			updateIntent.putExtra(AppConstants.EXTRA_LOCATION, location);
			updateIntent.putExtra(AppConstants.EXTRA_RADIUS, prefs.getInt(
				AppConstants.PREF_SEARCH_RADIUS, AppConstants.DEFAULT_RADIUS));
			updateIntent.putExtra(AppConstants.EXTRA_FORCE_UPDATE, true);
			context.startService(updateIntent);
		}
	}
}
