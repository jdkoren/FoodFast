package com.simbiosys.apps.foodfast;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.simbiosys.apps.foodfast.utils.AppConstants;
import com.simbiosys.apps.foodfast.utils.LocationHelper;

/**
 * An application wrapper that monitors its own lifecycle (through callbacks
 * from activities) in order to manage a LocationHelper.
 * 
 */
public class FoodFastApp extends Application {
	private static final String TAG = "FoodFastApp";

	private LocationHelper locationHelper;

	private int activityCount;
	private boolean resumed;
	private boolean inForeground;

	protected SharedPreferences prefs;
	protected Editor prefsEditor;

	@Override
	public void onCreate() {
		super.onCreate();
		activityCount = 0;
		resumed = inForeground = false;

		locationHelper = new LocationHelper(this);

		// Save that this app has been launched
		prefs = getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
		prefsEditor = prefs.edit();
		prefsEditor.putBoolean(AppConstants.LAUNCHED_ONCE, true);
		prefsEditor.commit();
	}

	public LocationHelper getLocationHelper() {
		return locationHelper;
	}

	/**
	 * Callback for when an activity associated with this app is created.
	 */
	public void onActivityCreated() {
		activityCount++;
	}

	/**
	 * Callback for when an activity associated with this app is started.
	 */
	public void onActivityStarted() {
		// no-op
	}

	/**
	 * Callback for when an activity associated with this app is resumed.
	 */
	public void onActivityResumed(Activity activity) {
		// If we're coming into the foreground, start active updates
		if (!inForeground) {
			locationHelper.startActiveUpdates();
		}
		// We're in the foreground and resumed
		resumed = true;
		inForeground = true;
		prefsEditor.putBoolean(AppConstants.IN_BACKGROUND, false);
		prefsEditor.commit();

	}

	/**
	 * Callback for when an activity associated with this app is paused.
	 */
	public void onActivityPaused() {
		resumed = false;
	}

	/**
	 * Callback for when an activity associated with this app is stopped.
	 */
	public void onActivityStopped() {
		// Activity stopped, but we could still be in the foreground. If none of
		// our other activities resumed before this one stopped, we're no longer
		// in the foreground.
		if (!resumed) {
			inForeground = false;
			prefsEditor.putBoolean(AppConstants.IN_BACKGROUND, true);
			prefsEditor.commit();
			locationHelper.stopActiveUpdates();
			locationHelper.startPassiveUpdates();
		}
	}

	/**
	 * Callback for when an activity associated with this app is destroyed. The
	 * activity should pass the result of isFinishing() to this method.
	 */
	public void onActivityDestroyed(boolean isFinishing) {
		activityCount--;

		// If this was the last activity and the app is closing (by the user
		// pressing BACK, for instance), then our application is exiting.
		if (activityCount < 1 && isFinishing) {
			locationHelper.stopPassiveUpdates();
		}
	}

}
