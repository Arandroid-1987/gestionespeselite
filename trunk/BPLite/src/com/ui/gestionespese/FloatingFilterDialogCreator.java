package com.ui.gestionespese;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Observable;

import utils.DateUtils;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
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
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;
import android.widget.Toast;

import com.arandroid.bplite.R;
import com.dao.RicavoDao;
import com.dao.SpesaDao;
import com.dao.TagRicavoDao;
import com.dao.TagSpesaDao;
import com.db.DatabaseHandler;

public class FloatingFilterDialogCreator extends Observable {

	private Context context;

	private TabHost tabHost;
	private TabHost dataTabHost;

	private AutoCompleteTextView editTextTag;

	private Button addTag;
	private LinearLayout tagContainer;
	private HorizontalScrollView tagContainerScrollView;
	private LinearLayout mostUsedTagContainer;

	private EditText minImporto;
	private EditText maxImporto;

	private FiltroHolder holder;
	private Spinner classicoSpinner;

	private Typeface myTypeface;
	private boolean isSpesa;

	private DatePickerDialog.OnDateSetListener fromListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			if (DateUtils.isDate2AfterDate1(day, month, year, holder.toDay,
					holder.toMonth, holder.toYear)) {
				holder.fromDay = day;
				holder.fromMonth = month;
				holder.fromYear = year;
				holder.dalSpinnerAdapter.clear();
				holder.dalSpinnerAdapter.add(day + "/" + (month + 1) + "/"
						+ year);
				holder.dalSpinnerAdapter.notifyDataSetChanged();
				setChanged();
				notifyObservers(isSpesa);
			} else {
				Toast.makeText(context,
						context.getString(R.string.imposta_data_inizio_fine),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	private DatePickerDialog.OnDateSetListener toListener = new DatePickerDialog.OnDateSetListener() {

		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			if (DateUtils.isDate2AfterDate1(holder.fromDay, holder.fromMonth,
					holder.fromYear, day, month, year)) {
				holder.toDay = day;
				holder.toMonth = month;
				holder.toYear = year;
				holder.alSpinnerAdapter.clear();
				holder.alSpinnerAdapter.add(day + "/" + (month + 1) + "/"
						+ year);
				holder.alSpinnerAdapter.notifyDataSetChanged();
				setChanged();
				notifyObservers(isSpesa);
			} else {
				Toast.makeText(context,
						context.getString(R.string.imposta_data_fine_inizio),
						Toast.LENGTH_SHORT).show();
			}
		}
	};

	public AlertDialog create(Context context, boolean isSpesa,
			FiltroHolder holder) {
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		this.context = context;
		this.holder = holder;
		holder.reset();
		final Calendar c = Calendar.getInstance();
		holder.fromYear = c.get(Calendar.YEAR);
		holder.fromMonth = c.get(Calendar.MONTH) - 1;
		holder.fromDay = c.get(Calendar.DAY_OF_MONTH);
		holder.toYear = c.get(Calendar.YEAR);
		holder.toMonth = c.get(Calendar.MONTH);
		holder.toDay = c.get(Calendar.DAY_OF_MONTH);
		myTypeface = Typeface.createFromAsset(context.getAssets(),
				"fonts/ht.ttf");
		this.isSpesa = isSpesa;
		setup(builder);
		return builder.create();
	}

	public void setup(AlertDialog.Builder builder) {

		LayoutInflater inflater = (LayoutInflater) this.context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View convertView = inflater.inflate(R.layout.filtri_floating_layout,
				null);

		tabHost = (TabHost) convertView.findViewById(android.R.id.tabhost);
		tabHost.setup();

		TabSpec spec1 = tabHost.newTabSpec(FiltroHolder.TAB1);
		spec1.setContent(R.id.tab1);
		spec1.setIndicator(context.getString(R.string.filtra_per_data));

		TabSpec spec2 = tabHost.newTabSpec(FiltroHolder.TAB2);
		spec2.setContent(R.id.tab2);
		spec2.setIndicator(context.getString(R.string.filtra_per_importo));

		TabSpec spec3 = tabHost.newTabSpec(FiltroHolder.TAB3);
		spec3.setContent(R.id.tab3);
		spec3.setIndicator(context.getString(R.string.filtra_per_tag));

		tabHost.addTab(spec1);
		tabHost.addTab(spec2);
		tabHost.addTab(spec3);

		dataTabHost = (TabHost) convertView.findViewById(R.id.dataTabHost);
		dataTabHost.setup();

		TabSpec spec11 = tabHost.newTabSpec(FiltroHolder.CLASSICO_TAB);
		spec11.setContent(R.id.classico);
		spec11.setIndicator(context.getString(R.string.classico));

		TabSpec spec12 = tabHost.newTabSpec(FiltroHolder.TAB12);
		spec12.setContent(R.id.personalizzato);
		spec12.setIndicator(context.getString(R.string.personalizzato));

		dataTabHost.addTab(spec11);
		dataTabHost.addTab(spec12);

		classicoSpinner = (Spinner) convertView
				.findViewById(R.id.classicoSpinner);
		classicoSpinner
				.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> arg0, View arg1,
							int arg2, long arg3) {
						holder.periodoSelezionato = holder.classicoSpinnerAdapter
								.getItem(arg2);
						setChanged();
						notifyObservers(isSpesa);
					}

					@Override
					public void onNothingSelected(AdapterView<?> arg0) {
						holder.periodoSelezionato = null;
						setChanged();
						notifyObservers(isSpesa);
					}
				});

		holder.classicoSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_spinner_item);

		String[] names = context.getResources().getStringArray(R.array.periodi);

		for (String name : names) {
			holder.classicoSpinnerAdapter.add(name);
		}

		holder.classicoSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		classicoSpinner.setAdapter(holder.classicoSpinnerAdapter);
		if (holder.periodoSelezionato != null) {
			classicoSpinner.setSelection(holder.classicoSpinnerAdapter
					.getPosition(holder.periodoSelezionato));
		}

		holder.dalSpinner = (Spinner) convertView.findViewById(R.id.dalSpinner);
		holder.alSpinner = (Spinner) convertView.findViewById(R.id.alSpinner);

		holder.dalSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, new ArrayList<String>());

		holder.dalSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		holder.dalSpinner.setAdapter(holder.dalSpinnerAdapter);
		holder.dalSpinner.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					DatePickerDialog newDialog = new DatePickerDialog(context,
							fromListener, holder.fromYear, holder.fromMonth,
							holder.fromDay);
					newDialog.show();
				}
				return true;
			}
		});

		holder.alSpinnerAdapter = new ArrayAdapter<String>(context,
				android.R.layout.simple_list_item_1, new ArrayList<String>());

		holder.alSpinnerAdapter
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		holder.alSpinner.setAdapter(holder.alSpinnerAdapter);
		holder.alSpinner.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_UP) {
					DatePickerDialog newDialog = new DatePickerDialog(context,
							toListener, holder.toYear, holder.toMonth,
							holder.toDay);
					newDialog.show();
				}
				return true;
			}
		});

		dataTabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {

			@Override
			public void onTabChanged(String tabId) {
				holder.currentTab = tabId;
				dataTabHost.setCurrentTabByTag(tabId);
				setChanged();
				notifyObservers(isSpesa);
			}
		});
		if (holder.currentTab != null) {
			dataTabHost.setCurrentTabByTag(holder.currentTab);
		}
		holder.dalSpinnerAdapter.clear();
		holder.dalSpinnerAdapter.add(holder.fromDay + "/"
				+ (holder.fromMonth + 1) + "/" + holder.fromYear);
		holder.alSpinnerAdapter.clear();
		holder.alSpinnerAdapter.add(holder.toDay + "/" + (holder.toMonth + 1)
				+ "/" + holder.toYear);

		editTextTag = (AutoCompleteTextView) convertView
				.findViewById(R.id.editTextTag);
		tagContainer = (LinearLayout) convertView
				.findViewById(R.id.tagContainer);
		tagContainerScrollView = (HorizontalScrollView) convertView
				.findViewById(R.id.tagContainerScrollView);

		mostUsedTagContainer = (LinearLayout) convertView
				.findViewById(R.id.mostUsedTagContainer);

		populateTags();

		addTag = (Button) convertView.findViewById(R.id.addTag);
		addTag.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (editTextTag.getText().toString().length() > 0) {
					addTag(editTextTag.getText().toString());
					editTextTag.setText("");
				}
			}

		});

		minImporto = (EditText) convertView.findViewById(R.id.minImporto);
		maxImporto = (EditText) convertView.findViewById(R.id.maxImporto);
		minImporto.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				holder.minImporto = s.toString();
				if (holder.minImporto.length() > 0) {
					setChanged();
					notifyObservers(isSpesa);
				}
			}
		});
		maxImporto.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				holder.maxImporto = s.toString();
				if (holder.maxImporto.length() > 0) {
					setChanged();
					notifyObservers(isSpesa);
				}
			}
		});
		if (minImporto != null) {
			minImporto.setText(holder.minImporto);
		}
		if (maxImporto != null) {
			maxImporto.setText(holder.maxImporto);
		}

		builder.setView(convertView);
	}

	public FiltroHolder getHolder() {
		return holder;
	}

	public void reset() {
		holder.reset();
		tagContainer.removeAllViews();
		if (holder.periodoSelezionato != null) {
			classicoSpinner.setSelection(holder.classicoSpinnerAdapter
					.getPosition(holder.periodoSelezionato));
		}
		if (holder.currentTab != null) {
			tabHost.setCurrentTabByTag(holder.currentTab);
		}
	}

	private void populateTags() {
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getWritableDatabase();
		int limit = 10;
		Collection<String> mostUsedTags;
		if (isSpesa) {
			mostUsedTags = TagSpesaDao.getMostUsedTags(db, limit);
		} else {
			mostUsedTags = TagRicavoDao.getMostUsedTags(db, limit);
		}
		for (String string : mostUsedTags) {
			final Button b = new Button(context);
			b.setText(string);
			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT);
			params.leftMargin = 5;
			b.setLayoutParams(params);
			b.setBackgroundResource(R.drawable.my_button);
			b.setTextColor(context.getResources().getColor(R.color.white));
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
		if (isSpesa) {
			cursor = SpesaDao.getTags(db);
		} else {
			cursor = RicavoDao.getTags(db);
		}
		while (cursor.moveToNext()) {
			String causale = cursor.getString(0);
			causali.add(causale);
		}
		cursor.close();

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(context,
				android.R.layout.select_dialog_item, causali);
		editTextTag.setThreshold(1);
		editTextTag.setAdapter(adapter);

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
		final String toAdd = capitalLetter
				+ valore.substring(1).toLowerCase(Locale.ITALIAN);
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
		tagText.setText(toAdd);
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
								holder.tags.remove(toAdd);
								tagContainer.removeView(tagLayout);
								setChanged();
								notifyObservers(isSpesa);
							}
						});

					}
				});
			}
		});
		tagLayout.setVisibility(View.INVISIBLE);
		if (tagLayout.getParent() != null) {
			ViewParent parent = tagLayout.getParent();
			if (parent instanceof ViewGroup) {
				ViewGroup w = (ViewGroup) parent;
				w.removeView(tagLayout);
			}
		}
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

		holder.tags.add(toAdd);
		setChanged();
		notifyObservers(isSpesa);
	}

}
