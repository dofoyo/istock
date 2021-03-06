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
		LocalDate beginDate = LocalDate.parse("2017-01-01");
		LocalDate endDate = LocalDate.parse("2018-05-31");

		simulation.simulate(beginDate, endDate);
	}
	
}
