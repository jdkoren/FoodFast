package com.simbiosys.apps.foodfast.utils;

import android.app.Activity;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.ui.HomeActivity;

/**
 * A class that handles some common activity functions, such as setting up the
 * action bar.
 * 
 */
public class ActivityHelper {
	private static final String TAG = "ActivityHelper";

	protected Activity activity;

	public static ActivityHelper createInstance(Activity activity) {
		if (AppConstants.SUPPORTS_HONEYCOMB) {
			return new ActivityHelperHoneycomb(activity);
		}
		return new ActivityHelper(activity);
	}

	protected ActivityHelper(Activity activity) {
		this.activity = activity;
	}

	public void onPostCreate(CharSequence title) {
		setupActionBar(title);

		// Add action items to action bar
		SimpleMenu menu = new SimpleMenu(activity);
		activity.onCreatePanelMenu(Window.FEATURE_OPTIONS_PANEL, menu);
		// activity.onPrepareOptionsMenu(menu);
		for (int i = 0; i < menu.size(); i++) {
			MenuItem item = menu.getItem(i);
			addActionButtonCompatFromMenuItem(item);
		}
	}

	/**
	 * Load default menu items into menu.
	 */
	public boolean onCreateOptionsMenu(Menu menu) {
		activity.getMenuInflater().inflate(R.menu.menu_items_default, menu);
		return false;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Default option item handling goes here
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {
			return true;
		}
		return false;
	}

	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			goHome();
			return true;
		}
		return false;
	}

	/**
	 * Sets up this activity as the home activity for the app.
	 */
	public void setupHomeActivity() {
	}
	
	/**
	 * Sets up this activity as a sub-activity in the app.
	 */
	public void setupSubActivity() {
	}

	/**
	 * Return to the home screen.
	 */
	public void goHome() {
		if (activity instanceof HomeActivity) {
			return;
		}

		final Intent intent = new Intent(activity, HomeActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		activity.startActivity(intent);
	}

	/**
	 * Returns the compatibility action bar.
	 */
	public ViewGroup getActionBarCompat() {
		return (ViewGroup) activity.findViewById(R.id.actionbar_compat);
	}

	/**
	 * Sets up the action bar for the activity.
	 */
	public void setupActionBar(CharSequence title) {
		final ViewGroup actionBarCompat = getActionBarCompat();
		if (actionBarCompat == null) {
			return;
		}

		LinearLayout.LayoutParams springParams = new LinearLayout.LayoutParams(0,
				ViewGroup.LayoutParams.FILL_PARENT);
		springParams.weight = 1;

		View.OnClickListener homeClickListener = new OnClickListener() {
			public void onClick(View v) {
				goHome();
			}
		};

		if (title != null) {
			// Add Home button
			addActionButtonCompat(R.drawable.ic_action_home, R.string.description_home,
				homeClickListener, true);

			// Add title text
			TextView titleText = new TextView(activity, null, R.attr.actionbarCompatTextStyle);
			titleText.setLayoutParams(springParams);
			titleText.setText(title);
			actionBarCompat.addView(titleText);
		} else {
			// Add logo
			ImageButton logo = new ImageButton(activity, null, R.attr.actionbarCompatLogoStyle);
			logo.setOnClickListener(homeClickListener);
			actionBarCompat.addView(logo);

			// Add spring view (to right-align future children)
			View spring = new View(activity);
			spring.setLayoutParams(springParams);
			actionBarCompat.addView(spring);
		}
	}

	/**
	 * Sets the title of the action bar on pre-honeycomb phones.
	 */
	public void setActionBarTitle(CharSequence title) {
		ViewGroup actionBar = getActionBarCompat();
		if (actionBar == null) {
			return;
		}

		TextView titleText = (TextView) activity.findViewById(R.id.actionbar_compat_text);
		titleText.setText(title);
	}

	/**
	 * Adds an action bar button to the action bar on pre-honeycomb phones.
	 */
	private View addActionButtonCompat(int iconRes, int textRes,
			View.OnClickListener clickListener, boolean separatorAfter) {
		final ViewGroup actionBar = getActionBarCompat();
		if (actionBar == null) {
			return null;
		}

		// Separator
		ImageView separator = new ImageView(activity, null, R.attr.actionbarCompatSeparatorStyle);
		separator
				.setLayoutParams(new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

		// Action button
		ImageButton actionButton = new ImageButton(activity, null,
				R.attr.actionbarCompatButtonStyle);
		int actionBarHeight = (int) activity.getResources().getDimension(
			R.dimen.actionbar_compat_height);
		actionButton.setLayoutParams(new ViewGroup.LayoutParams(actionBarHeight,
				ViewGroup.LayoutParams.FILL_PARENT));
		actionButton.setImageResource(iconRes);
		actionButton.setScaleType(ImageView.ScaleType.CENTER);
		actionButton.setContentDescription(activity.getResources().getString(textRes));
		actionButton.setOnClickListener(clickListener);

		// Add separator and action button to action bar
		if (!separatorAfter) {
			actionBar.addView(separator);
		}

		actionBar.addView(actionButton);

		if (separatorAfter) {
			actionBar.addView(separator);
		}

		return actionButton;
	}

	/**
	 * Adds an action bar button to the action bar from a MenuItem.
	 */
	private View addActionButtonCompatFromMenuItem(final MenuItem item) {
		final ViewGroup actionBar = getActionBarCompat();
		if (actionBar == null) {
			return null;
		}

		// Separator
		ImageView separator = new ImageView(activity, null, R.attr.actionbarCompatSeparatorStyle);
		separator
				.setLayoutParams(new ViewGroup.LayoutParams(2, ViewGroup.LayoutParams.FILL_PARENT));

		// Action button
		ImageButton actionButton = new ImageButton(activity, null,
				R.attr.actionbarCompatButtonStyle);
		actionButton.setId(item.getItemId());
		int actionBarHeight = (int) activity.getResources().getDimension(
			R.dimen.actionbar_compat_height);
		actionButton.setLayoutParams(new ViewGroup.LayoutParams(actionBarHeight,
				ViewGroup.LayoutParams.FILL_PARENT));
		actionButton.setImageDrawable(item.getIcon());
		actionButton.setScaleType(ImageView.ScaleType.CENTER);
		actionButton.setContentDescription(item.getTitle());
		actionButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				activity.onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, item);
			}
		});

		actionBar.addView(separator);
		actionBar.addView(actionButton);

		// Refresh buttons should be stateful and show indeterminate progress
		// indicator.
		if (item.getItemId() == R.id.menu_refresh) {
			int btnWidth = activity.getResources().getDimensionPixelSize(
				R.dimen.actionbar_compat_height);
			int btnWidthDiv3 = btnWidth / 3;
			ProgressBar indicator = new ProgressBar(activity, null,
					R.attr.actionbarCompatProgressIndicatorStyle);
			LinearLayout.LayoutParams indicatorParams = new LinearLayout.LayoutParams(btnWidthDiv3,
					btnWidthDiv3);
			indicatorParams.setMargins(btnWidthDiv3, btnWidthDiv3, btnWidthDiv3, 0);
			indicator.setLayoutParams(indicatorParams);
			indicator.setVisibility(View.GONE);
			indicator.setId(R.id.menu_refresh_progress);
			actionBar.addView(indicator);
		}

		return actionButton;
	}

	/**
	 * Sets the state of a refresh button added by
	 * AddActionButtonCompatFromMenuItem, where the item ID was menu_refresh.
	 */
	public void setRefreshActionButtonCompatState(boolean refreshing) {
		View refreshButton = activity.findViewById(R.id.menu_refresh);
		View refreshIndicator = activity.findViewById(R.id.menu_refresh_progress);

		if (refreshButton != null) {
			refreshButton.setVisibility(refreshing ? View.GONE : View.VISIBLE);
		}
		if (refreshIndicator != null) {
			refreshIndicator.setVisibility(refreshing ? View.VISIBLE : View.GONE);
		}
	}
}
