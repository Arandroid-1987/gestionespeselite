package com.ui.gestionespese;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;

public class Filtro implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7995742034658267398L;
	public String startDate = "1900-01-01";
	public String endDate = "2100-01-01";
	public Collection<String> tagRichiesti = new HashSet<String>();
	public double minImp = -1;
	public double maxImp = -1;

}
