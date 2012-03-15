package com.simbiosys.apps.foodfast.utils;

import android.app.PendingIntent;
import android.location.Criteria;
import android.location.LocationManager;

/**
 * A utility class to request active and passive location updates.
 *
 */
public abstract class LocationUpdateRequester {
	@SuppressWarnings("unused")
	private static final String TAG = "LocationUpdateRequester";

	protected LocationManager locationManager;

	protected LocationUpdateRequester(LocationManager locationManager) {
		this.locationManager = locationManager;
	}

	public void requestLocationUpdates(Criteria criteria, long maxInterval, long maxDistance,
			PendingIntent pendingIntent) {
		String provider = locationManager.getBestProvider(criteria, true);
		if (provider != null) {
			locationManager.requestLocationUpdates(provider, maxInterval, maxDistance, pendingIntent);
		}
	}

	public abstract void requestPassiveLocationUpdates(long maxInterval, long maxDistance,
			PendingIntent pendingIntent);
}
