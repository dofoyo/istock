package com.rhb.istock.trade.twin.service;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.trade.twin.TwinService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TwinServiceTest {
	@Autowired
	@Qualifier("twinServiceImp")
	TwinService twinService;
	
	@Test
	public void generateTradeList() {
		twinService.generateTradeList();
		System.out.println("done.");
	}
	
	//@Test
	public void getTradeList() {
		LocalDate date = LocalDate.parse("2019-03-21");
		List<String> list = twinService.getOpenList(date);
		System.out.println(list);
		System.out.println("done.");
	}
	
}
