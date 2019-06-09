package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface KdataRepository {
	public KdataEntity getDailyKdataByCache(String itemID);
	public KdataEntity getDailyKdata(String itemID);
	public void evictDailyKDataCache();
	public LocalDate getLastDate();
	
	public void saveLatestMusters(LocalDate date, List<KdataMusterEntity> entities, Integer period);
	public LocalDate getLatestMusterDate();
	public List<KdataMusterEntity> getKdataMusters();
	public void evictKdataMustersCache();
	
	public TreeMap<LocalDate,BigDecimal> getFactors(String itemID);
	public void saveLatestFactors(Map<String,BigDecimal> factors);
	public Map<String,BigDecimal> getLatestFactors();
	public void evictLatestFactorsCache();


}
