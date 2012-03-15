package com.simbiosys.apps.foodfast.ui.phone;

import android.support.v4.app.Fragment;

import com.google.android.maps.MapView;
import com.simbiosys.apps.foodfast.R;
import com.simbiosys.apps.foodfast.ui.BaseSinglePaneActivity;
import com.simbiosys.apps.foodfast.ui.MapScreenFragment;
import com.simbiosys.apps.foodfast.ui.MapScreenFragment.IMapViewHolder;
import com.simbiosys.apps.foodfast.utils.AppConstants;

public class MapScreenActivity extends BaseSinglePaneActivity implements IMapViewHolder {
	@SuppressWarnings("unused")
	private static final String TAG = "MapScreenActivity";
	
	protected MapView mapView;

	@Override
	protected Fragment onCreatePane() {
		return new MapScreenFragment();
	}
	
	public MapView getMapView() {
		if (mapView == null) {
			mapView = new MapView(this, AppConstants.MAPS_API_KEY);
		}

		return mapView;
	}

}
