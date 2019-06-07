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
import com.rhb.istock.selector.SelectorService;
import com.rhb.istock.selector.breaker.BreakerService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationTest {
	@Autowired
	@Qualifier("turtleStaticSimulation")
	TurtleSimulation turtleSimulation;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
	@Autowired
	@Qualifier("breakerService")
	BreakerService breakerService;
	
	//@Test
	public void simulate1() {
		Map<LocalDate, Map<String,String>> results = new TreeMap<LocalDate,Map<String,String>>();
		Integer beginYear = 2010;
		LocalDate beginDate, endDate;
		boolean cache = false;

		for(int year=beginYear; year<=2019; year++) {
			beginDate = LocalDate.of(year, 1, 1);
			endDate = LocalDate.of(year+1, 1, 1);

			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getBreakers(beginDate, endDate);
			TreeMap<LocalDate,List<String>> dailyItems = selectorService.getAverageAmountTops(13, beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems = this.getSpecifyItem("sh600309", beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getBluechipIDs(beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getDailyAmountTops(13, beginDate, endDate);
			//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getHighLowTops(13, beginDate, endDate);
			
			results.put(beginDate, turtleSimulation.simulate(dailyItems, null, cache));
			
			//kdataService.evictDailyKDataCache();
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
	
	@Test
	public void simulate2() {
		boolean cache = true;

		LocalDate beginDate, endDate;
		//beginDate = LocalDate.of(2019,2,11);endDate = LocalDate.of(2019,5,1);
		//beginDate = LocalDate.of(2014,7,22);endDate = LocalDate.of(2015,6,19);
		//beginDate = LocalDate.of(2009,1,22);endDate = LocalDate.of(2009,8,12);
		beginDate = LocalDate.of(2005,12,9);endDate = LocalDate.of(2007,11,5);

		/*
		 * 在breakers中分别按hl、dt、av排序，取前5名
		 * 即在breakers中找hl、dt、av
		 */
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByAV(5, beginDate, endDate);
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByHL(5, beginDate, endDate);
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByDT(5, beginDate, endDate);

		/*
		 * 在排好序的hl、dt、av、bluechip中找出breakers
		 */
		//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getAverageAmountTops(21, beginDate, endDate);
		//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getHighLowTops(21, beginDate, endDate);
		TreeMap<LocalDate,List<String>> dailyItems = selectorService.getDailyAmountTops(21, beginDate, endDate);
		
		
		//TreeMap<LocalDate,List<String>> dailyItems = selectorService.getBluechipIDs(beginDate, endDate);

		/*
		 * 仅跟踪个股
		 */
		//TreeMap<LocalDate,List<String>> dailyItems = this.getSpecifyItem("sh000001", beginDate, endDate);
		
		/*
		 * dailyItems必须每个交易日都要有
		 */
		turtleSimulation.simulate(dailyItems, null, cache);
	}
	
	//@Test
	public void getHolds() {
		System.out.println(turtleSimulation.getHolds("av", "2019", LocalDate.of(2019,3,12)));
	}

	//@Test
	public void getAmount() {
		System.out.println(turtleSimulation.getAmount("av", "2019", LocalDate.of(2019,3,12)));
	}
	
	//@Test
	public void getDates() {
		System.out.println(turtleSimulation.getDates("av", "2019"));
	}
	
}
