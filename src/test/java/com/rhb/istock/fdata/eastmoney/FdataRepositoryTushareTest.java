package com.rhb.istock.fdata.eastmoney;

import java.util.Map;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataRepositoryTushareTest {

	@Autowired
	@Qualifier("fdataRepositoryEastmoney")
	FdataRepositoryEastmoney fdataRepositoryEastmoney;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Test
	public void getCashflows() {
		String itemID = "sz002610";
		Map<String,String[]> forcasts = fdataRepositoryEastmoney.getForcasts(itemID);
		String rq, yyzsr, yylr;
		for(Map.Entry<String, String[]> entry : forcasts.entrySet()) {
			rq = entry.getKey();
			yyzsr = entry.getValue()[0];
			yylr = entry.getValue()[1];
			System.out.println(rq + "," + yyzsr +  ", " + yylr );
		}
	}
	
}
