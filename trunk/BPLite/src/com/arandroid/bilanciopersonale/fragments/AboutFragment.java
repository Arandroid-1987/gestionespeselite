package com.arandroid.bilanciopersonale.fragments;

import java.util.ArrayList;

import utils.Email;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.about.utils.EntryAdapter;
import com.about.utils.EntryItem;
import com.about.utils.Item;
import com.about.utils.SectionItem;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.google.android.gms.ads.AdRequest;
import com.ui.gestionespese.BuyProDialogGenerator;

public class AboutFragment extends ListFragment {
	/** Called when the activity is first created. */

	ArrayList<Item> items = new ArrayList<Item>();
	private static final int SITO_WEB = 3;
	private static final int FACEBOOK = 4;
	private static final int FAQ = 6;
	private static final int CHANGE_LOG = 7;
	private static final int MARKET_AUTORE = 9;
	private static final int SEGNALA_BUG = 10;
	private static final int COMPRA_PRO = 11;
	
	private Dialog bugDialog;
	
	private View rootView;
	private Activity context;
	
	

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			super.onCreate(savedInstanceState);
			
			rootView = inflater.inflate(R.layout.about_layout,
					container, false);
			context = getActivity();
			
			if(context instanceof MainLayoutActivity){
				MainLayoutActivity act = (MainLayoutActivity) context;
				act.displayInterstitial();
				AdRequest adRequest = new AdRequest.Builder().build();
			    act.interstitial.loadAd(adRequest);
			}

			PackageManager manager = context.getPackageManager();
			PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
			items.add(new SectionItem(getString(R.string.informazioni)));
			items.add(new EntryItem(getString(context.getApplicationInfo().labelRes),
					getString(R.string.versione_space_column) + info.versionName));
			items.add(new EntryItem(getString(R.string.autore), "ARAndroid"));
			items.add(new EntryItem(getString(R.string.sito_web_autore),
					getString(R.string.visualizzo_sito_autore)));
			items.add(new EntryItem("Facebook", getString(R.string.visualizza_pagina_facebook)));

			items.add(new SectionItem(getString(R.string.informazioni_applicazione)));
			items.add(new EntryItem("FAQ", getString(R.string.visualizza_faq)));
			items.add(new EntryItem("ChangeLog", getString(R.string.visualizza_cambiamenti)));

			items.add(new SectionItem(getString(R.string.varie)));
			items.add(new EntryItem(getString(R.string.market_autore),
					getString(R.string.visualizza_altre_app_arandroid)));
			items.add(new EntryItem(getString(R.string.segnala_un_bug),
					getString(R.string.grazie_tuo_supporto)));
			items.add(new EntryItem(getString(R.string.acquista_pro),
					getString(R.string.grazie_tuo_supporto)));

			EntryAdapter adapter = new EntryAdapter(context, items);

			setListAdapter(adapter);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return rootView;
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		if (!items.get(position).isSection()) {

			switch (position) {
			case SITO_WEB:
				Intent myIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://arandroid.altervista.org"));
				startActivity(myIntent);
				break;
			case FACEBOOK:
				myIntent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("https://www.facebook.com/pages/Arandroid/140377209441207?fref=ts"));
				startActivity(myIntent);
				break;
			case FAQ:
				String title = "FAQ";
				StringBuilder text = new StringBuilder();
				text.append("1) Come inserisco una nuova spesa / un nuovo ricavo?\n\n");
				text.append("Nella schermata principale, clicca sul pulsante relativo. Ti apparirà una semplice schermata di inserimento.\n");
				text.append("Attraverso questa, hai la possibilità di impostare l'importo, i tag, la data ed un eventuale ripetizione/programmazione.\n");
				text.append("N.B. Inserisci almeno importo e un tag per ogni spesa/ricavo.\n\n");
				text.append("2) Cosa sono i tag?\n\n");
				text.append("I tag sono uno strumento semplice ed efficace per contrassegnare in maniera esaustiva tutto ciò che registri come spese o ricavi.\n");
				text.append("Ad esempio, se sei andato a mangiare una pizza con i tuoi amici,"
						+ " potresti inserire i tag 'pizza', 'amici' e 'alimentari' in modo da poter ritrovare la tua spesa cercando per uno qualsiasi di questi tag.\n\n");
				text.append("3) Come viene calcolato il bilancio mensile?\n\n");
				text.append("Il bilancio mensile viene calcolato a partire dal primo giorno del mese. In maniera equivalente, per gli altri periodi.\n\n");
				text.append("4) Come disabilito l'apertura automatica delle voci nelle liste?\n\n");
				text.append("Vai nella schermata impostazioni e sposta lo switch 'Tutorial' nella posizione OFF. RICORDATI DI SALVARE!!!\n\n");
				text.append("5) Dove sono finite le spese / i ricavi dell'ultimo mese?\n\n");
				text.append("Quando hai spese o ricavi relativi a più mesi, per evitare di avere troppe voci in lista, "
						+ "ti verranno mostrati solo i ricavi e le spese del mese corrente.\n");
				text.append("Cliccando sul pulsante 'Carica Altri', ad ogni click, ti appariranno le voci relative al mese precedente.\n\n");
				createDialog(title, text.toString());
				break;
			case CHANGE_LOG:
				title = "ChangeLog";
				text = new StringBuilder();
				text.append("\n\nVersione 2.0: Completo restyling grafico.");
				text.append("\n\nVersione 1.1.0: Modifiche alla UI.\nCorretto bug database.\nNuova gestione backup/restore.\nAggiunta personalizzazione valuta.\nAggiunta descrizione per spese e ricavi.\nNuova Icona.");
				text.append("\n\nVersione 1.0.8: Completo restyling grafico. Aggiunta lingua inglese.");
				text.append("\n\nVersione 1.0.7: Aggiunta funzionalità spostamento su SD Card. Correzione bug minori.");
				text.append("\n\nVersione 1.0.6: Correzione bug backup automatico.");
				text.append("\n\nVersione 1.0.5: Aggiunta funzionalità backup automatico.");
				text.append("\n\nVersione 1.0.4: Corretti errori di visualizzazione in schermi grandi.");
				text.append("\n\nVersione 1.0.3: Corretto un bug minore.");
				createDialog(title, text.toString());
				break;
			case MARKET_AUTORE:
				myIntent = new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("https://play.google.com/store/apps/developer?id=ARAndroid"));
				startActivity(myIntent);
				break;
			case SEGNALA_BUG:
				createBugDialog();
				break;
			case COMPRA_PRO:
				createBuyProDialog();
				break;
			default:
				break;
			}

		}

		super.onListItemClick(l, v, position, id);
	}

	private void createBuyProDialog() {
		BuyProDialogGenerator.create(context).show();
	}

	private Dialog createDialog(String titleTxt, String text) {
		Dialog dialog;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = context.getLayoutInflater();
		View v = inflater.inflate(R.layout.about_dialog_template, null);

		TextView testo = (TextView) v.findViewById(R.id.testo);
		testo.setText(text);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title,
					(ViewGroup) context.findViewById(R.id.titleLayout));
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(titleTxt);
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(titleTxt);
		}

		builder.setView(v);
		dialog = builder.create();
		dialog.show();
		return dialog;
	}

	private Dialog createBugDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.segnala_bug_dialog, null);
		
		final EditText descrizioneET = (EditText) v.findViewById(R.id.descrizioneET);
		final Spinner schermateSpinner = (Spinner) v.findViewById(R.id.schermateSpinner);
		ArrayAdapter<String> schermateSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item);
		String[] names = getResources()
				.getStringArray(R.array.schermate);

		for (String name : names) {
			schermateSpinnerAdapter.add(name);
		}

		schermateSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		schermateSpinner.setAdapter(schermateSpinnerAdapter);

		Button segnalaButton = (Button) v.findViewById(R.id.segnalaButton);
		segnalaButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				String schermata = (String) schermateSpinner.getSelectedItem();
				String descrizione = descrizioneET.getText().toString();
				Email.sendEmailBug(schermata, descrizione);
				Toast.makeText(context, getString(R.string.grazie_tua_segnalazione), Toast.LENGTH_LONG).show();
				bugDialog.dismiss();
			}
		});

		Button annullaButton = (Button) v.findViewById(R.id.annullaButton);
		annullaButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				bugDialog.dismiss();
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title,
					(ViewGroup) context.findViewById(R.id.titleLayout));
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getString(R.string.segnalaci_un_bug));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getString(R.string.segnalaci_un_bug));
		}

		builder.setView(v);
		bugDialog = builder.create();
		bugDialog.show();
		return bugDialog;
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.filtri_bar, menu);
	}

}