package com.ui.gestionespese;

import com.arandroid.bplite.R;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ImageAdapter extends BaseAdapter {
	private Context context;

	public ImageAdapter(Context c) {
		context = c;
		Resources res = context.getResources();
		mTitles = new String[]{ res.getString(R.string.home), res.getString(R.string.nuova_spesa), res.getString(R.string.nuovo_ricavo),
				res.getString(R.string.riepilogo_spese), res.getString(R.string.riepilogo_ricavi), res.getString(R.string.spese_programmate),
				res.getString(R.string.ricavi_programmati), res.getString(R.string.bilanci), res.getString(R.string.confronti), res.getString(R.string.impostazioni),
				res.getString(R.string.about), res.getString(R.string.gestione_db) };
	}

	public int getCount() {
		return mThumbIds.length;
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return mThumbIds[position];
	}

	// create a new ImageView for each item referenced by the Adapter
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater li = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = li.inflate(R.layout.main_item, parent, false);
			holder = new ViewHolder();
			holder.titolo = (TextView) convertView.findViewById(R.id.titolo);
			holder.immagine = (ImageView) convertView
					.findViewById(R.id.immagine);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.titolo.setText(mTitles[position]);
		holder.immagine.setImageResource(mThumbIds[position]);

		return convertView;
	}

	// references to our images
	private Integer[] mThumbIds = { R.drawable.home, R.drawable.nuova_spesa,
			R.drawable.nuovo_ricavo, R.drawable.riepilogo_spese,
			R.drawable.riepilogo_ricavi, R.drawable.spese_programmate,
			R.drawable.ricavi_programmati, R.drawable.bilancio, R.drawable.confronti,
			R.drawable.impostazioni, R.drawable.about, R.drawable.database };

	private String[] mTitles; 

	static class ViewHolder {
		TextView titolo;
		ImageView immagine;
	}

}