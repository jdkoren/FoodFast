package com.simbiosys.apps.foodfast.ui.phone;

import android.support.v4.app.Fragment;

import com.simbiosys.apps.foodfast.ui.BaseSinglePaneActivity;
import com.simbiosys.apps.foodfast.ui.PlaceListFragment;

public class PlaceListActivity extends BaseSinglePaneActivity {
	private static final String TAG = "PlaceListActivity";

	@Override
	protected Fragment onCreatePane() {
		return new PlaceListFragment();
	}

}
