package com.rhb.istock.fdata.sina.repository;

import java.util.Map;

public interface ReportDateRepository {

	public void init();
	public String getReportDate(String stockcode, Integer year);
	public Map<Integer,String>getReportDates(String stockcode);
	public void saveReportDates(Map<String, String> codeDates, Integer year);
}
