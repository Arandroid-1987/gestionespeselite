package com.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.arandroid.bplite.R;
import com.dao.RicavoDao;
import com.dao.RicavoProgrammatoDao;
import com.dao.SpesaDao;
import com.dao.SpesaProgrammataDao;
import com.dao.TagRicavoDao;
import com.dao.TagSpesaDao;

public class DatabaseHandler extends SQLiteOpenHelper {

	private Context context;

	private static final String KEY_ID = "id";
	private static final String KEY_DATE = "data";
	private static final String KEY_IMPORTO = "importo";
	private static final String KEY_DESCRIZIONE = "descrizione";
	private static final String KEY_RIPETI_FRA = "ripetifra";

	private static final String KEY_USE_PASSWORD = "usepw";
	private static final String KEY_PASSWORD = "pw";
	private static final String KEY_EMAIL = "email";
	private static final String KEY_TUTORIAL_ON = "tutorial";
	private static final String KEY_USE_BACKUP = "useBackup";
	private static final String KEY_REPEAT = "repeat";
	private static final String KEY_USE_SAME_FILE = "useSameFile";
	private static final String KEY_LAST_BACKUP = "lastBackup";
	private static final String KEY_CURRENCY_NAME = "currencyName";
	private static final String KEY_CURRENCY_SYMBOL = "currencySymbol";

	private static final String KEY_SPESA = "spesa";
	private static final String KEY_VALORE = "valore";
	private static final String KEY_RICAVO = "ricavo";
	private static final String KEY_SPESA_PROGRAMMATA = "spesa_programmata";
	private static final String KEY_RICAVO_PROGRAMMATO = "ricavo_programmato";

	// must be 4
	private static final int DATABASE_VERSION = 6;

	// Database Name
	private static final String DATABASE_NAME = "gestioneSpese";
	private static final String FILE_DIR = "gsdb";
	private static File AppDir;

	private static final String TABLE_SPESE = "spese";
	private static final String TABLE_SPESE_PROGRAMMATE = "speseprogrammate";
	private static final String TABLE_RICAVI = "ricavi";
	private static final String TABLE_RICAVI_PROGRAMMATI = "ricaviprogrammati";
	private static final String TABLE_PASSWORD = "passwordtable";
	private static final String TABLE_TAG_SPESE = "tagSpese";
	private static final String TABLE_TAG_RICAVI = "tagRicavi";

	private static final String CREATE_SPESE_TABLE = "CREATE TABLE "
			+ TABLE_SPESE + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_DATE + " TEXT,"
			+ KEY_DESCRIZIONE + " TEXT," + KEY_IMPORTO + " REAL" + ")";

	private static final String CREATE_SPESE_PROGRAMMATE_TABLE = "CREATE TABLE "
			+ TABLE_SPESE_PROGRAMMATE
			+ "("
			+ KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_DATE
			+ " TEXT,"
			+ KEY_DESCRIZIONE
			+ " TEXT,"
			+ KEY_IMPORTO
			+ " REAL,"
			+ KEY_RIPETI_FRA + " INTEGER" + ")";

	private static final String CREATE_RICAVI_TABLE = "CREATE TABLE "
			+ TABLE_RICAVI + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_DATE + " TEXT,"
			+ KEY_DESCRIZIONE + " TEXT," + KEY_IMPORTO + " REAL" + ")";

	private static final String CREATE_RICAVI_PROGRAMMATI_TABLE = "CREATE TABLE "
			+ TABLE_RICAVI_PROGRAMMATI
			+ "("
			+ KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ KEY_DATE
			+ " TEXT,"
			+ KEY_DESCRIZIONE
			+ " TEXT,"
			+ KEY_IMPORTO
			+ " REAL,"
			+ KEY_RIPETI_FRA + " INTEGER" + ")";

	private static final String CREATE_PASSWORD_TABLE = "CREATE TABLE "
			+ TABLE_PASSWORD + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_EMAIL + " TEXT,"
			+ KEY_USE_PASSWORD + " INTEGER," + KEY_PASSWORD + " TEXT,"
			+ KEY_TUTORIAL_ON + " INTEGER," + KEY_USE_BACKUP + " INTEGER,"
			+ KEY_REPEAT + " INTEGER," + KEY_USE_SAME_FILE + " INTEGER,"
			+ KEY_LAST_BACKUP + " INTEGER," + KEY_CURRENCY_NAME + " TEXT,"
			+ KEY_CURRENCY_SYMBOL + " TEXT" + ")";

	private static final String CREATE_TAG_SPESE_TABLE = "CREATE TABLE "
			+ TABLE_TAG_SPESE + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_SPESA + " INTEGER,"
			+ KEY_VALORE + " TEXT," + KEY_SPESA_PROGRAMMATA + " INTEGER" + ")";

	private static final String CREATE_TAG_RICAVI_TABLE = "CREATE TABLE "
			+ TABLE_TAG_RICAVI + "(" + KEY_ID
			+ " INTEGER PRIMARY KEY AUTOINCREMENT," + KEY_RICAVO + " INTEGER,"
			+ KEY_VALORE + " TEXT," + KEY_RICAVO_PROGRAMMATO + " INTEGER" + ")";

//	private static final String ADD_COLUMN_BACKUP = "ALTER TABLE "
//			+ TABLE_PASSWORD + " ADD COLUMN " + KEY_USE_BACKUP + " INTEGER";
//	private static final String ADD_COLUMN_REPEAT = "ALTER TABLE "
//			+ TABLE_PASSWORD + " ADD COLUMN " + KEY_REPEAT + " INTEGER";
//	private static final String ADD_COLUMN_SAME_FILE = "ALTER TABLE "
//			+ TABLE_PASSWORD + " ADD COLUMN " + KEY_USE_SAME_FILE + " INTEGER";
//	private static final String ADD_COLUMN_LAST_BACKUP = "ALTER TABLE "
//			+ TABLE_PASSWORD + " ADD COLUMN " + KEY_LAST_BACKUP + " INTEGER";
//	private static final String ADD_COLUMN_CURRENCY_NAME = "ALTER TABLE "
//			+ TABLE_PASSWORD + " ADD COLUMN " + KEY_CURRENCY_NAME + " TEXT";
//	private static final String ADD_COLUMN_CURRENCY_SYMBOL = "ALTER TABLE "
//			+ TABLE_PASSWORD + " ADD COLUMN " + KEY_CURRENCY_SYMBOL + " TEXT";
	private static final String ADD_COLUMN_DESCRIZIONE_SPESE = "ALTER TABLE "
			+ TABLE_SPESE + " ADD COLUMN " + KEY_DESCRIZIONE + " TEXT";
	private static final String ADD_COLUMN_DESCRIZIONE_RICAVI = "ALTER TABLE "
			+ TABLE_RICAVI + " ADD COLUMN " + KEY_DESCRIZIONE + " TEXT";
	private static final String ADD_COLUMN_DESCRIZIONE_SPESE_PROGRAMMATE = "ALTER TABLE "
			+ TABLE_SPESE_PROGRAMMATE + " ADD COLUMN " + KEY_DESCRIZIONE + " TEXT";
	private static final String ADD_COLUMN_DESCRIZIONE_RICAVI_PROGRAMMATI = "ALTER TABLE "
			+ TABLE_RICAVI_PROGRAMMATI + " ADD COLUMN " + KEY_DESCRIZIONE + " TEXT";

	private final String _dirName = "gestionespese";
	private final String _backupFileName = _dirName + "/backup.gdb";

	// Step1 : Checked accessiblity on sd card
	public static boolean doesSDCardAccessible() {
		try {
			return (Environment.getExternalStorageState()
					.equals(Environment.MEDIA_MOUNTED));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	// Step2 : create driectory on SD Card
	// APP_DIR : your PackageName
	public static void createAndInitAppDir() {
		try {
			if (doesSDCardAccessible()) {
				AppDir = new File(Environment.getExternalStorageDirectory(),
						FILE_DIR + File.separator);
				if (!AppDir.exists()) {
					AppDir.mkdirs();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Step 3 : Create Database on sdcard
	// APP_DIR : your PackageName
	// DATABASE_VERSION : give Database Version
	// DATABASE_NAME : your Database Name
	public void initDB() {
		try {

			// Using SQLiteHelper Class Created Database
			// sqliteHelper = new
			// SQLiteHelper(Application.this,AppDir.getAbsolutePath()+"/"+DATABASE_NAME,
			// null, DATABASE_VERSION);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.context = context;
	}

	private DatabaseHandler(Context context, boolean sd) {
		super(context, Environment.getExternalStorageDirectory()
				+ File.separator + FILE_DIR + File.separator + DATABASE_NAME,
				null, DATABASE_VERSION);
		this.context = context;
	}

	public static DatabaseHandler getInstance(Context context) {
//		boolean sd = doesSDCardAccessible();
//		if (sd) {
//			createAndInitAppDir();
//			return new DatabaseHandler(context, sd);
//		} else {
//			return new DatabaseHandler(context);
//		}
		return new DatabaseHandler(context);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_SPESE_TABLE);
		db.execSQL(CREATE_SPESE_PROGRAMMATE_TABLE);
		db.execSQL(CREATE_RICAVI_TABLE);
		db.execSQL(CREATE_RICAVI_PROGRAMMATI_TABLE);
		db.execSQL(CREATE_PASSWORD_TABLE);
		db.execSQL(CREATE_TAG_SPESE_TABLE);
		db.execSQL(CREATE_TAG_RICAVI_TABLE);
		TagSpesaDao.initDefaultTags(db, context);
		TagRicavoDao.initDefaultTags(db, context);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//		if (oldVersion == 1) {
//			db.execSQL(ADD_COLUMN_BACKUP);
//			db.execSQL(ADD_COLUMN_REPEAT);
//			db.execSQL(ADD_COLUMN_SAME_FILE);
//			db.execSQL(ADD_COLUMN_LAST_BACKUP);
//		} else if (oldVersion == 3) {
//			db.execSQL(ADD_COLUMN_LAST_BACKUP);
//		}
//		if (newVersion == 5) {
//			db.execSQL(ADD_COLUMN_CURRENCY_NAME);
//			db.execSQL(ADD_COLUMN_CURRENCY_SYMBOL);
//		} else if (newVersion == 6) {
//			db.execSQL(ADD_COLUMN_DESCRIZIONE_SPESE);
//			db.execSQL(ADD_COLUMN_DESCRIZIONE_RICAVI);
//			db.execSQL(ADD_COLUMN_DESCRIZIONE_SPESE_PROGRAMMATE);
//			db.execSQL(ADD_COLUMN_DESCRIZIONE_RICAVI_PROGRAMMATI);
//		}
		 db.execSQL("DROP TABLE " + TABLE_PASSWORD);
		 db.execSQL(CREATE_PASSWORD_TABLE);
		 if (newVersion == 6) {
			 db.execSQL(ADD_COLUMN_DESCRIZIONE_RICAVI);
			 db.execSQL(ADD_COLUMN_DESCRIZIONE_SPESE);
			 db.execSQL(ADD_COLUMN_DESCRIZIONE_RICAVI_PROGRAMMATI);
			 db.execSQL(ADD_COLUMN_DESCRIZIONE_SPESE_PROGRAMMATE);
		 }
//		 db.execSQL("DROP TABLE " + TABLE_RICAVI);
//		 db.execSQL("DROP TABLE " + TABLE_RICAVI_PROGRAMMATI);
//		 db.execSQL("DROP TABLE " + TABLE_SPESE);
//		 db.execSQL("DROP TABLE " + TABLE_SPESE_PROGRAMMATE);
//		 db.execSQL("DROP TABLE " + TABLE_TAG_RICAVI);
//		 db.execSQL("DROP TABLE " + TABLE_TAG_SPESE);
//		 onCreate(db);
//		 }
//		 else if(newVersion >= 7){
//		
//		 }
	}

	public void backup(SQLiteDatabase db, boolean useSameFile) {
		DBStateTask task = new DBStateTask(false, db, null, this, context,
				_dirName, _backupFileName, useSameFile);
		task.execute();
	}

	public void backupWhole(SQLiteDatabase db, boolean useSameFile) {
		try {
			File sd = Environment.getExternalStorageDirectory();
			if (sd.canWrite()) {
				File directory = new File(sd, _dirName);
				if (!directory.exists())
					directory.mkdirs();
				File currentDB = new File(db.getPath());// path del db su
														// telefono
				File backupDB = null;
				if (useSameFile) {
					backupDB = new File(sd, _backupFileName);
				} else {
					SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy",
							Locale.ITALIAN);
					String today = sdf.format(new Date());
					backupDB = new File(sd, _dirName + "/backup_" + today
							+ ".gdb");
				}
				FileInputStream fis = new FileInputStream(currentDB);
				FileChannel src = fis.getChannel();
				FileOutputStream fos = new FileOutputStream(backupDB);
				FileChannel dst = fos.getChannel();
				dst.transferFrom(src, 0, src.size());// trasferiamo il contenuto
				fis.close();
				fos.close();
				src.close();
				dst.close();
				Toast.makeText(context,
						context.getString(R.string.backup_effettuato_in),
						Toast.LENGTH_SHORT).show();
			} else {
				Log.e("Permission denied",
						"Can't write to SD card, add permission");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void backup(SQLiteDatabase db) {
		backup(db, true);
	}

	public void restoreWhole(SQLiteDatabase db, String file_path) {
		try {
			File sd = Environment.getExternalStorageDirectory();
			if (sd.canRead()) {
				File currentDB = new File(db.getPath());// path del db su
														// telefono
				File backupDB = new File(file_path);
				FileInputStream fis = new FileInputStream(backupDB);
				FileChannel src = fis.getChannel();
				FileOutputStream fos = new FileOutputStream(currentDB);
				FileChannel dst = fos.getChannel();
				dst.transferFrom(src, 0, src.size());// trasferiamo il contenuto
				fis.close();
				fos.close();
				src.close();
				dst.close();
				Toast.makeText(context,
						context.getString(R.string.restore_effettuato),
						Toast.LENGTH_LONG).show();
			} else {
				Log.e("Permission denied",
						"Can't read from SD card, add permission");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void restore(SQLiteDatabase db, String file_path) {
		DBStateTask task = new DBStateTask(true, db, file_path, this, context,
				null, null, false);
		task.execute();
	}

	public void clear(SQLiteDatabase db) {
		RicavoDao.clear(db);
		SpesaDao.clear(db);
		RicavoProgrammatoDao.clear(db);
		SpesaProgrammataDao.clear(db);
		TagSpesaDao.initDefaultTags(db, context);
		TagRicavoDao.initDefaultTags(db, context);
	}

	public void reset(SQLiteDatabase db) {
		RicavoDao.clearNoNotify(db);
		SpesaDao.clearNoNotify(db);
		RicavoProgrammatoDao.clear(db);
		SpesaProgrammataDao.clear(db);
	}

}
