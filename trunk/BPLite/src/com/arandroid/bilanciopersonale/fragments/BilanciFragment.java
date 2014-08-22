package com.arandroid.bilanciopersonale.fragments;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.achartengine.GraphicalView;
import org.achartengine.model.CategorySeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;

import utils.DateUtils;
import utils.NumberUtils;
import utils.file.export.TXTExport;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Spinner;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.FileChooser;
import com.arandroid.bplite.R;
import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bilanciopersonale.VisualizzaRicaviActivity;
import com.arandroid.bilanciopersonale.VisualizzaSpeseActivity;
import com.arandroid.bilanciopersonale.qr.ObservableScrollView;
import com.arandroid.charts.BarChartView;
import com.arandroid.charts.PieChartView;
import com.dao.RicavoDao;
import com.dao.SettingsDao;
import com.dao.SpesaDao;
import com.dao.TagRicavoDao;
import com.dao.TagSpesaDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.Spesa;
import com.google.android.gms.ads.AdRequest;
import com.nineoldandroids.animation.ObjectAnimator;
import com.ui.gestionespese.Filtro;
import com.ui.gestionespese.FiltroHolder;

public class BilanciFragment extends Fragment implements OnClickListener,
		OnCheckedChangeListener, ObservableScrollView.Callbacks {
	private final static int ANDAMENTO_BILANCIO_N_TILES = 10;

	private TabHost tabHost;
	private Spinner classicoSpinner;

	private Spinner dalSpinner;
	private Spinner alSpinner;

	private ArrayAdapter<String> classicoSpinnerAdapter;
	private ArrayAdapter<String> dalSpinnerAdapter;
	private ArrayAdapter<String> alSpinnerAdapter;

	public int fromYear;
	public int fromMonth;
	public int fromDay;
	public int toYear;
	public int toMonth;
	public int toDay;

	private TextView bilancioTV;
	private TextView speseTV;
	private TextView ricaviTV;

	private View speseShow;
	private View ricaviShow;

	private TextView bilancioLabel;
	private String label = "";

	private double totaleSpese;
	private double totaleRicavi;

	private LinearLayout contentView;
	private GraphicalView chartView;
	private Collection<Spesa> spese;
	private Collection<Ricavo> ricavi;
	private Button esportaButton;
	private Checkable exportText;
	private Bitmap graphBitmap;
	private String startDate;
	private String endDate;

	private ArrayAdapter<String> graficoSpinnerAdapter;
	private String[] grafici;
	private String graficoSelezionato;
	private Spinner graficoSpinner;

	private AutoCompleteTextView editTextTag;
	private Button addTag;
	private LinearLayout tagContainer;
	private HorizontalScrollView tagContainerScrollView;
	private LinearLayout mostUsedTagContainer;

	private Filtro filtro = new Filtro();

	private Typeface myTypeface;
	private String[] periodi;
	private String periodoSelezionato;

	private final static int FILE_CHOOSER_ACTIVITY = 0;

	private Collection<String> tagsForFilter = new LinkedList<String>();

	private View rootView;
	private Activity context;

	private static final int STATE_ONSCREEN = 0;
	private static final int STATE_OFFSCREEN = 1;
	private static final int STATE_RETURNING = 2;

	private View mQuickReturnView;
	private View mPlaceholderView;
	private ObservableScrollView mObservableScrollView;
	private ScrollSettleHandler mScrollSettleHandler = new ScrollSettleHandler();
	private int mMinRawY = 0;
	private int mState = STATE_ONSCREEN;
	private int mQuickReturnHeight;
	private int mMaxScrollY;
	private ObjectAnimator anim;
	private int translationY;

	private Dialog dialog;

	private DatePickerDialog.OnDateSetListener fromListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			if (DateUtils.isDate2AfterDate1(day, month, year, toDay, toMonth,
					toYear)) {
				fromDay = day;
				fromMonth = month;
				fromYear = year;
				dalSpinnerAdapter.clear();
				dalSpinnerAdapter.add(day + "/" + (month + 1) + "/" + year);
				dalSpinnerAdapter.notifyDataSetChanged();
				setCustomDatesAndLabel();
				bilancioLabel.setText(label);
				calcolaBilancio();
			} else {
				Toast.makeText(
						context,
						getResources().getString(
								R.string.imposta_data_inizio_fine),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	private DatePickerDialog.OnDateSetListener toListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			if (DateUtils.isDate2AfterDate1(fromDay, fromMonth, fromYear, day,
					month, year)) {
				toDay = day;
				toMonth = month;
				toYear = year;
				alSpinnerAdapter.clear();
				alSpinnerAdapter.add(day + "/" + (month + 1) + "/" + year);
				alSpinnerAdapter.notifyDataSetChanged();
				setCustomDatesAndLabel();
				bilancioLabel.setText(label);
				calcolaBilancio();
			} else {
				Toast.makeText(
						context,
						getResources().getString(
								R.string.imposta_data_fine_inizio),
						Toast.LENGTH_SHORT).show();
			}
		}

	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		rootView = inflater.inflate(R.layout.bilanci_activity_layout,
				container, false);
		context = getActivity();
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}

		Typeface myTypeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/vag_rounded.ttf");
		this.myTypeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/ht.ttf");

		final Calendar c = Calendar.getInstance();
		fromYear = c.get(Calendar.YEAR);
		fromMonth = c.get(Calendar.MONTH) - 1;
		fromDay = c.get(Calendar.DAY_OF_MONTH);
		toYear = c.get(Calendar.YEAR);
		toMonth = c.get(Calendar.MONTH);
		toDay = c.get(Calendar.DAY_OF_MONTH);

		bilancioLabel = (TextView) rootView.findViewById(R.id.rowTitle);
		bilancioTV = (TextView) rootView.findViewById(R.id.bilancioTV);
		speseTV = (TextView) rootView.findViewById(R.id.speseTV);
		ricaviTV = (TextView) rootView.findViewById(R.id.ricaviTV);
		speseShow = rootView.findViewById(R.id.speseShow);
		ricaviShow = rootView.findViewById(R.id.ricaviShow);

		tagContainer = (LinearLayout) rootView.findViewById(R.id.tagContainer);
		tagContainerScrollView = (HorizontalScrollView) rootView
				.findViewById(R.id.tagContainerScrollView);

		mostUsedTagContainer = (LinearLayout) rootView
				.findViewById(R.id.mostUsedTagContainer);

		editTextTag = (AutoCompleteTextView) rootView
				.findViewById(R.id.editTextTag);
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

		populateTags();

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				getResources().getConfiguration().locale);
		String today = sdf.format(c.getTime());

		endDate = today;
		c.add(Calendar.MONTH, -1);
		startDate = sdf.format(c.getTime());

		tabHost = (TabHost) rootView.findViewById(android.R.id.tabhost);
		tabHost.setup();

		TabSpec spec1 = tabHost.newTabSpec(FiltroHolder.CLASSICO_TAB);
		spec1.setContent(R.id.classico);
		spec1.setIndicator(getResources().getString(R.string.classico));

		classicoSpinner = (Spinner) rootView.findViewById(R.id.classicoSpinner);
		classicoSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						periodi = getResources().getStringArray(
								R.array.periodi_bilancio);
						periodoSelezionato = classicoSpinnerAdapter
								.getItem(position);
						setClassicDatesAndLabel();
						bilancioLabel.setText(label);
						calcolaBilancio();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});

		classicoSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item);

		String[] names = getResources()
				.getStringArray(R.array.periodi_bilancio);

		for (String name : names) {
			classicoSpinnerAdapter.add(name);
		}

		classicoSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		classicoSpinner.setAdapter(classicoSpinnerAdapter);

		classicoSpinner.setSelection(3);

		TabSpec spec2 = tabHost.newTabSpec(FiltroHolder.TAB12);
		spec2.setIndicator(getResources().getString(R.string.personalizzato_uc));
		spec2.setContent(R.id.personalizzato);

		dalSpinner = (Spinner) rootView.findViewById(R.id.dalSpinner);
		alSpinner = (Spinner) rootView.findViewById(R.id.alSpinner);

		dalSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, new ArrayList<String>());

		dalSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		dalSpinner.setAdapter(dalSpinnerAdapter);
		dalSpinner.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					DatePickerDialog newDialog = new DatePickerDialog(context,
							fromListener, fromYear, fromMonth, fromDay);
					newDialog.show();
				}
				return true;
			}
		});

		alSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, new ArrayList<String>());

		alSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		alSpinner.setAdapter(alSpinnerAdapter);
		alSpinner.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					DatePickerDialog newDialog = new DatePickerDialog(context,
							toListener, toYear, toMonth, toDay);
					newDialog.show();
				}
				return true;
			}
		});

		dalSpinnerAdapter.clear();
		dalSpinnerAdapter.add(fromDay + "/" + (fromMonth + 1) + "/" + fromYear);
		alSpinnerAdapter.clear();
		alSpinnerAdapter.add(toDay + "/" + (toMonth + 1) + "/" + toYear);

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);

		tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				if (tabId.equals(FiltroHolder.CLASSICO_TAB)) {
					setClassicDatesAndLabel();
					bilancioLabel.setText(label);
					calcolaBilancio();
				} else if (tabId.equals(FiltroHolder.TAB12)) {
					setCustomDatesAndLabel();
					bilancioLabel.setText(label);
					calcolaBilancio();
				}
			}
		});

		bilancioLabel.setTypeface(myTypeface);
		bilancioTV.setTypeface(myTypeface);
		speseTV.setTypeface(myTypeface);
		ricaviTV.setTypeface(myTypeface);

		speseTV.setOnClickListener(this);
		ricaviTV.setOnClickListener(this);
		ricaviShow.setOnClickListener(this);
		speseShow.setOnClickListener(this);

		FrameLayout frame = (FrameLayout) rootView
				.findViewById(R.id.FrameLayout1);
		contentView = (LinearLayout) frame.findViewById(R.id.chartContainer);

		grafici = getResources().getStringArray(R.array.grafici);
		graficoSpinner = (Spinner) rootView.findViewById(R.id.graficoSpinner);
		graficoSpinnerAdapter = new ArrayAdapter<String>(context,
				R.layout.white_list_item, new ArrayList<String>());
		graficoSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (String grafico : grafici) {
			graficoSpinnerAdapter.add(grafico);
		}

		graficoSelezionato = grafici[0];
		graficoSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						graficoSelezionato = graficoSpinnerAdapter
								.getItem(position);
						disegnaGrafico();
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
					}
				});
		graficoSpinner.setAdapter(graficoSpinnerAdapter);

		mObservableScrollView = (ObservableScrollView) rootView
				.findViewById(R.id.scroll_view);
		mObservableScrollView.setCallbacks(this);

		mQuickReturnView = rootView.findViewById(R.id.sticky);
		mPlaceholderView = rootView.findViewById(R.id.placeholder);

		mObservableScrollView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						onScrollChanged(mObservableScrollView.getScrollY());
						mMaxScrollY = mObservableScrollView
								.computeVerticalScrollRange()
								- mObservableScrollView.getHeight();
						mQuickReturnHeight = mQuickReturnView.getHeight();
					}
				});

		calcolaBilancio();

		return rootView;
	}

	protected void setCustomDatesAndLabel() {
		startDate = DateUtils.getDate(fromYear, fromMonth + 1, fromDay);
		endDate = DateUtils.getDate(toYear, toMonth + 1, toDay);
		label = getResources().getString(R.string.bilancio_personalizzato);
	}

	protected void setClassicDatesAndLabel() {
		for (int i = 0; i < periodi.length; i++) {
			if (periodoSelezionato.equals(periodi[i])) {
				Calendar c = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
						Locale.ITALY);
				String today = sdf.format(c.getTime());
				switch (i) {
				case 0:
					startDate = "1900-01-01";
					endDate = "2200-01-01";
					label = getResources().getString(R.string.bilancio_totale);
					break;
				case 1:
					endDate = today;
					startDate = endDate;
					label = getResources().getString(
							R.string.bilancio_giornaliero);
					break;
				case 2:
					endDate = today;
					c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
					startDate = sdf.format(c.getTime());
					label = getResources().getString(
							R.string.bilancio_settimanale);
					break;
				case 3:
					endDate = today;
					c.set(Calendar.DAY_OF_MONTH, 1);
					startDate = sdf.format(c.getTime());
					label = getResources().getString(R.string.bilancio_mensile);
					break;
				case 4:
					endDate = today;
					c.set(Calendar.MONTH, (c.get(Calendar.MONTH) / 3) * 3);
					c.set(Calendar.DAY_OF_MONTH, 1);
					startDate = sdf.format(c.getTime());
					label = getResources().getString(
							R.string.bilancio_trimestrale);
					break;
				case 5:
					endDate = today;
					c.set(Calendar.MONTH, (c.get(Calendar.MONTH) / 6) * 6);
					c.set(Calendar.DAY_OF_MONTH, 1);
					startDate = sdf.format(c.getTime());
					label = getResources().getString(
							R.string.bilancio_semestrale);
					break;
				case 6:
					endDate = today;
					c.set(Calendar.DAY_OF_YEAR, 1);
					startDate = sdf.format(c.getTime());
					label = getResources().getString(R.string.bilancio_annuale);
					break;
				case 7:
					endDate = today;
					c.add(Calendar.YEAR, -1);
					c.set(Calendar.DAY_OF_YEAR, 1);
					startDate = sdf.format(c.getTime());
					label = getResources()
							.getString(R.string.bilancio_biennale);
					break;
				default:
					break;
				}
			}
		}
	}

	private void calcolaBilancio() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();

		spese = SpesaDao.filterSpese(db, startDate, endDate, tagsForFilter, 0,
				1000000);
		ricavi = RicavoDao.filterRicavi(db, startDate, endDate, tagsForFilter,
				0, 1000000);

		filtro.startDate = startDate;
		filtro.endDate = endDate;
		filtro.tagRichiesti = tagsForFilter;

		double bilancio = 0;
		totaleSpese = 0;
		totaleRicavi = 0;
		for (Ricavo ricavo : ricavi) {
			totaleRicavi += ricavo.getImporto();
		}
		for (Spesa spesa : spese) {
			totaleSpese += spesa.getImporto();
		}
		bilancio = totaleRicavi - totaleSpese;
		if (bilancio < 0) {
			bilancioTV.setTextColor(getResources().getColor(
					R.color.bilancio_passivo));
		} else if (bilancio > 0) {
			bilancioTV.setTextColor(getResources().getColor(
					R.color.bilancio_attivo));
		} else {
			bilancioTV.setTextColor(Color.BLACK);
		}

		String bilancioFormattato = NumberUtils.getString(bilancio);

		String bilancioTxt = bilancio <= 0 ? bilancioFormattato
				: ("+" + bilancioFormattato);
		String speseTxt = NumberUtils.getString(totaleSpese);
		String ricaviTxt = NumberUtils.getString(totaleRicavi);

		String[] currency = SettingsDao.getCurrency(db);
		String space_currency = " " + currency[1];

		bilancioTV.setText(getResources().getString(R.string.bilancio_column)
				+ bilancioTxt + space_currency);
		speseTV.setText(getResources().getString(R.string.spese_column)
				+ speseTxt + space_currency);
		ricaviTV.setText(getResources().getString(R.string.ricavi_column)
				+ ricaviTxt + space_currency);

		db.close();

		disegnaGrafico();
	}

	private void disegnaGrafico() {

		for (int i = 0; i < grafici.length; i++) {
			if (graficoSelezionato.equals(grafici[i])) {
				switch (i) {
				case 0:
					CategorySeries values = new CategorySeries("");
					values.add(getResources().getString(R.string.spese),
							totaleSpese);
					values.add(getResources().getString(R.string.ricavi),
							totaleRicavi);
					chartView = (GraphicalView) PieChartView.getView(context,
							values);
					break;
				case 1:
					if (periodoSelezionato != null
							&& (periodoSelezionato.equals(periodi[0]) || periodoSelezionato
									.equals(periodi[1]) || periodoSelezionato
									.equals(periodi[2]))) {
						Toast.makeText(
								context,
								getResources().getString(
										R.string.grafico_non_supportato),
								Toast.LENGTH_LONG).show();
						graficoSpinner.setSelection(0);
						graficoSelezionato = grafici[0];
						disegnaGrafico();
					} else {
						values = new CategorySeries("");
						values.add(getResources().getString(R.string.spese),
								totaleSpese);
						values.add(getResources().getString(R.string.ricavi),
								totaleRicavi);
						XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
						List<Double[]> serieBilanci = calcolaAndamentoBilancio();
						XYSeries ricaviSeries = new XYSeries(getResources()
								.getString(R.string.ricavi));
						XYSeries speseSeries = new XYSeries(getResources()
								.getString(R.string.spese));
						for (int j = 0; j < serieBilanci.size(); j++) {
							ricaviSeries.add(j + 1, serieBilanci.get(j)[0]);
							speseSeries.add(j + 1, serieBilanci.get(j)[1]);
						}
						dataset.addSeries(speseSeries);
						dataset.addSeries(ricaviSeries);
						
						String [] labels = new String[ricaviSeries.getItemCount()];
						for (int j = 0; j < labels.length; j++) {
							labels[i] = getString(R.string.periodo)+" "+(i+1);
						}
						
						chartView = (GraphicalView) BarChartView.getView(
								context, values, dataset, labels);
					}
					break;
				default:
					break;
				}
			}
		}
		DisplayMetrics metrics = new DisplayMetrics();
		context.getWindowManager().getDefaultDisplay().getMetrics(metrics);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		chartView.setLayoutParams(params);
		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) contentView
				.getLayoutParams();
		double hp = metrics.heightPixels;
		hp = hp / 2.5;
		layoutParams.height = (int) hp;
		contentView.removeAllViews();
		contentView.setLayoutParams(layoutParams);
		contentView.addView(chartView, params);
	}

	private List<Double[]> calcolaAndamentoBilancio() {
		List<Double[]> ris = new ArrayList<Double[]>();
		try {
			DatabaseHandler handler = DatabaseHandler.getInstance(context);
			SQLiteDatabase db = handler.getWritableDatabase();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
					Locale.ITALIAN);
			Date start;

			start = sdf.parse(startDate);

			Date end = sdf.parse(endDate);
			double differenza = DateUtils.differenza(start, end);
			double size = differenza < ANDAMENTO_BILANCIO_N_TILES ? differenza
					: ANDAMENTO_BILANCIO_N_TILES;

			int tileSize = (int) Math.ceil(differenza / size);

			for (int i = 0; i < size; i++) {
				if (i > 0) {
					start = DateUtils.addDay(start, tileSize);
				}
				end = DateUtils.addDay(start, tileSize);
				String startDate = sdf.format(start);
				String endDate = sdf.format(end);
				Collection<Spesa> spese = SpesaDao.filterSpese(db, startDate,
						endDate, tagsForFilter, 0, 1000000);
				Collection<Ricavo> ricavi = RicavoDao.filterRicavi(db,
						startDate, endDate, tagsForFilter, 0, 1000000);

				double totaleSpese = 0;
				double totaleRicavi = 0;
				for (Ricavo ricavo : ricavi) {
					totaleRicavi += ricavo.getImporto();
				}
				for (Spesa spesa : spese) {
					totaleSpese += spesa.getImporto();
				}
				ris.add(new Double[] { totaleRicavi, totaleSpese });
			}

			db.close();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return ris;
	}

	@Override
	public void onClick(View v) {
		if (v.equals(esportaButton)) {
			boolean esportaTXT = exportText.isChecked();
			if (esportaTXT) {
				Intent intent = new Intent(context, FileChooser.class);
				intent.putExtra(FileChooser.SELECT_FILES, false);
				startActivityForResult(intent, FILE_CHOOSER_ACTIVITY);
			}
			dialog.dismiss();
		} else if (v.equals(speseTV) || v.equals(speseShow)) {
			Intent intent = new Intent(context, VisualizzaSpeseActivity.class);
			intent.putExtra("filtro", filtro);
			context.startActivity(intent);
		} else if (v.equals(ricaviTV) || v.equals(ricaviShow)) {
			Intent intent = new Intent(context, VisualizzaRicaviActivity.class);
			intent.putExtra("filtro", filtro);
			context.startActivity(intent);
		}
	}

	private void setupGraphBitmap() {
		View v = chartView;
		graphBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight() - 100,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(graphBitmap);
		c.drawColor(Color.TRANSPARENT, Mode.CLEAR);
		v.layout(0, 0, v.getWidth(), v.getHeight() - 100);
		v.draw(c);

	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == FILE_CHOOSER_ACTIVITY) {
			if (resultCode == Activity.RESULT_OK) {
				String path = data.getStringExtra("file_path");
				esportaReport(path);
			} else {
				Toast.makeText(
						context,
						getResources().getString(
								R.string.attenzione_nessun_file_selezionato),
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
		db.close();
		boolean ok = true;
		int count = 0;
		setupGraphBitmap();
		if (esportaTXT) {
			count++;
			File file = new File(dir, filename + TXTExport.EXTENSION);
			TXTExport export = new TXTExport(spese, ricavi, file, startDate,
					endDate);
			ok = ok && export.export();
		}
		if (count > 0) {
			if (ok) {
				Toast.makeText(
						context,
						getResources()
								.getString(R.string.esportazione_successo),
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(
						context,
						getResources().getString(
								R.string.errore_esportazione_dati),
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton view, boolean checked) {
	}

	private void populateTags() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		int limit = 5;
		Collection<String> mostUsedTags;
		mostUsedTags = TagSpesaDao.getMostUsedTags(db, limit);
		mostUsedTags.addAll(TagRicavoDao.getMostUsedTags(db, limit));
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

		List<String> causali = new ArrayList<String>();
		Cursor cursor;
		cursor = SpesaDao.getTags(db);
		while (cursor.moveToNext()) {
			String causale = cursor.getString(0);
			causali.add(causale);
		}
		cursor.close();

		Cursor cursor1 = RicavoDao.getTags(db);
		while (cursor1.moveToNext()) {
			String causale = cursor1.getString(0);
			if (!causali.contains(causale)) {
				causali.add(causale);
			}
		}

		cursor1.close();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.select_dialog_item, causali);
		editTextTag.setThreshold(1);
		editTextTag.setAdapter(adapter);

		db.close();
	}

	private void addTag(final String valore) {
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
		String valoreDaInserire = capitalLetter
				+ valore.substring(1).toLowerCase(Locale.ITALIAN);
		LayoutInflater inflater = getLayoutInflater(null);
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
		tagText.setText(valoreDaInserire);
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
								tagsForFilter.remove(valore);
								calcolaBilancio();
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

		tagsForFilter.add(valore);
		calcolaBilancio();

	}

	@Override
	public void onScrollChanged(int scrollY) {
		scrollY = Math.min(mMaxScrollY, scrollY);

		mScrollSettleHandler.onScroll(scrollY);

		int rawY = mPlaceholderView.getTop() - scrollY;
		translationY = 0;

		switch (mState) {
		case STATE_OFFSCREEN:
			if (rawY <= mMinRawY) {
				mMinRawY = rawY;
			} else {
				mState = STATE_RETURNING;
			}
			translationY = rawY;
			break;

		case STATE_ONSCREEN:
			if (rawY < -mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			translationY = rawY;
			break;

		case STATE_RETURNING:
			translationY = (rawY - mMinRawY) - mQuickReturnHeight;
			if (translationY > 0) {
				translationY = 0;
				mMinRawY = rawY - mQuickReturnHeight;
			}

			if (rawY > 0) {
				mState = STATE_ONSCREEN;
				translationY = rawY;
			}

			if (translationY < -mQuickReturnHeight) {
				mState = STATE_OFFSCREEN;
				mMinRawY = rawY;
			}
			break;
		}
		anim = ObjectAnimator.ofFloat(mQuickReturnView, "translationY", scrollY
				+ translationY);
		anim.setDuration(0);
		anim.start();
	}

	@Override
	public void onDownMotionEvent() {
		mScrollSettleHandler.setSettleEnabled(false);
	}

	@Override
	public void onUpOrCancelMotionEvent() {
		mScrollSettleHandler.setSettleEnabled(true);
		mScrollSettleHandler.onScroll(mObservableScrollView.getScrollY());
	}

	@SuppressLint("HandlerLeak")
	private class ScrollSettleHandler extends Handler {
		private static final int SETTLE_DELAY_MILLIS = 100;

		private int mSettledScrollY = Integer.MIN_VALUE;
		private boolean mSettleEnabled;

		public void onScroll(int scrollY) {
			if (mSettledScrollY != scrollY) {
				// Clear any pending messages and post delayed
				removeMessages(0);
				sendEmptyMessageDelayed(0, SETTLE_DELAY_MILLIS);
				mSettledScrollY = scrollY;
			}
		}

		public void setSettleEnabled(boolean settleEnabled) {
			mSettleEnabled = settleEnabled;
		}

		@Override
		public void handleMessage(Message msg) {
			// Handle the scroll settling.
			if (STATE_RETURNING == mState && mSettleEnabled) {
				int mDestTranslationY;
				float amount1 = (Float) anim.getAnimatedValue("translationY");
				if (mSettledScrollY - amount1 > mQuickReturnHeight / 2) {
					mState = STATE_OFFSCREEN;
					mDestTranslationY = Math.max(mSettledScrollY
							- mQuickReturnHeight, mPlaceholderView.getTop());
				} else {
					mDestTranslationY = mSettledScrollY;
				}

				mMinRawY = mPlaceholderView.getTop() - mQuickReturnHeight
						- mDestTranslationY;
			}
			mSettledScrollY = Integer.MIN_VALUE; // reset
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.bilanci_bar, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.esporta) {
			createAndShowExportDialog();
			return true;
		}
		return false;
	}

	private void createAndShowExportDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = getLayoutInflater(getArguments());
		View v = inflater.inflate(R.layout.bilanci_activity_menu, null);

		esportaButton = (Button) v.findViewById(R.id.esportaButton);
		exportText = (Checkable) v.findViewById(R.id.exportText);
		esportaButton.setOnClickListener(this);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(getString(R.string.esporta));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(getString(R.string.esporta));
		}

		builder.setView(v);
		dialog = builder.create();
		dialog.show();
	}

}
