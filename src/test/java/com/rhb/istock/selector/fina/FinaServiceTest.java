package com.rhb.istock.selector.fina;

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
public class FinaServiceTest {
	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	//@Test
	public void generateQuarterCompare() {
		//String itemID = "sz300022";
		//String begin = "20190331";
		//String indicator_date = "20200331";
		//String forecast_date = "20200630";
		//Integer ratio = 100;
		finaService.generateQuarterCompares();

	}
	
	//@Test
	public void getQuarterCompares() {
		Map<String,QuarterCompare> qcs = finaService.getForecasts();
		for(Map.Entry<String, QuarterCompare> entry : qcs.entrySet()) {
			System.out.println(entry.getValue().getTxt());
		}
	}
	
	@Test
	public void generateNewPE() {
		String date = "20191231";
		List<NewPE> pes = finaService.generateNewPE(date);
		int i=0;
		for(NewPE pe : pes) {
			System.out.println(pe);
			if(i++ > 100) {
				break;
			}
		}
	}
}
