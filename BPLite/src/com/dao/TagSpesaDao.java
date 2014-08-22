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
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.TagSpesa;

public class TagSpesaDao {

	public final static String ID = "id";
	public static final String SPESA = "spesa";
	public static final String VALORE = "valore";
	public static final String SPESA_PROGRAMMATA = "spesa_programmata";

	public static final String TABELLA = "tagSpese";
	public static final String[] ALL_COLONNE = new String[] { ID, SPESA, VALORE, SPESA_PROGRAMMATA };

	private static String[] DEFAULT_TAGS;

	public static void insertTagSpesa(SQLiteDatabase db, TagSpesa tag) {
		ContentValues v = new ContentValues();
		Spesa s = tag.getSpesa();
		v.put(SPESA, s.getId());
		if(s instanceof SpesaProgrammata){
			v.put(SPESA_PROGRAMMATA, 1);
		}
		else{
			v.put(SPESA_PROGRAMMATA, 0);
		}
		v.put(VALORE, tag.getValore().toLowerCase(Locale.ITALIAN));
		db.insert(TABELLA, null, v);
	}

	public static Cursor getAllTagSpesa(SQLiteDatabase db) {
		return db.query(TABELLA, ALL_COLONNE, null, null, null, null, null);
	}

	public static Cursor getAllTagFromSpesa(SQLiteDatabase db, long idSpesa, int programmata) {
		return db.query(TABELLA, ALL_COLONNE, SPESA + "=" + idSpesa+" AND "+SPESA_PROGRAMMATA+" = "+programmata, null,
				null, null, null);
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

	public static boolean deleteTagSpesa(SQLiteDatabase db, long id) {
		boolean delete = db.delete(TABELLA, ID + "=" + id, null) > 0;
		return delete;
	}

	public static void clear(SQLiteDatabase db) {
		db.delete(TABELLA, null, null);
	}

	public static Cursor getAllUniqueTagSpesa(SQLiteDatabase db) {
		return db.query(true, TABELLA, new String[] { VALORE }, null, null,
				null, null, null, null);
	}

	public static boolean deleteFromSpesa(SQLiteDatabase db, long idSpesa, int programmata) {
		boolean delete = db.delete(TABELLA,  SPESA + "=" + idSpesa+" AND "+SPESA_PROGRAMMATA+" = "+programmata, null) > 0;
		return delete;
	}
	
	public static void initDefaultTags(SQLiteDatabase db, Context context){
		Resources res = context.getResources();
		DEFAULT_TAGS  = new String[] { res.getString(R.string.casa),
				res.getString(R.string.affitto), res.getString(R.string.contanti),
				res.getString(R.string.bancomat), res.getString(R.string.alimentari),
				res.getString(R.string.rata),
				res.getString(R.string.tecnologia), res.getString(R.string.abbigliamento),
				res.getString(R.string.hobby), res.getString(R.string.automobile)};
		for (int i = 0; i < DEFAULT_TAGS.length; i++) {
			String value = DEFAULT_TAGS[i];
			ContentValues v = new ContentValues();
			v.put(VALORE, value);
			db.insert(TABELLA, null, v);
		}
	}

}
