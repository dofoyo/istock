package com.rhb.istock.selector.lpb;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class LpbServiceTest {
	@Autowired
	@Qualifier("lpbService")
	LpbService lpbService;
	
	//@Test
	public void generateLPB() {
		LocalDate endDate = LocalDate.parse("2020-02-05");
		lpbService.generateLPB(endDate,13);
	}
	
	@Test
	public void getLpb() {
		String  str = lpbService.getLpb();
		System.out.println(str);
	}
}
