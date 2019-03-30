package com.rhb.istock.trade.twin.simulation;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.FileUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class TwinSimulationTest {
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("twinSimulationImp")
	TwinSimulation twinSimulation;
	
	@Test
	public void test() {
		Map<String,String> result = twinSimulation.simulate();

		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("CAGR: " + result.get("cagr"));
		System.out.println("winRatio: " + result.get("winRatio"));
		
		FileUtil.writeTextFile(reportPath + "/twinSimulation" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);

	}
}
