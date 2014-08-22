package com.dao;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Set;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.dao.mvc.Notifier;
import com.dto.Ricavo;
import com.dto.TagRicavo;

public class RicavoDao {

	private final static String ID = "id";
	private final static String DATA = "data";
	private final static String IMPORTO = "importo";
	private final static String DESCRIZIONE = "descrizione";

	private final static Notifier notifier = new Notifier();

	static {
		notifier.setTag(Notifier.RICAVI_TAG);
	}

	public static final String TABELLA = "ricavi";
	public static final String[] ALL_COLONNE = new String[] { ID, DATA, DESCRIZIONE, IMPORTO };

	public static void insertRicavo(SQLiteDatabase db, Ricavo r) {
		ContentValues v = new ContentValues();
		v.put(ID, r.getId());
		v.put(DATA, r.getData());
		v.put(DESCRIZIONE, r.getDescrizione());
		v.put(IMPORTO, r.getImporto());
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
	public static Collection<Ricavo> getAllRicavi(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") DESC");
		Collection<Ricavo> ris = new LinkedList<Ricavo>();
		while (c.moveToNext()) {
			Ricavo r = new Ricavo();
			r.setId(c.getLong(c.getColumnIndex(ID)));
			r.setData(c.getString(c.getColumnIndex(DATA)));
			r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			Set<TagRicavo> tags = new HashSet<TagRicavo>();
			Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 0);
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
		boolean deleteTag = TagRicavoDao.deleteFromRicavo(db, id, 0);
		delete = delete && deleteTag;
		if (delete) {
			notifier.setChanged();
			notifier.notifyObservers(false);
		}
		return delete;
	}

	public static Collection<Ricavo> getMostRecentRicavo(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") DESC", "10");
		Collection<Ricavo> ris = new LinkedList<Ricavo>();
		while (c.moveToNext()) {
			Ricavo r = new Ricavo();
			r.setId(c.getLong(c.getColumnIndex(ID)));
			r.setData(c.getString(c.getColumnIndex(DATA)));
			r.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			r.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			Set<TagRicavo> tags = new HashSet<TagRicavo>();
			Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 0);
			while (c1.moveToNext()) {
				TagRicavo tag = new TagRicavo();
				tag.setId(c1.getLong(0));
				tag.setRicavo(r);
				tag.setValore(c1.getString(2));
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
	public static boolean updateRicavo(SQLiteDatabase db, Ricavo r) {
		ContentValues v = new ContentValues();
		v.put(DATA, r.getData());
		v.put(DESCRIZIONE, r.getDescrizione());
		v.put(IMPORTO, r.getImporto());

		boolean update = db.update(TABELLA, v, ID + "=" + r.getId(), null) > 0;

		boolean deleteTag = TagRicavoDao.deleteFromRicavo(db, r.getId(), 0);
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
	public static Collection<Ricavo> filterRicavi(SQLiteDatabase db,
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
		Cursor c = db.query(TABELLA, ALL_COLONNE, where.toString(), null, null,
				null, "date(" + DATA + ") DESC", null);
		Collection<Ricavo> ris = new LinkedList<Ricavo>();
		while (c.moveToNext()) {
			Ricavo r = new Ricavo();
			r.setId(c.getLong(c.getColumnIndex(ID)));

			boolean add = true;

			if (tagRichiesti != null && !tagRichiesti.isEmpty()) {

				// join con tagSpesa
				Cursor c1 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 0);
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
				Set<TagRicavo> tags = new HashSet<TagRicavo>();
				Cursor c2 = TagRicavoDao.getAllTagFromRicavo(db, r.getId(), 0);
				while (c2.moveToNext()) {
					TagRicavo tag = new TagRicavo();
					tag.setId(c2.getLong(c2.getColumnIndex(TagSpesaDao.ID)));
					tag.setRicavo(r);
					tag.setValore(c2.getString(c2.getColumnIndex(TagSpesaDao.VALORE)));
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
	
	public static void clearNoNotify(SQLiteDatabase db){
		db.delete(TABELLA, null, null);
		TagRicavoDao.clear(db);
	}

	public static void clear(SQLiteDatabase db) {
		db.delete(TABELLA, null, null);
		TagRicavoDao.clear(db);
		notifier.setChanged();
		notifier.notifyObservers(false);
	}
	
	public static int getCount(SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT COUNT(*) FROM "+TABELLA, null);
		c.moveToFirst();
		int ris = c.getInt(0);
		c.close();
		return ris;
	}

	public static void insertAll(SQLiteDatabase db, Collection<Ricavo> ricavi) {
		for (Ricavo r : ricavi) {
			ContentValues v = new ContentValues();
			v.put(ID, r.getId());
			v.put(DATA, r.getData());
			v.put(DESCRIZIONE, r.getDescrizione());
			v.put(IMPORTO, r.getImporto());
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
