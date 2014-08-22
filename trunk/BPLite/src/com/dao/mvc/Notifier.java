package com.dao.mvc;

import java.util.Observable;

public class Notifier extends Observable{
	
	public final static String RICAVI_TAG = "Ricavo";
	public final static String SPESE_TAG = "Spesa";
	public final static String ACTIVITY_TAG = "Activity";
	
	private String tag;
	
	@Override
	public void setChanged() {
		super.setChanged();
	}
	
	public void setTag(String tag) {
		this.tag = tag;
	}
	
	public String getTag() {
		return tag;
	}

}
