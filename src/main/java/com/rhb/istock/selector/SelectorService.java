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

	
	public List<String> getLatestHighLowTops(Integer top);
	public List<String> getHighLowTops(Integer top, LocalDate date);
	public TreeMap<LocalDate,List<String>> getHighLowTops(Integer top, LocalDate beginDate, LocalDate endDate);
	public void generateHighLowTops();
	
	/*
	 * 用于operation
	 */	
	public List<String> getLatestAverageAmountTops(Integer top);
	
	/*
	 * 用于simulation
	 */
	public List<String> getAverageAmountTops(Integer top, LocalDate date);
	public TreeMap<LocalDate,List<String>> getAverageAmountTops(Integer top, LocalDate beginDate, LocalDate endDate);
	
	/*
	 * 用于simulation，测试前执行一次
	 */
	public void generateAverageAmountTops();

	
	public void generateBluechip();
	public List<String> getBluechipIDs(LocalDate date);
	public TreeMap<LocalDate,List<String>> getBluechipIDs(LocalDate beginDate, LocalDate endDate);
	public List<String> getLatestBluechipIDs();

	public List<String> getLatestDailyAmountTops(Integer top);
	public List<String> getDailyAmountTops(Integer top, LocalDate date);
	public TreeMap<LocalDate,List<String>> getDailyAmountTops(Integer top, LocalDate beginDate, LocalDate endDate);
	
	public void generateBreakers();
	public void generateTmpLatestPotentials();
	public TreeMap<LocalDate, List<String>> getBreakers(LocalDate beginDate, LocalDate endDate);
	
	
	public List<Potential> getLatestPotentials();

}