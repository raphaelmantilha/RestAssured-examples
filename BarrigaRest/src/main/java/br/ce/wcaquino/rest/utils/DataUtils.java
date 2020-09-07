package br.ce.wcaquino.rest.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DataUtils {

	public static String getDataDiferencaDias(Integer qtdDias) {
		Calendar cal = Calendar.getInstance();
		DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
		
		cal.add(Calendar.DAY_OF_MONTH, qtdDias);
		return format.format(cal.getTime());
	}
}
