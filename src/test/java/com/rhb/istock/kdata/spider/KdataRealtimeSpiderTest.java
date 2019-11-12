package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
		String id = "sh000001";
		Map<String,String> data = kdataRealtimeSpider.getLatestMarketData(id);
		System.out.println(data);
		
	}

}
