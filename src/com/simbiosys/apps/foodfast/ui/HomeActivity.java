package com.simbiosys.apps.foodfast.ui;

import android.os.Bundle;

import com.simbiosys.apps.foodfast.R;

public class HomeActivity extends BaseActivity {
	private static final String TAG = "HomeActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		setTitle(null);
		super.onPostCreate(savedInstanceState);
	}

}
