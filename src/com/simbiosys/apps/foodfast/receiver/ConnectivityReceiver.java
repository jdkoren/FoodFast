package com.simbiosys.apps.foodfast.receiver;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Receiver to listen for changes in connectivity.
 * 
 */
public class ConnectivityReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		ConnectivityManager connectivityManager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);

		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

		if (activeNetwork != null && activeNetwork.isConnectedOrConnecting()) {
			// We are connected; disable this receiver and enable the location
			// receivers.

			PackageManager packageManager = context.getPackageManager();

			ComponentName connectivityReceiver = new ComponentName(context,
					ConnectivityReceiver.class);
			ComponentName locationReceiver = new ComponentName(context,
					LocationChangedReceiver.class);
			ComponentName passiveLocationReceiver = new ComponentName(context,
					PassiveLocationChangedReceiver.class);

			// This receiver should only have been enabled when a service
			// disables updates due to no connectivity. Default state is
			// disabled.
			packageManager.setComponentEnabledSetting(connectivityReceiver,
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

			// Location receivers should only have been disabled when a service
			// disables updates due to no connectivity. Default state for both
			// is enabled.
			packageManager.setComponentEnabledSetting(locationReceiver,
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

			packageManager.setComponentEnabledSetting(passiveLocationReceiver,
				PackageManager.COMPONENT_ENABLED_STATE_DEFAULT, PackageManager.DONT_KILL_APP);

		}

	}

}
