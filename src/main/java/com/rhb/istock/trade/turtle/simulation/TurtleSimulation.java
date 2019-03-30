package com.rhb.istock.trade.turtle.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface TurtleSimulation {
	public Map<String, String> simulate(TreeMap<LocalDate,List<String>> dailyItems, Toption option);
}
