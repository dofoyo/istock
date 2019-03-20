package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Bluechip {
	private String code;
	private String name;
	private Map<Integer,LocalDate> reportDates = new HashMap<Integer,LocalDate>();
	private Set<Integer> okYears = new HashSet<Integer>();
	private LocalDate IpoDate;
	
	public boolean hasReported(LocalDate date){
		boolean flag = false;
		if(date.getMonthValue()>4) {  //5月1日后，都已公布年报
			flag = true;
		}else {
			LocalDate reportDate = reportDates.get(date.getYear()-1);
			if(reportDate!=null && (date.isAfter(reportDate) ||date.equals(reportDate))){
				flag = true;
			}			
		}

		return flag;
	}
	
	public void addReportDate(Integer year, LocalDate date){
		reportDates.put(year, date);
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
	
	public String getReportDateString() {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<Integer, LocalDate> entry : reportDates.entrySet()) {
			sb.append(entry.getKey());
			sb.append("(");
			sb.append(entry.getValue().toString());
			sb.append("),");
		}
		
		return sb.toString();
	}

	public Map<Integer, LocalDate> getReportDates() {
		return reportDates;
	}

	public void setReportDates(Map<Integer, LocalDate> reportDates) {
		this.reportDates = reportDates;
	}

	public Set<Integer> getOkYears() {
		return okYears;
	}
	
	public String getOkYearString() {
		StringBuffer sb = new StringBuffer();
		for(Integer year : okYears) {
			sb.append(year);
			sb.append(",");
		}
		return sb.toString();
	}

	public void setOkYears(Set<Integer> okYears) {
		this.okYears = okYears;
	}

	public LocalDate getIpoDate() {
		return IpoDate;
	}

	public void setIpoDate(LocalDate ipoDate) {
		IpoDate = ipoDate;
	}

	@Override
	public String toString() {
		return "BluechipEntity [code=" + code + ", name=" + name + ", okYears="
				+ okYears + ", IpoDate=" + IpoDate + ", reportDates=" + reportDates  + "]";
	}
}
