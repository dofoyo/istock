package com.rhb.istock.kdata.repository;

public interface KdataRepository {
	public KdataEntity getDailyKdata(String itemID);
	public void EvictDailyKDataCache();
}
