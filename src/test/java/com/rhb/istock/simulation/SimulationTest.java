package com.rhb.istock.simulation;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SimulationTest {
	@Autowired
	@Qualifier("simulation")
	Simulation simulation;
	
	@Test
	public void simulate() {
		LocalDate beginDate = LocalDate.parse("2020-01-25");
		LocalDate endDate = LocalDate.parse("2020-10-29");

		try {
			simulation.simulate(beginDate, endDate);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}
	
}
