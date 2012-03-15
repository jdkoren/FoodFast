package com.simbiosys.apps.foodfast.ui;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.provider.PlacesContract.PlaceDetails;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;
import com.simbiosys.apps.foodfast.service.PlaceDetailsUpdateService;
import com.simbiosys.apps.foodfast.service.PlaceDetailsUpdateServiceICS;
import com.simbiosys.apps.foodfast.ui.phone.SettingsActivity;
import com.simbiosys.apps.foodfast.utils.AppConstants;

public class PlaceListFragment extends ListFragment implements LoaderCallbacks<Cursor> {
	@SuppressWarnings("unused")
	private static final String TAG = "PlaceListFragment";

	protected Cursor cursor = null;
	protected SimpleCursorAdapter adapter;
	protected Handler handler = new Handler();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		adapter = new SimpleCursorAdapter(
				getActivity(),
				R.layout.place_list_item,
				cursor,
				new String[] { Places.PLACE_NAME, Places.PLACE_VICINITY, Places.PLACE_DISTANCE },
				new int[] { R.id.text_place_name, R.id.text_place_address, R.id.text_place_distance },
				0);

		adapter.setViewBinder(new ViewBinder() {
			public boolean setViewValue(View v, Cursor c, int id) {
				if (v.getId() == R.id.text_place_distance) {
					int distance = (int) c.getFloat(c.getColumnIndex(Places.PLACE_DISTANCE));
					((TextView) v).setText("(" + distance + "m) ");
					return true;
				}
				return false;
			}
		});

		View headerView = getActivity().getLayoutInflater().inflate(R.layout.place_list_header, null);
		Button btnMapAll = (Button) headerView.findViewById(R.id.btn_map_all);
		
		btnMapAll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AppConstants.ACTION_VIEW_MAP, Places.CONTENT_URI);
				startActivity(intent);
			}
		});
		
		getListView().addHeaderView(headerView);
		setEmptyText(getResources().getString(R.string.place_list_empty));
		
		setListAdapter(adapter);
		setListShown(false);
		getLoaderManager().initLoader(0, null, this);

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Cursor c = adapter.getCursor();
		c.moveToPosition(position - 1); // offset by one because of list header

		String placeId = c.getString(c.getColumnIndex(Places.PLACE_ID));
		String placeReference = c.getString(c.getColumnIndex(Places.PLACE_REFERENCE));
		String placeName = c.getString(c.getColumnIndex(Places.PLACE_NAME));

		// Have PlaceDetailsUpdateService get details about this place
		Intent updateIntent = new Intent(getActivity(),
				AppConstants.SUPPORTS_ICS ? PlaceDetailsUpdateServiceICS.class
						: PlaceDetailsUpdateService.class);
		updateIntent.putExtra(AppConstants.EXTRA_ID, placeId);
		updateIntent.putExtra(AppConstants.EXTRA_REFERENCE, placeReference);
		updateIntent.putExtra(AppConstants.EXTRA_FORCE_UPDATE, true);
		getActivity().startService(updateIntent);

		// Start an activity to show the place details
		Intent showDetailIntent = new Intent(Intent.ACTION_VIEW, PlaceDetails
				.buildPlaceDetailUri(placeId));
		showDetailIntent.putExtra(Intent.EXTRA_TITLE, placeName);
		getActivity().startActivity(showDetailIntent);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.place_list_menu, menu);
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
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		((BaseActivity) getActivity()).getActivityHelper().setRefreshActionButtonCompatState(true);
		
		String[] projection = new String[] { Places._ID, Places.PLACE_ID, Places.PLACE_NAME,
				Places.PLACE_VICINITY, Places.PLACE_DISTANCE, Places.PLACE_REFERENCE };
		return new CursorLoader(getActivity(), Places.CONTENT_URI, projection, null, null,
				Places.DEFAULT_SORT);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		adapter.swapCursor(data);
		if (isResumed()) {
			setListShown(true);
		} else {
			setListShownNoAnimation(true);
		}
		((BaseActivity) getActivity()).getActivityHelper().setRefreshActionButtonCompatState(false);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		adapter.swapCursor(null);

	}

}
