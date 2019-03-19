package com.rhb.istock.trade.balloon.simulation;

import java.util.Map;

import com.rhb.istock.trade.turtle.simulation.DailyItem;

public interface BalloonSimulation {
	public Map<String, String> simulate(DailyItem dailyItem, Option option);
}
