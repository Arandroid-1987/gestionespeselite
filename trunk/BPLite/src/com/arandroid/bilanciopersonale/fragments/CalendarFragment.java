package com.arandroid.bilanciopersonale.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import utils.DateUtils;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.TextView;

import com.arandroid.bilanciopersonale.AddRicavoActivity;
import com.arandroid.bilanciopersonale.AddSpesaActivity;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.arandroid.bilanciopersonale.RiepilogoGiornalieroActivity;
import com.dao.RicavoDao;
import com.dao.RicavoProgrammatoDao;
import com.dao.SettingsDao;
import com.dao.SpesaDao;
import com.dao.SpesaProgrammataDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.TagRicavo;
import com.dto.TagSpesa;
import com.dto.VoceBilancio;
import com.google.android.gms.ads.AdRequest;
import com.tyczj.extendedcalendarview.Day;
import com.tyczj.extendedcalendarview.ExtendedCalendarView;

public class CalendarFragment extends Fragment implements OnClickListener {
	private ExtendedCalendarView calendarView;

	private View rootView;
	private Activity context;

	private View nuovaSpesa;
	private View nuovoRicavo;
	private AlertDialog dialog;
	private String data;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rootView = inflater.inflate(R.layout.calendar_layout, container, false);
		context = getActivity();
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}

		calendarView = (ExtendedCalendarView) rootView
				.findViewById(R.id.calendarView1);
		calendarView.setGesture(ExtendedCalendarView.LEFT_RIGHT_GESTURE);
		calendarView
				.setOnDayClickListener(new ExtendedCalendarView.OnDayClickListener() {

					@Override
					public void onDayClicked(AdapterView<?> adapter, View view,
							int position, long id, Day day) {
						ArrayList<VoceBilancio> events = day.getEvents();
						Intent intent = new Intent(context,
								RiepilogoGiornalieroActivity.class);
						intent.putExtra("lista", events);
						intent.putExtra("data",
								DateUtils.getDate(day.getYear(),
										day.getMonth() + 1, day.getDay()));
						context.startActivity(intent);
					}
				});
		checkVociProgrammateScadute();
		data = DateUtils.getDate(new Date());
		return rootView;
	}

	private void checkVociProgrammateScadute() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();

		Collection<VoceBilancio> vociScadute = new LinkedList<VoceBilancio>();
		Collection<SpesaProgrammata> speseProgrammate = SpesaProgrammataDao
				.getSpeseScadute(db);
		vociScadute.addAll(speseProgrammate);
		Collection<RicavoProgrammato> ricaviProgrammati = RicavoProgrammatoDao
				.getRicaviScaduti(db);
		vociScadute.addAll(ricaviProgrammati);
		for (VoceBilancio voce : vociScadute) {
			createAndShowVoceScadutaDialog(voce);
		}
		db.close();
	}

	private void createAndShowVoceScadutaDialog(final VoceBilancio item) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = context.getLayoutInflater();
		View v = inflater.inflate(R.layout.voce_scaduta_dialog, null);

		final TextView dataTW = (TextView) v.findViewById(R.id.data);
		final TextView importoTW = (TextView) v.findViewById(R.id.importo);
		final Button mostraTag = (Button) v.findViewById(R.id.mostraTag);
		final TextView firstTagTW = (TextView) v.findViewById(R.id.firstTag);

		String data = item.getData();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALY);
		Date tmp = null;
		try {
			tmp = sdf.parse(data);
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		sdf.applyPattern("dd/MM/yyyy");
		String newData = sdf.format(tmp);
		dataTW.setText(getString(R.string.data_uc_column_space) + newData);

		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		String[] currency = SettingsDao.getCurrency(db);
		db.close();
		String space_currency = " " + currency[1];

		importoTW.setText(getString(R.string.importo_uc_column_space)
				+ item.getImporto() + space_currency);

		final Collection<String> ris = new LinkedList<String>();
		if (item instanceof Spesa) {
			Spesa s = (Spesa) item;
			Collection<TagSpesa> tags = s.getTags();
			for (TagSpesa tag : tags) {
				ris.add(tag.getValore());
			}
		} else if (item instanceof Ricavo) {
			Ricavo r = (Ricavo) item;
			Collection<TagRicavo> tags = r.getTags();
			for (TagRicavo tag : tags) {
				ris.add(tag.getValore());
			}
		}
		String etc = ris.size() == 1 ? "" : "...";
		Iterator<String> it = ris.iterator();
		String firstTag = "";
		if (it.hasNext()) {
			firstTag = it.next();
		}
		firstTagTW.setText(getString(R.string.tag_uc_column_space) + firstTag
				+ etc);
		mostraTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PopupMenu menu = createPopupMenu(ris, mostraTag);
				menu.show();
			}
		});

		final Button confermaButton = (Button) v
				.findViewById(R.id.confermaButton);
		confermaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				confermaVoceProgrammata(item);
				Dialog dialog = (Dialog) v.getTag();
				dialog.dismiss();
				calendarView.refreshCalendar();
			}
		});

		final Button posticipaButton = (Button) v
				.findViewById(R.id.posticipaButton);

		posticipaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				posticipaVoceProgrammata(item);
				Dialog dialog = (Dialog) v.getTag();
				dialog.dismiss();
				calendarView.refreshCalendar();
			}
		});

		final Button cancellaButton = (Button) v
				.findViewById(R.id.cancellaButton);
		cancellaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				cancellaVoceProgrammata(item);
				Dialog dialog = (Dialog) v.getTag();
				dialog.dismiss();
				calendarView.refreshCalendar();
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = context.getLayoutInflater();
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getString(R.string.voce_programmata_scaduta));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getString(R.string.voce_programmata_scaduta));
		}

		builder.setView(v);
		Dialog d = builder.create();
		d.show();
		posticipaButton.setTag(d);
		cancellaButton.setTag(d);
		confermaButton.setTag(d);
	}

	protected void cancellaVoceProgrammata(VoceBilancio item) {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		if (item instanceof Spesa) {
			SpesaProgrammataDao.deleteSpesa(db, item.getId());
		} else {
			RicavoProgrammatoDao.deleteRicavo(db, item.getId());
		}
		db.close();
	}

	protected void posticipaVoceProgrammata(VoceBilancio item) {
		createAndShowDatePickerDialog(item);
	}

	private void createAndShowDatePickerDialog(final VoceBilancio item) {
		final Calendar c = Calendar.getInstance(Locale.ITALIAN);
		DatePickerDialog dialog = new DatePickerDialog(context,
				new OnDateSetListener() {

					@Override
					public void onDateSet(DatePicker view, int year,
							int monthOfYear, int dayOfMonth) {
						c.set(Calendar.YEAR, year);
						c.set(Calendar.MONTH, monthOfYear);
						c.set(Calendar.DAY_OF_MONTH, dayOfMonth);
						Date date = c.getTime();
						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd",
								getResources().getConfiguration().locale);
						String newDate = sdf.format(date);
						DatabaseHandler handler = DatabaseHandler
								.getInstance(context);
						SQLiteDatabase db = handler.getWritableDatabase();
						if (item instanceof Spesa) {
							item.setData(newDate);
							SpesaProgrammata s = (SpesaProgrammata) item;
							SpesaProgrammataDao.updateSpesa(db, s);
						} else {
							item.setData(newDate);
							RicavoProgrammato r = (RicavoProgrammato) item;
							RicavoProgrammatoDao.updateRicavo(db, r);
						}
						db.close();
					}
				}, c.get(Calendar.YEAR), c.get(Calendar.MONTH),
				c.get(Calendar.DAY_OF_MONTH));
		dialog.show();
		dialog.setCancelable(false);
	}

	protected void confermaVoceProgrammata(VoceBilancio item) {
		try {
			DatabaseHandler handler = DatabaseHandler.getInstance(context);
			SQLiteDatabase db = handler.getWritableDatabase();
			if (item instanceof Spesa) {
				SpesaProgrammata item1 = (SpesaProgrammata) item;
				Spesa s = new Spesa();
				s.setData(item.getData());
				s.setImporto(item.getImporto());
				Collection<TagSpesa> tags = ((Spesa) item).getTags();
				Set<TagSpesa> newTags = new HashSet<TagSpesa>();
				for (TagSpesa tagSpesa : tags) {
					TagSpesa newTagSpesa = new TagSpesa();
					newTagSpesa.setValore(tagSpesa.getValore());
					newTagSpesa.setSpesa(s);
					newTags.add(newTagSpesa);
				}
				s.setTags(newTags);
				s.setId(System.currentTimeMillis());
				SpesaProgrammataDao.deleteSpesa(db, item.getId());
				int ripetifra = item1.getRipetiFra();
				if (ripetifra > 0) {
					SpesaProgrammata sp = new SpesaProgrammata();
					sp.setId(s.getId());
					sp.setImporto(s.getImporto());
					String[] arrayPeriodoRipeti = getResources()
							.getStringArray(R.array.periodi_ripeti);
					String periodoRipeti = arrayPeriodoRipeti[DateUtils
							.getRipetiFra(ripetifra, arrayPeriodoRipeti)];
					String newDate = DateUtils.newDate(item.getData(),
							ripetifra, periodoRipeti, arrayPeriodoRipeti);
					sp.setData(newDate);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
							getResources().getConfiguration().locale);
					sp.setRipetiFra(DateUtils.getDaysFromString(periodoRipeti,
							arrayPeriodoRipeti, sdf.parse(newDate)));
					newTags = new HashSet<TagSpesa>();
					for (TagSpesa tagSpesa : tags) {
						TagSpesa newTagSpesa = new TagSpesa();
						newTagSpesa.setValore(tagSpesa.getValore());
						newTagSpesa.setSpesa(sp);
						newTags.add(newTagSpesa);
					}
					sp.setTags(newTags);
					SpesaProgrammataDao.insertSpesa(db, sp);
				}
				SpesaDao.insertSpesa(db, s);
			} else {
				RicavoProgrammato item1 = (RicavoProgrammato) item;
				Ricavo s = new Ricavo();
				s.setData(item.getData());
				s.setImporto(item.getImporto());
				Collection<TagRicavo> tags = ((Ricavo) item).getTags();
				Set<TagRicavo> newTags = new HashSet<TagRicavo>();
				for (TagRicavo tagSpesa : tags) {
					TagRicavo newTagSpesa = new TagRicavo();
					newTagSpesa.setValore(tagSpesa.getValore());
					newTagSpesa.setRicavo(s);
					newTags.add(newTagSpesa);
				}
				s.setTags(newTags);
				s.setId(System.currentTimeMillis());
				RicavoProgrammatoDao.deleteRicavo(db, item.getId());
				int ripetifra = item1.getRipetiFra();
				if (ripetifra > 0) {
					RicavoProgrammato sp = new RicavoProgrammato();
					sp.setId(s.getId());
					sp.setImporto(s.getImporto());

					String[] arrayPeriodoRipeti = getResources()
							.getStringArray(R.array.periodi_ripeti);
					String periodoRipeti = arrayPeriodoRipeti[DateUtils
							.getRipetiFra(ripetifra, arrayPeriodoRipeti)];
					String newDate = DateUtils.newDate(item.getData(),
							ripetifra, periodoRipeti, arrayPeriodoRipeti);
					sp.setData(newDate);
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
							getResources().getConfiguration().locale);
					sp.setRipetiFra(DateUtils.getDaysFromString(periodoRipeti,
							arrayPeriodoRipeti, sdf.parse(newDate)));
					newTags = new HashSet<TagRicavo>();
					for (TagRicavo tagSpesa : tags) {
						TagRicavo newTagSpesa = new TagRicavo();
						newTagSpesa.setValore(tagSpesa.getValore());
						newTagSpesa.setRicavo(sp);
						newTags.add(newTagSpesa);
					}
					sp.setTags(newTags);
					RicavoProgrammatoDao.insertRicavo(db, sp);
				}
				RicavoDao.insertRicavo(db, s);
			}
			db.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private PopupMenu createPopupMenu(Collection<String> tags, View v) {
		PopupMenu menu = new PopupMenu(v);
		for (String tag : tags) {
			ActionItem item = new ActionItem();
			item.setTitle(tag);
			item.setIcon(getResources().getDrawable(R.drawable.tag));
			menu.addActionItem(item);
		}
		return menu;
	}

	@Override
	public void onResume() {
		super.onResume();
		calendarView.refreshCalendar();
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
			Intent intent;
			intent = new Intent(context, AddSpesaActivity.class);
			intent.putExtra("data", data);
			context.startActivity(intent);
			dialog.dismiss();
		} else if (arg0.equals(nuovoRicavo)) {
			Intent intent;
			intent = new Intent(context, AddRicavoActivity.class);
			intent.putExtra("data", data);
			context.startActivity(intent);
			dialog.dismiss();
		}
	}

}
