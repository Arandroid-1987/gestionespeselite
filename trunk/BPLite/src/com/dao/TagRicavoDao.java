package com.dao;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.arandroid.bplite.R;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.TagRicavo;

public class TagRicavoDao {

	public final static String ID = "id";
	public static final String RICAVO = "ricavo";
	public static final String VALORE = "valore";
	public static final String RICAVO_PROGRAMMATO = "ricavo_programmato";

	public static final String TABELLA = "tagRicavi";
	public static final String[] ALL_COLONNE = new String[] { ID, RICAVO,
			VALORE, RICAVO_PROGRAMMATO };
	
	private static String[] DEFAULT_TAGS;

	public static void insertTagRicavo(SQLiteDatabase db, TagRicavo tag) {
		ContentValues v = new ContentValues();
		Ricavo r = tag.getRicavo();
		v.put(RICAVO, r.getId());
		
		if(r instanceof RicavoProgrammato){
			v.put(RICAVO_PROGRAMMATO, 1);
		}
		else{
			v.put(RICAVO_PROGRAMMATO, 0);
		}
		v.put(VALORE, tag.getValore().toLowerCase(Locale.ITALIAN));
		db.insert(TABELLA, null, v);
	}

	public static Cursor getAllTagRicavo(SQLiteDatabase db) {
		return db.query(TABELLA, ALL_COLONNE, null, null, null, null, null);
	}

	public static Collection<String> getMostUsedTags(SQLiteDatabase db,
			int limit) {
		Collection<String> ris = new LinkedList<String>();

		Cursor c = db.query(TABELLA, new String[] { VALORE, "COUNT(VALORE)" },
				null, null, "VALORE", null, "COUNT(VALORE) DESC", limit + "");

		while (c.moveToNext()) {
			String valore = c.getString(0);
			ris.add(valore);
		}
		c.close();

		return ris;
	}

	public static Cursor getAllTagFromRicavo(SQLiteDatabase db, long idRicavo, int programmata) {
		return db.query(TABELLA, ALL_COLONNE, RICAVO + "=" + idRicavo+" AND "+RICAVO_PROGRAMMATO+" = "+programmata, null,
				null, null, null);
	}

	public static boolean deleteTagRicavo(SQLiteDatabase db, long id) {
		boolean delete = db.delete(TABELLA, ID + "=" + id, null) > 0;
		return delete;
	}

	public static void clear(SQLiteDatabase db) {
		db.delete(TABELLA, null, null);
	}

	public static Cursor getAllUniqueTagRicavo(SQLiteDatabase db) {
		Cursor c = null;
		try {
			c = db.query(true, TABELLA, new String[] { VALORE }, null, null,
					null, null, null, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return c;
	}

	public static boolean deleteFromRicavo(SQLiteDatabase db, long idRicavo, int programmata) {
		boolean delete = db.delete(TABELLA, RICAVO + "=" + idRicavo+" AND "+RICAVO_PROGRAMMATO+" = "+programmata, null) > 0;
		return delete;
	}
	
	public static void initDefaultTags(SQLiteDatabase db, Context context){
		Resources res = context.getResources();
		DEFAULT_TAGS  = new String[] { res.getString(R.string.stipendio),
				res.getString(R.string.conto_corrente), res.getString(R.string.contanti),
				res.getString(R.string.commercio), res.getString(R.string.vincita),
				res.getString(R.string.vendita),
				res.getString(R.string.affitto), res.getString(R.string.regalo),
				res.getString(R.string.hobby), res.getString(R.string.internet)};
		for (int i = 0; i < DEFAULT_TAGS.length; i++) {
			String value = DEFAULT_TAGS[i];
			ContentValues v = new ContentValues();
			v.put(VALORE, value);
			db.insert(TABELLA, null, v);
		}
	}

}
