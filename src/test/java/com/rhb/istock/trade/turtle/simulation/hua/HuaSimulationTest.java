package com.rhb.istock.trade.turtle.simulation.hua;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class HuaSimulationTest {
	@Autowired
	@Qualifier("huaSimulation")
	HuaSimulation huaSimulation;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	
	//@Test
	public void simulationByID() {
		String itemID = "sh600673";
		BigDecimal mcst_ratio = new BigDecimal(-0.13);
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-12-01");
		Integer period = 5;
		Integer[] wins_total = huaSimulation.simulate(itemID, mcst_ratio, beginDate, endDate, period);
		System.out.println("wins/total: " + wins_total[0] + "/" + wins_total[1]);
	}
	
	@Test
	public void simulation() {
		BigDecimal mcst_ratio = new BigDecimal(-0.13);
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-12-31");
		Integer period = 5;
		Integer[] wins_total = huaSimulation.simulate(mcst_ratio, beginDate, endDate, period);
		System.out.println("wins/total: " + wins_total[0] + "/" + wins_total[1]);
	}
}
