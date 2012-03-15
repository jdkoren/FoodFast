package com.simbiosys.apps.foodfast.ui;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.service.PlaceDetailsUpdateService;
import com.simbiosys.apps.foodfast.service.PlaceDetailsUpdateServiceICS;
import com.simbiosys.apps.foodfast.utils.AppConstants;

public class PlaceDetailFragment extends Fragment implements LoaderCallbacks<Cursor>,
		OnClickListener {
	@SuppressWarnings("unused")
	private static final String TAG = "PlaceDetailFragment";

	protected String placeId = null;
	protected String placeReference = null;
	protected Uri placeDetailsUri;

	protected Handler handler = new Handler();
	protected TextView textName, textAddress, textPhone;
	protected Button btnPhone, btnWebsite, btnViewMap, btnNavigate, btnShare;

	protected Cursor cursor = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);

		final Intent intent = BaseActivity.fragmentArgumentsToIntent(getArguments());
		placeDetailsUri = intent.getData();

		if (placeDetailsUri != null) {
			placeId = PlaceDetails.getPlaceDetailId(placeDetailsUri);
		}

	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (placeDetailsUri != null) {
			getLoaderManager().initLoader(0, null, this);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.place_detail_fragment, container, false);

		// Get handles to views that need to be manipulated later
		textName = (TextView) root.findViewById(R.id.text_name);
		textAddress = (TextView) root.findViewById(R.id.text_address);
		textPhone = (TextView) root.findViewById(R.id.text_phone);
		btnPhone = (Button) root.findViewById(R.id.btn_call_phone);
		btnWebsite = (Button) root.findViewById(R.id.btn_visit_website);
		btnViewMap = (Button) root.findViewById(R.id.btn_view_on_map);
		btnNavigate = (Button) root.findViewById(R.id.btn_navigate_to);
		btnShare = (Button) root.findViewById(R.id.btn_share);

		// Register button listeners
		btnPhone.setOnClickListener(this);
		btnWebsite.setOnClickListener(this);
		btnViewMap.setOnClickListener(this);
		btnNavigate.setOnClickListener(this);
		btnShare.setOnClickListener(this);

		return root;
	}

	@Override
	public void onResume() {
		super.onResume();
		// refresh details on resume, but don't force an update
		if (placeId != null && placeReference != null) {
			updateDetails(false);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.place_detail_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_map:
			if (cursor.isFirst()) {
				doMapAction();
			}
			return true;
		case R.id.menu_share:
			if (cursor.isFirst()) {
				doShareAction();
			}
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Updates place details using the PlaceDetailsUpdateService.
	 * 
	 * @param forceUpdate
	 *            flag whether to force the update
	 */
	protected void updateDetails(boolean forceUpdate) {
		if (placeId != null && placeReference != null) {
			// Start the place details update service to get details. If
			// forceUpdate is false, the update service will figure out whether
			// it needs to call the server or not.
			Intent updateIntent = new Intent(getActivity(),
					AppConstants.SUPPORTS_ICS ? PlaceDetailsUpdateServiceICS.class
							: PlaceDetailsUpdateService.class);
			updateIntent.putExtra(AppConstants.EXTRA_ID, placeId);
			updateIntent.putExtra(AppConstants.EXTRA_REFERENCE, placeReference);
			updateIntent.putExtra(AppConstants.EXTRA_FORCE_UPDATE, forceUpdate);
			getActivity().startService(updateIntent);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		String[] projection = new String[] { PlaceDetails.PLACE_REFERENCE, PlaceDetails.PLACE_NAME,
				PlaceDetails.PLACE_ADDRESS, PlaceDetails.PLACE_PHONE, PlaceDetails.PLACE_WEBSITE,
				PlaceDetails.PLACE_LATITUDE, PlaceDetails.PLACE_LONGITUDE, PlaceDetails.PLACE_URL };

		return new CursorLoader(getActivity(), placeDetailsUri, projection, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		if (data.moveToFirst()) {
			placeReference = data.getString(data.getColumnIndex(PlaceDetails.PLACE_REFERENCE));

			final String name = data.getString(data.getColumnIndex(PlaceDetails.PLACE_NAME));
			final String address = data.getString(data.getColumnIndex(PlaceDetails.PLACE_ADDRESS));
			final String phone = data.getString(data.getColumnIndex(PlaceDetails.PLACE_PHONE));
			final String website = data.getString(data.getColumnIndex(PlaceDetails.PLACE_WEBSITE));
			final boolean hasPhone = !phone.equals("");
			final boolean hasWebsite = !website.equals("");

			// Post UI updates
			handler.post(new Runnable() {
				public void run() {
					textName.setText(name);
					textAddress.setText(address);
					textPhone.setText(phone);
					btnViewMap.setEnabled(true);
					btnNavigate.setEnabled(true);
					btnShare.setEnabled(true);
					btnPhone.setEnabled(hasPhone);
					btnWebsite.setEnabled(hasWebsite);
				}
			});

		}

		cursor = data;

	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		cursor.close();
		final String loading = getResources().getString(R.string.loading);
		handler.post(new Runnable() {
			public void run() {
				// Clear data from views and disable buttons
				textName.setText(loading);
				textAddress.setText(loading);
				textPhone.setText(loading);
				btnViewMap.setEnabled(false);
				btnNavigate.setEnabled(false);
				btnShare.setEnabled(false);
				btnPhone.setEnabled(false);
				btnWebsite.setEnabled(false);
			}
		});

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_view_on_map:
			doMapAction();
			break;
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

	public void doMapAction() {
		Intent mapIntent = new Intent(AppConstants.ACTION_VIEW_MAP, PlaceDetails
				.buildPlaceDetailUri(placeId));
		mapIntent.putExtra(Intent.EXTRA_TITLE, cursor.getString(cursor
				.getColumnIndex(PlaceDetails.PLACE_NAME)));
		startActivity(mapIntent);
	}

	public void doShareAction() {
		String placeUrl = cursor.getString(cursor.getColumnIndex(PlaceDetails.PLACE_URL));
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent
				.putExtra(Intent.EXTRA_SUBJECT, getResources().getString(R.string.share_subject));
		shareIntent.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.share_text)
				+ placeUrl);
		startActivity(shareIntent);
	}

}
