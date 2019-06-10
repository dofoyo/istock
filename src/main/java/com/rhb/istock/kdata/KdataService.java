package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface KdataService {
	/*
	 * 获得的数据是不包含endDate的
	 */
	public Kdata getKdata(String itemID, LocalDate endDate, Integer count, boolean byCache);
	public Kdata getLastKdata(String itemID, Integer count, boolean byCache);
	public Kdata getKdata(String itemID, LocalDate endDate, boolean byCache);
	public Kdata getKdata(String itemID, boolean byCache);
	public Kbar getKbar(String itemID,LocalDate date, boolean byCache);
	public void evictKDataCache();
	
	public LocalDate getLatestMarketDate(); 
	public LocalDate getLastKdataDate();
	
	public Kbar getLatestMarketData(String itemID);
	public List<String> getLatestDailyTop(Integer top); 
	
	public void downKdatasAndFactors()  throws Exception ;
	
	public void generateMusters();
	public void generateLastMusters();
	public List<Muster> getLastMusters();
	
	public void downLatestFactors();
	public void generateLatestFactors();
	public BigDecimal getLatestFactors(String itemID);

}
