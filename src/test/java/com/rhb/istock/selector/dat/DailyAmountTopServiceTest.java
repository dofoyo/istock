package com.rhb.istock.selector.dat;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class DailyAmountTopServiceTest {
	@Autowired
	@Qualifier("dailyAmountTopServiceImp")
	DailyAmountTopService dailyAmountTopService;
	
	@Test
	public void generateDailyAmountTops() {
		dailyAmountTopService.generateDailyAmountTops();
	}
	
	//@Test
	public void getBluechipIDs() {
		LocalDate beginDate = LocalDate.parse("2019-03-01");
		LocalDate endDate = LocalDate.parse("2019-03-21");
		for(LocalDate date = beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			List<String> ids = dailyAmountTopService.getDailyAmountTops(10, date);
			System.out.println(ids);			
		}
	}
}
