package com.dto;

public class RicavoProgrammato extends Ricavo {
	/**
	 * 
	 */
	private static final long serialVersionUID = 6403918046919677794L;
	private int ripetiFra;

	public RicavoProgrammato() {
		super();
	}

	public RicavoProgrammato(String data, String descrizione, double importo) {
		super(data, descrizione, importo);
	}

	public int getRipetiFra() {
		return ripetiFra;
	}

	public void setRipetiFra(int ripetiFra) {
		this.ripetiFra = ripetiFra;
	}

}
