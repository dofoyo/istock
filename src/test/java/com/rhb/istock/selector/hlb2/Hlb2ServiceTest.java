package com.rhb.istock.selector.hlb2;

import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class Hlb2ServiceTest {
	@Autowired
	@Qualifier("hlb2Service")
	Hlb2Service hlb2Service;
	
	@Test
	public void generateHLB2() {
		LocalDate endDate = LocalDate.parse("2020-02-28");
		hlb2Service.generateHLB2(endDate,2);
	}
	
	//@Test
	public void getLpb() {
		String  str = hlb2Service.getHLB2();
		System.out.println(str);
	}
}
