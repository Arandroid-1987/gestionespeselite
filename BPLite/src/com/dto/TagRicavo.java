package com.dto;

import java.io.Serializable;

public class TagRicavo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2445163253592705168L;
	private long id;
	private Ricavo ricavo;
	private String valore;
	
	public TagRicavo() {
	}
	
	public TagRicavo(Ricavo ricavo, String valore) {
		super();
		this.ricavo = ricavo;
		this.valore = valore;
	}
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public Ricavo getRicavo() {
		return ricavo;
	}
	public void setRicavo(Ricavo ricavo) {
		this.ricavo = ricavo;
	}
	public String getValore() {
		return valore;
	}
	public void setValore(String valore) {
		this.valore = valore;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ricavo == null) ? 0 : ricavo.hashCode());
		result = prime * result + ((valore == null) ? 0 : valore.hashCode());
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
		TagRicavo other = (TagRicavo) obj;
		if (ricavo == null) {
			if (other.ricavo != null)
				return false;
		} else if (!ricavo.equals(other.ricavo))
			return false;
		if (valore == null) {
			if (other.valore != null)
				return false;
		} else if (!valore.equals(other.valore))
			return false;
		return true;
	}

}
