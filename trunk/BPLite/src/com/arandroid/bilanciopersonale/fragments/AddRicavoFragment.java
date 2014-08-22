package com.arandroid.bilanciopersonale.fragments;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import net.londatiga.android.ActionItem;
import net.londatiga.android.PopupMenu;
import utils.CustomToast;
import utils.DateUtils;
import utils.NumberUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.AddRicavoActivity;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.dao.RicavoDao;
import com.dao.RicavoProgrammatoDao;
import com.dao.SpesaProgrammataDao;
import com.dao.TagRicavoDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.TagRicavo;
import com.dto.TagSpesa;
import com.dto.VoceBilancio;
import com.google.android.gms.ads.AdRequest;

public class AddRicavoFragment extends Fragment implements OnClickListener,
		OnTouchListener {

	private EditText editTextImporto;
	private View aggiungiButton;
	private View buttonReset;
	private CustomToast customToast;
	private Toast toast;
	private Spinner dataSpinner;
	private EditText editTextDescrizione;

	private View aiutoDescrizione;
	private View aiutoTag;

	private Spinner ripetiSpinner;

	private int pYear;
	private int pMonth;
	private int pDay;
	private ArrayAdapter<String> spinnerAdapter;

	private Dialog customDialog;
	private int ripetiFra;

	private boolean copy = true;
	private VoceBilancio voce;
	private SpesaProgrammata spesaProgrammata;
	private RicavoProgrammato ricavoProgrammato;

	private AutoCompleteTextView editTextTag;
	private Button addTag;
	private LinearLayout tagContainer;
	private HorizontalScrollView tagContainerScrollView;
	private LinearLayout mostUsedTagContainer;
	private TextView submitButtonText;

	private Typeface myTypeface;

	private View rootView;
	private Activity context;

	private String data;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		rootView = inflater.inflate(R.layout.aggiungi_voce_bilancio_layout,
				container, false);
		context = getActivity();
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}

		this.myTypeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/ht.ttf");

		voce = (VoceBilancio) context.getIntent().getSerializableExtra(
				"vocebilancio");
		data = context.getIntent().getStringExtra("data");

		aggiungiButton = rootView.findViewById(R.id.imageViewButtonAddSR);
		editTextTag = (AutoCompleteTextView) rootView
				.findViewById(R.id.editTextTag);
		editTextImporto = (EditText) rootView
				.findViewById(R.id.editTextImporto);
		editTextDescrizione = (EditText) rootView
				.findViewById(R.id.editTextDescrizione);

		aiutoDescrizione = rootView.findViewById(R.id.aiutoDescrizione);
		aiutoTag = rootView.findViewById(R.id.aiutoTag);

		List<String> causali = new ArrayList<String>();
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		Cursor cursor;
		cursor = RicavoDao.getTags(db);
		while (cursor.moveToNext()) {
			String causale = cursor.getString(0);
			causali.add(causale);
		}
		cursor.close();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.select_dialog_item, causali);
		editTextTag.setThreshold(1);
		editTextTag.setAdapter(adapter);

		tagContainer = (LinearLayout) rootView.findViewById(R.id.tagContainer);
		tagContainerScrollView = (HorizontalScrollView) rootView
				.findViewById(R.id.tagContainerScrollView);

		mostUsedTagContainer = (LinearLayout) rootView
				.findViewById(R.id.mostUsedTagContainer);

		populateTags();

		addTag = (Button) rootView.findViewById(R.id.addTag);
		addTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editTextTag.getText().toString().trim().length() > 0) {
					addTag(editTextTag.getText().toString().trim());
					editTextTag.setText("");
				}
			}

		});

		Resources res = getResources();
		buttonReset = rootView.findViewById(R.id.imageViewReset);
		aggiungiButton.setOnClickListener(this);
		buttonReset.setOnClickListener(this);
		customToast = new CustomToast(context,
				res.getString(R.string.inserimento_avvenuto_con_successo),
				res.getString(R.string.errore_nell_inserimento_dei_dati));

		dataSpinner = (Spinner) rootView.findViewById(R.id.Spinner1);
		spinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, new ArrayList<String>());
		spinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		dataSpinner.setAdapter(spinnerAdapter);
		dataSpinner.setOnTouchListener(this);

		copy = context.getIntent().getBooleanExtra("copy", true);
		voce = (VoceBilancio) context.getIntent().getSerializableExtra(
				"vocebilancio");

		submitButtonText = (TextView) rootView
				.findViewById(R.id.submitButtonText);
		if (copy) {
			submitButtonText.setText(res.getString(R.string.aggiungi));
		} else {
			submitButtonText.setText(res.getString(R.string.modifica));
		}

		ripetiSpinner = (Spinner) rootView.findViewById(R.id.ripetiSpinner);
		ArrayAdapter<String> ripetiSpinnerAdapter = new ArrayAdapter<String>(
				context, android.R.layout.simple_spinner_item);

		String[] periodiRipeti = getResources().getStringArray(
				R.array.periodi_ripeti);

		for (String name : periodiRipeti) {
			ripetiSpinnerAdapter.add(name);
		}

		ripetiSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		ripetiSpinner.setAdapter(ripetiSpinnerAdapter);
		ripetiSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						if (position == 9) {
							createAndShowDialogCustom();
						}
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {

					}
				});

		Date d = new Date();
		if (voce != null) {
			if (voce instanceof Spesa) {
				Spesa s = (Spesa) voce;
				Collection<TagSpesa> tags = s.getTags();
				for (TagSpesa tag : tags) {
					addTag(tag.getValore());
				}
				spesaProgrammata = SpesaProgrammataDao.getSpesa(db, s.getId());
				int position = 0;
				if (spesaProgrammata != null) {
					int ripetiFra = spesaProgrammata.getRipetiFra();
					position = DateUtils.getRipetiFra(ripetiFra, periodiRipeti);
				}
				ripetiSpinner.setSelection(position);
			} else if (voce instanceof Ricavo) {
				Ricavo r = (Ricavo) voce;
				Collection<TagRicavo> tags = r.getTags();
				for (TagRicavo tag : tags) {
					addTag(tag.getValore());
				}
				ricavoProgrammato = RicavoProgrammatoDao.getRicavo(db,
						r.getId());
				int position = 0;
				if (ricavoProgrammato != null) {
					int ripetiFra = ricavoProgrammato.getRipetiFra();
					position = DateUtils.getRipetiFra(ripetiFra, periodiRipeti);
				}
				ripetiSpinner.setSelection(position);
			}
			editTextImporto.setText(NumberUtils.getString(voce.getImporto())
					.replace(',', '.'));
			editTextDescrizione.setText(voce.getDescrizione());
			String date = voce.getData();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.ITALY);
			try {
				d = sdf.parse(date);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		if (data != null) {
			try {
				d = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
						.parse(data);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		final Calendar c = Calendar.getInstance();
		c.setTime(d);
		pYear = c.get(Calendar.YEAR);
		pMonth = c.get(Calendar.MONTH);
		pDay = c.get(Calendar.DAY_OF_MONTH);
		spinnerAdapter.add(pDay + "/" + (pMonth + 1) + "/" + pYear);

		aiutoDescrizione.setOnClickListener(this);
		aiutoTag.setOnClickListener(this);

		db.close();

		return rootView;
	}

	private void createAndShowDialogCustom() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.custom_dialog_layout, null);

		final EditText number = (EditText) v.findViewById(R.id.number);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String testo = number.getText().toString();
				int numero = Integer.parseInt(testo);
				if (numero > 0) {
					ripetiFra = numero;
				} else {
					Toast.makeText(
							context,
							getResources()
									.getString(
											R.string.inserire_un_numero_diverso_da_zero),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title,
					(ViewGroup) rootView.findViewById(R.id.titleLayout));
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getResources().getString(
					R.string.periodo_personalizzato));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getResources().getString(
					R.string.periodo_personalizzato));
		}

		builder.setView(v);
		customDialog = builder.create();
		customDialog.show();
	}

	private void populateTags() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		int limit = 10;
		Collection<String> mostUsedTags;
		mostUsedTags = TagRicavoDao.getMostUsedTags(db, limit);
		for (String string : mostUsedTags) {
			final Button b = new Button(context);
			b.setText(string);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin = 5;
			b.setLayoutParams(params);
			b.setBackgroundResource(R.drawable.my_button);
			b.setTextColor(getResources().getColor(R.color.white));
			b.setPadding(5, 5, 5, 5);
			b.setTypeface(myTypeface);
			mostUsedTagContainer.addView(b);
			b.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					addTag(b.getText().toString());
				}
			});
		}
		db.close();
	}

	private void addTag(String valore) {
		int childCount = tagContainer.getChildCount();
		int count = 0;
		while (count < childCount) {
			View tagLayout = tagContainer.getChildAt(count);
			TextView tagText = (TextView) tagLayout.findViewById(R.id.tagText);
			String text = tagText.getText().toString();
			if (text.equalsIgnoreCase(valore)) {
				return;
			} else {
				count++;
			}
		}
		String capitalLetter = valore.substring(0, 1).toUpperCase(
				Locale.ITALIAN);
		valore = capitalLetter
				+ valore.substring(1).toLowerCase(Locale.ITALIAN);
		LayoutInflater inflater = context.getLayoutInflater();
		LinearLayout tmp = (LinearLayout) inflater.inflate(R.layout.tag, null,
				false);

		final LinearLayout tagLayout = (LinearLayout) tmp
				.findViewById(R.id.tagLayout);

		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT);
		params.gravity = Gravity.CENTER_VERTICAL;
		params.leftMargin = 5;

		tagLayout.setLayoutParams(params);

		TextView tagText = (TextView) tagLayout.findViewById(R.id.tagText);

		tagText.setTypeface(myTypeface);

		ImageButton deleteTag = (ImageButton) tagLayout
				.findViewById(R.id.deleteTag);
		deleteTag.getBackground().setAlpha(100);
		tagText.setText(valore);
		deleteTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Animation animation = AnimationUtils.loadAnimation(context,
						R.anim.zoom_out);
				tagLayout.startAnimation(animation);
				animation.setAnimationListener(new AnimationListener() {

					@Override
					public void onAnimationStart(Animation animation) {
					}

					@Override
					public void onAnimationRepeat(Animation animation) {
					}

					@Override
					public void onAnimationEnd(Animation animation) {
						new Handler().post(new Runnable() {
							public void run() {
								tagContainer.removeView(tagLayout);
							}
						});

					}
				});
			}
		});
		if (tagLayout.getParent() != null) {
			ViewParent parent = tagLayout.getParent();
			if (parent instanceof ViewGroup) {
				ViewGroup w = (ViewGroup) parent;
				w.removeView(tagLayout);
			}
		}
		tagLayout.setVisibility(View.INVISIBLE);
		tagContainer.addView(tagLayout);

		tagContainerScrollView.post(new Runnable() {

			@Override
			public void run() {
				int scrollTo = 0;
				final int count = ((LinearLayout) tagContainerScrollView
						.getChildAt(0)).getChildCount();
				for (int i = 0; i < count; i++) {
					final View child = ((LinearLayout) tagContainerScrollView
							.getChildAt(0)).getChildAt(i);
					if (child != tagLayout) {
						scrollTo += child.getWidth();
					} else {
						break;
					}
				}
				tagLayout.setVisibility(View.VISIBLE);
				tagContainerScrollView.scrollTo(scrollTo, 0);
				Animation animation = AnimationUtils.loadAnimation(context,
						R.anim.zoom_in);
				tagLayout.startAnimation(animation);
			}
		});

	}

	@Override
	public void onClick(View v) {
		if (v.equals(aggiungiButton)) {
			DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
			// apro il DB sia in lettura che in scrittura
			SQLiteDatabase db = dbHandler.getWritableDatabase();
			try {
				if (!editTextImporto.getText().toString().equals("")
						&& tagContainer.getChildCount() > 0) {

					Calendar c = Calendar.getInstance();
					c.set(Calendar.YEAR, pYear);
					c.set(Calendar.MONTH, pMonth);
					c.set(Calendar.DAY_OF_MONTH, pDay);

					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
							Locale.ITALY);
					Date dataAttuale = c.getTime();
					String dateString = sdf.format(dataAttuale);

					String descrizione = editTextDescrizione.getText()
							.toString();

					String importo = editTextImporto.getText().toString();
					double doubleImp = Double.parseDouble(importo);

					String periodoRipeti = (String) ripetiSpinner
							.getSelectedItem();
					String[] arrayPeriodoRipeti = getResources()
							.getStringArray(R.array.periodi_ripeti);
					if (periodoRipeti.equals(getResources().getString(
							R.string.personalizzato))) {

					} else {
						ripetiFra = DateUtils.getDaysFromString(periodoRipeti,
								arrayPeriodoRipeti, dataAttuale);
					}

					if (dataAttuale.after(new Date())) {
						if (copy) {
							insertRicavoProgrammato(ripetiFra, doubleImp,
									dateString, descrizione, db,
									System.currentTimeMillis());
						} else {
							if (voce instanceof RicavoProgrammato) {
								updateRicavoProgrammato(ripetiFra, doubleImp,
										dateString, descrizione, db,
										System.currentTimeMillis());
							} else {
								insertRicavoProgrammato(ripetiFra, doubleImp,
										dateString, descrizione, db,
										System.currentTimeMillis());
							}
						}
					} else {
						if (copy) {
							insertRicavo(c, ripetiFra, doubleImp, dateString,
									descrizione, db,
									System.currentTimeMillis(), sdf,
									periodoRipeti, arrayPeriodoRipeti);
						} else {
							updateRicavo(c, ripetiFra, doubleImp, dateString,
									descrizione, db,
									System.currentTimeMillis(), sdf);
						}
					}
					toast = customToast.getCorrectToast();
					toast.show();
				} else
					throw new Exception();

			} catch (Exception e) {
				e.printStackTrace();
				toast = customToast.getErrorToast();
				toast.show();
			} finally {
				db.close();
				if (context instanceof MainLayoutActivity) {
					FragmentActivity activity = (FragmentActivity) context;
					FragmentManager fragmentManager = activity
							.getSupportFragmentManager();
					Fragment newFragment = new CalendarFragment();
					fragmentManager.beginTransaction()
							.replace(R.id.container, newFragment).commit();
					InputMethodManager inputMethodManager = (InputMethodManager) context
							.getSystemService(Activity.INPUT_METHOD_SERVICE);
					try {
						inputMethodManager.hideSoftInputFromWindow(activity
								.getCurrentFocus().getWindowToken(), 0);
					} catch (Exception e) {

					}
				} else if (context instanceof AddRicavoActivity) {
					context.setResult(Activity.RESULT_OK);
					context.finish();
				}
			}

		} else if (v.equals(buttonReset)) {
			editTextImporto.setText("");
			editTextTag.setText("");
			tagContainer.removeAllViews();
			Calendar c = Calendar.getInstance();
			c.setTime(new Date());
			pYear = c.get(Calendar.YEAR);
			pMonth = c.get(Calendar.MONTH);
			pDay = c.get(Calendar.DAY_OF_MONTH);
			spinnerAdapter.clear();
			spinnerAdapter.add(pDay + "/" + (pMonth + 1) + "/" + pYear);
		} else if (v.equals(aiutoDescrizione)) {
			PopupMenu menu = createPopupMenu(aiutoDescrizione);
			menu.show();
		} else if (v.equals(aiutoTag)) {
			PopupMenu menu = createPopupMenu(aiutoTag);
			menu.show();
		}
	}

	private PopupMenu createPopupMenu(View view) {
		String msg = null;
		if (view.equals(aiutoTag)) {
			msg = getString(R.string.aiuto_tag);
		} else if (view.equals(aiutoDescrizione)) {
			msg = getString(R.string.aiuto_descrizione);
		}
		PopupMenu menu = new PopupMenu(view);
		ActionItem item = new ActionItem();
		item.setTitle(msg);
		item.setIcon(getResources().getDrawable(R.drawable.aiuto));
		menu.addActionItem(item);
		return menu;
	}

	private void updateRicavoProgrammato(int ripetifra, double doubleImp,
			String dateString, String descrizione, SQLiteDatabase db,
			long currentTimeMillis) {
		RicavoProgrammato r = (RicavoProgrammato) voce;
		Set<TagRicavo> tags = new HashSet<TagRicavo>();

		int numTag = tagContainer.getChildCount();
		for (int i = 0; i < numTag; i++) {
			View tagLayout = tagContainer.getChildAt(i);
			TextView tagText = (TextView) tagLayout.findViewById(R.id.tagText);

			TagRicavo tag = new TagRicavo();
			tag.setRicavo(r);
			tag.setValore(tagText.getText().toString());
			tags.add(tag);
		}

		r.setTags(tags);
		r.setData(dateString);
		r.setDescrizione(descrizione);
		r.setImporto(doubleImp);
		r.setRipetiFra(ripetifra);
		RicavoProgrammatoDao.updateRicavo(db, r);
	}

	private void updateRicavo(Calendar c, int ripetifra, double doubleImp,
			String dateString, String descrizione, SQLiteDatabase db, long id,
			SimpleDateFormat sdf) {
		Ricavo r = (Ricavo) voce;
		Set<TagRicavo> tags = new HashSet<TagRicavo>();

		int numTag = tagContainer.getChildCount();
		for (int i = 0; i < numTag; i++) {
			View tagLayout = tagContainer.getChildAt(i);
			TextView tagText = (TextView) tagLayout.findViewById(R.id.tagText);

			TagRicavo tag = new TagRicavo();
			tag.setRicavo(r);
			tag.setValore(tagText.getText().toString());
			tags.add(tag);
		}

		r.setTags(tags);
		r.setData(dateString);
		r.setDescrizione(descrizione);
		r.setImporto(doubleImp);
		if (r instanceof RicavoProgrammato) {
			RicavoProgrammato r1 = (RicavoProgrammato) r;
			RicavoProgrammatoDao.updateRicavo(db, r1);
		} else {
			RicavoDao.updateRicavo(db, r);
		}

		if (ripetifra > 0) {
			c.add(Calendar.DAY_OF_YEAR, ripetifra);
			Date prossimaData = c.getTime();
			if (ricavoProgrammato != null) {
				ricavoProgrammato.setRipetiFra(ripetifra);
				ricavoProgrammato.setData(sdf.format(prossimaData));
				RicavoProgrammatoDao.updateRicavo(db, ricavoProgrammato);
			} else {
				ricavoProgrammato = new RicavoProgrammato(
						sdf.format(prossimaData), descrizione, doubleImp);
				ricavoProgrammato.setId(r.getId());

				Set<TagRicavo> tags1 = new HashSet<TagRicavo>();

				for (int i = 0; i < numTag; i++) {
					View tagLayout = tagContainer.getChildAt(i);
					TextView tagText = (TextView) tagLayout
							.findViewById(R.id.tagText);

					TagRicavo tag = new TagRicavo();
					tag.setRicavo(ricavoProgrammato);
					tag.setValore(tagText.getText().toString());
					tags1.add(tag);
				}
				ricavoProgrammato.setTags(tags1);
				ricavoProgrammato.setRipetiFra(ripetifra);
				RicavoProgrammatoDao.insertRicavo(db, ricavoProgrammato);
			}
		}
	}

	private void insertRicavo(Calendar c, int ripetifra, double doubleImp,
			String dateString, String descrizione, SQLiteDatabase db, long id,
			SimpleDateFormat sdf, String periodoRipeti,
			String[] arrayPeriodoRipeti) {
		Ricavo r = new Ricavo(dateString, descrizione, doubleImp);

		Set<TagRicavo> tags = new HashSet<TagRicavo>();

		int numTag = tagContainer.getChildCount();
		for (int i = 0; i < numTag; i++) {
			View tagLayout = tagContainer.getChildAt(i);
			TextView tagText = (TextView) tagLayout.findViewById(R.id.tagText);

			TagRicavo tag = new TagRicavo();
			tag.setRicavo(r);
			tag.setValore(tagText.getText().toString());
			tags.add(tag);
		}

		r.setTags(tags);
		r.setId(id);
		RicavoDao.insertRicavo(db, r);
		if (ripetifra > 0) {
			c.add(Calendar.DAY_OF_YEAR, ripetifra);
			Date prossimaData = c.getTime();
			ripetifra = DateUtils.getDaysFromString(periodoRipeti,
					arrayPeriodoRipeti, prossimaData);
			insertRicavoProgrammato(ripetifra, doubleImp,
					sdf.format(prossimaData), descrizione, db, id);
		}
	}

	private void insertRicavoProgrammato(int ripetifra, double doubleImp,
			String dateString, String descrizione, SQLiteDatabase db, long id) {
		RicavoProgrammato r = new RicavoProgrammato();
		r.setId(System.currentTimeMillis());
		r.setDescrizione(descrizione);
		r.setRipetiFra(ripetifra);
		r.setImporto(doubleImp);
		Set<TagRicavo> tags = new HashSet<TagRicavo>();
		int numTag = tagContainer.getChildCount();
		for (int i = 0; i < numTag; i++) {
			View tagLayout = tagContainer.getChildAt(i);
			TextView tagText = (TextView) tagLayout.findViewById(R.id.tagText);

			TagRicavo tag = new TagRicavo();
			tag.setRicavo(r);
			tag.setValore(tagText.getText().toString());
			tags.add(tag);
		}
		r.setTags(tags);
		r.setData(dateString);
		RicavoProgrammatoDao.insertRicavo(db, r);
	}

	/** Callback received when the user "picks" a date in the dialog */
	private DatePickerDialog.OnDateSetListener pDateSetListener = new DatePickerDialog.OnDateSetListener() {

		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			pYear = year;
			pMonth = monthOfYear;
			pDay = dayOfMonth;
			updateDisplay();
		}
	};

	/** Updates the date in the TextView */
	private void updateDisplay() {
		spinnerAdapter.clear();
		spinnerAdapter.add(pDay + "/" + (pMonth + 1) + "/" + pYear);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.ITALIAN);
		try {
			Date d = sdf.parse(DateUtils.getDate(pYear, pMonth + 1, pDay));
			if (d.after(new Date())) {
				submitButtonText.setText(getResources().getString(
						R.string.programma));
			} else {
				if (copy) {
					submitButtonText.setText(getResources().getString(
							R.string.aggiungi));
				} else {
					submitButtonText.setText(getResources().getString(
							R.string.modifica));
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_UP) {
			DatePickerDialog dialog = new DatePickerDialog(context,
					pDateSetListener, pYear, pMonth, pDay);
			dialog.show();
		}
		return true;
	}

}
