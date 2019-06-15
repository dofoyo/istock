package com.rhb.istock.trade.turtle.simulation.muster;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.turtle.simulation.muster.TurtleMusterSimulation.Ratio;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TurtleMusterSimulationTest {
	@Autowired
	@Qualifier("turtleMusterSimulation")
	TurtleMusterSimulation turtleMusterSimulation;
	
	//@Test
	public void simulate() {
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-06-01");
		turtleMusterSimulation.simulate(beginDate, endDate);
	}
	
	@Test
	public void test() {
		String type = "dtb";
		Integer total = 0;
		TreeMap<LocalDate, Ratio> result = turtleMusterSimulation.calculateSecondDayWinRatio(type);
		for(Map.Entry<LocalDate, Ratio> entry : result.entrySet()) {
			total = total + entry.getValue().getResult();
			System.out.println(entry.getKey() + "," + total);
		}
	
	}


	
}
