package com.rhb.istock.selector.bluechip;

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
public class BluechipServiceTest {
	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;
	
	//@Test
	public void generateBluechip() {
		bluechipService.generateBluechip();
	}
	
	@Test
	public void getBluechipIDs() {
		LocalDate today = LocalDate.now();
		List<String> ids = bluechipService.getBluechipIDs(today);
		System.out.println(ids);
	}
}
