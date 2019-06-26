package com.rhb.istock.selector.aat;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class AverageAmountTopServiceTest {
	@Autowired
	@Qualifier("averageAmountTopServiceImp")
	AverageAmountTopService averageAmountTopService;
	
	@Test
	public void getLatestAverageAmountTops() {
		List<String> ids = averageAmountTopService.getLatestTops(100);
		for(String id : ids) {
			if(id.startsWith("sh")){
				System.out.println(id);			
			}
		}
	}
}
