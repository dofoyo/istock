package com.rhb.istock.fdata.eastmoney;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.tushare.FdataSpiderTushare;
import com.rhb.istock.item.repository.ItemEntity;
import com.rhb.istock.item.repository.ItemRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FdataSpiderEastmoneyTest {
	@Autowired
	@Qualifier("fdataSpiderEastmoney")
	FdataSpiderEastmoney fdataSpiderEastmoney;

	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;

	@Autowired
	@Qualifier("fdataRepositoryEastmoney")
	FdataRepositoryEastmoney fdataRepositoryEastmoney;
	
	@Test
	public void getRecommendations() {
		LocalDate date = LocalDate.parse("2020-10-27");
		Map<String, Integer> results = fdataRepositoryEastmoney.getRecommendations(date);
		System.out.println(results);

	}
	
	//@Test
	public void generateRecommendation() {
		fdataRepositoryEastmoney.generateRecommendation();
	}
	
	//@Test
	public void downForecast() throws Exception {
/*		String code = "603345";
		
		Integer year = 2020;
		JSONArray str = fdataSpiderEastmoney.downReports(code, year);
		System.out.println(str);
		System.out.println(str.length());
		
		 year = 2019;
		 str = fdataSpiderEastmoney.downReports(code, year);
		System.out.println(str);
		System.out.println(str.length());
*/
		fdataSpiderEastmoney.downReports();
	
	}

}
