package com.ui.gestionespese;

import java.util.Calendar;
import java.util.LinkedList;

import com.arandroid.bplite.R;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class FiltroHolder {

	public final static String TAB1 = "TAB 1";
	public final static String TAB2 = "TAB 2";
	public final static String TAB3 = "TAB 3";
	public final static String CLASSICO_TAB = "TAB 11";
	public final static String TAB12 = "TAB 12";

	public int fromYear;
	public int fromMonth;
	public int fromDay;
	public int toYear;
	public int toMonth;
	public int toDay;

	public Spinner dalSpinner;
	public Spinner alSpinner;
	public ArrayAdapter<String> dalSpinnerAdapter;
	public ArrayAdapter<String> alSpinnerAdapter;

	public String currentTab = CLASSICO_TAB;

	public LinkedList<String> tags = new LinkedList<String>();

	public String periodoSelezionato;
	public ArrayAdapter<String> classicoSpinnerAdapter;

	public String minImporto = "";
	public String maxImporto = "";

	public Context context;

	public FiltroHolder(Context context) {
		this.context = context;
		periodoSelezionato = context.getString(R.string.mese);
	}

	public void reset() {
		periodoSelezionato = context.getString(R.string.mese);
		tags = new LinkedList<String>();
		minImporto = "";
		maxImporto = "";
		currentTab = CLASSICO_TAB;
		final Calendar c = Calendar.getInstance();
		fromYear = c.get(Calendar.YEAR);
		fromMonth = c.get(Calendar.MONTH) - 1;
		fromDay = c.get(Calendar.DAY_OF_MONTH);
		toYear = c.get(Calendar.YEAR);
		toMonth = c.get(Calendar.MONTH);
		toDay = c.get(Calendar.DAY_OF_MONTH);

		if (dalSpinner != null) {
			dalSpinnerAdapter.clear();
			dalSpinnerAdapter.add(fromDay + "/" + (fromMonth + 1) + "/"
					+ fromYear);
		}
		if (alSpinnerAdapter != null) {
			alSpinnerAdapter.clear();
			alSpinnerAdapter.add(toDay + "/" + (toMonth + 1) + "/" + toYear);
		}
	}

}
