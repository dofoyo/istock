package com.rhb.istock.kdata.repository;

public interface KdataRepository {
	public KdataEntity getDailyKdataByCache(String itemID);
	public KdataEntity getDailyKdata(String itemID);
	public void evictDailyKDataCache();
}
