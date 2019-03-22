package com.rhb.istock.kdata.repository;

import java.time.LocalDate;

public interface KdataRepository {
	public KdataEntity getDailyKdataByCache(String itemID);
	public KdataEntity getDailyKdata(String itemID);
	public void evictDailyKDataCache();
	public LocalDate getLatestDate();
}
