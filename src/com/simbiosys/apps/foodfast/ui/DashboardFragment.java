package com.simbiosys.apps.foodfast.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.provider.PlacesContract.Places;
import com.simbiosys.apps.foodfast.ui.phone.PlaceListActivity;
import com.simbiosys.apps.foodfast.ui.phone.SettingsActivity;
import com.simbiosys.apps.foodfast.utils.AboutDialogHelper;
import com.simbiosys.apps.foodfast.utils.AppConstants;

public class DashboardFragment extends Fragment {
	private static final String TAG = "DashboardFragment";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View root = inflater.inflate(R.layout.dashboard, container);

		// Handle button clicks
		root.findViewById(R.id.btn_list_places).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), PlaceListActivity.class);
				startActivity(intent);
			}
		});

		root.findViewById(R.id.btn_map_places).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(AppConstants.ACTION_VIEW_MAP, Places.CONTENT_URI);
				startActivity(intent);
			}
		});

		root.findViewById(R.id.btn_settings).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(getActivity(), SettingsActivity.class);
				startActivity(intent);
			}
		});

		return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.dashboard_menu, menu);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_about) {
			AboutDialogHelper.showAboutDialog(getActivity());
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
