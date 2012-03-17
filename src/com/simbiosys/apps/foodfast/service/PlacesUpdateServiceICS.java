package com.simbiosys.apps.foodfast.service;

import android.net.NetworkInfo;

/**
 * Platform-specific implementation of PlacesUpdateService for Ice Cream Sandwich and later.
 */
public class PlacesUpdateServiceICS extends PlacesUpdateServiceEclair {
	private static final String TAG = "PlacesUpdateServiceICS";

	@Override
	protected boolean getBackgroundEnabledSetting() {
		// Starting in ICS, ConnectivityManager.getBackgroudDataSetting() always
		// returns true. instead, to check for connectivity, use
		// getActiveNetworkInfo() and see if it's connected.
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		return activeNetwork.isConnectedOrConnecting();
	}

}
