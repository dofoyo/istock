package com.rhb.istock.trade.turtle.operation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.kdata.api.KdatasView;
import com.rhb.istock.trade.turtle.operation.api.ItemView;
import com.rhb.istock.trade.turtle.operation.api.HoldView;
import com.rhb.istock.trade.turtle.operation.api.IndustryView;
import com.rhb.istock.trade.turtle.operation.api.PotentialView;
import com.rhb.istock.trade.turtle.operation.api.ForecastView;
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
	public List<ItemView> getOks();
	public List<HoldView> getHolds();
	public List<ItemView> getFavors();
	public List<ItemView> getNewbs();
	public List<TurtleView> getPotentials(String type);
	public List<PotentialView> getPotentials(String type, LocalDate date);
	public List<PotentialView> getPotentials_hlb(LocalDate date);
	public List<PotentialView> getPotentials_avb(LocalDate date);
	public Map<String,IndustryView> getPotentialIndustrys(LocalDate date);
	public void redoPotentials();
	public String[] getTopics();
	public List<ItemView> getHAHs();
	public List<ItemView> getPowers();
	public List<ItemView> getHorizons();
	public List<ForecastView> getForecastViews();
	public List<ItemView> getB21Views();
	public List<ItemView> getB21upViews();
	public ItemView getItemView(String itemID, LocalDate date);
	
	/*
	 * 一般每周1-5，每天上午9:00初始化一次。主要是导入之前的历史记录
	 */
	public void init();
	
	

	public KdatasView getKdatas(String itemID);
	
	
	
}
