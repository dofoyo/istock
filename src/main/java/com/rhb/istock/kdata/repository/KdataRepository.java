package com.rhb.istock.kdata.repository;

import java.time.LocalDate;


public interface KdataRepository {
	public KdataEntity getKdataByCache(String itemID);
	public KdataEntity getKdata(String itemID);
	public void evictKDataCache();
	public LocalDate getLastDate(String itemID);
}
