package com.simbiosys.apps.foodfast.ui.phone;

import android.support.v4.app.Fragment;

import com.simbiosys.apps.foodfast.ui.BaseSinglePaneActivity;
import com.simbiosys.apps.foodfast.ui.PlaceDetailFragment;

public class PlaceDetailActivity extends BaseSinglePaneActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "PlaceDetailActivity";

	@Override
	protected Fragment onCreatePane() {
		return new PlaceDetailFragment();
	}

}
