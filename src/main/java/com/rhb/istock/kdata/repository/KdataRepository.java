package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface KdataRepository {
	public KdataEntity getKdataByCache(String itemID);
	public KdataEntity getKdata(String itemID);
	public void evictKDataCache();
	public LocalDate getLastDate();
	
	public void saveMusters(LocalDate date, List<MusterEntity> entities, Integer openPeriod, Integer dropPeriod);
	public void saveMuster(LocalDate date, MusterEntity entity, Integer openPeriod, Integer dropPeriod);
	
	public boolean isMustersExist(LocalDate date);
	public LocalDate getLastMusterDate();
	public List<MusterEntity> getMusters(LocalDate date);
	public void evictMustersCache();
	public void cleanMusters();
	
	public TreeMap<LocalDate,BigDecimal> getFactors(String itemID);
	public void saveLatestFactors(Map<String,BigDecimal> factors);
	public Map<String,BigDecimal> getLatestFactors();
	public void evictLatestFactorsCache();


}
