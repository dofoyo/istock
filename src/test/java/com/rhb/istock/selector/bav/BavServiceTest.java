package com.rhb.istock.selector.bav;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BavServiceTest {
	@Autowired
	@Qualifier("bavService")
	BavService bavService;
	
	@Test
	public void generateBAV() {
		LocalDate endDate = LocalDate.parse("2020-01-11");
		bavService.generateBAV(endDate,13);
	}
}
