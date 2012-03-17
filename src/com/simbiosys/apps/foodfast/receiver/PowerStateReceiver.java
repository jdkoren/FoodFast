package com.simbiosys.apps.foodfast.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

/**
 * A receiver to detect changes is battery state. When the system broadcasts
 * that battery is low, the passive location receiver should be disabled,
 * otherwise it may fire the updater service to download place data.
 * 
 * When the system broadcasts that battery is OK, the receiver can be enabled
 * (by setting it to default state, which is enabled).
 * 
 */
public class PowerStateReceiver extends BroadcastReceiver {
	private static final String TAG = "PowerStateReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean batteryLow = intent.getAction().equals(Intent.ACTION_BATTERY_LOW);

		PackageManager packageManager = context.getPackageManager();
		ComponentName passiveLocationReceiver = new ComponentName(context,
				PassiveLocationChangedReceiver.class);

		packageManager.setComponentEnabledSetting(passiveLocationReceiver,
			batteryLow ? PackageManager.COMPONENT_ENABLED_STATE_DISABLED
					: PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

	}

}
