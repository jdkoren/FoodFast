package com.simbiosys.apps.foodfast.ui.phone;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckedTextView;
import android.widget.Toast;

import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.ui.BaseActivity;
import com.simbiosys.apps.foodfast.utils.AboutDialogHelper;
import com.simbiosys.apps.foodfast.utils.AppConstants;
import com.simbiosys.apps.foodfast.utils.IntegerInputDialog;
import com.simbiosys.apps.foodfast.utils.IntegerInputDialog.IntegerInputDialogListener;

public class SettingsFragment extends Fragment implements OnClickListener,
		IntegerInputDialogListener {
	@SuppressWarnings("unused")
	private static final String TAG = "SettingsFragment";

	private static final int SEARCH_RADIUS_DIALOG = 0;
	private static final int LOCATION_SENSITIVITY_DIALOG = 1;

	protected SharedPreferences prefs;
	protected Editor editor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle args) {
		View root = inflater.inflate(R.layout.settings, container, false);

		prefs = getActivity().getSharedPreferences(AppConstants.SHARED_PREFERENCES_FILE,
			Context.MODE_PRIVATE);

		View.OnClickListener checkChangedListener = new View.OnClickListener() {
			public void onClick(View v) {
				((CheckedTextView) v).toggle();
				onCheckChanged(v);
			}
		};

		// Use GPS
		CheckedTextView checkBox = (CheckedTextView) root.findViewById(R.id.check_use_gps);
		checkBox.setOnClickListener(checkChangedListener);
		checkBox.setChecked(prefs.getBoolean(AppConstants.PREF_USE_GPS,
			AppConstants.DEFAULT_USE_GPS));

		// Allow active updates
		checkBox = (CheckedTextView) root.findViewById(R.id.check_allow_active_updates);
		checkBox.setOnClickListener(checkChangedListener);
		checkBox.setChecked(prefs.getBoolean(AppConstants.PREF_ACTIVE_UPDATES_ALLOWED,
			AppConstants.DEFAULT_ACTIVE_UPDATES_ALLOWED));

		// Allow passive updates
		checkBox = (CheckedTextView) root.findViewById(R.id.check_allow_passive_updates);
		checkBox.setOnClickListener(checkChangedListener);
		checkBox.setChecked(prefs.getBoolean(AppConstants.PREF_PASSIVE_UPDATES_ALLOWED,
			AppConstants.DEFAULT_PASSIVE_UPDATES_ALLOWED));

		// Turn off passive updates on exit
		checkBox = (CheckedTextView) root.findViewById(R.id.check_disable_passive_on_exit);
		checkBox.setOnClickListener(checkChangedListener);
		checkBox.setChecked(prefs.getBoolean(AppConstants.PREF_DISABLE_PASSIVE_ON_EXIT,
			AppConstants.DEFAULT_DISABLE_PASSIVE_ON_EXIT));

		// Preserve Battery
		checkBox = (CheckedTextView) root.findViewById(R.id.check_preserve_battery);
		checkBox.setOnClickListener(checkChangedListener);
		checkBox.setChecked(prefs.getBoolean(AppConstants.PREF_PRESERVE_BATTERY,
			AppConstants.DEFAULT_PRESERVE_BATTERY));

		View v = root.findViewById(R.id.search_radius);
		v.setOnClickListener(this);
		v = root.findViewById(R.id.location_sensitivity);
		v.setOnClickListener(this);

		return root;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.settings_menu, menu);
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

	public void onCheckChanged(View v) {
		CheckedTextView checkbox = (CheckedTextView) v;
		boolean isChecked = checkbox.isChecked();
		editor = prefs.edit();

		switch (v.getId()) {
		case R.id.check_use_gps:
			editor.putBoolean(AppConstants.PREF_USE_GPS, isChecked);
			((BaseActivity) getActivity()).getLocationHelper().enableActiveLocationUpdates();
			break;
		case R.id.check_allow_active_updates:
			editor.putBoolean(AppConstants.PREF_ACTIVE_UPDATES_ALLOWED, isChecked);
			((BaseActivity) getActivity()).getLocationHelper().toggleActiveUpdates(isChecked);
			break;
		case R.id.check_allow_passive_updates:
			editor.putBoolean(AppConstants.PREF_PASSIVE_UPDATES_ALLOWED, isChecked);
			// ((BaseActivity)
			// getActivity()).getLocationHelper().togglePassiveUpdates(isChecked);
			break;
		case R.id.check_disable_passive_on_exit:
			editor.putBoolean(AppConstants.PREF_DISABLE_PASSIVE_ON_EXIT, isChecked);
		case R.id.check_preserve_battery:
			editor.putBoolean(AppConstants.PREF_PRESERVE_BATTERY, isChecked);
			break;
		}
		editor.commit();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.search_radius:
			showDialog(SEARCH_RADIUS_DIALOG);
			break;
		case R.id.location_sensitivity:
			showDialog(LOCATION_SENSITIVITY_DIALOG);
			break;
		}
	}

	/**
	 * Shows a dialog for user input.
	 */
	public void showDialog(int id) {
		int titleRes, messageRes, defValue;
		DialogFragment dialog = null;

		switch (id) {
		case SEARCH_RADIUS_DIALOG:
			titleRes = R.string.dialog_title_search_radius;
			messageRes = R.string.dialog_message_search_radius;
			defValue = prefs.getInt(AppConstants.PREF_SEARCH_RADIUS, AppConstants.DEFAULT_RADIUS);
			dialog = IntegerInputDialog.newInstance(id, titleRes, messageRes, defValue, this);
			break;
		case LOCATION_SENSITIVITY_DIALOG:
			titleRes = R.string.dialog_title_location_sensitivity;
			messageRes = R.string.dialog_message_location_sensitivity;
			defValue = prefs.getInt(AppConstants.PREF_LOCATION_SENSITIVITY,
				AppConstants.DEFAULT_SENSITIVITY);
			dialog = IntegerInputDialog.newInstance(id, titleRes, messageRes, defValue, this);
			break;
		}

		if (dialog != null) {
			dialog.show(getFragmentManager(), "dialog");
		}

	}

	@Override
	public void onPositiveClick(int id, String result) {
		int newValue = Integer.parseInt(result);
		editor = prefs.edit();

		switch (id) {
		case SEARCH_RADIUS_DIALOG:
			editor.putInt(AppConstants.PREF_SEARCH_RADIUS, newValue);
			break;
		case LOCATION_SENSITIVITY_DIALOG:
			editor.putInt(AppConstants.PREF_LOCATION_SENSITIVITY, newValue);
			((BaseActivity) getActivity()).getLocationHelper().enableActiveLocationUpdates();
			break;
		}
		editor.commit();
		Toast.makeText(getActivity(), R.string.setting_saved, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNegativeClick(int id) {
		// No-op
	}

}