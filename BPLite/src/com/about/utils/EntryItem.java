package com.about.utils;


public class EntryItem implements Item{

	public final String title;
	public final String subtitle;

	public EntryItem(String title, String subtitle) {
		this.title = title;
		this.subtitle = subtitle;
	}
	
	public String getTitle() {
		return title;
	}

	public String getSubtitle() {
		return subtitle;
	}

	@Override
	public boolean isSection() {
		return false;
	}

}
