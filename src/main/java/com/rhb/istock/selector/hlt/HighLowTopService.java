package com.rhb.istock.selector.hlt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface HighLowTopService {
	/*
	 * 用于operation
	 */
	public Map<String,Integer> getLatestHighLowTops(Integer top);

	public List<String> getHighLowTops(Integer top, LocalDate date);
	public TreeMap<LocalDate,List<String>> getHighLowTops(Integer top, LocalDate beginDate, LocalDate endDate);

	/*
	 * 用于simulation
	 * 模拟测试前执行一次
	 */
	public void generateHighLowTops();
	
	/*
	 * 排序从小到大排序
	 */
	public List<String> sort(List<String> itemIDs,LocalDate date, Integer duration, boolean byCache);
}
