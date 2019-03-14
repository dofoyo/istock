package com.rhb.istock.kdata;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KdataService {
	/*
	 * 获得的数据是不包含endDate的
	 */
	public Kdata getDailyKdata(String itemID, LocalDate endDate, Integer count);
	public List<String> getDailyAmountTops(LocalDate date, Integer top);
	public List<String> getDailyAverageAmountTops(LocalDate date, Integer top);
	
	
	public LocalDate getLatestMarketDate(); 
	public Kbar getLatestMarketData(String itemID);
	
	public void downLatestKdatas()  throws Exception ;
	
}
