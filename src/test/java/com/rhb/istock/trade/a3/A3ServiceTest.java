package com.rhb.istock.trade.a3;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class A3ServiceTest {
	@Autowired
	@Qualifier("a3Service")
	A3Service a3Service;
	
	@Test
	public void test() {
		//LocalDate bDate = LocalDate.parse("2017-01-28");
		LocalDate bDate = LocalDate.parse("2020-01-25");
		LocalDate eDate = LocalDate.now();

		a3Service.run(bDate, eDate);
	}
	
}
