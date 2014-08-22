package com.tyczj.extendedcalendarview;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import utils.DateUtils;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.text.format.Time;
import android.widget.BaseAdapter;

import com.dao.RicavoDao;
import com.dao.RicavoProgrammatoDao;
import com.dao.SpesaDao;
import com.dao.SpesaProgrammataDao;
import com.db.DatabaseHandler;
import com.dto.Ricavo;
import com.dto.RicavoProgrammato;
import com.dto.Spesa;
import com.dto.SpesaProgrammata;
import com.dto.VoceBilancio;

public class Day{
	
	int startDay;
	int monthEndDay;
	int day;
	int year;
	int month;
	Context context;
	BaseAdapter adapter;
	ArrayList<VoceBilancio> events = new ArrayList<VoceBilancio>();
	
	public Day(Context context,int day, int year, int month){
		this.day = day;
		this.year = year;
		this.month = month;
		this.context = context;
		Calendar cal = Calendar.getInstance();
		cal.set(year, month-1, day);
		int end = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
		cal.set(year, month, end);
		TimeZone tz = TimeZone.getDefault();
		monthEndDay = Time.getJulianDay(cal.getTimeInMillis(), TimeUnit.MILLISECONDS.toSeconds(tz.getOffset(cal.getTimeInMillis())));
	}
	
	public int getMonth(){
		return month;
	}
	
	public int getYear(){
		return year;
	}
	
	public void setDay(int day){
		this.day = day;
	}
	
	public int getDay(){
		return day;
	}
	
	/**
	 * Add an event to the day
	 * 
	 * @param event
	 */
	public void addEvent(VoceBilancio event){
		events.add(event);
	}
	
	/**
	 * Set the start day
	 * 
	 * @param startDay
	 */
	public void setStartDay(int startDay){
		this.startDay = startDay;
		new GetEvents().execute();
	}
	
	public int getStartDay(){
		return startDay;
	}
	
	public int getNumOfEvents(){
		return events.size();
	}
	
	/**
	 * Returns a list of all the colors on a day
	 * 
	 * @return list of colors
	 */
	public Set<Integer> getColors(){
		Set<Integer> colors = new HashSet<Integer>();
		for(VoceBilancio event : events){
			int color = -1;
			if(event instanceof SpesaProgrammata){
				color = Event.COLOR_YELLOW;
			}
			else if(event instanceof RicavoProgrammato){
				color = Event.COLOR_GREEN;
			}
			else if(event instanceof Spesa){
				color = Event.COLOR_RED;
			}
			else if(event instanceof Ricavo){
				color = Event.COLOR_BLUE;
			}
			colors.add(color);
		}
		
		return colors;
	}
	
	/**
	 * Get all the events on the day
	 * 
	 * @return list of events
	 */
	public ArrayList<VoceBilancio> getEvents(){
		return events;
	}
	
	public void setAdapter(BaseAdapter adapter){
		this.adapter = adapter;
	}
	
	private class GetEvents extends AsyncTask<Void,Void,Void>{

		@Override
		protected Void doInBackground(Void... params) {
			DatabaseHandler handler = DatabaseHandler.getInstance(context);
			SQLiteDatabase db = handler.getReadableDatabase();
			String startDate = DateUtils.getDate(year, month +1, day);
			String endDate = DateUtils.getDate(year, month +1, day);
			Collection<Spesa> spese = SpesaDao.filterSpese(db, startDate, endDate, null, -1, -1);
			Collection<Ricavo> ricavi = RicavoDao.filterRicavi(db, startDate, endDate, null, -1, -1);
			Collection<SpesaProgrammata> speseProgrammate = SpesaProgrammataDao
					.filterSpese(db, startDate, endDate, null, -1, -1);
			Collection<RicavoProgrammato> ricaviProgrammati = RicavoProgrammatoDao
					.filterRicavi(db, startDate, endDate, null, -1, -1);
			events.addAll(ricaviProgrammati);
			events.addAll(speseProgrammate);
			events.addAll(ricavi);
			events.addAll(spese);
			
//			Cursor c = context.getContentResolver().query(CalendarProvider.CONTENT_URI,new String[] {CalendarProvider.ID,CalendarProvider.EVENT,
//					CalendarProvider.DESCRIPTION,CalendarProvider.LOCATION,CalendarProvider.START,CalendarProvider.END,CalendarProvider.COLOR},"?>="+CalendarProvider.START_DAY+" AND "+ CalendarProvider.END_DAY+">=?",
//					new String[] {String.valueOf(startDay),String.valueOf(startDay)}, null);
//			if(c != null && c.moveToFirst()){
//				do{
//					Event event = new Event(c.getLong(0),c.getLong(4),c.getLong(5));
//					event.setName(c.getString(1));
//					event.setDescription(c.getString(2));
//					event.setLocation(c.getString(3));
//					event.setColor(c.getInt(6));
//					events.add(event);
//				}while(c.moveToNext());	
//			}
//			c.close();
			return null;
		}
		
		protected void onPostExecute(Void par){
			adapter.notifyDataSetChanged();
		}
		
	}
	

}
