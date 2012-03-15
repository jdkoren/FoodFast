package com.simbiosys.apps.foodfast.utils;

import android.location.Location;
import android.location.LocationListener;

/**
 * Classes that implement this interface must define a method to find the best
 * last known location found by any available location providers.
 * 
 */
public interface ILastLocationFinder {

	/**
	 * Finds the best last known location found by any available providers. If
	 * the best location found isn't accurate or timely enough, it should
	 * request a single location update.
	 * 
	 * @param maxDistance
	 *            maximum distance allowed before requiring an update
	 * @param minTime
	 *            minimum timestamp before requiring an update
	 * @return the most accurate and/or timely last known location
	 */
	public abstract Location getLastKnownLocation(int maxDistance, long minTime);

	/**
	 * Set the LocationListener that will receive an update if a one-time
	 * location update is requested
	 * 
	 * @param new location listener
	 */
	public abstract void setLocationListener(LocationListener listener);

	/**
	 * Cancel any active one-time forced updates
	 */
	public abstract void cancel();

}