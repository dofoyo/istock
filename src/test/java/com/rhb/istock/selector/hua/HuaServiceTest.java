package com.rhb.istock.selector.hua;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class HuaServiceTest {
	@Autowired
	@Qualifier("huaService")
	HuaService huaService;

	//@Test
	public void generateMcstBreakers() {
		LocalDate endDate = LocalDate.parse("2019-12-14");
		BigDecimal ratio = new BigDecimal(0.05);
		Integer count = 13;
		huaService.generateMcstBreakers(endDate,count, ratio);
	}
	
	@Test
	public void generateHuaFirst() {
/*		LocalDate beginDate = LocalDate.parse("2010-01-01");
		LocalDate endDate = LocalDate.parse("2020-01-02");
		Integer boll_period = 21;  //表示多少日内，布林线突破过下轨
		BigDecimal mcst_ratio = new BigDecimal(-0.13);
		BigDecimal volume_r = new BigDecimal(2);
		huaService.generateHuaFirst(beginDate,endDate,boll_period, mcst_ratio,volume_r, false);
*/		
		huaService.generateLatestHuaFirst();
	}
	
	@Test
	public void generateHuaPotentials() {
		LocalDate endDate = LocalDate.parse("2020-01-09");
		Integer period = 13;
		BigDecimal mcst_ratio = new BigDecimal(0.13);
		huaService.generateHuaPotentials(endDate,period, mcst_ratio, false);
	}
	
	//@Test
	public void test() {
		LocalDate date = LocalDate.parse("2019-11-11");
		Set<String> huas = huaService.getHua(date);
		System.out.println(huas);
	}
}
