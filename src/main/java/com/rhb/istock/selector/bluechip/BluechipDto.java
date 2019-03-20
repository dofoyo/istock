package com.rhb.istock.selector.bluechip;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BluechipDto {
	private String code;
	private String name;
	private Map<Integer,String> reportDates = new HashMap<Integer,String>();
	private Set<Integer> okYears = new HashSet<Integer>();
	private String IpoDate;
	
	public String getOkYearString() {
		StringBuffer sb = new StringBuffer();
		for(Integer year : okYears) {
			sb.append(year);
			sb.append(",");
		}
		
		return sb.toString();
	}
	
	public void addReportDate(Integer year, String date){
		this.reportDates.put(year, date);
	}
	
	public void addOkYear(Integer year){
		this.okYears.add(year);
	}
	
	public boolean isOk(Integer year){
		return okYears.contains(year);
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<Integer, String> getReportDates() {
		return reportDates;
	}

	public void setReportDates(Map<Integer, String> reportDates) {
		this.reportDates = reportDates;
	}

	public Set<Integer> getOkYears() {
		return okYears;
	}

	public void setOkYears(Set<Integer> okYears) {
		this.okYears = okYears;
	}

	public String getIpoDate() {
		return IpoDate;
	}

	public void setIpoDate(String ipoDate) {
		IpoDate = ipoDate;
	}

	@Override
	public String toString() {
		return "BluechipDto [code=" + code + ", name=" + name + ", okYears=" + okYears
				+ ", IpoDate=" + IpoDate + ", reportDates=" + reportDates + "]";
	}
	
}
