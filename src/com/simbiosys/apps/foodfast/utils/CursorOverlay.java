package com.simbiosys.apps.foodfast.utils;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;

/**
 * An ItemizedOverlay built from a Cursor.
 *
 */
public class CursorOverlay extends ItemizedOverlay<OverlayItem> {
	@SuppressWarnings("unused")
	private static final String TAG = "CursorOverlay";

	protected Cursor cursor;
	protected boolean shadow = false;

	public CursorOverlay(Drawable drawable, Cursor cursor) {
		super(drawable);
		boundCenter(drawable);
		changeCursor(cursor);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, this.shadow);
	}

	@Override
	protected OverlayItem createItem(int position) {
		if (cursor.moveToPosition(position)) {
			int latitude = (int) (cursor.getFloat(cursor.getColumnIndex(Places.PLACE_LATITUDE)) * 1e6);
			int longitude = (int) (cursor.getFloat(cursor.getColumnIndex(Places.PLACE_LONGITUDE)) * 1e6);
			GeoPoint point = new GeoPoint(latitude, longitude);

			String title = cursor.getString(cursor.getColumnIndex(Places.PLACE_NAME));
			return new OverlayItem(point, title, null);
		}
		return null;
	}

	@Override
	protected boolean onTap(int index) {
		return super.onTap(index);
	}

	@Override
	public int size() {
		return cursor.getCount();
	}
	
	public void changeCursor(Cursor newCursor) {
		this.cursor = newCursor;
		populate();
	}

}
