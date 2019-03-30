package com.rhb.istock.trade.balloon.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public interface BalloonSimulation {
	public Map<String, String> simulate(TreeMap<LocalDate,List<String>> dailyItems, Boption option);
}
