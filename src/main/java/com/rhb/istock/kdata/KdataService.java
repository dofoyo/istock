package com.rhb.istock.kdata;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface KdataService {
	/*
	 * 获得的数据是不包含endDate的
	 */
	public Kdata getDailyKdata(String itemID, LocalDate endDate, Integer count, boolean byCache);
	public Kdata getDailyKdata(String itemID, boolean byCache);
	public Kbar getKbar(String itemID,LocalDate date, boolean byCache);
	public void evictDailyKDataCache();
	
	public LocalDate getLatestMarketDate(); 
	public Kbar getLatestMarketData(String itemID);
	
	public void downKdatas()  throws Exception ;
	
	
	public List<String> getDailyAverageAmountTops(Integer top);
	public void generateDailyAverageAmountTops(List<String> itemIDs, Integer duration);

}
