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
import com.dto.Spesa;
import com.dto.TagSpesa;

public class SpesaDao {

	private final static Notifier notifier = new Notifier();

	private final static String ID = "id";
	private final static String DATA = "data";
	private final static String IMPORTO = "importo";
	private final static String DESCRIZIONE = "descrizione";

	static {
		notifier.setTag(Notifier.SPESE_TAG);
	}

	public static final String TABELLA = "spese";
	public static final String[] ALL_COLONNE = new String[] { ID, DATA, DESCRIZIONE, IMPORTO };

	public static void insertSpesa(SQLiteDatabase db, Spesa s) {
		ContentValues v = new ContentValues();
		v.put(ID, s.getId());
		v.put(DATA, s.getData());
		v.put(DESCRIZIONE, s.getDescrizione());
		v.put(IMPORTO, s.getImporto());
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
	public static Collection<Spesa> getAllSpese(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") DESC");
		Collection<Spesa> ris = new LinkedList<Spesa>();
		while (c.moveToNext()) {
			Spesa s = new Spesa();
			s.setId(c.getLong(c.getColumnIndex(ID)));
			s.setData(c.getString(c.getColumnIndex(DATA)));
			s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			Set<TagSpesa> tags = new HashSet<TagSpesa>();
			Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 0);
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

	public static boolean deleteSpesa(SQLiteDatabase db, long id) {
		boolean delete = db.delete(TABELLA, ID + "=" + id, null) > 0;
		boolean deleteTag = TagSpesaDao.deleteFromSpesa(db, id, 0);
		delete = delete && deleteTag;
		if (delete) {
			notifier.setChanged();
			notifier.notifyObservers(true);
		}
		return delete;
	}

	public static Collection<Spesa> getMostRecentSpesa(SQLiteDatabase db) {
		Cursor c = db.query(TABELLA, ALL_COLONNE, null, null, null, null,
				"date(" + DATA + ") DESC", "10");
		Collection<Spesa> ris = new LinkedList<Spesa>();
		while (c.moveToNext()) {
			Spesa s = new Spesa();
			s.setId(c.getLong(c.getColumnIndex(ID)));
			s.setData(c.getString(c.getColumnIndex(DATA)));
			s.setDescrizione(c.getString(c.getColumnIndex(DESCRIZIONE)));
			s.setImporto(c.getDouble(c.getColumnIndex(IMPORTO)));
			Set<TagSpesa> tags = new HashSet<TagSpesa>();
			Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 0);
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
	public static boolean updateSpesa(SQLiteDatabase db, Spesa s) {
		ContentValues v = new ContentValues();
		v.put(DATA, s.getData());
		v.put(DESCRIZIONE, s.getDescrizione());
		v.put(IMPORTO, s.getImporto());

		boolean update = db.update(TABELLA, v, ID + "=" + s.getId(), null) > 0;

		boolean deleteTag = TagSpesaDao.deleteFromSpesa(db, s.getId(), 0);
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
	public static Collection<Spesa> filterSpese(SQLiteDatabase db,
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
		Collection<Spesa> ris = new LinkedList<Spesa>();
		while (c.moveToNext()) {
			Spesa s = new Spesa();
			s.setId(c.getLong(c.getColumnIndex(ID)));

			boolean add = true;

			if (tagRichiesti != null && !tagRichiesti.isEmpty()) {

				// join con tagSpesa
				Cursor c1 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 0);
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
				Set<TagSpesa> tags = new HashSet<TagSpesa>();
				Cursor c2 = TagSpesaDao.getAllTagFromSpesa(db, s.getId(), 0);
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
	
	public static void clearNoNotify(SQLiteDatabase db) {
		db.delete(TABELLA, null, null);
		TagSpesaDao.clear(db);
	}

	public static void clear(SQLiteDatabase db) {
		db.delete(TABELLA, null, null);
		TagSpesaDao.clear(db);
		notifier.setChanged();
		notifier.notifyObservers(true);
	}

	public static int getCount(SQLiteDatabase db) {
		Cursor c = db.rawQuery("SELECT COUNT(*) FROM "+TABELLA, null);
		c.moveToFirst();
		int ris = c.getInt(0);
		c.close();
		return ris;
	}

	public static void insertAll(SQLiteDatabase db, Collection<Spesa> spese) {
		for (Spesa s : spese) {
			ContentValues v = new ContentValues();
			v.put(ID, s.getId());
			v.put(DATA, s.getData());
			v.put(DESCRIZIONE, s.getDescrizione());
			v.put(IMPORTO, s.getImporto());
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
