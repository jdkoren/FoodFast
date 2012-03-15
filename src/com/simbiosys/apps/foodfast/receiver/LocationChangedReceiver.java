package com.simbiosys.apps.foodfast.receiver;

import com.simbiosys.apps.foodfast.service.PlacesUpdateService;
import com.simbiosys.apps.foodfast.service.PlacesUpdateServiceEclair;
import com.simbiosys.apps.foodfast.service.PlacesUpdateServiceICS;
import com.simbiosys.apps.foodfast.utils.AppConstants;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * Receiver to listen for location change broadcasts from an active provider.
 * 
 */
public class LocationChangedReceiver extends BroadcastReceiver {
	private static final String TAG = "LocationChangedReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		// Check if the provider has been disabled
		if (intent.hasExtra(LocationManager.KEY_PROVIDER_ENABLED)) {
			boolean providerEnabled = intent.getBooleanExtra(LocationManager.KEY_PROVIDER_ENABLED,
				false);
			if (!providerEnabled) {
				// Send a broadcast that the provider has been disabled
				Intent notifyProviderDisabled = new Intent(
						AppConstants.ACTION_LOCATION_PROVIDER_DISABLED);
				context.sendBroadcast(notifyProviderDisabled);
			}
		}

		// Check for a location
		if (intent.hasExtra(LocationManager.KEY_LOCATION_CHANGED)) {
			Location location = (Location) intent.getExtras().get(
				LocationManager.KEY_LOCATION_CHANGED);
			Log.d(TAG, "Got location lat=" + location.getLatitude() + ", long="
					+ location.getLongitude());

			SharedPreferences prefs = context.getSharedPreferences(
				AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);

			// Start the update service and give it the new location
			Intent updateIntent = new Intent(context,
					AppConstants.SUPPORTS_ICS ? PlacesUpdateServiceICS.class
							: AppConstants.SUPPORTS_ECLAIR ? PlacesUpdateServiceEclair.class
									: PlacesUpdateService.class);
			updateIntent.putExtra(AppConstants.EXTRA_LOCATION, location);
			updateIntent.putExtra(AppConstants.EXTRA_RADIUS, prefs.getInt(
				AppConstants.PREF_SEARCH_RADIUS, AppConstants.DEFAULT_RADIUS));
			updateIntent.putExtra(AppConstants.EXTRA_FORCE_UPDATE, true);
			context.startService(updateIntent);
		}

	}

}