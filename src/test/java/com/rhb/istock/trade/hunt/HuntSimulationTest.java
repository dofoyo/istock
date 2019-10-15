package com.rhb.istock.trade.hunt;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class HuntSimulationTest {
	@Autowired
	@Qualifier("huntSimulation")
	HuntSimulation huntSimulation;
	
	@Test
	public void simulate() {
		LocalDate beginDate = LocalDate.parse("2010-01-01");
		LocalDate endDate = LocalDate.parse("2019-09-16");
		huntSimulation.simulate(beginDate, endDate);
	}
	
}
