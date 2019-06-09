package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataRepositoryTest {
	@Autowired
	@Qualifier("kdataRepositorySina")
	KdataRepository kdataRepositorySina;

	@Autowired
	@Qualifier("kdataRepositoryDzh")
	KdataRepository kdataRepositoryDzh;

	@Autowired
	@Qualifier("kdataRepositoryTushare")
	KdataRepository kdataRepositoryTushare;
	
	@Autowired
	@Qualifier("kdataRepository163")
	KdataRepository kdataRepository163;
	
	@Test
	public void tt() {
		String itemID = "SZ300538";
		TreeMap<LocalDate,BigDecimal> factors = kdataRepositoryTushare.getFactors(itemID);

		LocalDate latestDate, previousDate;
		BigDecimal latestFactor, previousFactor;
		
		latestDate = factors.lastKey();
		latestFactor = factors.get(latestDate);
		previousDate = factors.lowerKey(latestDate);
		previousFactor = factors.get(previousDate);
		
		System.out.println(latestDate + "," + latestFactor);
		System.out.println(previousDate + "," + previousFactor);

	}
	
	
	//@Test
	public void testGetDailyKdata() {
		String id="sz000620";
		LocalDate date = LocalDate.parse("2019-04-09");

/*		System.out.println("kdataRepositoryDzh");
		KdataEntity kdata1 = kdataRepositoryDzh.getDailyKdata(id);
		System.out.println(kdata1.getBar(date));

		System.out.println("kdataRepositorySina");
		KdataEntity kdata2 = kdataRepositorySina.getDailyKdata(id);
		System.out.println(kdata2.getBar(date));*/

		System.out.println("kdataRepositoryTushare");
		KdataEntity kdata3 = kdataRepositoryTushare.getDailyKdata(id);
		System.out.println(kdata3.getBar(date));
	
	}
	
	//@Test
	public void testGetDailyKdataFrom163() {
		String id="sh000001";
		LocalDate date = LocalDate.parse("2017-02-17");

		System.out.println("kdataRepository163");
		KdataEntity kdata3 = kdataRepository163.getDailyKdata(id);
		System.out.println(kdata3.getBar(date));		
	}
	
}
