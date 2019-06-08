package com.rhb.istock.kdata.repository;

import java.time.LocalDate;
import java.util.List;

public interface KdataRepository {
	public KdataEntity getDailyKdataByCache(String itemID);
	public KdataEntity getDailyKdata(String itemID);
	public void evictDailyKDataCache();
	public LocalDate getLatestDate();
	
	public void saveLatestMusters(LocalDate date, List<KdataMusterEntity> entities, Integer period);
	public LocalDate getLatestMusterDate();
	public List<KdataMusterEntity> getKdataMusters();
	public void evictKdataMustersCache();

}
