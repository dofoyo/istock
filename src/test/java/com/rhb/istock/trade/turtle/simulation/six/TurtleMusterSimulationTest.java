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
	
	//@Test
	public void getDailyMeans() {
		LocalDate beginDate = LocalDate.parse("2019-06-01");
		LocalDate endDate = LocalDate.parse("2019-06-14");
		//turtleMusterSimulation.generateDailyRatios(beginDate,endDate);
		
		Map<String,Map<String,String>> results = turtleSimulationRepository.getDailyMeans();
		System.out.println(results);
	}
	
	@Test
	public void generateRecords() {
		turtleMusterSimulationAnalysis.generateRecords("hlb");
	}


	
}
