package com.rhb.istock.trade.turtle.simulation.muster;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleSimulationTest {
	@Autowired
	@Qualifier("turtleMusterSimulation")
	TurtleMusterSimulation turtleMusterSimulation;
	
	@Test
	public void simulate() {
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-06-01");
		turtleMusterSimulation.simulate(beginDate, endDate);
	}


	
}
