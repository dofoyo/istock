package com.rhb.istock.selector.aat;

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
public class AverageAmountTopServiceTest {
	@Autowired
	@Qualifier("averageAmountTopServiceImp")
	AverageAmountTopService averageAmountTopService;
	
	//@Test
	public void getLatestAverageAmountTops() {
		List<String> ids = averageAmountTopService.getLatestAverageAmountTops(10);
		System.out.println(ids);			
	}
	
	//@Test
	public void generateLatestAverageAmountTops() {
		averageAmountTopService.generateLatestAverageAmountTops();
	}
	
	@Test
	public void generateAverageAmountTops() {
		averageAmountTopService.generateAverageAmountTops();
	}
	
	//@Test
	public void getBluechipIDs() {
		LocalDate beginDate = LocalDate.parse("2019-03-01");
		LocalDate endDate = LocalDate.parse("2019-03-21");
		for(LocalDate date = beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			List<String> ids = averageAmountTopService.getAverageAmountTops(10, date);
			System.out.println(ids);			
		}
	}
}
