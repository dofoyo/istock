package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KdataRealtimeSpider {
	public LocalDate getLatestMarketDate(); 
	public Map<String,String> getLatestMarketData(String id);
	public List<String> getLatestDailyTop(Integer top);
	
	/*
	 * 返回值中，包括startDate，不包括endDate
	 */
	public List<LocalDate> getCalendar(LocalDate startDate,LocalDate endDate) throws Exception;
	
	public boolean isTradeDate(LocalDate date);

}
