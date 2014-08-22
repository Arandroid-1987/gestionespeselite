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
import com.dto.RicavoProgrammato;
import com.dto.TagRicavo;

public class RicavoProgrammatoDao {

	private final static String ID = "id";
	private final static String DATA = "data";
	private final static String IMPORTO = "importo";
	private final static String RIPETI_FRA = "ripetifra";
	private final static String DESCRIZIONE = "descrizione";

	private final static Notifier notifier = new Notifier();

	static {
		notifier.setTag(Notifier.RICAVI_TAG);
	}

	public static final String TABELLA = "ricaviprogrammati";
	public static final String[] ALL_COLONNE = new String[] { ID, DATA, DESCRIZIONE,
			IMPORTO, RIPETI_FRA };

	public static void insertRicavo(SQLiteDatabase db, RicavoProgrammato r) {
		ContentValues v = new ContentValues();
		v.put(ID, r.getId());
		v.put(DATA, r.getData());
		v.put(DESCRIZIONE, r.getDescrizione());
		v.put(IMPORTO, r.getImporto());
		v.put(RIPETI_FRA, r.getRipetiFra());
		db.insert(TABELLA, null, v);

		Collection<TagRicavo> tags = r.getTags();
		if (tags != null) {
			for (TagRicavo tag : tags) {
				TagRicavoDao.insertTagRicavo(db, tag);
			}
		}

		notifier.setChanged();
		notifier.notifyObservers(false);
	}

	/**
	 * Ritorna un cursore che punta a tutti le spese contenute nel DB
	 * 
	 * @param db
	 * @return
	 */
	public static Collection<RicavoProgrammato> getAllRicavi(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") ASC");
		Collection<RicavoProgrammato> ris = new LinkedList<RicavoProgrammato>();
		while (c.moveToNext()) {
			RicavoProgrammato r = new RicavoProgrammato();
			r.setId(c.getLong(c.getColumnIndex(ID)));
			r.setData(c.getString(c.getColumnIndex(DATA)));
			r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			r.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagRicavo> tags = new HashSet<TagRicavo>();
			Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 1);
			while (c1.moveToNext()) {
				TagRicavo tag = new TagRicavo();
				tag.setId(c1.getLong(c1.getColumnIndex(TagRicavoDao.ID)));
				tag.setRicavo(r);
				tag.setValore(c1.getString(c1.getColumnIndex(TagRicavoDao.VALORE)));
				tags.add(tag);
			}
			c1.close();
			r.setTags(tags);
			ris.add(r);
		}
		c.close();
		return ris;
	}

	public static Cursor getTags(SQLiteDatabase db) {
		return TagRicavoDao.getAllUniqueTagRicavo(db);
	}

	// da aggiornare con i tag
	public static boolean deleteRicavo(SQLiteDatabase db, long id) {
		boolean delete = db.delete(TABELLA, ID + "=" + id, null) > 0;
		boolean deleteTag = TagRicavoDao.deleteFromRicavo(db, id, 1);
		delete = delete && deleteTag;
		if (delete) {
			notifier.setChanged();
			notifier.notifyObservers(false);
		}
		return delete;
	}

	public static Collection<RicavoProgrammato> getMostRecentRicavo(
			SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") ASC", "10");
		Collection<RicavoProgrammato> ris = new LinkedList<RicavoProgrammato>();
		while (c.moveToNext()) {
			RicavoProgrammato r = new RicavoProgrammato();
			r.setId(c.getLong(c.getColumnIndex(ID)));
			r.setData(c.getString(c.getColumnIndex(DATA)));
			r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			r.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagRicavo> tags = new HashSet<TagRicavo>();
			Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 1);
			while (c1.moveToNext()) {
				TagRicavo tag = new TagRicavo();
				tag.setId(c1.getLong(c1.getColumnIndex(TagRicavoDao.ID)));
				tag.setRicavo(r);
				tag.setValore(c1.getString(c1.getColumnIndex(TagRicavoDao.VALORE)));
				tags.add(tag);
			}
			c1.close();
			r.setTags(tags);
			ris.add(r);
		}
		c.close();
		return ris;
	}

	// da aggiornare con i tag
	public static boolean updateRicavo(SQLiteDatabase db, RicavoProgrammato r) {
		ContentValues v = new ContentValues();
		v.put(DATA, r.getData());
		v.put(DESCRIZIONE, r.getDescrizione());
		v.put(IMPORTO, r.getImporto());
		v.put(RIPETI_FRA, r.getRipetiFra());

		boolean update = db.update(TABELLA, v, ID + "=" + r.getId(), null) > 0;

		boolean deleteTag = TagRicavoDao.deleteFromRicavo(db, r.getId(), 1);
		Collection<TagRicavo> tags = r.getTags();
		if (tags != null) {
			for (TagRicavo tag : tags) {
				TagRicavoDao.insertTagRicavo(db, tag);
			}
		}
		update = update && deleteTag;

		if (update) {
			notifier.setChanged();
			notifier.notifyObservers(false);
		}

		return update;
	}

	// da aggiornare con i tag
	public static Collection<RicavoProgrammato> filterRicavi(SQLiteDatabase db,
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
		Collection<RicavoProgrammato> ris = new LinkedList<RicavoProgrammato>();
		while (c.moveToNext()) {
			RicavoProgrammato r = new RicavoProgrammato();
			r.setId(c.getLong(c.getColumnIndex(ID)));

			boolean add = true;

			if (tagRichiesti != null && !tagRichiesti.isEmpty()) {

				// join con tagSpesa
				Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 1);
				for (String tag : tagRichiesti) {
					c1.moveToPosition(-1);
					boolean tagFound = false;
					while (c1.moveToNext() && add) {
						String valore = c1.getString(c1.getColumnIndex(TagRicavoDao.VALORE)).toLowerCase(
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
				r.setData(c.getString(c.getColumnIndex(DATA)));
				r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
				r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
				r.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
				Set<TagRicavo> tags = new HashSet<TagRicavo>();
				Cursor c2 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 1);
				while (c2.moveToNext()) {
					TagRicavo tag = new TagRicavo();
					tag.setId(c2.getLong(c2.getColumnIndex(TagRicavoDao.ID)));
					tag.setRicavo(r);
					tag.setValore(c2.getString(c2.getColumnIndex(TagRicavoDao.VALORE)));
					tags.add(tag);
				}
				c2.close();
				r.setTags(tags);
				ris.add(r);
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

	public static RicavoProgrammato getRicavo(SQLiteDatabase db, long id) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, ID + "=" + id, null, null,
				null, null);
		RicavoProgrammato r = null;
		if (c.moveToNext()) {
			r = new RicavoProgrammato();
			r.setId(id);
			r.setData(c.getString(c.getColumnIndex(DATA)));
			r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			r.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagRicavo> tags = new HashSet<TagRicavo>();
			Cursor c2 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 1);
			while (c2.moveToNext()) {
				TagRicavo tag = new TagRicavo();
				tag.setId(c2.getLong(c2.getColumnIndex(TagRicavoDao.ID)));
				tag.setRicavo(r);
				tag.setValore(c2.getString(c2.getColumnIndex(TagRicavoDao.VALORE)));
				tags.add(tag);
			}
			c2.close();
			r.setTags(tags);
		}
		c.close();
		return r;
	}

	public static Collection<RicavoProgrammato> getRicaviScaduti(
			SQLiteDatabase db) {
		Collection<RicavoProgrammato> ris = new LinkedList<RicavoProgrammato>();
		Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd",
				Locale.ITALIAN);
		String date = sdf.format(now);

		String where = DATA + " <= \"" + date + "\"";

		Cursor c = db.query(TABELLA, ALL_COLONNE, where, null, null, null,
				null, null);
		while (c.moveToNext()) {
			RicavoProgrammato r = new RicavoProgrammato();
			r.setId(c.getLong(c.getColumnIndex(ID)));
			r.setData(c.getString(c.getColumnIndex(DATA)));
			r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			r.setRipetiFra(c.getInt(c.getColumnIndex(RIPETI_FRA)));
			Set<TagRicavo> tags = new HashSet<TagRicavo>();
			Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 1);
			while (c1.moveToNext()) {
				TagRicavo tag = new TagRicavo();
				tag.setId(c1.getLong(c1.getColumnIndex(TagRicavoDao.ID)));
				tag.setRicavo(r);
				tag.setValore(c1.getString(c1.getColumnIndex(TagRicavoDao.VALORE)));
				tags.add(tag);
			}
			c1.close();
			r.setTags(tags);
			ris.add(r);
		}
		c.close();
		return ris;
	}

	public static void insertAll(SQLiteDatabase db,
			Collection<RicavoProgrammato> ricaviProgrammati) {
		for (RicavoProgrammato r : ricaviProgrammati) {
			ContentValues v = new ContentValues();
			v.put(ID, r.getId());
			v.put(DATA, r.getData());
			v.put(DESCRIZIONE, r.getDescrizione());
			v.put(IMPORTO, r.getImporto());
			v.put(RIPETI_FRA, r.getRipetiFra());
			db.insert(TABELLA, null, v);

			Collection<TagRicavo> tags = r.getTags();
			if (tags != null) {
				for (TagRicavo tag : tags) {
					TagRicavoDao.insertTagRicavo(db, tag);
				}
			}
		}
	}

}
