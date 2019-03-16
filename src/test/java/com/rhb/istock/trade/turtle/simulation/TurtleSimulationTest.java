package com.rhb.istock.trade.turtle.simulation;

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
import com.rhb.istock.trade.turtle.simulation.repository.SimulationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationTest {
	@Autowired
	@Qualifier("turtleStaticSimulation")
	TurtleSimulation turtleSimulation;
	
	@Autowired
	@Qualifier("simulationRepositoryImp")
	SimulationRepository simulationRepository;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Test
	public void simulate() {
		Map<LocalDate, Map<String,String>> results = new TreeMap<LocalDate,Map<String,String>>();

		for(int year=2010; year<=2019; year++) {
			LocalDate beginDate = LocalDate.of(year, 1, 1);
			LocalDate endDate = LocalDate.of(year+1, 1, 1);

			DailyItem dailyItem = simulationRepository.getAvarageAmountTops(13, beginDate, endDate);
			//DailyItem dailyItem = this.getSpecifyItem("sh600519", beginDate, endDate);
			//DailyItem dailyItem = simulationRepository.getBluechips(300, beginDate, endDate);
			//DailyItem dailyItem = simulationRepository.getDailyAmountTops(13, beginDate, endDate);
			
			results.put(beginDate, turtleSimulation.simulate(dailyItem, null));
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
	
	private DailyItem getSpecifyItem(String itemID,LocalDate beginDate, LocalDate endDate) {
		DailyItem dailyItem = new DailyItem();
		for(LocalDate date=beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			dailyItem.putItemID(date, "sh600519");
		}
		return dailyItem;
	}
}
