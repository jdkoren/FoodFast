package com.simbiosys.apps.foodfast.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Contract class for interacting with the PlacesProvider. All time-based fields
 * use milliseconds since epoch and can be compared against
 * System.currentTimeMillis().
 * 
 */
public class PlacesContract {

	/**
	 * Column definitions for places table.
	 */
	interface PlacesColumns {
		String PLACE_ID = "place_id";
		String PLACE_NAME = "name";
		String PLACE_LATITUDE = "latitude";
		String PLACE_LONGITUDE = "longitude";
		String PLACE_DISTANCE = "distance";
		String PLACE_ICON = "icon";
		String PLACE_REFERENCE = "reference";
		String PLACE_TYPES = "types";
		String PLACE_VICINITY = "vicinity";
		String PLACE_LAST_UPDATED = "last_update_time";
	}

	/**
	 * Column definitions for place_details table.
	 */
	interface PlaceDetailsColumns {
		String PLACE_ID = "place_id";
		String PLACE_NAME = "name";
		String PLACE_ADDRESS = "address";
		String PLACE_PHONE = "phone";
		String PLACE_LATITUDE = "latitude";
		String PLACE_LONGITUDE = "longitude";
		String PLACE_ICON = "icon";
		String PLACE_REFERENCE = "reference";
		String PLACE_TYPES = "types";
		String PLACE_VICINITY = "vicinity";
		String PLACE_RATING = "rating";
		String PLACE_URL = "url";
		String PLACE_WEBSITE = "website";
		String PLACE_LAST_UPDATED = "last_update_time";
	}

	public static final String CONTENT_AUTHORITY = "com.simbiosys.apps.foodfast";

	public static final Uri BASE_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	/**
	 * Places are search results returned from a Google Places request. Each row
	 * is one search result.
	 */
	public static class Places implements PlacesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath("places").build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.foodfast.place";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.foodfast.place";

		/**
		 * Default "ORDER BY" clause
		 */
		public static final String DEFAULT_SORT = PlacesColumns.PLACE_DISTANCE + " ASC, "
				+ PlacesColumns.PLACE_ID + " ASC";

		/** Build URI for requested PLACE_ID */
		public static Uri buildPlaceUri(String placeId) {
			return CONTENT_URI.buildUpon().appendPath(placeId).build();
		}

		/** Read PLACE_ID from a Places Uri */
		public static String getPlaceId(Uri uri) {
			return uri.getLastPathSegment();
		}
	}

	/**
	 * PlaceDetails are detailed information about a place. Each row is one
	 * place.
	 */
	public static class PlaceDetails implements PlaceDetailsColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_URI.buildUpon().appendPath("place_details")
				.build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.foodfast.place_detail";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.foodfast.place_detail";

		/** Build URI for requested PLACE_ID */
		public static Uri buildPlaceDetailUri(String placeId) {
			return CONTENT_URI.buildUpon().appendPath(placeId).build();
		}

		/** Read PLACE_ID from a PlaceDetails Uri */
		public static String getPlaceDetailId(Uri uri) {
			return uri.getLastPathSegment();
		}
	}

	private PlacesContract() {
	}

}
