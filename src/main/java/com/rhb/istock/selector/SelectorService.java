package com.rhb.istock.selector; 

import java.math.BigDecimal;
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

	public Map<String,Potential> getLatestPotentials();
	public Map<String,Potential> getPotentials(LocalDate date);
	public List<Potential> getPotentials_hlb(LocalDate date, Integer tops);
	public List<Potential> getPotentials_avb(LocalDate date, Integer tops);
	public List<String> getPowerIDs();
	
	public Map<LocalDate, BigDecimal> getMCSTs(String itemID, boolean cache);
	public Map<LocalDate, BigDecimal[]> getBOLLs(String itemID, Integer period, boolean cache);
	public Map<LocalDate, BigDecimal[]> getMACDs(String itemID, boolean cache);
	public List<LocalDate> getHuaFirst(String itemID, LocalDate beginDate, LocalDate endDate);
	public BigDecimal getMCST(String itemID, LocalDate endDate, Integer count, BigDecimal ratio);


}