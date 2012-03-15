package com.simbiosys.apps.foodfast.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetailsColumns;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlacesColumns;

/**
 * Helper for managing SQLiteDatabase that stores data for the PlacesProvider.
 *
 */
public class PlacesDatabase extends SQLiteOpenHelper {
	private static final String TAG = "PlacesDatabase";

	private static final String DB_NAME = "places.db";

	private static final int VERSION = 1;

	interface Tables {
		String PLACES = "places";
		String PLACE_DETAILS = "place_details";
	}

	public PlacesDatabase(Context context) {
		super(context, DB_NAME, null, VERSION);

	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.PLACES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ PlacesColumns.PLACE_ID + " TEXT NOT NULL, "
				+ PlacesColumns.PLACE_NAME + " TEXT, "
				+ PlacesColumns.PLACE_LATITUDE + " FLOAT, "
				+ PlacesColumns.PLACE_LONGITUDE + " FLOAT, "
				+ PlacesColumns.PLACE_DISTANCE + " FLOAT, "
				+ PlacesColumns.PLACE_ICON + " TEXT, "
				+ PlacesColumns.PLACE_REFERENCE + " TEXT, "
				+ PlacesColumns.PLACE_TYPES + " TEXT, "
				+ PlacesColumns.PLACE_VICINITY + " TEXT, "
				+ PlacesColumns.PLACE_LAST_UPDATED + " INTEGER, " + "UNIQUE ("
				+ PlacesColumns.PLACE_ID + ") ON CONFLICT REPLACE);");

		db.execSQL("CREATE TABLE " + Tables.PLACE_DETAILS + " ("
			+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ PlaceDetailsColumns.PLACE_ID + " TEXT NOT NULL, "
				+ PlaceDetailsColumns.PLACE_NAME + " TEXT, "
				+ PlaceDetailsColumns.PLACE_ADDRESS + " TEXT, "
				+ PlaceDetailsColumns.PLACE_PHONE + " TEXT, "
				+ PlaceDetailsColumns.PLACE_LATITUDE + " FLOAT, "
				+ PlaceDetailsColumns.PLACE_LONGITUDE + " FLOAT, "
				+ PlaceDetailsColumns.PLACE_ICON + " TEXT, "
				+ PlaceDetailsColumns.PLACE_RATING + " FLOAT, "
				+ PlaceDetailsColumns.PLACE_REFERENCE + " TEXT, "
				+ PlaceDetailsColumns.PLACE_TYPES + " TEXT, "
				+ PlaceDetailsColumns.PLACE_VICINITY + " TEXT, "
				+ PlaceDetailsColumns.PLACE_URL + " TEXT, "
				+ PlaceDetailsColumns.PLACE_WEBSITE + " TEXT, "
				+ PlaceDetailsColumns.PLACE_LAST_UPDATED + " INTEGER, "
				+ "UNIQUE (" + PlaceDetailsColumns.PLACE_ID
				+ ") ON CONFLICT REPLACE);");
		
		Log.d(TAG, "Database created");

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "Database updated");

	}

}
