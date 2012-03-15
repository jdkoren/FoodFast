package com.simbiosys.apps.foodfast.service;

import android.net.NetworkInfo;

/**
 * Platform-specific implementation of PlaceDetailsUpdateService for Ice Cream Sandwich and later.
 */
public class PlaceDetailsUpdateServiceICS extends PlaceDetailsUpdateService {

	@Override
	protected boolean getBackgroundEnabledSetting() {
		// Starting in ICS, ConnectivityManager.getBackgroudDataSetting() always
		// returns true. instead, to check for connectivity, use
		// getActiveNetworkInfo() and see if it's connected.
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		return activeNetwork.isConnectedOrConnecting();
	}

}
