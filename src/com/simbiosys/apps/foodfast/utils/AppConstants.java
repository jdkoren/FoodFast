package com.simbiosys.apps.foodfast.utils;

import android.app.AlarmManager;
import android.os.Build;

/**
 * Constants used for various things throughout the application.
 *
 */
public class AppConstants {

	/**
	 * Google Maps API key. 
	 */
	public static final String MAPS_API_KEY = "YOUR MAPS API KEY";

	/**
	 * Constants for place searches
	 */
	private static String PLACES_API_KEY = "YOUR PLACES API KEY";
	public static String PLACES_SEARCH_BASE_URI = "https://maps.googleapis.com/maps/api/place/search/xml?sensor=true";
	public static String PLACE_DETAILS_SEARCH_BASE_URI = "https://maps.googleapis.com/maps/api/place/details/xml?sensor=true";
	public static String HTTP_PARAM_API_KEY = "&key=" + PLACES_API_KEY;
	public static String HTTP_PARAM_TYPE = "&types=food";

	// Default radius for searches (in meters) if not specified in preferences
	public static int DEFAULT_RADIUS = 150;
	
	// Default location sensitivity, or the maximum distance (in meters) allowed
	// between location updates if not specified in preferences
	public static int DEFAULT_SENSITIVITY = 75;
	
	// Maximum time interval between location updates
	public static long MAX_INTERVAL = AlarmManager.INTERVAL_FIFTEEN_MINUTES;
	
	// Location sensitivity for passive updates
	// public static int PASSIVE_DEFAULT_SENSITIVITY = DEFAULT_SENSITIVITY;
	
	// Maximum time interval between passive updates
	// public static long PASSIVE_MAX_INTERVAL = MAX_INTERVAL;

	// How long before forcing refresh of old location information
	public static long MAX_REFRESH_DELAY = AlarmManager.INTERVAL_DAY;
	
	// Default setting for whether to use GPS
	public static boolean DEFAULT_USE_GPS = true;
	
	// Default setting for whether active location updates are allowed
	public static boolean DEFAULT_ACTIVE_UPDATES_ALLOWED = true;
	
	// Default setting for whether passive location updates are allowed
	public static boolean DEFAULT_PASSIVE_UPDATES_ALLOWED = true;
	
	// Default setting for whether passive location updates should be disabled
	// when the app exits
	public static boolean DEFAULT_DISABLE_PASSIVE_ON_EXIT = false;
	
	// Default setting for preserve battery
	public static boolean DEFAULT_PRESERVE_BATTERY = true;
	

	/**
	 * Constants used for shared preferences and extras
	 */
	public static String SHARED_PREFERENCES_FILE = "preferences_file";
	public static String LAUNCHED_ONCE = "launched_once";
	public static String IN_BACKGROUND = "in_background";
	public static String LAST_UPDATE_TIME = "last_update_time";
	public static String LAST_UPDATE_LAT = "last_update_lat";
	public static String LAST_UPDATE_LONG = "last_update_long";

	public static String PREF_USE_GPS = "use_gps";
	public static String PREF_ACTIVE_UPDATES_ALLOWED = "active_updates";
	public static String PREF_PASSIVE_UPDATES_ALLOWED = "passive_updates";
	public static String PREF_DISABLE_PASSIVE_ON_EXIT = "disable_passive_on_exit";
	public static String PREF_PRESERVE_BATTERY = "preserve_battery";
	public static String PREF_SEARCH_RADIUS = "search_radius";
	public static String PREF_LOCATION_SENSITIVITY = "location_sensitivity";

	public static String EXTRA_FORCE_UPDATE = "force_update";
	public static String EXTRA_ID = "id";
	public static String EXTRA_LOCATION = "location";
	public static String EXTRA_RADIUS = "radius";
	public static String EXTRA_REFERENCE = "reference";
	public static String EXTRA_UPDATE_RESULT = "update_result";

	/**
	 * Intent Actions
	 */
	public static String ACTION_LOCATION_PROVIDER_DISABLED = "com.simbiosys.apps.foodfast.action.LOCATION_PROVIDER_DISABLED";
	public static String ACTION_VIEW_MAP = "com.simbiosys.apps.foodfast.action.VIEW_MAP";
	public static String ACTION_UPDATE_SERVICE_RESULT = "com.simbiosys.apps.foodfast.action.UPDATE_SERVICE_RESULT";

	// A bogus provider when constructing Location objects from stored values.
	public static String CONSTRUCTED_PROVIDER = "constructed_provider";

	/**
	 * Used for platform-specific implementations of various classes.
	 */
	public static boolean SUPPORTS_ECLAIR = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ECLAIR;
	public static boolean SUPPORTS_FROYO = Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
	public static boolean SUPPORTS_GINGERBREAD = Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
	public static boolean SUPPORTS_HONEYCOMB = Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	public static boolean SUPPORTS_ICS = Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH;

}
