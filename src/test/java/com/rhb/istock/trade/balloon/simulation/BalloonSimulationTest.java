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
import com.rhb.istock.selector.SelectorService;

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
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
	//@Test
	public void simulateBySeperateYear() {
		Map<LocalDate, Map<String,String>> results = new TreeMap<LocalDate,Map<String,String>>();

		for(int year=2010; year<=2018; year++) {
			LocalDate beginDate = LocalDate.of(year, 1, 1);
			LocalDate endDate = LocalDate.of(year+1, 1, 1);

			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getAverageAmountTops(13, beginDate, endDate);
			TreeMap<LocalDate,List<String>> dailyItems = this.getSpecifyItem("sh600519", beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getBluechipIDs(beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems= selectorService.getDailyAmountTops(13, beginDate, endDate);
			
			results.put(beginDate, balloonSimulation.simulate(dailyItems, null));
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
			LocalDate endDate = LocalDate.of(2019, 6, 1);

			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getAverageAmountTops(13, beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems = this.getSpecifyItem("sh600519", beginDate, endDate);
			TreeMap<LocalDate,List<String>> dailyItems = selectorService.getBluechipIDs(beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems= selectorService.getDailyAmountTops(13, beginDate, endDate);
			
			balloonSimulation.simulate(dailyItems, null);
	}

	
	private TreeMap<LocalDate,List<String>> getSpecifyItem(String itemID,LocalDate beginDate, LocalDate endDate) {
		TreeMap<LocalDate,List<String>> dailyItems = new TreeMap<LocalDate,List<String>>();
		List<String> ids;
		for(LocalDate date=beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids = new ArrayList<String>();
			ids.add(itemID);
			dailyItems.put(date, ids);
		}
		return dailyItems;
	}
}
