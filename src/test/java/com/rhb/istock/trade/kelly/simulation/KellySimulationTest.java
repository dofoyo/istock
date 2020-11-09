package com.rhb.istock.trade.kelly.simulation;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KellySimulationTest {
	@Autowired
	@Qualifier("kellySimulation")
	KellySimulation kellySimulation;
	
	
	@Test
	public void simulate() {
		boolean cache = true;
		LocalDate beginDate, endDate;
		//beginDate = LocalDate.of(2019,2,11);endDate = LocalDate.of(2019,5,1);
		//beginDate = LocalDate.of(2014,7,22);endDate = LocalDate.of(2015,6,19);
		//beginDate = LocalDate.of(2009,1,22);endDate = LocalDate.of(2009,8,12);
		beginDate = LocalDate.of(2005,12,9);endDate = LocalDate.of(2007,11,5);

		//beginDate = LocalDate.of(2016,1,1);endDate = LocalDate.of(2017,1,1);

		
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByHL(5, beginDate, endDate);
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByDT(5, beginDate, endDate);
		//TreeMap<LocalDate,List<String>> dailyItems = breakerService.getBreakersSortByAV(5, beginDate, endDate);
			
		//kellySimulation.simulate(dailyItems, null, cache);
	}
	
	
}
