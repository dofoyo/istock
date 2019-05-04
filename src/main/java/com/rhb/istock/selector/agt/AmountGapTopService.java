package com.rhb.istock.selector.agt;

import java.util.List;

public interface AmountGapTopService {
	
	/*
	 * 每天计算一次
	 * 成交量差距统计
	 * 最新月成交量突然放大，说明有资金介入
	 * 以最近30日成交量与N天前的30日成交量进行比较，差距越大，排位靠前
	 * 例如，当N=34时，今天4月17日，近34天的成交量的起始日期为3月27日，则：
	 * 2019-04-17 -- 2019-03-27： totalAmount1
	 * 2019-03-26 -- 2019-01-10: totalAmount2
	 * ...
	 * 
	 * 以totalAmount1是totalAmount2的多少倍进行排序，倍数越大，越靠前
	 */
	public void generateAmountGaps();
	
	public List<String> getAmountGapTops(Integer top);
	
	
}
