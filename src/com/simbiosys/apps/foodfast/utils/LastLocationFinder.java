package com.simbiosys.apps.foodfast.utils;

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * Finds the last known location using whatever location providers are
 * available, while attempting to find the most timely and most accurate. When
 * necessary, it will find the current location using a single location update.
 * 
 */
public class LastLocationFinder implements ILastLocationFinder {
	private static final String TAG = "LastLocationFinder";

	private Context context;
	private Criteria criteria;
	private LocationManager locationManager;
	private LocationListener listener;

	public LastLocationFinder(Context contex) {
		this.context = contex;

		// Set the criteria in case we need to do a one-time location update.
		// Coarse accuracy is used to reduce latency in receiving this update.
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
	}

	@Override
	public Location getLastKnownLocation(int maxDistance, long minTime) {
		Location bestLocation = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;

		// Get the last known location from each available provider.
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			Location lastLocation = locationManager.getLastKnownLocation(provider);
			if (lastLocation != null) {
				// Find the best location by comparing time and accuracy from
				// each last known location.
				float lastAccuracy = lastLocation.getAccuracy();
				long lastTime = lastLocation.getTime();

				if (lastTime > minTime && lastAccuracy < bestAccuracy) {
					bestLocation = lastLocation;
					bestAccuracy = lastAccuracy;
					bestTime = lastTime;
				} else if (lastTime < minTime && lastTime > bestTime
						&& bestAccuracy == Float.MAX_VALUE) {
					bestLocation = lastLocation;
					bestTime = lastTime;
				}
			}
		}

		// Request a single update if the best result is too old or not accurate
		// enough.
		if (listener != null && (bestTime < minTime || bestAccuracy > maxDistance)) {
			String provider = locationManager.getBestProvider(criteria, true);
			if (provider != null) {
				Log.d(TAG, "Requesting a one-time update");
				locationManager.requestLocationUpdates(provider, 0, 0, oneTimeListener, context
						.getMainLooper());
			}
		}
		return bestLocation;
	}

	/**
	 * A listener to receive a one-time update and pass it to the activity.
	 */
	private LocationListener oneTimeListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			if (listener != null && location != null) {
				listener.onLocationChanged(location);
			}
			Log.d(TAG, "Got a one-time update");
			locationManager.removeUpdates(oneTimeListener);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}

	};

	@Override
	public void setLocationListener(LocationListener listener) {
		this.listener = listener;
	}

	@Override
	public void cancel() {
		locationManager.removeUpdates(oneTimeListener);
	}

}
