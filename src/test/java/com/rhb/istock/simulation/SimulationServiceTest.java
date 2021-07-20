package com.rhb.istock.simulation;

import java.time.LocalDate;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SimulationServiceTest {
	@Autowired
	@Qualifier("simulationService")
	SimulationService simulationService;
	
	@Test
	public void simulate() {
		LocalDate date = LocalDate.parse("2021-06-01");

		Set<Brief> holds = simulationService.getHolds("hlb", date, true);
		for(Brief hold : holds) {
			System.out.println(hold);
		}
	}
	
}
