package com.arandroid.bilanciopersonale.fragments;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import utils.BilancioMeseCalculator;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.arandroid.charts.BarChartView;
import com.dto.BilancioMese;
import com.google.android.gms.ads.AdRequest;
import com.ui.gestionespese.BilancioMeseAdapter;

public class ConfrontiFragment extends Fragment implements OnClickListener {
	private ListView listView;
	private List<BilancioMese> events;
	private BilancioMeseAdapter adapter;
	private Activity context;
	private View rootView;

	private View okButton;
	private Spinner mesiSpinner;
	private Spinner anniSpinner;
	private AlertDialog dialog;

	private int currentYear;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.confronti_fragment_layout,
				container, false);
		listView = (ListView) rootView.findViewById(R.id.confrontiListView);
		context = getActivity();
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}
		
		if (context instanceof MainLayoutActivity) {
			MainLayoutActivity activity = (MainLayoutActivity) context;
			events = activity.getBilanciFragmentConfronti();
			Calendar c = Calendar.getInstance(Locale.getDefault());
			currentYear = c.get(Calendar.YEAR);
		} else {
			events = new ArrayList<BilancioMese>();

			Calendar c = Calendar.getInstance(Locale.getDefault());

			int currentMonth = c.get(Calendar.MONTH);
			currentYear = c.get(Calendar.YEAR);

			c.add(Calendar.MONTH, -1);
			int lastMonth = c.get(Calendar.MONTH);

			BilancioMese current = BilancioMeseCalculator.get(currentMonth,
					currentYear, context);
			BilancioMese last = BilancioMeseCalculator.get(lastMonth,
					currentYear, context);

			events.add(last);
			events.add(current);
		}

		adapter = new BilancioMeseAdapter(context,
				android.R.layout.simple_list_item_1, events);
		listView.setAdapter(adapter);
		return rootView;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.confronti, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.nuovo) {
			createAndShowNewDialog();
			return true;
		} else if (id == R.id.grafici) {
			createAndShowGraphDialog();
			return true;
		}
		return false;
	}

	private void createAndShowGraphDialog() {
		CategorySeries values = new CategorySeries("");
		values.add(getResources().getString(R.string.ricavi), 0);
		values.add(getResources().getString(R.string.ricavi), 0);
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		List<Double[]> serieBilanci = calcolaAndamentoBilancio();
		XYSeries ricaviSeries = new XYSeries(getResources().getString(
				R.string.ricavi));
		XYSeries speseSeries = new XYSeries(getResources().getString(
				R.string.spese));
		for (int j = 0; j < serieBilanci.size(); j++) {
			ricaviSeries.add(j + 1, serieBilanci.get(j)[0]);
			speseSeries.add(j + 1, serieBilanci.get(j)[1]);
		}
		dataset.addSeries(ricaviSeries);
		dataset.addSeries(speseSeries);

		String[] labels = new String[events.size()];
		int index = 0;
		for (BilancioMese bm1 : events) {
			labels[index++] = bm1.getMese() + " " + bm1.getAnno();
		}

		GraphicalView chartView = (GraphicalView) BarChartView.getView(context,
				values, dataset, labels);

		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setView(chartView);
		dialog = builder.create();
		dialog.show();
	}

	private List<Double[]> calcolaAndamentoBilancio() {
		List<Double[]> ris = new ArrayList<Double[]>();
		for (BilancioMese bm : events) {
			Double[] array = new Double[2];
			array[1] = bm.getSpese();
			array[0] = bm.getRicavi();
			ris.add(array);
		}
		return ris;
	}

	private void createAndShowNewDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getLayoutInflater(getArguments());
		View v = inflater.inflate(R.layout.confronti_new_dialog, null);

		okButton = v.findViewById(R.id.okButton);
		anniSpinner = (Spinner) v.findViewById(R.id.anniSpinner);
		mesiSpinner = (Spinner) v.findViewById(R.id.mesiSpinner);

		ArrayList<Integer> years = new ArrayList<Integer>();

		for (int i = currentYear; i >= currentYear - 10; i--) {
			years.add(i);
		}

		ArrayAdapter<Integer> anniSpinnerAdapter = new ArrayAdapter<Integer>(
				context, android.R.layout.simple_spinner_dropdown_item, years);

		anniSpinner.setAdapter(anniSpinnerAdapter);

		okButton.setOnClickListener(this);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getString(R.string.aggiungi));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getString(R.string.aggiungi));
		}

		builder.setView(v);
		dialog = builder.create();
		dialog.show();
	}

	@Override
	public void onClick(View arg0) {
		if (arg0.equals(okButton)) {
			int mese = mesiSpinner.getSelectedItemPosition();
			int anno = (Integer) anniSpinner.getSelectedItem();
			BilancioMese bm = BilancioMeseCalculator.get(mese, anno, context);
			int index = 0;
			for (BilancioMese bm1 : events) {
				if (bm1.getAnnoNumero() > anno) {
					break;
				}
				if (bm1.getMeseNumero() > mese) {
					break;
				}
				index++;
			}
			events.add(index, bm);
			adapter.notifyDataSetChanged();
			dialog.dismiss();
		}
	}
}
