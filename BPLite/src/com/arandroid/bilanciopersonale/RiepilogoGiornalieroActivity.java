package com.arandroid.bilanciopersonale;

import com.arandroid.bilanciopersonale.fragments.RiepilogoGiornalieroFragment;
import com.arandroid.bplite.R;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;

public class RiepilogoGiornalieroActivity extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_riepilogo_giornaliero);

		if (savedInstanceState == null) {
			getSupportFragmentManager().beginTransaction()
					.add(R.id.container, new RiepilogoGiornalieroFragment()).commit();
		}
	}

}
