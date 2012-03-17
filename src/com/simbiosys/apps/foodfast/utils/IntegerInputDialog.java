package com.simbiosys.apps.foodfast.utils;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.simbiosys.apps.foodfast.R;

/**
 * Class for showing a dialog to collect integer input, showing a custom title
 * and message.
 */
public class IntegerInputDialog extends DialogFragment {
	private static final String TAG = "IntegerInputDialog";

	/**
	 * Interface for classes using this DialogFragment;
	 */
	public interface IntegerInputDialogListener {
		public void onPositiveClick(int id, String result);

		public void onNegativeClick(int id);
	}

	private static final String ARG_ID = "id";
	private static final String ARG_TITLE = "arg_title";
	private static final String ARG_MESSAGE = "arg_message";
	private static final String ARG_DEFAULT_VALUE = "arg_value";

	private IntegerInputDialogListener listener;

	public static IntegerInputDialog newInstance(int id, int titleRes, int messageRes,
			int defValue, IntegerInputDialogListener listener) {
		IntegerInputDialog fragment = new IntegerInputDialog();

		Bundle args = new Bundle();
		args.putInt(ARG_ID, id);
		args.putInt(ARG_TITLE, titleRes);
		args.putInt(ARG_MESSAGE, messageRes);
		args.putInt(ARG_DEFAULT_VALUE, defValue);
		fragment.setArguments(args);

		fragment.listener = listener;

		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();

		LayoutInflater inflater = getActivity().getLayoutInflater();
		final View dialogView = inflater.inflate(R.layout.integer_input_dialog, null);
		final EditText editInput = (EditText) dialogView.findViewById(R.id.edit_dialog_input);
		final TextView textMessage = (TextView) dialogView.findViewById(R.id.text_dialog_message);

		textMessage.setText(args.getInt(ARG_MESSAGE));
		editInput.append(Integer.toString(args.getInt(ARG_DEFAULT_VALUE)));

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(args.getInt(ARG_TITLE)).setView(dialogView);

		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.onPositiveClick(args.getInt(ARG_ID), editInput.getText().toString());
				dialog.dismiss();
			}
		});

		builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				listener.onNegativeClick(args.getInt(ARG_ID));
				dialog.dismiss();
			}
		});

		return builder.create();
	}

}
