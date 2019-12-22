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
	public void generateHua() {
		LocalDate beginDate = LocalDate.parse("2019-01-01");
		LocalDate endDate = LocalDate.parse("2019-12-18");
		huaService.generateHua(beginDate, endDate, true);
	}
	
	//@Test
	public void test() {
		LocalDate date = LocalDate.parse("2019-11-11");
		Set<String> huas = huaService.getHua(date);
		System.out.println(huas);
	}
}
