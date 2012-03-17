package com.simbiosys.apps.foodfast.utils;

import android.app.PendingIntent;
import android.location.LocationManager;

/**
 * Platform-specific implementation of LocationUpdateRequester for Froyo and later.
 */
public class LocationUpdateRequesterFroyo extends LocationUpdateRequester {
	private static final String TAG = "LocationUpdateRequesterFroyo";

	public LocationUpdateRequesterFroyo(LocationManager locationManager) {
		super(locationManager);

	}

	@Override
	public void requestPassiveLocationUpdates(long maxInterval, long maxDistance, PendingIntent pendingIntent) {
		// Use the new Passive Provider introduced in Froyo
		locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, maxInterval, maxDistance, pendingIntent);
	}

}
