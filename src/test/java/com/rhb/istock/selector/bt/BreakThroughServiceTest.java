package com.rhb.istock.selector.bt;

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
public class BreakThroughServiceTest {
	@Autowired
	@Qualifier("breakThroughService")
	BreakThroughService breakThroughService;

	//@Test
	public void testGetTmpLatestBreakers() {
		List<String> breakers = breakThroughService.getTmpLatestBreakers();
		System.out.println(breakers);
	}
	
	//@Test
	public void testGetLatestBreakers() {
		System.out.println(breakThroughService.getLatestBreakers());
	}
	
	@Test
	public void testGenerateLatestBreakersWithLatestKdata() {
		breakThroughService.generateTmpLatestBreakers();
	}
	
	@Test
	public void testGenerateLatestBreakers() {
		breakThroughService.generateLatestBreakers();
	}
	
	//@Test
	public void testGenerateBreakers() {
		breakThroughService.generateBreakers();
	}
	
	//@Test
	public void testGetBreaks() {
		Map<LocalDate,List<String>> breakers = breakThroughService.getBreakers();
		for(Map.Entry<LocalDate, List<String>> entry : breakers.entrySet()) {
			System.out.println(entry.getKey());
			System.out.println(entry.getValue());
		}
		
	}
}
