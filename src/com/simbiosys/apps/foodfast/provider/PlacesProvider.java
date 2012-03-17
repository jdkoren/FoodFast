package com.simbiosys.apps.foodfast.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetailsColumns;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlacesColumns;
import com.simbiosys.apps.foodfast.provider.PlacesDatabase.Tables;

/**
 * Provider that stores PlacesContract data. Data is usually inserted by
 * an update service.
 */
public class PlacesProvider extends ContentProvider {
	private static final String TAG = "PlacesProvider";

	private PlacesDatabase placesDatabase;

	private static final UriMatcher uriMatcher = buildUriMatcher();

	/** Uri match values */
	private static final int PLACES = 100;
	private static final int PLACES_ID = 101;
	
	private static final int PLACE_DETAILS = 200;
	private static final int PLACE_DETAILS_ID = 201;

	@Override
	public boolean onCreate() {
		final Context context = getContext();
		placesDatabase = new PlacesDatabase(context);
		return true;
	}

	private static UriMatcher buildUriMatcher() {
		final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
		final String authority = PlacesContract.CONTENT_AUTHORITY;

		matcher.addURI(authority, "places", PLACES);
		matcher.addURI(authority, "places/*", PLACES_ID);
		matcher.addURI(authority, "place_details", PLACE_DETAILS);
		matcher.addURI(authority, "place_details/*", PLACE_DETAILS_ID);

		return matcher;
	}

	@Override
	public String getType(Uri uri) {
		final int match = uriMatcher.match(uri);
		switch (match) {
		case PLACES:
			return Places.CONTENT_TYPE;
		case PLACES_ID:
			return Places.CONTENT_ITEM_TYPE;
		case PLACE_DETAILS:
			return PlaceDetails.CONTENT_TYPE;
		case PLACE_DETAILS_ID:
			return PlaceDetails.CONTENT_ITEM_TYPE;
		default:
			throw new UnsupportedOperationException("Unknown URI: " + uri);
		}
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		SQLiteDatabase db = placesDatabase.getReadableDatabase();
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		
		// Set table and where-clause for this query
		switch (uriMatcher.match(uri)) {
		case PLACES_ID:
			builder.appendWhere(PlacesColumns.PLACE_ID + "='" + uri.getLastPathSegment() + "'");
		case PLACES:
			builder.setTables(Tables.PLACES);
			break;
		case PLACE_DETAILS_ID:
			builder.appendWhere(PlaceDetailsColumns.PLACE_ID + "='" + uri.getLastPathSegment() + "'");
		case PLACE_DETAILS:
			builder.setTables(Tables.PLACE_DETAILS);
			break;
		default:
			throw new UnsupportedOperationException("Failed query with URI: " + uri);
		}
		
		Cursor cursor = builder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		SQLiteDatabase db = placesDatabase.getWritableDatabase();
		Uri newUri;
		
		switch (uriMatcher.match(uri)) {
		case PLACES:
			db.insert(Tables.PLACES, null, values);
			newUri = Places.buildPlaceUri(values.getAsString(Places.PLACE_ID));
			break;
		case PLACE_DETAILS:
			db.insert(Tables.PLACE_DETAILS, null, values);
			newUri = PlaceDetails.buildPlaceDetailUri(values.getAsString(PlaceDetails.PLACE_ID));
			break;
		default:
			throw new UnsupportedOperationException("Failed insert with URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(newUri, null);
		return newUri;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		int count;
		SQLiteDatabase db = placesDatabase.getWritableDatabase();

		// Set table and where-clause for this update
		switch (uriMatcher.match(uri)) {
		case PLACES:
			// Update rows according to selectionArgs
			count = db.update(Tables.PLACES, values, selection, selectionArgs);
			break;
		case PLACES_ID:
			// Update the row specified by the uri
			count = db.update(Tables.PLACES, values, PlacesColumns.PLACE_ID + " = ?",
				new String[] { Places.getPlaceId(uri) });
			break;
		case PLACE_DETAILS:
			// Update rows according to selectionArgs
			count = db.update(Tables.PLACE_DETAILS, values, selection, selectionArgs);
			break;
		case PLACE_DETAILS_ID:
			// Update the row specified by the uri
			count = db.update(Tables.PLACE_DETAILS, values, PlaceDetailsColumns.PLACE_ID + " = ?",
				new String[] { PlaceDetails.getPlaceDetailId(uri) });
			break;
		default:
			throw new UnsupportedOperationException("Failed update with URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		int count;
		SQLiteDatabase db = placesDatabase.getWritableDatabase();

		// Set table and where-clause for this delete
		switch (uriMatcher.match(uri)) {
		case PLACES:
			// Delete rows according to selectionArgs
			count = db.delete(Tables.PLACES, selection, selectionArgs);
			break;
		case PLACES_ID:
			// Delete the row specified by the uri
			count = db.delete(Tables.PLACES, PlacesColumns.PLACE_ID + " = ?",
				new String[] { Places.getPlaceId(uri) });
			break;
		case PLACE_DETAILS:
			// Delete values according to selectionArgs
			count = db.delete(Tables.PLACE_DETAILS, selection, selectionArgs);
			break;
		case PLACE_DETAILS_ID:
			// Delete the row specified by the uri
			count = db.delete(Tables.PLACE_DETAILS, PlaceDetailsColumns.PLACE_ID + " = ?",
				new String[] { PlaceDetails.getPlaceDetailId(uri) });
		default:
			throw new UnsupportedOperationException("Failed delete with URI: " + uri);
		}
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
