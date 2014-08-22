package com.arandroid.bilanciopersonale;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import utils.BilancioMeseCalculator;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;

import com.arandroid.bilanciopersonale.fragments.AboutFragment;
import com.arandroid.bilanciopersonale.fragments.AddRicavoFragment;
import com.arandroid.bilanciopersonale.fragments.AddSpesaFragment;
import com.arandroid.bilanciopersonale.fragments.BilanciFragment;
import com.arandroid.bilanciopersonale.fragments.CalendarFragment;
import com.arandroid.bilanciopersonale.fragments.ConfrontiFragment;
import com.arandroid.bilanciopersonale.fragments.DBManagerFragment;
import com.arandroid.bilanciopersonale.fragments.ImpostazioniFragment;
import com.arandroid.bilanciopersonale.fragments.ListFragmentRicavi;
import com.arandroid.bilanciopersonale.fragments.ListFragmentSpese;
import com.arandroid.bilanciopersonale.fragments.NavigationDrawerFragment;
import com.arandroid.bilanciopersonale.fragments.RicaviProgrammatiFragment;
import com.arandroid.bilanciopersonale.fragments.SpeseProgrammateFragment;
import com.arandroid.bplite.R;
import com.dto.BilancioMese;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

public class MainLayoutActivity extends ActionBarActivity implements
		NavigationDrawerFragment.NavigationDrawerCallbacks {

	private final static int HOME = 0;
	private final static int NUOVA_SPESA = 1;
	private final static int NUOVO_RICAVO = 2;
	private final static int RIEPILOGO_SPESE = 3;
	private final static int RIEPILOGO_RICAVI = 4;
	private final static int SPESE_PROGRAMMATE = 5;
	private final static int RICAVI_PROGRAMMATI = 6;
	private final static int BILANCI = 7;
	private final static int CONFRONTI = 8;
	private final static int GESTIONE_DB = 11;
	private final static int ABOUT = 10;
	private final static int IMPOSTAZIONI = 9;
	private static final String MY_AD_UNIT_ID = "ca-app-pub-8851138749802557/2364997223";

	private Fragment[] fragments = new Fragment[GESTIONE_DB + 1];
	
	private List<BilancioMese> bilanciFragmentConfronti;
	
	public InterstitialAd interstitial;

	/**
	 * Fragment managing the behaviors, interactions and presentation of the
	 * navigation drawer.
	 */
	private NavigationDrawerFragment mNavigationDrawerFragment;

	/**
	 * Used to store the last screen title. For use in
	 * {@link #restoreActionBar()}.
	 */
	private CharSequence mTitle;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_layout);

		mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager()
				.findFragmentById(R.id.navigation_drawer);
		mTitle = getTitle();

		// Set up the drawer.
		mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
				(DrawerLayout) findViewById(R.id.drawer_layout));
		
		 // Create the interstitial.
	    interstitial = new InterstitialAd(this);
	    interstitial.setAdUnitId(MY_AD_UNIT_ID);

	    // Create ad request.
	    AdRequest adRequest = new AdRequest.Builder().build();

	    // Begin loading your interstitial.
	    interstitial.loadAd(adRequest);
	    displayInterstitial();
		
		setupBilanciFragmentConfronti();
	}
	
	public void displayInterstitial() {
	    if (interstitial.isLoaded()) {
	      interstitial.show();
	    }
	  }

	@Override
	public void onNavigationDrawerItemSelected(int position) {
		// update the main content by replacing fragments
		FragmentManager fragmentManager = getSupportFragmentManager();
		Fragment newFragment = null;
		switch (position) {
		case HOME:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new CalendarFragment();
				fragments[position] = newFragment;
			}
			break;
		case NUOVA_SPESA:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new AddSpesaFragment();
				fragments[position] = newFragment;
			}
			break;
		case NUOVO_RICAVO:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new AddRicavoFragment();
				fragments[position] = newFragment;
			}
			break;
		case RIEPILOGO_SPESE:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new ListFragmentSpese();
				fragments[position] = newFragment;
			}
			break;
		case RIEPILOGO_RICAVI:
			if (newFragment == null) {
				newFragment = new ListFragmentRicavi();
				fragments[position] = newFragment;
			}
			break;
		case SPESE_PROGRAMMATE:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new SpeseProgrammateFragment();
				fragments[position] = newFragment;
			}
			break;
		case RICAVI_PROGRAMMATI:
			if (newFragment == null) {
				newFragment = new RicaviProgrammatiFragment();
				fragments[position] = newFragment;
			}
			break;
		case BILANCI:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new BilanciFragment();
				fragments[position] = newFragment;
			}
			break;
		case CONFRONTI:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new ConfrontiFragment();
				fragments[position] = newFragment;
			}
			break;
		case IMPOSTAZIONI:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new ImpostazioniFragment();
				fragments[position] = newFragment;
			}
			break;
		case ABOUT:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new AboutFragment();
				fragments[position] = newFragment;
			}
			break;
		case GESTIONE_DB:
			newFragment = fragments[position];
			if (newFragment == null) {
				newFragment = new DBManagerFragment();
				fragments[position] = newFragment;
			}
			break;
		default:
			break;
		}
		fragmentManager.beginTransaction().replace(R.id.container, newFragment)
				.commit();
	}

	public void restoreActionBar() {
		ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(mTitle);
	}
	
	private void setupBilanciFragmentConfronti(){
		bilanciFragmentConfronti = new ArrayList<BilancioMese>();

		Calendar c = Calendar.getInstance(Locale.getDefault());

		int currentMonth = c.get(Calendar.MONTH);
		int currentYear = c.get(Calendar.YEAR);

		c.add(Calendar.MONTH, -1);
		int lastMonth = c.get(Calendar.MONTH);

		BilancioMese current = BilancioMeseCalculator.get(currentMonth,
				currentYear, this);
		BilancioMese last = BilancioMeseCalculator.get(lastMonth,
				currentYear, this);

		bilanciFragmentConfronti.add(last);
		bilanciFragmentConfronti.add(current);
		
	}
	
	public List<BilancioMese> getBilanciFragmentConfronti() {
		return bilanciFragmentConfronti;
	}

}
