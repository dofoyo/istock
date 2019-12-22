package com.rhb.istock.selector;

import java.math.BigDecimal;
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
public class SelectorTest {
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorServiceImp;
	
	//@Test
	public void getMCSTs() {
		String itemID = "sz002456";
		Map<LocalDate, BigDecimal> mcsts = selectorServiceImp.getMCSTs(itemID, false);
		for(Map.Entry<LocalDate, BigDecimal> mcst : mcsts.entrySet()) {
			System.out.printf("%tF %.2f\n",mcst.getKey(), mcst.getValue());
		}
	}
	
	//@Test
	public void getBOLLs() {
		String itemID = "sz002456";
		Integer period = 21;
		Map<LocalDate, BigDecimal[]> bolls = selectorServiceImp.getBOLLs(itemID,period, false);
		for(Map.Entry<LocalDate, BigDecimal[]> mcst : bolls.entrySet()) {
			System.out.printf("%tF, %.2f, %.2f, %.2f\n ", mcst.getKey(), mcst.getValue()[0], mcst.getValue()[1], mcst.getValue()[2]);
		}
	}
	
	//@Test
	public void getMACDs() {
		String itemID = "sz002456";
		Map<LocalDate, BigDecimal[]> macds = selectorServiceImp.getMACDs(itemID, false);
		for(Map.Entry<LocalDate, BigDecimal[]> macd : macds.entrySet()) {
			System.out.printf("%tF, dif=%.2f, dea=%.2f, macd=%.2f\n ", macd.getKey(), macd.getValue()[0], macd.getValue()[1], macd.getValue()[2]);
		}
	}
		
	
	@Test
	public void getHuaFirst() {
		String itemID = "sh601319";
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-12-18");
		List<LocalDate> results = selectorServiceImp.getHuaFirst(itemID, beginDate, endDate);
		for(LocalDate date : results) {
			System.out.println(date);
		}
	}
} 
