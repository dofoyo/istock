package com.rhb.istock.kdata.repository;

import java.time.LocalDate;
import java.util.List;

import com.rhb.istock.kdata.muster.MusterEntity;

public interface KdataRepository {
	public KdataEntity getKdataByCache(String itemID);
	public KdataEntity getKdata(String itemID);
	public void evictKDataCache();
	public LocalDate getLastDate();
}
