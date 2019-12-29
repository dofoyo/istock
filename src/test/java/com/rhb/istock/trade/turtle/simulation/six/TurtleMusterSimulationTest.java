package com.rhb.istock.trade.turtle.simulation.six;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleMusterSimulationTest {
	@Autowired
	@Qualifier("turtleMusterSimulation")
	TurtleMusterSimulation turtleMusterSimulation;

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("turtleMusterSimulationAnalysis")
	TurtleMusterSimulationAnalysis turtleMusterSimulationAnalysis;
	
	@Autowired
	@Qualifier("turtleMusterSimulation_hua")
	TurtleMusterSimulation_hua turtleMusterSimulation_hua;
	
	@Test
	public void simulate() {
		LocalDate beginDate = LocalDate.parse("2000-01-01");
		LocalDate endDate = LocalDate.parse("2019-12-27");

		turtleMusterSimulation.simulate(beginDate, endDate); 
		turtleMusterSimulation_hua.simulate(beginDate, endDate);
	}
	
	//@Test
	public void generateRecords() {
		turtleMusterSimulationAnalysis.generateRecords("hlb");
	}


	
}
