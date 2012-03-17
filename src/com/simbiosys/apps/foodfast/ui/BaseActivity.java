package com.simbiosys.apps.foodfast.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.simbiosys.apps.foodfast.FoodFastApp;
import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.service.PlacesUpdateService;
import com.simbiosys.apps.foodfast.utils.ActivityHelper;
import com.simbiosys.apps.foodfast.utils.AppConstants;
import com.simbiosys.apps.foodfast.utils.LocationHelper;

/**
 * Base activity for the application. This class should not be used directly.
 * Activities should inherit from BaseSinglePaneActivity.
 * 
 */
public abstract class BaseActivity extends FragmentActivity {
	private static final String TAG = "BaseActivity";

	protected FoodFastApp application;
	final ActivityHelper activityHelper = ActivityHelper.createInstance(this);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		application = (FoodFastApp) getApplication();
		application.onActivityCreated();
	}

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		activityHelper.onPostCreate(getTitle());

		final String customTitle = getIntent().getStringExtra(Intent.EXTRA_TITLE);
		if (customTitle != null) {
			activityHelper.setActionBarTitle(customTitle);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		application.onActivityResumed(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		application.onActivityPaused();
	}

	@Override
	protected void onStop() {
		super.onStop();
		application.onActivityStopped();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		application.onActivityDestroyed(isFinishing());
	}

	@Override
	public boolean onKeyLongPress(int keyCode, KeyEvent event) {
		return activityHelper.onKeyLongPress(keyCode, event)
				|| super.onKeyLongPress(keyCode, event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return activityHelper.onKeyDown(keyCode, event) || super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		boolean result = false;
		result |= activityHelper.onCreateOptionsMenu(menu);
		result |= super.onCreateOptionsMenu(menu);
		return result;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		return activityHelper.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
	}

	/**
	 * Returns the ActivityHelper associated with this activity.
	 */
	public ActivityHelper getActivityHelper() {
		return activityHelper;
	}

	/**
	 * Returns the LocationHelper associated with the application.
	 */
	public LocationHelper getLocationHelper() {
		return application.getLocationHelper();
	}

	/**
	 * Transforms an intent into fragment arguments (a Bundle).
	 */
	public static Bundle intentToFragmentArguments(Intent intent) {
		Bundle arguments = new Bundle();
		if (intent == null) {
			return arguments;
		}

		final Uri data = intent.getData();
		if (data != null) {
			arguments.putParcelable("_uri", data);
		}

		if (intent.getExtras() != null) {
			arguments.putAll(intent.getExtras());
		}

		return arguments;
	}

	/**
	 * Transforms fragment arguments (a Bundle) into an intent.
	 */
	public static Intent fragmentArgumentsToIntent(Bundle arguments) {
		Intent intent = new Intent();
		if (arguments == null) {
			return intent;
		}

		final Uri data = arguments.getParcelable("_uri");
		if (data != null) {
			intent.setData(data);
			arguments.remove("_uri");
		}

		intent.putExtras(arguments);
		return intent;
	}

	public void performRefresh() {
		// Start spinning the refresh icon.
		activityHelper.setRefreshActionButtonCompatState(true);

		// Register to receive the result of the update
		IntentFilter filter = new IntentFilter(AppConstants.ACTION_UPDATE_SERVICE_RESULT);
		registerReceiver(updateResultReceiver, filter);

		getLocationHelper().performRefresh();
	}

	protected BroadcastReceiver updateResultReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			int resultCode = intent.getIntExtra(AppConstants.EXTRA_UPDATE_RESULT, -1);
			if (resultCode != PlacesUpdateService.RESULT_OK) {
				// There was an error or the update aborted. Stop spinning the
				// refresh icon.
				activityHelper.setRefreshActionButtonCompatState(false);
				Toast.makeText(context, R.string.update_service_error, Toast.LENGTH_SHORT).show();
			} /*
			 * else the activity will stop the refresh icon when the loader
			 * finishes
			 */
			// unregister receiver
			context.unregisterReceiver(updateResultReceiver);
		}
	};
}
