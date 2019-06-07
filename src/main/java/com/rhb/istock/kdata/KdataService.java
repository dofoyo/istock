package com.rhb.istock.kdata;

import java.time.LocalDate;
import java.util.List;

public interface KdataService {
	/*
	 * 获得的数据是不包含endDate的
	 */
	public Kdata getDailyKdata(String itemID, LocalDate endDate, Integer count, boolean byCache);
	public Kdata getDailyKdata(String itemID, LocalDate endDate, boolean byCache);
	public Kdata getDailyKdata(String itemID, boolean byCache);
	public Kbar getKbar(String itemID,LocalDate date, boolean byCache);
	public void evictDailyKDataCache();
	
	public LocalDate getLatestMarketDate(); 
	public LocalDate getLatestDownDate();
	
	public Kbar getLatestMarketData(String itemID);
	public List<String> getLatestDailyTop(Integer top); 
	
	public void downKdatas()  throws Exception ;

}
