package com.dto;

public class BilancioMese {
	private String mese;
	private String anno;
	private int meseNumero;
	private int annoNumero;
	private double spese;
	private double ricavi;
	private double bilancio;

	public BilancioMese() {
	}

	public String getMese() {
		return mese;
	}

	public void setMese(String mese) {
		this.mese = mese;
	}

	public double getSpese() {
		return spese;
	}

	public void setSpese(double spese) {
		this.spese = spese;
	}

	public double getRicavi() {
		return ricavi;
	}

	public void setRicavi(double ricavi) {
		this.ricavi = ricavi;
	}

	public double getBilancio() {
		return bilancio;
	}

	public void setBilancio(double bilancio) {
		this.bilancio = bilancio;
	}

	public String getAnno() {
		return anno;
	}

	public void setAnno(String anno) {
		this.anno = anno;
	}

	public int getMeseNumero() {
		return meseNumero;
	}

	public void setMeseNumero(int meseNumero) {
		this.meseNumero = meseNumero;
	}

	public int getAnnoNumero() {
		return annoNumero;
	}

	public void setAnnoNumero(int annoNumero) {
		this.annoNumero = annoNumero;
	}
	
}
