package com.rhb.istock.simulation;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class SimulationTest {

	@Value("${initCash}")
	private BigDecimal initCash;

	@Autowired
	@Qualifier("bdt")
	Simulate simulate;
	
	@Test
	public void test() {
		long beginTime=System.currentTimeMillis(); 

		LocalDate beginDate = LocalDate.parse("2020-04-19");
		LocalDate endDate = LocalDate.parse("2021-07-19");

		Integer top = 1000;
		boolean isAveValue = true;  //作市值平均
		Integer quantityType = 0;
		boolean isEvaluation = false;  //是模拟，不是评估

		try {
			simulate.run(beginDate, endDate, initCash, top, isAveValue, quantityType, isEvaluation);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("\nsimulate over, 用时：" + used + "秒");          

	}
	
}
