package com.simbiosys.apps.foodfast.utils;

import com.simbiosys.apps.foodfast.R;

import android.app.ActionBar;
import android.app.Activity;
import android.content.res.Configuration;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Platform-specific implementation of the ActivityHelper class.
 * 
 */
public class ActivityHelperHoneycomb extends ActivityHelper {
	@SuppressWarnings("unused")
	private static final String TAG = "ActivityHelperHoneycomb";

	protected Menu menu;

	protected ActivityHelperHoneycomb(Activity activity) {
		super(activity);
	}

	@Override
	public void onPostCreate(CharSequence title) {
		// No-op on Honeycomb
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// TODO Handle HOME / UP affordance. Currently just goes home.
			goHome();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void setupHomeActivity() {
		super.setupHomeActivity();
		if (isTablet()) {
			// Tablet
			activity.getActionBar().setDisplayOptions(0,
				ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
		} else {
			// Phone
			activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_USE_LOGO,
				ActionBar.DISPLAY_USE_LOGO | ActionBar.DISPLAY_SHOW_TITLE);
		}
	}

	@Override
	public void setupSubActivity() {
		super.setupSubActivity();
		if (isTablet()) {
			// Tablet
			activity.getActionBar().setDisplayOptions(
				ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO,
				ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
		} else {
			// Phone
			activity.getActionBar().setDisplayOptions(0,
				ActionBar.DISPLAY_HOME_AS_UP | ActionBar.DISPLAY_USE_LOGO);
		}

	}

	@Override
	public void setActionBarTitle(CharSequence title) {
		// No-op on Honeycomb
	}

	@Override
	public void setRefreshActionButtonCompatState(boolean refreshing) {
		if (menu == null) {
			return;
		}

		// On Honeycomb, show refresh state using custom action view on the
		// button.
		final MenuItem refreshItem = menu.findItem(R.id.menu_refresh);
		if (refreshItem != null) {
			if (refreshing) {
				refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
			} else {
				refreshItem.setActionView(null);
			}
		}
	}

	private boolean isTablet() {
		int screenSize = activity.getResources().getConfiguration().screenLayout
				& Configuration.SCREENLAYOUT_SIZE_MASK;
		return screenSize >= Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

}
