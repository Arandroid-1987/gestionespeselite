package utils;

import java.text.DecimalFormat;

public class NumberUtils {
	
	private static DecimalFormat df = new DecimalFormat("0.00");
	
	public static String getString(double value){
		return df.format(value);
	}

}
