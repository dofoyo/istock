package com.rhb.istock.selector.hlt;

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
public class HighLowTopServiceTest {
	@Autowired
	@Qualifier("highLowTopServiceImp")
	HighLowTopService highLowTopService;
	
	@Test
	public void generateBluechip() {
		highLowTopService.generateHighLowTops();
	}
	
	//@Test
	public void getBluechipIDs() {
		LocalDate today = LocalDate.now();
		List<String> ids = highLowTopService.getHighLowTops(20);
		System.out.println(ids);
	}
}
