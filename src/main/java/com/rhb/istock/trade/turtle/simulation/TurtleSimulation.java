package com.rhb.istock.trade.turtle.simulation;

import java.util.Map;

public interface TurtleSimulation {
	public Map<String, String> simulate(DailyItem dailyItem, Option option);
}
