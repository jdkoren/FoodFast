package com.simbiosys.apps.foodfast.receiver;

import com.simbiosys.apps.foodfast.utils.AppConstants;
import com.simbiosys.apps.foodfast.utils.LocationUpdateRequester;
import com.simbiosys.apps.foodfast.utils.LocationUpdateRequesterCupcake;
import com.simbiosys.apps.foodfast.utils.LocationUpdateRequesterFroyo;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;

/**
 * On system boot, this receiver will turn on passive location updates if the
 * app has been launched at least once and if passive location updates are
 * enabled in preferences.
 * 
 */
public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		SharedPreferences prefs = context.getSharedPreferences(
			AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
		
		boolean launchedOnce = prefs.getBoolean(AppConstants.LAUNCHED_ONCE, false);
		boolean passiveAllowed = prefs.getBoolean(AppConstants.PREF_PASSIVE_UPDATES_ALLOWED,
			AppConstants.DEFAULT_PASSIVE_UPDATES_ALLOWED);

		if (launchedOnce && passiveAllowed) {
			// Make a location update requester
			LocationManager locationManager = (LocationManager) context
					.getSystemService(Context.LOCATION_SERVICE);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Context.ALARM_SERVICE);
			LocationUpdateRequester locationUpdateRequester = AppConstants.SUPPORTS_FROYO ? new LocationUpdateRequesterFroyo(
					locationManager)
					: new LocationUpdateRequesterCupcake(locationManager, alarmManager);

			// Set up pending intents for passive location updates
			Intent passiveListenerIntent = new Intent(context, PassiveLocationChangedReceiver.class);
			PendingIntent passiveListenerPendingIntent = PendingIntent.getBroadcast(context, 0,
				passiveListenerIntent, PendingIntent.FLAG_UPDATE_CURRENT);

			// Request updates
			locationUpdateRequester.requestPassiveLocationUpdates(AppConstants.MAX_INTERVAL,
				AppConstants.DEFAULT_SENSITIVITY, passiveListenerPendingIntent);
		}
	}

}
