package com.rhb.istock.kdata.muster;

import java.time.LocalDate;
import java.util.List;

import com.rhb.istock.kdata.muster.MusterEntity;

public interface MusterRepository {
	public void saveMusters(LocalDate date, List<MusterEntity> entities, Integer openPeriod, Integer dropPeriod);
	public void saveMuster(LocalDate date, MusterEntity entity, Integer openPeriod, Integer dropPeriod);
	
	public boolean isMustersExist(LocalDate date);
	public LocalDate getLastMusterDate();
	public List<MusterEntity> getMusters(LocalDate date);
	public void evictMustersCache();
	public void cleanMusters();
	

}
