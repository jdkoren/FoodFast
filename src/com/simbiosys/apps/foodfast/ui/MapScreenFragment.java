package com.simbiosys.apps.foodfast.ui;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.ItemizedOverlay.OnFocusChangeListener;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;
import com.simbiosys.apps.foodfast.ui.phone.SettingsActivity;
import com.simbiosys.apps.foodfast.utils.AppConstants;
import com.simbiosys.apps.foodfast.utils.PopupBalloonCursorOverlay;

public class MapScreenFragment extends Fragment implements LoaderCallbacks<Cursor>, OnClickListener {
	private static final String TAG = "MapScreenFragment";

	/**
	 * The MapView for this fragment is actually being held by the activity.
	 * This interface must be used by the activity.
	 */
	public interface IMapViewHolder {

		public MapView getMapView();

	}

	private static final int LOADER_PLACES = 1;
	private static final int LOADER_PLACE_DETAILS = 2;

	protected IMapViewHolder mapHolder;
	protected MapView mapView;
	protected MapController mapController;
	protected FrameLayout mapContainer;

	protected ImageView userMarker;
	protected ImageView placeMarker;
	protected ImageButton btnNavigate, btnPhone, btnWebsite, btnShare;
	protected Cursor cursor;
	protected PopupBalloonCursorOverlay resultsOverlay;

	protected SharedPreferences prefs;
	protected Uri placeUri;

	protected static final double FIT_FACTOR = 1.5;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		try {
			mapHolder = (IMapViewHolder) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement IMapViewHolder");
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
		placeUri = intent.getData();
		
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mapContainer = new FrameLayout(getActivity());
		return mapContainer;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		prefs = getActivity().getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE,
			Context.MODE_PRIVATE);

		// Add the MapView to the view hierarchy
		mapView = mapHolder.getMapView();
		mapContainer.addView(mapView);

		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);
		mapController = mapView.getController();

		if (placeUri != null) {
			String type = getActivity().getContentResolver().getType(placeUri);
			if (type.equals(Places.CONTENT_TYPE)) {
				// Load several places
				getLoaderManager().initLoader(LOADER_PLACES, null, this);
			} else if (type.equals(PlaceDetails.CONTENT_ITEM_TYPE)) {
				// Load a single place and show the dropdown panel
				getLoaderManager().initLoader(LOADER_PLACE_DETAILS, null, this);
				showDropdownPanel();
			} else {
				Log.e(TAG, "Launched with unknown URI: " + placeUri);
			}
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		resetUserMarker();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Remove the MapView from the view hierarchy so another instance of
		// this fragment can adopt it from the activity.
		mapContainer.removeView(mapView);

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		if (placeUri != null) {
			String type = getActivity().getContentResolver().getType(placeUri);
			if (type.equals(Places.CONTENT_TYPE)) {
				inflater.inflate(R.menu.map_screen_menu_multiple, menu);
			} else if (type.equals(PlaceDetails.CONTENT_ITEM_TYPE)) {
				inflater.inflate(R.menu.map_screen_menu_single, menu);
			}
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_refresh:
			((BaseActivity) getActivity()).performRefresh();
			return true;
		case R.id.menu_settings:
			Intent intent = new Intent(getActivity(), SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.menu_share:
			if (cursor.isFirst()) {
				doShareAction();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		((BaseActivity) getActivity()).getActivityHelper().setRefreshActionButtonCompatState(true);
		String[] projection;

		switch (id) {
		case LOADER_PLACES:
			projection = new String[] { Places.PLACE_ID, Places.PLACE_REFERENCE, Places.PLACE_NAME,
					Places.PLACE_DISTANCE, Places.PLACE_LATITUDE, Places.PLACE_LONGITUDE };
			break;
		case LOADER_PLACE_DETAILS:
			projection = new String[] { PlaceDetails.PLACE_ID, PlaceDetails.PLACE_REFERENCE,
					PlaceDetails.PLACE_NAME, PlaceDetails.PLACE_LATITUDE,
					PlaceDetails.PLACE_LONGITUDE, PlaceDetails.PLACE_ADDRESS,
					PlaceDetails.PLACE_PHONE, PlaceDetails.PLACE_URL, PlaceDetails.PLACE_WEBSITE };
			break;
		default:
			throw new UnsupportedOperationException("Unkown URI: " + placeUri);
		}
		return new CursorLoader(getActivity(), placeUri, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		cursor = data;
		if (cursor.moveToFirst()) {
			switch (loader.getId()) {
			case LOADER_PLACES:
				setupOverlay();
				break;
			case LOADER_PLACE_DETAILS:
				drawPlaceMarker();
				enableDropdownPanelButtons();
				break;
			}
		}
		((BaseActivity) getActivity()).getActivityHelper().setRefreshActionButtonCompatState(false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	/**
	 * Creates and displays an itemized overlay of the places returned by the
	 * cursor loader.
	 */
	private void setupOverlay() {
		List<Overlay> overlays = mapView.getOverlays();

		if (resultsOverlay == null) {
			Drawable drawable = getResources().getDrawable(R.drawable.map_marker_food);
			resultsOverlay = new PopupBalloonCursorOverlay(drawable, cursor, drawable
					.getIntrinsicHeight() / 2, mapView);
			
			resultsOverlay.setOnFocusChangeListener(new OnFocusChangeListener() {
				public void onFocusChanged(ItemizedOverlay overlay, OverlayItem newFocus) {
					if (newFocus == null) {
						resultsOverlay.hideBalloon();
					}
				}
			});
			
			overlays.add(resultsOverlay);
		} else {
			resultsOverlay.changeCursor(cursor);
		}
		
		resetUserMarker();
	}

	/**
	 * Resets the map marker representing the user.
	 */
	protected void resetUserMarker() {
		if (userMarker == null) {
			userMarker = new ImageView(getActivity());
			userMarker.setImageResource(R.drawable.map_marker_user);
		}

		mapView.removeView(userMarker);

		float lastLat = prefs.getFloat(AppConstants.LAST_UPDATE_LAT, 0);
		float lastLng = prefs.getFloat(AppConstants.LAST_UPDATE_LONG, 0);
		GeoPoint userPosition = new GeoPoint((int) (lastLat * 1e6), (int) (lastLng * 1e6));

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, userPosition, LayoutParams.CENTER);
		mapView.addView(userMarker, params);

		mapController.animateTo(userPosition);
		if (resultsOverlay != null) {
			mapController.zoomToSpan((int) (resultsOverlay.getLatSpanE6() * FIT_FACTOR),
				(int) (resultsOverlay.getLonSpanE6() * FIT_FACTOR));
		} else {
			mapController.setZoom(19);
		}
	}

	/**
	 * Draw a marker on the map for a specified place.
	 */
	private void drawPlaceMarker() {
		if (placeMarker == null) {
			placeMarker = new ImageView(getActivity());
			placeMarker.setImageResource(R.drawable.map_marker_food);
		}

		mapView.removeView(placeMarker);

		float placeLat = cursor.getFloat(cursor.getColumnIndex(PlaceDetails.PLACE_LATITUDE));
		float placeLng = cursor.getFloat(cursor.getColumnIndex(PlaceDetails.PLACE_LONGITUDE));
		GeoPoint placePosition = new GeoPoint((int) (placeLat * 1e6), (int) (placeLng * 1e6));

		LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT, placePosition, LayoutParams.CENTER);
		mapView.addView(placeMarker, params);

		mapController.animateTo(placePosition);
		mapController.setZoom(19);
	}

	/**
	 * Set up the dropdown view that appears when displaying a single place.
	 */
	private void showDropdownPanel() {
		LayoutInflater inflater = getActivity().getLayoutInflater();
		View dropdown = inflater.inflate(R.layout.mapscreen_dropdown_panel, mapContainer);

		btnNavigate = (ImageButton) dropdown.findViewById(R.id.btn_navigate_to);
		btnPhone = (ImageButton) dropdown.findViewById(R.id.btn_call_phone);
		btnWebsite = (ImageButton) dropdown.findViewById(R.id.btn_visit_website);
		btnShare = (ImageButton) dropdown.findViewById(R.id.btn_share);

		btnNavigate.setOnClickListener(this);
		btnPhone.setOnClickListener(this);
		btnWebsite.setOnClickListener(this);
		btnShare.setOnClickListener(this);
	}

	/**
	 * Enable the dropdown panel buttons.
	 */
	private void enableDropdownPanelButtons() {
		boolean hasPhone = !cursor.getString(cursor.getColumnIndex(PlaceDetails.PLACE_PHONE))
				.equals("");
		boolean hasWebsite = !cursor.getString(cursor.getColumnIndex(PlaceDetails.PLACE_WEBSITE))
				.equals("");

		btnNavigate.setEnabled(true);
		btnPhone.setEnabled(hasPhone);
		btnWebsite.setEnabled(hasWebsite);
		btnShare.setEnabled(true);

	}

	/**
	 * Disable the dropdown panel buttons.
	 */
	private void disableDropdownPanelButtons() {
		if (getView().findViewById(R.id.dropdown_panel) != null) {
			btnNavigate.setEnabled(false);
			btnPhone.setEnabled(false);
			btnWebsite.setEnabled(false);
			btnShare.setEnabled(false);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_navigate_to:
			String navData = cursor.getFloat(cursor.getColumnIndex(PlaceDetails.PLACE_LATITUDE))
					+ "," + cursor.getFloat(cursor.getColumnIndex(PlaceDetails.PLACE_LONGITUDE));
			Intent navIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
					+ navData));
			startActivity(navIntent);
			break;
		case R.id.btn_call_phone:
			String phoneData = cursor.getString(cursor.getColumnIndex(PlaceDetails.PLACE_PHONE));
			Intent dialIntent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneData));
			startActivity(dialIntent);
			break;
		case R.id.btn_visit_website:
			String webData = cursor.getString(cursor.getColumnIndex(PlaceDetails.PLACE_WEBSITE));
			Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(webData));
			startActivity(webIntent);
			break;
		case R.id.btn_share:
			doShareAction();
			break;
		}

	}

	public void doShareAction() {
		String placeUrl = cursor.getString(cursor.getColumnIndex(PlaceDetails.PLACE_URL));
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent
				.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
		shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text) + " "
				+ placeUrl);
		startActivity(shareIntent);
	}

}
