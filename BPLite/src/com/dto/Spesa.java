package com.dto;

import java.util.Set;


public class Spesa extends VoceBilancio{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8422428346518209799L;
	private Set<TagSpesa> tags;

	public Spesa(){
		super();
	}
	
	public Spesa(String data, String descrizione, double importo){
		super(data, descrizione, importo);
	}
	
	public Set<TagSpesa> getTags() {
		return tags;
	}
	
	public void setTags(Set<TagSpesa> tags) {
		this.tags = tags;
	}

}
