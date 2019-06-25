package com.rhb.istock.selector.hlt;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class HighLowTopServiceTest {
	@Autowired
	@Qualifier("highLowTopServiceImp")
	HighLowTopService highLowTopService;
	
	@Test
	public void getLatestHighLowTops() {
		List<String> ids = highLowTopService.getLatestTops(100);
		for(String id: ids) {
			if(id.startsWith("sh")){
				System.out.println(id);
			}
		}
	}
	
/*	//@Test
	public void generateHighLowTops() {
		highLowTopService.generateHighLowTops();
	}
	
	//@Test
	public void getHighLowTops() {
		LocalDate beginDate = LocalDate.parse("2019-03-01");
		LocalDate endDate = LocalDate.parse("2019-03-21");
		for(LocalDate date = beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			List<String> ids = highLowTopService.getHighLowTops(10, date);
			System.out.println(ids);			
		}
	}*/
}
