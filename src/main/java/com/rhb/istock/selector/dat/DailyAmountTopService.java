package com.rhb.istock.selector.dat;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

public interface DailyAmountTopService {
	/*
	 * 用于实操 operation，直接从网上爬下来
	 */
	public List<String> getLatestDailyAmountTops(Integer top);
	
	/*
	 * 用于模拟 simulation
	 */
	public List<String> getDailyAmountTops(Integer top, LocalDate date);
	public TreeMap<LocalDate, List<String>> getDailyAmountTops(Integer top, LocalDate beginDate, LocalDate endDate);
	
	/*
	 * 用于模拟 simulation
	 * 模拟前重新生成一次即可
	 */
	public void generateDailyAmountTops();


}
