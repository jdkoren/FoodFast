package com.simbiosys.apps.foodfast.utils;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;
import com.simbiosys.apps.foodfast.service.PlaceDetailsUpdateService;
import com.simbiosys.apps.foodfast.service.PlaceDetailsUpdateServiceICS;
import com.simbiosys.apps.foodfast.ui.widget.PopupBalloonView;

/**
 * A CursorOverlay that shows a small, clickable balloon over a place marker in
 * a MapView.
 * 
 */
public class PopupBalloonCursorOverlay extends CursorOverlay {
	private static final String TAG = "PopupBalloonCursorOverlay";

	protected MapView mapView;
	protected MapController mapController;

	protected PopupBalloonView<OverlayItem> popupBalloon;
	protected View balloonRegion;
	protected int balloonPaddingbottom;

	protected int focusedIndex;
	protected OverlayItem focusedItem;

	public PopupBalloonCursorOverlay(Drawable drawable, Cursor cursor, int paddingBottom,
			MapView mapView) {
		super(drawable, cursor);
		this.balloonPaddingbottom = paddingBottom;
		this.mapView = mapView;
		mapController = mapView.getController();
	}

	@Override
	protected boolean onTap(int index) {
		focusedIndex = index;
		focusedItem = createItem(index);
		setLastFocusedIndex(index);

		createAndDisplayBalloon();

		mapController.animateTo(focusedItem.getPoint());
		return true;
	}

	/**
	 * Creates and displays the popup balloon. Since only one balloon is shown
	 * at a time, we can instantiate it once and recycle the same one.
	 */
	private boolean createAndDisplayBalloon() {
		boolean isRecycled;
		if (popupBalloon == null) {
			// Create balloon if there isn't one to recycle
			popupBalloon = new PopupBalloonView<OverlayItem>(mapView.getContext(),
					balloonPaddingbottom);
			balloonRegion = (View) popupBalloon.findViewById(R.id.popup_ballon_container);
			balloonRegion.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					onBalloonTap();
				}
			});
			isRecycled = false;
		} else {
			isRecycled = true;
		}

		// Remove current balloon
		popupBalloon.setVisibility(View.GONE);

		// Add data to the balloon
		if (focusedItem != null) {
			popupBalloon.setData(focusedItem);
		}

		GeoPoint point = focusedItem.getPoint();
		MapView.LayoutParams params = new MapView.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, point, MapView.LayoutParams.BOTTOM_CENTER);
		params.mode = MapView.LayoutParams.MODE_MAP;
		popupBalloon.setVisibility(View.VISIBLE);

		if (isRecycled) {
			popupBalloon.setLayoutParams(params);
		} else {
			mapView.addView(popupBalloon, params);
		}

		return isRecycled;
	}

	/**
	 * Callback for when the balloon is clicked.
	 */
	private boolean onBalloonTap() {
		cursor.moveToPosition(focusedIndex);
		String placeId = cursor.getString(cursor.getColumnIndex(Places.PLACE_ID));
		String placeReference = cursor.getString(cursor.getColumnIndex(Places.PLACE_REFERENCE));
		String placeName = cursor.getString(cursor.getColumnIndex(Places.PLACE_NAME));

		if (placeId != null && placeReference != null) {
			// Have PlaceDetailsUpdateService get details about this place
			Intent updateIntent = new Intent(mapView.getContext(),
					AppConstants.SUPPORTS_ICS ? PlaceDetailsUpdateServiceICS.class
							: PlaceDetailsUpdateService.class);
			updateIntent.putExtra(AppConstants.EXTRA_ID, placeId);
			updateIntent.putExtra(AppConstants.EXTRA_REFERENCE, placeReference);
			updateIntent.putExtra(AppConstants.EXTRA_FORCE_UPDATE, true);
			mapView.getContext().startService(updateIntent);

			// Start an activity to show the place details
			Intent showDetailIntent = new Intent(Intent.ACTION_VIEW, PlaceDetails
					.buildPlaceDetailUri(placeId));
			showDetailIntent.putExtra(Intent.EXTRA_TITLE, placeName);
			mapView.getContext().startActivity(showDetailIntent);
		}
		return true;
	}

	/**
	 * Hides the popup balloon.
	 */
	public void hideBalloon() {
		if (popupBalloon != null) {
			popupBalloon.setVisibility(View.GONE);
		}
		focusedItem = null;
	}

}
