package com.rhb.istock.selector.hlt;

import java.util.List;

public interface HighLowTopService {
	public List<String> getHighLowTops(Integer top);

	/*
	 * 下载新收盘数据后执行一次
	 */
	public void generateHighLowTops();
}
