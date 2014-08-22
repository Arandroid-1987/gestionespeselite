package com.ui.gestionespese;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import utils.BilancioMeseCalculator;
import utils.DateUtils;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.arandroid.bplite.R;
import com.arandroid.bilanciopersonale.VisualizzaRicaviActivity;
import com.arandroid.bilanciopersonale.VisualizzaSpeseActivity;
import com.dao.SettingsDao;
import com.db.DatabaseHandler;
import com.dto.BilancioMese;
import com.fortysevendeg.swipelistview.SwipeListView;

public class BilancioMeseAdapter extends ArrayAdapter<BilancioMese> {

	private SwipeListView parentList;
	private Activity context;

	private String[] currency;
	
	private AlertDialog dialog;

	private List<BilancioMese> items = new ArrayList<BilancioMese>();

	static class ViewHolder {
		TextView rowTitle;
		TextView speseTV;
		TextView ricaviTV;
		TextView bilancioTV;
		ImageView speseShow;
		ImageView ricaviShow;
		Button elimina;
		Button modifica;
	}

	public BilancioMeseAdapter(Activity context, int textViewResourceId,
			List<BilancioMese> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		this.items = objects;
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		currency = SettingsDao.getCurrency(db);
		db.close();
	}

	public int getCount() {
		return this.items.size();
	}

	public BilancioMese getItem(int index) {
		return this.items.get(index);
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.confronti_row, parent, false);
			holder = new ViewHolder();
			holder.bilancioTV = (TextView) convertView
					.findViewById(R.id.bilancioTV);
			holder.ricaviTV = (TextView) convertView
					.findViewById(R.id.ricaviTV);
			holder.rowTitle = (TextView) convertView
					.findViewById(R.id.rowTitle);
			holder.speseTV = (TextView) convertView.findViewById(R.id.speseTV);
			holder.speseShow = (ImageView) convertView
					.findViewById(R.id.speseShow);
			holder.ricaviShow = (ImageView) convertView
					.findViewById(R.id.ricaviShow);
			holder.elimina = (Button) convertView.findViewById(R.id.elimina);
			holder.modifica = (Button) convertView.findViewById(R.id.modifica);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		final BilancioMese item = getItem(position);

		double bilancio = item.getBilancio();
		double spese = item.getSpese();
		double ricavi = item.getRicavi();

		DecimalFormat df = new DecimalFormat("0.00");

		String bilancioTxt = df.format(bilancio);
		String speseTxt = df.format(spese);
		String ricaviTxt = df.format(ricavi);

		holder.bilancioTV.setText(context.getString(R.string.bilancio) + ": "
				+ bilancioTxt + " " + currency[1]);
		holder.speseTV.setText(context.getString(R.string.spese) + ": "
				+ speseTxt + " " + currency[1]);
		holder.ricaviTV.setText(context.getString(R.string.ricavi) + ": "
				+ ricaviTxt + " " + currency[1]);
		holder.rowTitle.setText(item.getMese() + " " + item.getAnno());

		parentList = ((SwipeListView) parent);

		holder.elimina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				items.remove(position);
				notifyDataSetChanged();
				parentList.closeAnimate(position);
			}
		});

		holder.modifica.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				parentList.closeAnimate(position);
				createAndShowEditDialog(item);
			}
		});

		holder.elimina.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				items.remove(position);
				notifyDataSetChanged();
				parentList.closeAnimate(position);
			}
		});

		holder.speseShow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Filtro filtro = new Filtro();
				int mese = item.getMeseNumero();
				int anno = item.getAnnoNumero();
				Calendar c = Calendar.getInstance(Locale.getDefault());
				c.set(Calendar.MONTH, mese);
				c.set(Calendar.YEAR, anno);
				c.set(Calendar.DAY_OF_MONTH, 1);
				Date d = c.getTime();
				String startDate = DateUtils.getDate(d);
				c.add(Calendar.MONTH, 1);
				c.add(Calendar.DAY_OF_YEAR, -1);
				d = c.getTime();
				String endDate = DateUtils.getDate(d);
				filtro.startDate = startDate;
				filtro.endDate = endDate;
				Intent intent = new Intent(context, VisualizzaSpeseActivity.class);
				intent.putExtra("filtro", filtro);
				context.startActivity(intent);
			}
		});

		holder.ricaviShow.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Filtro filtro = new Filtro();
				int mese = item.getMeseNumero();
				int anno = item.getAnnoNumero();
				Calendar c = Calendar.getInstance(Locale.getDefault());
				c.set(Calendar.MONTH, mese);
				c.set(Calendar.YEAR, anno);
				c.set(Calendar.DAY_OF_MONTH, 1);
				Date d = c.getTime();
				String startDate = DateUtils.getDate(d);
				c.add(Calendar.MONTH, 1);
				c.add(Calendar.DAY_OF_YEAR, -1);
				d = c.getTime();
				String endDate = DateUtils.getDate(d);
				filtro.startDate = startDate;
				filtro.endDate = endDate;
				Intent intent = new Intent(context, VisualizzaRicaviActivity.class);
				intent.putExtra("filtro", filtro);
				context.startActivity(intent);
			}
		});

		return convertView;
	}

	private void createAndShowEditDialog(final BilancioMese bm) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		LayoutInflater inflater = context.getLayoutInflater();
		View v = inflater.inflate(R.layout.confronti_new_dialog, null);

		View okButton = v.findViewById(R.id.okButton);
		final Spinner anniSpinner = (Spinner) v.findViewById(R.id.anniSpinner);
		final Spinner mesiSpinner = (Spinner) v.findViewById(R.id.mesiSpinner);

		final ArrayList<Integer> years = new ArrayList<Integer>();

		Calendar c = Calendar.getInstance(Locale.getDefault());

		int currentYear = c.get(Calendar.YEAR);

		for (int i = currentYear; i >= currentYear - 10; i--) {
			years.add(i);
		}
		
		final ArrayList<String> months = new ArrayList<String>();
		String [] m = context.getResources().getStringArray(R.array.mesi);
		for (int i = 0; i < m.length; i++) {
			months.add(m[i]);
		}
		
		mesiSpinner.setSelection(months.indexOf(bm.getMese()));

		ArrayAdapter<Integer> anniSpinnerAdapter = new ArrayAdapter<Integer>(
				context, android.R.layout.simple_spinner_dropdown_item, years);

		anniSpinner.setAdapter(anniSpinnerAdapter);
		Integer year = Integer.parseInt(bm.getAnno());
		anniSpinner.setSelection(years.indexOf(year));

		okButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				int mese = mesiSpinner.getSelectedItemPosition();
				int anno = (Integer) anniSpinner.getSelectedItem();
				BilancioMese item = BilancioMeseCalculator.get(mese, anno, context);
				bm.setAnno(item.getAnno());
				bm.setBilancio(item.getBilancio());
				bm.setMese(item.getMese());
				bm.setSpese(item.getSpese());
				bm.setRicavi(item.getRicavi());
				notifyDataSetChanged();
				dialog.dismiss();
				dialog.dismiss();
			}
		});

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View title = inflater.inflate(R.layout.dialog_title, null);
			TextView titleText = (TextView) title.findViewById(R.id.titleText);
			titleText.setText(context.getString(R.string.aggiungi));
			builder.setCustomTitle(title);
		} else {
			builder.setTitle(context.getString(R.string.aggiungi));
		}

		builder.setView(v);
		dialog = builder.create();
		dialog.show();
	}

}
