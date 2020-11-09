package com.rhb.istock.trade.turtle.simulation.old;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;


public interface TurtleSimulation {
	public Map<String, String> simulate(TreeMap<LocalDate,List<String>> dailyItems, Toption option, boolean byCache);
	
/*	public List<BreakerView> getBreakers(String type, LocalDate date);
	public List<HoldView> getHolds(String type, LocalDate date);
	public AmountView getAmount(String type, LocalDate date);
	public List<String> getDates(String type);
*/
	
}
