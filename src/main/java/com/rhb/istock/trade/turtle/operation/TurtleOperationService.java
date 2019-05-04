package com.rhb.istock.trade.turtle.operation;

import java.util.List;

import com.rhb.istock.kdata.api.KdatasView;
import com.rhb.istock.trade.turtle.operation.api.HoldView;
import com.rhb.istock.trade.turtle.operation.api.TurtleView;

/*
 * operation和simulation不同
 * operation主要用于实操，其数据为最新数据，其turtle的参数为默认值
 * 
 * 
 * simulation主要用于模拟，其数据为历史数据，其turtle的参数需要调优
 * 
 */
public interface TurtleOperationService {
	/*
	 * 对holds.txt中的股票进行提示：止损价(stopPrice)、平仓价(dropPrice)、加仓价(reopenPrice)
	 */
	public List<HoldView> getHolds();
	public List<TurtleView> getFavors();
	public List<TurtleView> getDailyTops(Integer top);
	public List<TurtleView> getAvTops(Integer top);
	public List<TurtleView> getBluechips();
	public List<TurtleView> getHighLowTops(Integer top);
	public List<TurtleView> getAgTops(Integer top);
	
	/*
	 * 一般每周1-5，每天上午9:00初始化一次。主要是导入之前的历史记录
	 */
	public void init();
	
	

	public KdatasView getKdatas(String itemID);
	
	
	
}
