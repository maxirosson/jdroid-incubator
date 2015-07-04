package com.jdroid.sample.android.ui.drive;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.jdroid.android.activity.FragmentContainerActivity;

public class GoogleDriveActivity extends FragmentContainerActivity {
	
	/**
	 * @see FragmentContainerActivity#getFragmentClass()
	 */
	@Override
	protected Class<? extends Fragment> getFragmentClass() {
		return GoogleDriveFragment.class;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		getFragment().onActivityResult(requestCode, resultCode, data);
	}
}
