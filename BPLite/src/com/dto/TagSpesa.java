package com.dto;

import java.io.Serializable;

public class TagSpesa implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5292937319368580515L;
	private long id;
	private Spesa spesa;
	private String valore;

	public TagSpesa() {
	}

	public TagSpesa(Spesa spesa, String valore) {
		super();
		this.spesa = spesa;
		this.valore = valore;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Spesa getSpesa() {
		return spesa;
	}

	public void setSpesa(Spesa spesa) {
		this.spesa = spesa;
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
		result = prime * result + ((spesa == null) ? 0 : spesa.hashCode());
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
		TagSpesa other = (TagSpesa) obj;
		if (spesa == null) {
			if (other.spesa != null)
				return false;
		} else if (!spesa.equals(other.spesa))
			return false;
		if (valore == null) {
			if (other.valore != null)
				return false;
		} else if (!valore.equals(other.valore))
			return false;
		return true;
	}

	
}
