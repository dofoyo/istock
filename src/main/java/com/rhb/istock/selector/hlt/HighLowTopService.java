package com.rhb.istock.selector.hlt;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

public interface HighLowTopService {
	/*
	 * 用于operation
	 */
	public List<String> getLatestHighLowTops(Integer top);

	/*
	 * 用于operation
	 * 下载新收盘数据后执行一次
	 */
	public void generateLatestHighLowTops();

	
	public List<String> getHighLowTops(Integer top, LocalDate date);
	public TreeMap<LocalDate,List<String>> getHighLowTops(Integer top, LocalDate beginDate, LocalDate endDate);

	/*
	 * 用于simulation
	 * 模拟测试前执行一次
	 */
	public void generateHighLowTops();

}
