package com.rhb.istock.trade.turtle.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.rhb.istock.trade.turtle.simulation.api.AmountView;
import com.rhb.istock.trade.turtle.simulation.api.BreakerView;
import com.rhb.istock.trade.turtle.simulation.api.HoldView;

public interface TurtleSimulation {
	public Map<String, String> simulate(TreeMap<LocalDate,List<String>> dailyItems, Toption option, boolean byCache);
	
	public List<BreakerView> getBreakers(String type, String year, LocalDate date);
	public List<HoldView> getHolds(String type, String year, LocalDate date);
	public AmountView getAmount(String type, String year, LocalDate date);
	public List<String> getDates(String type, String year);
	public void evictCache();
}
