package com.rhb.istock.kdata.muster;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.kdata.muster.MusterEntity;

public interface MusterRepository {
	public void saveMusters(LocalDate date,List<MusterEntity> musterEntities);
	
	public void saveMuster(LocalDate date, MusterEntity entity);
	public void saveMuster(LocalDate date, String txt);
	public void saveTmpMuster(LocalDate date, MusterEntity entity);
	
	public boolean isMustersExist(LocalDate date);
	
	public Map<String,MusterEntity> getMusters(LocalDate date);
	
	public List<LocalDate> getMusterDates(LocalDate beginDate, LocalDate endDate);
	public List<LocalDate> getMusterDates();
	public List<LocalDate> getLastMusterDates(Integer count);
	
	
	public void cleanTmpMusters();
	public void copyTmpMusters();


}
