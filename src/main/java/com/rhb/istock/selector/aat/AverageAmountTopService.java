package com.rhb.istock.selector.aat;

import java.util.List;

public interface AverageAmountTopService {
	public List<String> getAverageAmountTops(Integer top);
	
	/*
	 * 下载新收盘数据后执行一次
	 */
	public void generateAverageAmountTops();

}
