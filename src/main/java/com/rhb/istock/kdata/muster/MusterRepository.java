package com.rhb.istock.kdata.muster;

import java.time.LocalDate;
import java.util.List;

import com.rhb.istock.kdata.muster.MusterEntity;

public interface MusterRepository {
	public void saveMuster(LocalDate date, MusterEntity entity, Integer openPeriod, Integer dropPeriod);
	public void saveTmpMuster(LocalDate date, MusterEntity entity, Integer openPeriod, Integer dropPeriod);
	
	public boolean isMustersExist(LocalDate date);
	
	public List<MusterEntity> getMusters(LocalDate date);
	
	public void cleanTmpMusters();
	public void copyTmpMusters();


}
