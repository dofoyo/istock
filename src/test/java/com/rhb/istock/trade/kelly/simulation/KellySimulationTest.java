package com.rhb.istock.trade.kelly.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.selector.breaker.BreakerService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KellySimulationTest {
	@Autowired
	@Qualifier("kellySimulation")
	KellySimulation kellySimulation;
	
	@Autowired
	@Qualifier("breakerService")
	BreakerService breakerService;
	
	@Test
	public void simulate() {
		LocalDate beginDate, endDate;
		beginDate = LocalDate.of(2019,2,11);
		endDate = LocalDate.of(2019,6,1);
		boolean cache = true;

		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByHL(5, beginDate, endDate);
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByDT(5, beginDate, endDate);
		TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByAV(5, beginDate, endDate);
			
		kellySimulation.simulate(dailyItems, null, cache);
	}
	
	
}
