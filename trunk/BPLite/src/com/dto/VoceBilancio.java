package com.dto;

import java.io.Serializable;

public class VoceBilancio implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6658211364945204673L;
	protected long id;
	protected String data;
	protected String descrizione;
	protected double importo;
	
	public VoceBilancio() {
	}
	
	public VoceBilancio(String data, String descrizione, double importo) {
		super();
		this.data = data;
		this.descrizione = descrizione;
		this.importo = importo;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

	public double getImporto() {
		return importo;
	}

	public void setImporto(double importo) {
		this.importo = importo;
	}
	
	public String getDescrizione() {
		return descrizione;
	}
	
	public void setDescrizione(String descrizione) {
		this.descrizione = descrizione;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VoceBilancio other = (VoceBilancio) obj;
		if (id != other.id)
			return false;
		return true;
	}
	
}
