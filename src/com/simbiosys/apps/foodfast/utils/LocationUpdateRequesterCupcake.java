package com.simbiosys.apps.foodfast.utils;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.location.LocationManager;

/**
 * Platform-specific implementation of LocationUpdateRequester for Cupcake and later.
 * 
 */
public class LocationUpdateRequesterCupcake extends LocationUpdateRequester {
	@SuppressWarnings("unused")
	private static final String TAG = "LocationUpdateRequesterCupcake";

	protected AlarmManager alarmManager;

	public LocationUpdateRequesterCupcake(LocationManager locationManager, AlarmManager alarmManager) {
		super(locationManager);
		this.alarmManager = alarmManager;
	}

	public void requestPassiveLocationUpdates(long maxInterval, long maxDistance,
			PendingIntent pendingIntent) {
		// Set an alarm to to trigger our location receiver periodically.
		alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis()
				+ maxInterval, maxInterval, pendingIntent);
	}
}
