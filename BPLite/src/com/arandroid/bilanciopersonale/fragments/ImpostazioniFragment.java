package com.arandroid.bilanciopersonale.fragments;

import java.io.File;
import java.util.Collection;

import utils.file.export.TXTExport;
import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.FileChooser;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.dao.RicavoDao;
import com.dao.SettingsDao;
import com.dao.SpesaDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.Spesa;
import com.google.android.gms.ads.AdRequest;

public class ImpostazioniFragment extends Fragment implements OnClickListener {
	private Button salvaButton;
	private Button resetButton;
	private Checkable tutorialButton;
	private Button esportaButton;
	private Checkable exportText;
	private boolean tutorialOn = false;
	private EditText currencyET;
	private EditText symbolET;

	private final static int FILE_CHOOSER_ACTIVITY = 0;

	private View rootView;
	private Activity context;

	private final static int PASSWORD_LENGTH = 4;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rootView = inflater.inflate(R.layout.impostazioni_activity_layout,
				container, false);
		context = getActivity();
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}

		salvaButton = (Button) rootView.findViewById(R.id.salvaButton);
		resetButton = (Button) rootView.findViewById(R.id.resetButton);
		esportaButton = (Button) rootView.findViewById(R.id.esportaButton);
		exportText = (Checkable) rootView.findViewById(R.id.exportText);
		currencyET = (EditText) rootView.findViewById(R.id.currencyET);
		symbolET = (EditText) rootView.findViewById(R.id.symbolET);

		esportaButton.setOnClickListener(this);

		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();

		tutorialButton = (Checkable) rootView.findViewById(R.id.tutorialButton);

		tutorialOn = SettingsDao.isTutorialOn(db);

		salvaButton.setOnClickListener(this);
		resetButton.setOnClickListener(this);

		String[] currency = SettingsDao.getCurrency(db);
		currencyET.setText(currency[0]);
		symbolET.setText(currency[1]);

		db.close();

		tutorialButton.setChecked(tutorialOn);

		return rootView;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(salvaButton)) {
			salva();
		} else if (v.equals(resetButton)) {
			resetta();
		} else if (v.equals(esportaButton)) {
			esporta();
		}
	}

	private void esporta() {
		boolean esportaTXT = exportText.isChecked();
		if (esportaTXT) {
			Intent intent = new Intent(context, FileChooser.class);
			intent.putExtra(FileChooser.SELECT_FILES, false);
			startActivityForResult(intent, FILE_CHOOSER_ACTIVITY);
		}
	}

	private void resetta() {
		currencyET.setText("");
		symbolET.setText("");
		tutorialButton.setChecked(true);
	}

	private void salva() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		boolean ok = true;
		tutorialOn = tutorialButton.isChecked();
		SettingsDao.tutorialOn(db, tutorialOn);
		String currency = currencyET.getText().toString();
		String symbol = symbolET.getText().toString();
		if (currency != null && currency.length() > 0 && symbol != null
				&& symbol.length() == 1) {
			SettingsDao.setCurrency(db, currency, symbol);
		} else {
			String msg = getString(R.string.inserisci_valore_valido);
			Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
			ok = false;
		}
		if (ok) {
			Toast.makeText(context, context.getString(R.string.salvataggio_avvenuto_con_successo), Toast.LENGTH_SHORT).show();
			FragmentActivity activity = (FragmentActivity) context;
			FragmentManager fragmentManager = activity
					.getSupportFragmentManager();
			Fragment newFragment = new CalendarFragment();
			fragmentManager.beginTransaction()
					.replace(R.id.container, newFragment).commit();
			db.close();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_CHOOSER_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				String path = data.getStringExtra("file_path");
				esportaReport(path);
			} else {
				Toast.makeText(context,
						getString(R.string.attenzione_nessun_file_selezionato),
						Toast.LENGTH_SHORT).show();
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void esportaReport(String path) {
		boolean esportaTXT = exportText.isChecked();
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		// filechooser
		String filename = "report";
		File dir = new File(path);
		if (!dir.exists()) {
			dir.mkdirs();
		}
		Collection<Spesa> spese = SpesaDao.getAllSpese(db);
		Collection<Ricavo> ricavi = RicavoDao.getAllRicavi(db);
		db.close();
		boolean ok = true;
		int count = 0;
		String startDate = null, endDate = null;
		if (esportaTXT) {
			count++;
			File file = new File(dir, filename + TXTExport.EXTENSION);
			TXTExport export = new TXTExport(spese, ricavi, file, startDate,
					endDate);
			ok = ok && export.export();
		}
		if (count > 0) {
			if (ok) {
				Toast.makeText(context,
						getString(R.string.esportazione_successo),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(context,
						getString(R.string.errore_esportazione_dati),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

}
