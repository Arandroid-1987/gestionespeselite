package com.dto;

import java.util.Set;

public class Ricavo extends VoceBilancio {

	/**
	 * 
	 */
	private static final long serialVersionUID = -983389892350014159L;
	private Set<TagRicavo> tags;

	public Ricavo(String dateString, String descrizione, double importo) {
		super(dateString, descrizione, importo);
	}

	public Ricavo() {
		super();
	}
	
	public void setTags(Set<TagRicavo> tags) {
		this.tags = tags;
	}
	
	public Set<TagRicavo> getTags() {
		return tags;
	}
	
	

}
