package utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ITALIAN);;

	public static boolean isDate2AfterDate1(int day1, int month1, int year1,
			int day2, int month2, int year2) {
		if (year2 < year1)
			return false;
		if (year2 > year1)
			return true;
		if (month2 < month1)
			return false;
		if (month2 > month1)
			return true;
		if (day2 < day1)
			return false;
		return true;
	}

	public static void main(String[] args) {
		int day1 = 10;
		int month1 = 2;
		int year1 = 2013;

		int day2 = 10;
		int month2 = 3;
		int year2 = 2013;

		System.out.println(isDate2AfterDate1(day1, month1, year1, day2, month2,
				year2));
	}

	public static String getDate(int y, int m, int d) {
		String ys = "" + y;
		String ms = "" + m;
		String ds = "" + d;
		if (m < 10) {
			ms = "0" + m;
		}
		if (d < 10) {
			ds = "0" + d;
		}
		return ys + "-" + ms + "-" + ds;
	}

	public static int getDaysFromString(String periodoRipeti, String[] array,
			Date start) {
		int ris = 0;
		Calendar c = Calendar.getInstance();
		for (int i = 0; i < array.length; i++) {
			String x = array[i];
			if (periodoRipeti.equals(x)) {
				switch (i) {
				case 0:
					ris = -1;
					break;
				case 1:
					ris = 1;
					break;
				case 2:
					ris = 7;
					break;
				case 3:
					c.setTime(start);
					c.add(Calendar.MONTH, 1);
					Date end = c.getTime();
					ris = differenza(start, end);
					break;
				case 4:
					c.setTime(start);
					c.add(Calendar.MONTH, 2);
					end = c.getTime();
					ris = differenza(start, end);
					break;
				case 5:
					c.setTime(start);
					c.add(Calendar.MONTH, 3);
					end = c.getTime();
					ris = differenza(start, end);
					break;
				case 6:
					c.setTime(start);
					c.add(Calendar.MONTH, 6);
					end = c.getTime();
					ris = differenza(start, end);
					break;
				case 7:
					c.setTime(start);
					c.add(Calendar.YEAR, 1);
					end = c.getTime();
					ris = differenza(start, end);
					break;
				case 8:
					c.setTime(start);
					c.add(Calendar.YEAR, 2);
					end = c.getTime();
					ris = differenza(start, end);
					break;
				default:
					break;
				}
			}
		}
		return ris;
	}

	public static int differenza(Date start, Date end) {
		if (end.before(start))
			return -1;
		Calendar c = Calendar.getInstance();
		c.setTime(start);
		Date startTmp = c.getTime();
		int count = 0;
		while (!startTmp.equals(end)) {
			c.add(Calendar.DAY_OF_YEAR, 1);
			startTmp = c.getTime();
			count++;
		}
		return count;
	}

	public static String getPrintableDataFormat(String dataOriginale) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		String inputPattern = "yyyy-MM-dd";
		String outputPattern = "dd MMMM yyyy";
		sdf.applyPattern(inputPattern);
		Date d1 = null;
		try {
			d1 = sdf.parse(dataOriginale);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		sdf.applyPattern(outputPattern);
		String dataNuova = sdf.format(d1);
		return dataNuova;
	}

	public static int getRipetiFra(int ripetiFra, String[] periodiRipeti) {
		if (ripetiFra == 1) {
			return 1;
		} else if (ripetiFra == 7) {
			return 2;
		} else if (ripetiFra >= 28 && ripetiFra <= 31) {
			return 3;
		} else if (ripetiFra >= 56 && ripetiFra <= 62) {
			return 4;
		} else if (ripetiFra >= 84 && ripetiFra <= 93) {
			return 5;
		} else if (ripetiFra >= 168 && ripetiFra <= 186) {
			return 6;
		} else if (ripetiFra >= 365 && ripetiFra <= 366) {
			return 7;
		} else if (ripetiFra >= 730 && ripetiFra <= 732) {
			return 8;
		}
		return 0;
	}
	
	public static String newDate(String originalDate, int ripetifra, String periodoRipeti, String [] arrayPeriodoRipeti){
		Calendar c = Calendar.getInstance();
		try {
			Date origDate = sdf.parse(originalDate);
			c.setTime(origDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Date prossimaData = null;
		if (ripetifra > 0) {
			c.add(Calendar.DAY_OF_YEAR, ripetifra);
			prossimaData = c.getTime();
			ripetifra = DateUtils.getDaysFromString(periodoRipeti,
					arrayPeriodoRipeti, prossimaData);
		}
		return sdf.format(prossimaData);
	}

	public static String getDate(Date date) {
		return sdf.format(date);
	}

	public static Date getMonthStart(Date d) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.set(Calendar.DAY_OF_MONTH, 1);
		return c.getTime();
	}

	public static Date addMonth(Date d, int i) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.MONTH, i);
		return c.getTime();
	}

	public static Date addDay(Date d, int i) {
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_YEAR, i);
		return c.getTime();
	}

}
