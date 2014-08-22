package com.arandroid.bilanciopersonale.fragments;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.ListView;
import android.widget.TextView;

import com.arandroid.bilanciopersonale.AddRicavoActivity;
import com.arandroid.bilanciopersonale.AddSpesaActivity;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.dto.VoceBilancio;
import com.google.android.gms.ads.AdRequest;
import com.ui.gestionespese.VoceBilancioAdapterDay;

public class RiepilogoGiornalieroFragment extends Fragment implements
		OnClickListener {
	private ListView listView;
	private List<VoceBilancio> events;
	private Activity context;
	private View rootView;

	private View nuovaSpesa;
	private View nuovoRicavo;
	private AlertDialog dialog;
	private String data;

	@SuppressWarnings("unchecked")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_riepilogo_giornaliero,
				container, false);
		listView = (ListView) rootView.findViewById(R.id.listView1);
		context = getActivity();
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}
		
		data = context.getIntent().getStringExtra("data");
		events = (List<VoceBilancio>) context.getIntent().getSerializableExtra(
				"lista");
		VoceBilancioAdapterDay adapter = new VoceBilancioAdapterDay(context,
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
		inflater.inflate(R.menu.riepilogo, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.nuovo) {
			createAndShowNewDialog();
			return true;
		}
		return false;
	}

	private void createAndShowNewDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getLayoutInflater(getArguments());
		View v = inflater.inflate(R.layout.new_item_dialog, null);

		nuovaSpesa = v.findViewById(R.id.nuova_spesa);
		nuovoRicavo = v.findViewById(R.id.nuovo_ricavo);

		nuovaSpesa.setOnClickListener(this);
		nuovoRicavo.setOnClickListener(this);

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
		if (arg0.equals(nuovaSpesa)) {
			context.setResult(Activity.RESULT_OK);
			context.finish();
			Intent intent;
			intent = new Intent(context, AddSpesaActivity.class);
			intent.putExtra("data", data);
			context.startActivity(intent);
			dialog.dismiss();
		} else if (arg0.equals(nuovoRicavo)) {
			context.setResult(Activity.RESULT_OK);
			context.finish();
			Intent intent;
			intent = new Intent(context, AddRicavoActivity.class);
			intent.putExtra("data", data);
			context.startActivity(intent);
			dialog.dismiss();
		}
	}
}
