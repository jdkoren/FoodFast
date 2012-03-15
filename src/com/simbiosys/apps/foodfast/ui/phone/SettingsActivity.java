package com.simbiosys.apps.foodfast.ui.phone;

import android.support.v4.app.Fragment;

import com.simbiosys.apps.foodfast.ui.BaseSinglePaneActivity;

public class SettingsActivity extends BaseSinglePaneActivity {
	@SuppressWarnings("unused")
	private static final String TAG = "SettingsActivity";

	@Override
	protected Fragment onCreatePane() {
		return new SettingsFragment();
	}

}
