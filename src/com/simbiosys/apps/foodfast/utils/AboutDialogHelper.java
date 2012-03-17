package com.simbiosys.apps.foodfast.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;

import com.simbiosys.apps.foodfast.R;

public class AboutDialogHelper {
	private static final String TAG = "AboutDialogHelper";

	public static void showAboutDialog(Activity activity) {
		LayoutInflater inflater = activity.getLayoutInflater();
		View dialogView = inflater.inflate(R.layout.about_dialog, null);

		AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
		dialog.setTitle(R.string.dialog_title_about).setCancelable(true).setView(dialogView);

		dialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog.show();
	}
}
