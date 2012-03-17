package com.simbiosys.apps.foodfast.service;

/**
 * Platform-specific implementation of PlacesUpdateService for Eclair and later.
 */
public class PlacesUpdateServiceEclair extends PlacesUpdateService {
	private static final String TAG = "PlacesUpdateServiceEclair";
		
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.IntentService#setIntentRedelivery(boolean) Force intent
	 * redelivery on Eclair and later versions, where this defaults to false. 
	 */
	@Override
	public void setIntentRedelivery(boolean enabled) {
		super.setIntentRedelivery(true);
	}

}
