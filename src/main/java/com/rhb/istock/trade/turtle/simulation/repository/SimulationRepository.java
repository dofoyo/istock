package com.rhb.istock.trade.turtle.simulation.repository;

import java.time.LocalDate;

import com.rhb.istock.trade.turtle.simulation.DailyItem;

public interface SimulationRepository {
	public DailyItem getDailyAmountTops(Integer top,LocalDate beginDate, LocalDate endDate);
	public DailyItem getAvarageAmountTops(Integer top,LocalDate beginDate, LocalDate endDate);
	public DailyItem getBluechips(Integer top,LocalDate beginDate, LocalDate endDate);
	public void generateDailyAmountTops(Integer top);
	public void generateAvarageAmountTops(Integer top);

}
