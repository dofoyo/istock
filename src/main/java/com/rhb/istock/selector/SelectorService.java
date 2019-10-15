package com.rhb.istock.selector; 

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rhb.istock.selector.hold.HoldEntity;
import com.rhb.istock.selector.potential.Potential;

public interface SelectorService{
	/*
	 * 用于operation
	 */
	public List<HoldEntity> getHolds();
	public List<String> getHoldIDs();

	/*
	 * 用于operation
	 */
	public Map<String,String> getFavors();

	
	public Map<String,Integer> getHighLowTops(Integer top);
	
	/*
	 * 用于operation
	 */	
	public Map<String,Integer> getLatestAverageAmountTops(Integer top);
	
	public void generateBluechip();
	public List<String> getBluechipIDs(LocalDate date);
	public TreeMap<LocalDate,List<String>> getBluechipIDs(LocalDate beginDate, LocalDate endDate);
	public List<String> getLatestBluechipIDs();

	public Map<String,Integer> getLatestDailyAmountTops(Integer top);
	
	public Map<String,Potential> getLatestPotentials();
	public Map<String,Potential> getPotentials(LocalDate date);
	public List<Potential> getPotentials_hlb(LocalDate date, Integer tops);
	public List<String> getPowerIDs();

}