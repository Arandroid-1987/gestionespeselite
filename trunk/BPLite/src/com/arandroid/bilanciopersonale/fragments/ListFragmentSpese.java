package com.arandroid.bilanciopersonale.fragments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Observable;
import java.util.Observer;

import utils.DateUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.arandroid.bilanciopersonale.MainLayoutActivity;
import com.arandroid.bplite.R;
import com.dao.SpesaDao;
import com.dao.mvc.Notifier;
import com.db.DatabaseHandler;
import com.dto.Spesa;
import com.dto.VoceBilancio;
import com.google.android.gms.ads.AdRequest;
import com.ui.gestionespese.Filtro;
import com.ui.gestionespese.FiltroHolder;
import com.ui.gestionespese.FloatingFilterDialogCreator;
import com.ui.gestionespese.VoceBilancioAdapter;

public class ListFragmentSpese extends Fragment implements Observer,
		OnClickListener {

	private List<VoceBilancio> speseList = new ArrayList<VoceBilancio>();
	private Filtro filtro = new Filtro();
	private Filtro filtroTmp;
	private Button more;
	private Date startDate;
	private Date endDate;
	private VoceBilancioAdapter adapter;
	private ListView speseListView;

	private int position = 0;
	private final static int ROWS_LIMIT = 10;

	private Activity context;
	private View rootView;
	private boolean showMore = true;

	private AlertDialog filtriDialog;
	private FloatingFilterDialogCreator filtriCreator;
	private FiltroHolder holder;

	public ListFragmentSpese() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// Inflate the layout for this fragment

		rootView = inflater.inflate(R.layout.list_spese_layout, container,
				false);
		context = getActivity();

		speseListView = (ListView) rootView.findViewById(R.id.speseListView);
		
		if(context instanceof MainLayoutActivity){
			MainLayoutActivity act = (MainLayoutActivity) context;
			act.displayInterstitial();
			AdRequest adRequest = new AdRequest.Builder().build();
		    act.interstitial.loadAd(adRequest);
		}

		DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = dbHandler.getWritableDatabase();

		Intent intent = context.getIntent();
		position = intent.getIntExtra("position", 0);
		filtroTmp = (Filtro) intent.getSerializableExtra("filtro");
		speseList = new ArrayList<VoceBilancio>();
		@SuppressWarnings("unchecked")
		Collection<VoceBilancio> tmp = (Collection<VoceBilancio>) intent
				.getSerializableExtra("lista");
		if (tmp != null) {
			speseList.addAll(tmp);
			showMore = false;
		} else {
			if (filtroTmp != null) {
				Collection<Spesa> c = SpesaDao.filterSpese(db,
						filtroTmp.startDate, filtroTmp.endDate,
						filtroTmp.tagRichiesti, filtroTmp.minImp,
						filtroTmp.maxImp);
				speseList.addAll(c);
			} else {
				endDate = new Date();
				startDate = DateUtils.getMonthStart(endDate);
				String startDateStr = DateUtils.getDate(startDate);
				String endDateStr = DateUtils.getDate(endDate);
				Collection<Spesa> c = SpesaDao.filterSpese(db, startDateStr,
						endDateStr, null, -1, -1);
				int totalDBSize = SpesaDao.getCount(db);
				speseList.addAll(c);
				while (speseList.size() < ROWS_LIMIT
						&& speseList.size() < totalDBSize) {
					endDate = DateUtils.addDay(startDate, -1);
					startDate = DateUtils.getMonthStart(endDate);

					startDateStr = DateUtils.getDate(startDate);
					endDateStr = DateUtils.getDate(endDate);
					c = SpesaDao.filterSpese(db, startDateStr, endDateStr,
							null, -1, -1);
					speseList.addAll(c);
				}
			}
		}
		db.close();

		SpesaDao.getNotifier().addObserver(this);
		// VisualizzaSpeseActivity.getNotifier().addObserver(this);
		// filtro = VisualizzaSpeseActivity.getFiltro();

		return rootView;
	}

	@Override
	public void update(Observable observable, Object data) {
		if (!showMore) {
			return;
		}
		if (isVisible()) {
			if (observable instanceof Notifier) {
				Notifier n = (Notifier) observable;
				if (n.getTag().equals(Notifier.ACTIVITY_TAG)) {
					@SuppressWarnings("unchecked")
					Collection<Spesa> c = (Collection<Spesa>) data;
					speseList.clear();
					speseList.addAll(c);
					adapter.notifyDataSetChanged();
					if (more != null && more.getVisibility() == View.VISIBLE) {
						more.setVisibility(View.GONE);
					}

				} else {
					boolean isSpesa = (Boolean) data;
					DatabaseHandler dbHandler = DatabaseHandler
							.getInstance(context);
					SQLiteDatabase db = dbHandler.getWritableDatabase();
					Collection<Spesa> c;
					if (isSpesa) {
						speseList.clear();
						c = SpesaDao.filterSpese(db, filtro.startDate,
								filtro.endDate, filtro.tagRichiesti,
								filtro.minImp, filtro.maxImp);
						speseList.addAll(c);
						adapter = new VoceBilancioAdapter(context,
								R.layout.row_spesa_ricavo, speseList, isSpesa);
						speseListView.setAdapter(adapter);

						if (more != null && more.getVisibility() == View.GONE) {
							int currentSize = speseList.size();
							int totalDBSize = SpesaDao.getCount(db);

							db.close();
							if (currentSize < totalDBSize) {
								more.setVisibility(View.VISIBLE);
							}
						}
					}
					db.close();
				}
			} else if (observable instanceof FloatingFilterDialogCreator) {
				boolean isSpesa = (Boolean) data;
				DatabaseHandler dbHandler = DatabaseHandler
						.getInstance(context);
				SQLiteDatabase db = dbHandler.getWritableDatabase();
				if (isSpesa) {
					speseList.clear();
					filtro.minImp = 0;
					filtro.maxImp = 1000000;
					if (holder.minImporto != null && holder.maxImporto != null) {
						if (holder.minImporto.length() > 0) {
							filtro.minImp = Double
									.parseDouble(holder.minImporto);
						}
						if (holder.maxImporto.length() > 0) {
							filtro.maxImp = Double
									.parseDouble(holder.maxImporto);
						}
						if (filtro.minImp > filtro.maxImp) {
							Toast.makeText(context,
									getString(R.string.controllare_importi),
									Toast.LENGTH_SHORT).show();
							return;
						}
					}
					filtro.startDate = DateUtils.getDate(holder.fromYear,
							holder.fromMonth + 1, holder.fromDay);
					filtro.endDate = DateUtils.getDate(holder.toYear,
							holder.toMonth + 1, holder.toDay);

					if (holder.currentTab.equals(FiltroHolder.CLASSICO_TAB)) {
						String[] periodi = getResources().getStringArray(
								R.array.periodi);
						for (int i = 0; i < periodi.length; i++) {
							if (holder.periodoSelezionato.equals(periodi[i])) {
								Calendar c = Calendar.getInstance();
								SimpleDateFormat sdf = new SimpleDateFormat(
										"yyyy-MM-dd", Locale.ITALY);
								String today = sdf.format(c.getTime());
								switch (i) {
								case 0:
									filtro.startDate = "1900-01-01";
									filtro.endDate = "2200-01-01";
									break;
								case 1:
									filtro.endDate = today;
									filtro.startDate = filtro.endDate;
									break;
								case 2:
									filtro.endDate = today;
									c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
									filtro.startDate = sdf.format(c.getTime());
									break;
								case 3:
									filtro.endDate = today;
									c.set(Calendar.DAY_OF_MONTH, 1);
									filtro.startDate = sdf.format(c.getTime());
									break;
								case 4:
									filtro.endDate = today;
									c.set(Calendar.MONTH,
											(c.get(Calendar.MONTH) / 3) * 3);
									c.set(Calendar.DAY_OF_MONTH, 1);
									filtro.startDate = sdf.format(c.getTime());
									break;
								case 5:
									filtro.endDate = today;
									c.set(Calendar.MONTH,
											(c.get(Calendar.MONTH) / 6) * 6);
									c.set(Calendar.DAY_OF_MONTH, 1);
									filtro.startDate = sdf.format(c.getTime());
									break;
								case 6:
									filtro.endDate = today;
									c.set(Calendar.DAY_OF_YEAR, 1);
									filtro.startDate = sdf.format(c.getTime());
									break;
								case 7:
									filtro.endDate = today;
									c.add(Calendar.YEAR, -1);
									c.set(Calendar.DAY_OF_YEAR, 1);
									filtro.startDate = sdf.format(c.getTime());
									break;
								default:
									break;
								}
							}
						}
					}
					filtro.tagRichiesti = holder.tags;

					applyFilter();
				}
				db.close();
			}
		}
	}

	private void applyFilter() {
		DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = dbHandler.getWritableDatabase();

		Collection<Spesa> collection = SpesaDao.filterSpese(db,
				filtro.startDate, filtro.endDate, filtro.tagRichiesti,
				filtro.minImp, filtro.maxImp);
		db.close();

		speseList.addAll(collection);
		adapter = new VoceBilancioAdapter(context, R.layout.row_spesa_ricavo,
				speseList, true);
		speseListView.setAdapter(adapter);

		if (more != null && more.getVisibility() == View.GONE) {
			int currentSize = speseList.size();
			int totalDBSize = SpesaDao.getCount(db);

			db.close();
			if (currentSize < totalDBSize) {
				more.setVisibility(View.VISIBLE);
			}
		}
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		if (filtroTmp == null && showMore) {

			int currentSize = speseList.size();

			DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
			SQLiteDatabase db = dbHandler.getWritableDatabase();

			int totalDBSize = SpesaDao.getCount(db);

			db.close();

			if (currentSize < totalDBSize) {
				more = new Button(context);
				more.setText(context.getString(R.string.carica_altri));
				more.getBackground().setAlpha(100);
				more.setOnClickListener(this);
				speseListView.addFooterView(more);
			}
		}
		adapter = new VoceBilancioAdapter(context, R.layout.row_spesa_ricavo,
				speseList, true);
		speseListView.setAdapter((adapter));

		speseListView.setSelection(position);

	}

	@Override
	public void onClick(View v) {
		if (v.equals(more)) {
			endDate = DateUtils.addDay(startDate, -1);
			startDate = DateUtils.addMonth(endDate, -1);
			String startDateStr = DateUtils.getDate(startDate);
			String endDateStr = DateUtils.getDate(endDate);

			DatabaseHandler dbHandler = DatabaseHandler.getInstance(context);
			SQLiteDatabase db = dbHandler.getWritableDatabase();

			Collection<Spesa> c = SpesaDao.filterSpese(db, startDateStr,
					endDateStr, null, -1, -1);
			speseList.addAll(c);

			int currentSize = speseList.size();
			int totalDBSize = SpesaDao.getCount(db);

			db.close();

			if (currentSize >= totalDBSize) {
				speseListView.removeFooterView(more);
			}

			adapter.notifyDataSetChanged();

		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.filtri_bar, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		if (id == R.id.filtra) {
			if (filtriDialog == null) {
				holder = new FiltroHolder(context);
				filtriCreator = new FloatingFilterDialogCreator();
				filtriCreator.addObserver(this);
				filtriDialog = filtriCreator.create(context, true, holder);
			}
			filtriDialog.show();
			return true;
		}
		return false;
	}

}
