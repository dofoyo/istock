package com.rhb.istock.trade.balloon.simulation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.turtle.simulation.DailyItem;
import com.rhb.istock.trade.turtle.simulation.repository.TurtleSimulationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BalloonSimulationTest {
	@Autowired
	@Qualifier("balloonStaticSimulation")
	BalloonSimulation balloonSimulation;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("turtleSimulationRepositoryImp")
	TurtleSimulationRepository turtleSimulationRepository;
	
	//@Test
	public void simulateBySeperateYear() {
		Map<LocalDate, Map<String,String>> results = new TreeMap<LocalDate,Map<String,String>>();

		for(int year=2017; year<=2017; year++) {
			LocalDate beginDate = LocalDate.of(year, 1, 1);
			LocalDate endDate = LocalDate.of(year+1, 1, 1);

			//DailyItem dailyItem = simulationRepository.getAvarageAmountTops(13, beginDate, endDate);
			//DailyItem dailyItem = this.getSpecifyItem("sz002107", beginDate, endDate);
			DailyItem dailyItem = turtleSimulationRepository.getBluechips(300, beginDate, endDate);
			//DailyItem dailyItem = simulationRepository.getDailyAmountTops(13, beginDate, endDate);
			
			results.put(beginDate, balloonSimulation.simulate(dailyItem, null));
			beginDate = beginDate.plusYears(1);
			endDate = endDate.plusYears(1);
			
			kdataService.evictDailyKDataCache();
		}
		
		for(Map.Entry<LocalDate, Map<String,String>> result : results.entrySet()) {
			System.out.println("-----------------");
			System.out.println("year: " + result.getKey().getYear());
			System.out.println("initCash: " + result.getValue().get("initCash"));
			System.out.println("cash: " + result.getValue().get("cash"));
			System.out.println("value: " + result.getValue().get("value"));
			System.out.println("total: " + result.getValue().get("total"));
			System.out.println("CAGR: " + result.getValue().get("cagr"));
			System.out.println("winRatio: " + result.getValue().get("winRatio"));
		}
	}

	@Test
	public void simulateByContinuedYear() {
			LocalDate beginDate = LocalDate.of(2010, 1, 1);
			LocalDate endDate = LocalDate.of(2019, 1, 1);

			//DailyItem dailyItem = simulationRepository.getAvarageAmountTops(13, beginDate, endDate);
			//DailyItem dailyItem = this.getSpecifyItem("sh603898", beginDate, endDate);
			DailyItem dailyItem = turtleSimulationRepository.getBluechips(300, beginDate, endDate);
			//DailyItem dailyItem = simulationRepository.getDailyAmountTops(13, beginDate, endDate);
			
			balloonSimulation.simulate(dailyItem, null);
	}

	
	private DailyItem getSpecifyItem(String itemID,LocalDate beginDate, LocalDate endDate) {
		DailyItem dailyItem = new DailyItem();
		for(LocalDate date=beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			dailyItem.putItemID(date, itemID);
		}
		return dailyItem;
	}
}
