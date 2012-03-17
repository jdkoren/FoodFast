package com.simbiosys.apps.foodfast.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.simbiosys.apps.foodfast.R;

/**
 * A BaseActivity holding a single fragment. The intent used to start this
 * activity is forwarded to the fragment as arguments. Subclasses should only
 * have to implement onCreatePane().
 * 
 */
public abstract class BaseSinglePaneActivity extends BaseActivity {
	private static final String TAG = "BaseSinglePaneActivity";
	
	private Fragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_singlepane_empty);
		
		if (savedInstanceState == null) {
			fragment = onCreatePane();
			fragment.setArguments(intentToFragmentArguments(getIntent()));
			
			getSupportFragmentManager().beginTransaction().add(R.id.root_container, fragment).commit();
		}
	}

	/**
	 * Called in onCreate when the fragment for this activity is needed.
	 */
	protected abstract Fragment onCreatePane();

}
