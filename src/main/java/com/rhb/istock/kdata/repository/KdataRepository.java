package com.rhb.istock.kdata.repository;

import java.time.LocalDate;
import java.util.List;

public interface KdataRepository {
	public KdataEntity getDailyKdataByCache(String itemID);
	public KdataEntity getDailyKdata(String itemID);
	public void evictDailyKDataCache();
	public LocalDate getLatestDate();
	
	public List<String> getDailyAverageAmountTops();
	public void generateDailyAverageAmountTops(List<String> itemIDs, Integer duration);

}
