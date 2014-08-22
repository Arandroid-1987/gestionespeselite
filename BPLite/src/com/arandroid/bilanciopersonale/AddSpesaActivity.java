package com.arandroid.bilanciopersonale;

import com.arandroid.bilanciopersonale.fragments.AddSpesaFragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

public class AddSpesaActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment newFragment = null;
		newFragment = new AddSpesaFragment();
		fragmentManager.beginTransaction()
				.add(android.R.id.content, newFragment).commit();
	}

}
