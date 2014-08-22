package com.dao;

import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dao.mvc.Notifier;
import com.dto.SpesaProgrammata;
import com.dto.TagSpesa;

public class SpesaProgrammataDao {

	private final static Notifier notifier = new Notifier();

	private final static String ID = "id";
	private final static String DATA = "data";
	private final static String IMPORTO = "importo";
	private final static String DESCRIZIONE = "descrizione";
	private final static String RIPETI_FRA = "ripetifra";

	static {
		notifier.setTag(Notifier.SPESE_TAG);
	}

	public static final String TABELLA = "speseprogrammate";
	public static final String[] ALL_COLONNE = new String[] { ID, DATA, DESCRIZIONE,
			IMPORTO, RIPETI_FRA };

	public static void insertSpesa(SQLiteDatabase db, SpesaProgrammata s) {
		ContentValues v = new ContentValues();
		v.put(ID, s.getId());
		v.put(DATA, s.getData());
		v.put(DESCRIZIONE, s.getDescrizione());
		v.put(IMPORTO, s.getImporto());
		v.put(RIPETI_FRA, s.getRipetiFra());
		db.insert(TABELLA, null, v);

		Collection<TagSpesa> tags = s.getTags();
		if (tags != null) {
			for (TagSpesa tag : tags) {
				TagSpesaDao.insertTagSpesa(db, tag);
			}
		}

		notifier.setChanged();
		notifier.notifyObservers(true);
	}

	/**
	 * Ritorna un cursore che punta a tutti le spese contenute nel DB
	 * 
	 * @param db
	 * @return
	 */
	public static Collection<SpesaProgrammata> getAllSpese(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") ASC");
		Collection<SpesaProgrammata> ris = new LinkedList<SpesaProgrammata>();
		while (c.moveToNext()) {
			SpesaProgrammata s = new SpesaProgrammata();
			s.setId(c.getLong(c.getColumnIndex(ID)));
			s.setData(c.getString(c.getColumnIndex(DATA)));
			s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			s.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagSpesa> tags = new HashSet<TagSpesa>();
			Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 1);
			while (c1.moveToNext()) {
				TagSpesa tag = new TagSpesa();
				tag.setId(c1.getLong(c1.getColumnIndex(TagSpesaDao.ID)));
				tag.setSpesa(s);
				tag.setValore(c1.getString(c1.getColumnIndex(TagSpesaDao.VALORE)));
				tags.add(tag);
			}
			c1.close();
			s.setTags(tags);
			ris.add(s);
		}
		c.close();
		return ris;
	}

	public static Cursor getTags(SQLiteDatabase db) {
		return TagSpesaDao.getAllUniqueTagSpesa(db);
	}

	// da aggiornare con i tag
	public static boolean deleteSpesa(SQLiteDatabase db, long id) {
		boolean delete = db.delete(TABELLA, ID + "=" + id, null) > 0;
		boolean deleteTag = TagSpesaDao.deleteFromSpesa(db, id, 1);
		delete = delete && deleteTag;
		if (delete) {
			notifier.setChanged();
			notifier.notifyObservers(true);
		}
		return delete;
	}

	public static Collection<SpesaProgrammata> getMostRecentSpesa(
			SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") ASC", "10");
		Collection<SpesaProgrammata> ris = new LinkedList<SpesaProgrammata>();
		while (c.moveToNext()) {
			SpesaProgrammata s = new SpesaProgrammata();
			s.setId(c.getLong(c.getColumnIndex(ID)));
			s.setData(c.getString(c.getColumnIndex(DATA)));
			s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			s.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagSpesa> tags = new HashSet<TagSpesa>();
			Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 1);
			while (c1.moveToNext()) {
				TagSpesa tag = new TagSpesa();
				tag.setId(c1.getLong(c1.getColumnIndex(TagSpesaDao.ID)));
				tag.setSpesa(s);
				tag.setValore(c1.getString(c1.getColumnIndex(TagSpesaDao.VALORE)));
				tags.add(tag);
			}
			c1.close();
			s.setTags(tags);
			ris.add(s);
		}
		c.close();
		return ris;
	}

	// da aggiornare con i tag
	public static boolean updateSpesa(SQLiteDatabase db, SpesaProgrammata s) {
		ContentValues v = new ContentValues();
		v.put(DATA, s.getData());
		v.put(DESCRIZIONE, s.getDescrizione());
		v.put(IMPORTO, s.getImporto());
		v.put(RIPETI_FRA, s.getRipetiFra());

		boolean update = db.update(TABELLA, v, ID + "=" + s.getId(), null) > 0;

		boolean deleteTag = TagSpesaDao.deleteFromSpesa(db, s.getId(), 1);
		Collection<TagSpesa> tags = s.getTags();
		if (tags != null) {
			for (TagSpesa tag : tags) {
				TagSpesaDao.insertTagSpesa(db, tag);
			}
		}
		update = update && deleteTag;
		if (update) {
			notifier.setChanged();
			notifier.notifyObservers(true);
		}
		return update;
	}

	// da aggiornare con i tag
	public static Collection<SpesaProgrammata> filterSpese(SQLiteDatabase db,
			String startDate, String endDate, Collection<String> tagRichiesti,
			double minImp, double maxImp) {
		StringBuilder where = new StringBuilder();
		where.append(DATA).append(" BETWEEN \"").append(startDate);
		where.append("\" AND \"").append(endDate).append("\"");
		// where.append(" AND ").append(CAUSALE).append(" LIKE \"%")
		// .append(causale).append("%\"");
		if (minImp > 0) {
			where.append(" AND ").append(IMPORTO).append(" >= ").append(minImp);
		}
		if (maxImp > 0) {
			where.append(" AND ").append(IMPORTO).append(" <= ").append(maxImp);
		}
		System.out.println(where);
		Cursor c = db.query(TABELLA, ALL_COLONNE, where.toString(), null, null,
				null, "date(" + DATA + ") DESC", null);
		Collection<SpesaProgrammata> ris = new LinkedList<SpesaProgrammata>();
		while (c.moveToNext()) {
			SpesaProgrammata s = new SpesaProgrammata();
			s.setId(c.getLong(c.getColumnIndex(ID)));

			boolean add = true;

			if (tagRichiesti != null && !tagRichiesti.isEmpty()) {

				// join con tagSpesa
				Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 1);
				for (String tag : tagRichiesti) {
					c1.moveToPosition(-1);
					boolean tagFound = false;
					while (c1.moveToNext() && add) {
						String valore = c1.getString(c1.getColumnIndex(TagSpesaDao.VALORE)).toLowerCase(
								Locale.ITALIAN);
						if (valore.contains(tag.toLowerCase(Locale.ITALIAN))) {
							tagFound = true;
							break;
						}
					}
					if (!tagFound) {
						add = false;
						break;
					}
				}
				c1.close();
			} else {
				add = true;
			}

			if (add) {
				s.setData(c.getString(c.getColumnIndex(DATA)));
				s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
				s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
				s.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
				Set<TagSpesa> tags = new HashSet<TagSpesa>();
				Cursor c2 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 1);
				while (c2.moveToNext()) {
					TagSpesa tag = new TagSpesa();
					tag.setId(c2.getLong(c2.getColumnIndex(TagSpesaDao.ID)));
					tag.setSpesa(s);
					tag.setValore(c2.getString(c2.getColumnIndex(TagSpesaDao.VALORE)));
					tags.add(tag);
				}
				c2.close();
				s.setTags(tags);
				ris.add(s);
			}
		}
		c.close();
		return ris;
	}

	public static Notifier getNotifier() {
		return notifier;
	}

	public static void clear(SQLiteDatabase db) {
		db.delete(TABELLA, null, null);
	}

	public static SpesaProgrammata getSpesa(SQLiteDatabase db, long id) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + id, null, null,
				null, null);
		SpesaProgrammata s = null;
		if (c.moveToNext()) {
			s = new SpesaProgrammata();
			s.setId(id);
			s.setData(c.getString(c.getColumnIndex(ID)));
			s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			s.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagSpesa> tags = new HashSet<TagSpesa>();
			Cursor c2 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 1);
			while (c2.moveToNext()) {
				TagSpesa tag = new TagSpesa();
				tag.setId(c2.getLong(c2.getColumnIndex(TagSpesaDao.ID)));
				tag.setSpesa(s);
				tag.setValore(c2.getString(c2.getColumnIndex(TagSpesaDao.VALORE)));
				tags.add(tag);
			}
			c2.close();
			s.setTags(tags);
		}
		c.close();
		return s;
	}

	public static Collection<SpesaProgrammata> getSpeseScadute(SQLiteDatabase db) {

		Collection<SpesaProgrammata> ris = new LinkedList<SpesaProgrammata>();
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.ITALIAN);
		String date = sdf.format(now);

		String where = DATA + " <= \"" + date + "\"";

		Cursor c = db.query(TABELLA, ALL_COLONNE, where, null, null, null,
				null, null);
		while (c.moveToNext()) {
			SpesaProgrammata s = new SpesaProgrammata();
			s.setId(c.getLong(c.getColumnIndex(ID)));
			s.setData(c.getString(c.getColumnIndex(DATA)));
			s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			s.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagSpesa> tags = new HashSet<TagSpesa>();
			Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 1);
			while (c1.moveToNext()) {
				TagSpesa tag = new TagSpesa();
				tag.setId(c1.getLong(c1.getColumnIndex(TagSpesaDao.ID)));
				tag.setSpesa(s);
				tag.setValore(c1.getString(c1.getColumnIndex(TagSpesaDao.VALORE)));
				tags.add(tag);
			}
			c1.close();
			s.setTags(tags);
			ris.add(s);
		}
		c.close();
		return ris;
	}

	public static void insertAll(SQLiteDatabase db,
			Collection<SpesaProgrammata> spese) {
		for (SpesaProgrammata s : spese) {
			ContentValues v = new ContentValues();
			v.put(ID, s.getId());
			v.put(DATA, s.getData());
			v.put(DESCRIZIONE, s.getDescrizione());
			v.put(IMPORTO, s.getImporto());
			v.put(RIPETI_FRA, s.getRipetiFra());
			db.insert(TABELLA, null, v);

			Collection<TagSpesa> tags = s.getTags();
			if (tags != null) {
				for (TagSpesa tag : tags) {
					TagSpesaDao.insertTagSpesa(db, tag);
				}
			}
		}
	}
}
