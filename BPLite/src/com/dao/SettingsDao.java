package com.dao;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class SettingsDao {

	public static final String ID = "id";
	public static final String USE_PASSWORD = "usepw";
	public static final String PASSWORD = "pw";
	public static final String EMAIL = "email";
	public static final String TUTORIAL_ON = "tutorial";
	public static final String USE_BACKUP = "useBackup";
	public static final String REPEAT = "repeat";
	public static final String USE_SAME_FILE = "useSameFile";
	public static final String LAST_BACKUP = "lastBackup";
	private static final String CURRENCY_NAME = "currencyName";
	private static final String CURRENCY_SYMBOL = "currencySymbol";

	public static final String TABELLA = "passwordtable";
	public static final String[] ALL_COLONNE = new String[] { ID, USE_PASSWORD,
			PASSWORD, EMAIL, TUTORIAL_ON, USE_BACKUP, REPEAT, USE_SAME_FILE, LAST_BACKUP, CURRENCY_NAME, CURRENCY_SYMBOL };

	private static final int ONLY_ID = 100;

	public static void setPassword(SQLiteDatabase db, String password, String email) {
		ContentValues values = new ContentValues();
		if (getCurrentPassword(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(USE_PASSWORD, 1);
			values.put(PASSWORD, password);
			values.put(EMAIL, email);
			values.put(TUTORIAL_ON, 1);
			db.insert(TABELLA, null, values);
		} else {
			values.put(USE_PASSWORD, 1);
			values.put(PASSWORD, password);
			values.put(EMAIL, email);
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static Cursor getData(SQLiteDatabase db){
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + ONLY_ID, null,
				null, null, null);
		c.moveToFirst();
		if (c.getCount() <= 0) {
			c.close();
			return null;
		}
		return c;
	}

	public static String getCurrentPassword(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + ONLY_ID, null,
				null, null, null);
		c.moveToFirst();
		if (c.getCount() <= 0) {
			c.close();
			return null;
		}
		String pw = c.getString(c.getColumnIndex(PASSWORD));
		c.close();
		return pw;
	}
	
	public static String getCurrentEmail(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + ONLY_ID, null,
				null, null, null);
		c.moveToFirst();
		if (c.getCount() <= 0) {
			c.close();
			return null;
		}
		String pw = c.getString(c.getColumnIndex(EMAIL));
		c.close();
		return pw;
	}

	public static boolean isUsingPassword(SQLiteDatabase db) {
		return false;
	}

	public static void doNotUsePassword(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		if (getCurrentPassword(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(USE_PASSWORD, 0);
			values.put(PASSWORD, "");
			values.put(EMAIL, "");
			values.put(TUTORIAL_ON, 1);
			db.insert(TABELLA, null, values);
		} else {
			values.put(USE_PASSWORD, 0);
			values.put(PASSWORD, "");
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static void tutorialOn(SQLiteDatabase db, boolean on){
		ContentValues values = new ContentValues();
		if (getCurrentPassword(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(USE_PASSWORD, 0);
			values.put(PASSWORD, "");
			values.put(EMAIL, "");
			values.put(TUTORIAL_ON, on?1:0);
			db.insert(TABELLA, null, values);
		} else {
			values.put(TUTORIAL_ON, on?1:0);
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static boolean isTutorialOn(SQLiteDatabase db){
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + ONLY_ID, null,
				null, null, null);
		c.moveToFirst();
		if (c.getCount() <= 0) {
			c.close();
			return true;
		}
		int onInt = c.getInt(c.getColumnIndex(TUTORIAL_ON));
		boolean on = onInt == 1;
		c.close();
		return on;
	}

	public static void doNotUseBackup(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		if (getData(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(USE_PASSWORD, 0);
			values.put(PASSWORD, "");
			values.put(EMAIL, "");
			values.put(TUTORIAL_ON, 1);
			values.put(USE_BACKUP, 0);
			values.put(REPEAT, 0);
			values.put(USE_SAME_FILE, 0);
			values.put(LAST_BACKUP, 0);
			db.insert(TABELLA, null, values);
		} else {
			values.put(USE_BACKUP, 0);
			values.put(REPEAT, 0);
			values.put(USE_SAME_FILE, 0);
			values.put(LAST_BACKUP, 0);
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static void useBackup(SQLiteDatabase db, int repeat, boolean useSameFile, int lastBackup){
		ContentValues values = new ContentValues();
		if (getData(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(USE_PASSWORD, 0);
			values.put(PASSWORD, "");
			values.put(EMAIL, "");
			values.put(TUTORIAL_ON, 1);
			values.put(USE_BACKUP, 1);
			values.put(REPEAT, repeat);
			values.put(USE_SAME_FILE, useSameFile?1:0);
			values.put(LAST_BACKUP, lastBackup);
			db.insert(TABELLA, null, values);
		} else {
			values.put(USE_BACKUP, 1);
			values.put(REPEAT, repeat);
			values.put(USE_SAME_FILE, useSameFile?1:0);
			values.put(LAST_BACKUP, lastBackup);
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static void useBackup(SQLiteDatabase db, int repeat, boolean useSameFile){
		ContentValues values = new ContentValues();
		if (getData(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(USE_PASSWORD, 0);
			values.put(PASSWORD, "");
			values.put(EMAIL, "");
			values.put(TUTORIAL_ON, 1);
			values.put(USE_BACKUP, 1);
			values.put(REPEAT, repeat);
			values.put(USE_SAME_FILE, useSameFile?1:0);
			db.insert(TABELLA, null, values);
		} else {
			values.put(USE_BACKUP, 1);
			values.put(REPEAT, repeat);
			values.put(USE_SAME_FILE, useSameFile?1:0);
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static int [] getBackupData(SQLiteDatabase db){
		int [] ris = new int[4];
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + ONLY_ID, null,
				null, null, null);
		c.moveToFirst();
		if (c.getCount() <= 0) {
			c.close();
			return null;
		}
		int onInt = c.getInt(c.getColumnIndex(USE_BACKUP));
		ris[0] = onInt;
		onInt = c.getInt(c.getColumnIndex(REPEAT));
		ris[1] = onInt;
		onInt = c.getInt(c.getColumnIndex(USE_SAME_FILE));
		ris[2] = onInt;
		onInt = c.getInt(c.getColumnIndex(LAST_BACKUP));
		ris[3] = onInt;
		c.close();
		return ris;
	}
	
	public static void setCurrency(SQLiteDatabase db, String currency, String symbol){
		ContentValues values = new ContentValues();
		if (getData(db) == null) {
			values = new ContentValues();
			values.put(ID, ONLY_ID);
			values.put(CURRENCY_NAME, currency);
			values.put(CURRENCY_SYMBOL, symbol);
			db.insert(TABELLA, null, values);
		} else {
			values.put(CURRENCY_NAME, currency);
			values.put(CURRENCY_SYMBOL, symbol);
			db.update(TABELLA, values, ID + "=" + ONLY_ID, null);
		}
	}
	
	public static String [] getCurrency(SQLiteDatabase db){
		String [] ris = new String []{"Euro", "€"};
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + ONLY_ID, null,
				null, null, null);
		c.moveToFirst();
		if (c.getCount() <= 0) {
			c.close();
			return ris;
		}
		String name = c.getString(c.getColumnIndex(CURRENCY_NAME));
		String symb = c.getString(c.getColumnIndex(CURRENCY_SYMBOL));
		ris[0] = name==null?ris[0]:name;
		ris[1] = symb==null?ris[1]:symb;
		return ris;
	}

}
