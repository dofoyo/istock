package com.rhb.istock.trade.turtle.simulation.repository;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.turtle.simulation.DailyItem;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SimulationRepositoryTest {

	@Autowired
	@Qualifier("simulationRepositoryImp")
	SimulationRepository simulationRepository;
	
	//@Test
	public void getDailyAmountTops() {
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-03-14");
		DailyItem dailyItem = simulationRepository.getDailyAmountTops(1, beginDate, endDate);
		System.out.println(dailyItem);
	}
	
	//@Test
	public void generateDailyAmountTops() {
		simulationRepository.generateDailyAmountTops(50);
		System.out.println("done");
	}
	
	@Test
	public void generateAvarageAmountTops() {
		simulationRepository.generateAvarageAmountTops(50);
		System.out.println("done");
	}
}
