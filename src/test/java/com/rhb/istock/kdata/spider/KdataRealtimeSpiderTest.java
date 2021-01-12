package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.kdata.Kbar;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class KdataRealtimeSpiderTest {
	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;
	
	//@Test
	public void testIsTradeDate() {
		LocalDate date = LocalDate.now();
		System.out.println(kdataRealtimeSpider.isTradeDate1(date));
	}
	
	
	@Test
	public void getLatestMarketData() {
		String id = "sz002564";
		Map<String,String> data = kdataRealtimeSpider.getLatestMarketData(id);
		System.out.println(data);
		
	}
	
	//@Test
	public void getLatestMarketData_all() {
		Map<String,Kbar> bars = kdataRealtimeSpider.getLatestMarketData();
		System.out.println(bars);
	}

}
