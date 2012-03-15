package com.simbiosys.apps.foodfast.utils;

import java.util.List;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

/**
 * Platform-specific implementation of LastLocationFinder for Gingerbread and
 * later.
 * 
 */
public class LastLocationFinderGingerbread implements ILastLocationFinder {
	private static final String TAG = "LastLocationFinderGingerbread";

	private Context context;
	private Criteria criteria;
	private LocationManager locationManager;
	private LocationListener listener;
	private PendingIntent oneTimeUpdateIntent;
	private static String ACTION_ONE_TIME_UPDATE = "com.simbiosys.apps.foodfast.ACTION_ONE_TIME_UPDATE";
	private boolean registered = false;

	public LastLocationFinderGingerbread(Context context) {
		this.context = context;

		// Set criteria in case we need to do a one-time location update. Coarse
		// accuracy is used to reduce latency
		criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_COARSE);
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

		// Create pending intent that will be broadcast by the one-time update
		Intent updateIntent = new Intent(ACTION_ONE_TIME_UPDATE);
		oneTimeUpdateIntent = PendingIntent.getBroadcast(context, 0, updateIntent,
			PendingIntent.FLAG_UPDATE_CURRENT);
	}

	@Override
	public Location getLastKnownLocation(int maxDistance, long minTime) {
		Location bestLocation = null;
		float bestAccuracy = Float.MAX_VALUE;
		long bestTime = Long.MIN_VALUE;

		// Get the last known location from each available provider
		List<String> providers = locationManager.getAllProviders();
		
		for (String provider : providers) {
			Location lastLocation = locationManager.getLastKnownLocation(provider);
			if (lastLocation != null) {
				// Find the best location by comparing time and accuracy of each
				// last known location
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
			Log.d(TAG, "requesting single update");
			// Set an intent filter to catch our unique action.
			IntentFilter filter = new IntentFilter(ACTION_ONE_TIME_UPDATE);
			context.registerReceiver(oneTimeUpdateReceiver, filter);
			registered = true;
			// Use single update request introduced in Gingerbread
			locationManager.requestSingleUpdate(criteria, oneTimeUpdateIntent);
		}
		return bestLocation;
	}

	/**
	 * A receiver to receive a one-time update.
	 */
	private BroadcastReceiver oneTimeUpdateReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Location location = (Location) intent.getExtras().get(
				LocationManager.KEY_LOCATION_CHANGED);
			if (listener != null && location != null) {
				Log.d(TAG, "got a one-time update");
				listener.onLocationChanged(location);
			}
			Log.d(TAG, "unregistering one-time listener in receiver");
			locationManager.removeUpdates(oneTimeUpdateIntent);
		}
	};

	@Override
	public void setLocationListener(LocationListener listener) {
		this.listener = listener;

	}

	@Override
	public void cancel() {
		if (registered) {
			Log.d(TAG, "unregistering one-time listener in cancel()");
			context.unregisterReceiver(oneTimeUpdateReceiver);
			registered = false;
		}
		locationManager.removeUpdates(oneTimeUpdateIntent);
	}

}
