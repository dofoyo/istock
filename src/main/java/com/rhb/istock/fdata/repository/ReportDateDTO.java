package com.rhb.istock.fdata.repository;

import java.util.HashMap;
import java.util.Map;

@Deprecated
public class ReportDateDTO {
	Map<Integer,String> dates = new HashMap<Integer,String>();  // 年份， 发布日期
	
	public void add(Integer year, String date){
		dates.put(year, date);
	}
	
	public String getDate(Integer year){
		return dates.get(year);
	}
	
	public Map<Integer,String> getDates(){
		return this.dates;
	}
}
