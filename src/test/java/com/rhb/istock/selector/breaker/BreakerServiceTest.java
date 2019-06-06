package com.rhb.istock.selector.breaker;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BreakerServiceTest {
	@Autowired
	@Qualifier("breakerService")
	BreakerService breakerService;
	
	//@Test
	public void generateBreakers() {
		breakerService.generateBreakers();
	}
	
	//@Test
	public void getBreakers() {
		Map<LocalDate,List<String>> breakers = breakerService.getBreakerIDs();
		for(Map.Entry<LocalDate, List<String>> entry : breakers.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
		
	}

	
	//@Test
	public void getBreakersSortByHL() {
		Map<LocalDate,List<String>> breakers = breakerService.getBreakersSortByHL();
		for(Map.Entry<LocalDate, List<String>> entry : breakers.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
	}
	
	@Test
	public void generateBreakersSortByHL() {
		LocalDate date = LocalDate.parse("2004-01-01");
		breakerService.generateBreakersSortByHL(date);
	}
	

	//@Test
	public void generateBreakersSortByDT() {
		LocalDate date = LocalDate.parse("2004-01-01");
		breakerService.generateBreakersSortByDT(date);
	}
	
	//@Test
	public void generateBreakersSortByAV() {
		LocalDate date = LocalDate.parse("2004-01-01");
		breakerService.generateBreakersSortByAV(date);
	}

}
