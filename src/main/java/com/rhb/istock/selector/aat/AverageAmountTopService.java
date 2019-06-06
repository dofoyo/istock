package com.rhb.istock.selector.aat;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

public interface AverageAmountTopService {
	/*
	 * 用于实操 operation
	 */
	public List<String> getLatestAverageAmountTops(Integer top);
	
	/*
	 * 用于实操 operation
	 * 下载新收盘数据后执行一次
	 */
	public void generateLatestAverageAmountTops();
	
	
	/*
	 * 用于模拟测试 simulation
	 */
	public List<String> getAverageAmountTops(Integer top, LocalDate date);
	public TreeMap<LocalDate,List<String>> getAverageAmountTops(Integer top, LocalDate beginDate, LocalDate endDate);
	
	/*
	 * 用于模拟测试 simulation
	 * 在测试前执行一次
	 */
	public void generateAverageAmountTops();
	
	/*
	 * 排序从大到小排序
	 */
	public List<String> sort(List<String> itemIDs,LocalDate date, Integer duration, boolean byCache);


}
