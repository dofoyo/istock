package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KdataService {
	/*
	 * 获得的数据是不包含endDate的
	 */
	
	public void downSSEI();  //下载上证指数
	public Integer getSseiFlag(LocalDate date); 
	public Integer getSseiRatio(LocalDate date, Integer period);
	
	public BigDecimal getHighestPrice(String itemID, LocalDate beginDate, boolean byCache);
	
	public Kdata getKdata(String itemID, LocalDate endDate, Integer count, boolean byCache);
	public Kdata getKdata(String itemID, LocalDate beginDate, LocalDate endDate, boolean byCache);
	public Kdata getKdata(String itemID, LocalDate endDate, boolean byCache);
	public Kdata getKdata(String itemID, boolean byCache);
	public Kbar getKbar(String itemID,LocalDate date, boolean byCache);
	public Kdata getLastKdata(String itemID, Integer count, boolean byCache);
	public void evictKDataCache();
	
	public LocalDate getLatestMarketDate(String itemID); 
	public LocalDate getLastKdataDate(String itemID);
	
	public Kbar getLatestMarketData(String itemID);
	public List<String> getLatestDailyTop(Integer top); 
	
	public void downKdatas()  throws Exception ;
	public void downKdatasAndFactors()  throws Exception ;
	public void downClosedDatas()  throws Exception ;
	public void downClosedDatas(LocalDate date)  throws Exception ;
	public void downFactors()  throws Exception ;
	public void downFactors(LocalDate date)  throws Exception ;
	
	public void generateMusters(LocalDate beginDate);  //用于simulation
	public Map<String,Muster> getMusters(LocalDate date); //用于simulation
	public Map<String,Muster> getMusters(LocalDate date, Set<String> excludeIndustrys); //用于simulation
	public Map<String,Muster> getMusters(LocalDate date, String industry); //用于simulation
	public Map<String,Muster> getMustersOfTheDayAfter(LocalDate date);

	public void generateLatestMusters(LocalDate date);  //用于operation，每天开盘前，根据上一交易日的收盘价和最新的除权因子计算
	public Map<String,Muster> getLatestMusters();//用于operation，提供给前端显示。和updateLatestMusters配合，可以提升操作体验
	public List<Map<String,Muster>> getPreviousMusters(Integer previous_period, LocalDate endDate);//给定日期前的muster
	public void updateLatestMusters();//用于operation, 在交易时间无限循环对其中的potential刷新latestPrice，
	public void updateLatestMustersOfFavors();//用于operation, 在交易时间无限循环对其中的potential刷新latestPrice，

	public List<LocalDate> getMusterDates(LocalDate beginDate, LocalDate endDate);
	public List<LocalDate> getMusterDates(Integer count, LocalDate endDate);
	public List<LocalDate> getMusterDates();
	public List<LocalDate> getLastMusterDates();
	

}
