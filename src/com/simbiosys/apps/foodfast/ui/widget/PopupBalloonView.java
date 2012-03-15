package com.simbiosys.apps.foodfast.ui.widget;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.maps.OverlayItem;
import com.simbiosys.apps.foodfast.R;

/**
 * A view used with the PopupBalloonCursorOverlay.
 */
public class PopupBalloonView<Item extends OverlayItem> extends FrameLayout {
	@SuppressWarnings("unused")
	private static final String TAG = "PopupBalloonView";
	
	protected LinearLayout layout;
	protected TextView textTitle;
	protected TextView textSubtitle;

	public PopupBalloonView(Context context, int paddingBottom) {
		super(context);

		layout = new LinearLayout(context);
		setupView(context, layout);
		layout.setPadding(0, 0, 0, paddingBottom);

		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.NO_GRAVITY;
		layout.setVisibility(VISIBLE);

		addView(layout, params);
	}

	private void setupView(Context context, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.popup_balloon, parent);
		textTitle = (TextView) v.findViewById(R.id.text_balloon_title);
		textSubtitle = (TextView) v.findViewById(R.id.text_balloon_subtitle);

	}
	
	public void setData(Item item) {
		if (item.getTitle() != null) {
			textTitle.setText(item.getTitle());
			textTitle.setVisibility(VISIBLE);
		} else {
			textTitle.setVisibility(GONE);
		}
	}

}
