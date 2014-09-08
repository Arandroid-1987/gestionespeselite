package utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.dao.RicavoDao;
import com.dao.SpesaDao;
import com.db.DatabaseHandler;
import com.dto.BilancioMese;
import com.dto.Ricavo;
import com.dto.Spesa;

public class BilancioMeseCalculator {

	public static BilancioMese get(int mese, int anno, Activity context) {
		BilancioMese bm = new BilancioMese();
		DatabaseHandler handler = DatabaseHandler.getInstance(context);
		SQLiteDatabase db = handler.getReadableDatabase();
		
		Calendar c = Calendar.getInstance(Locale.getDefault());
		c.set(Calendar.YEAR, anno);
		c.set(Calendar.MONTH, mese);
		c.set(Calendar.DAY_OF_MONTH, 1);
		
		Date d = c.getTime();
		String startDate = DateUtils.getDate(d);
		c.add(Calendar.MONTH, 1);
		c.add(Calendar.DAY_OF_YEAR, -1);
		
		d = c.getTime();
		String endDate = DateUtils.getDate(d);
		
		String month = new SimpleDateFormat("MMMM", Locale.getDefault()).format(d);
		String year = new SimpleDateFormat("yyyy", Locale.getDefault()).format(d);
		bm.setAnno(year);
		bm.setMese(month);
		bm.setMeseNumero(mese);
		bm.setAnnoNumero(anno);
		
		Collection<Spesa> spese = SpesaDao.filterSpese(db, startDate, endDate,
				null, -1, -1);
		Collection<Ricavo> ricavi = RicavoDao.filterRicavi(db, startDate,
				endDate, null, -1, -1);
		double ricavo = 0;
		double spesa = 0;
		for (Ricavo r : ricavi) {
			ricavo += r.getImporto();
		}
		for (Spesa s : spese) {
			spesa += s.getImporto();
		}
		bm.setRicavi(ricavo);
		bm.setSpese(spesa);
		double bilancio = ricavo - spesa;
		bm.setBilancio(bilancio);
		db.close();
		return bm;
	}

}
