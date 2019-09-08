package com.rhb.istock.fdata;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class FinancialStatementServiceTest {
	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	//@Test
	public void test() {
		financialStatementService.downloadReports();
	}
	
	@Test
	public void test1() {
		List<String> itemIDs = itemService.getItemIDs();
		Item item;
		FinancialStatement fs;
		Double revenue1,revenue2,revenue3;
		
		
		for(String id : itemIDs) {
			if(id.indexOf("sh688")==0) {
				item = itemService.getItem(id);
				fs = financialStatementService.getFinancialStatement(item.getCode());
				revenue1 = fs.getProfitstatements().get("20161231").getOperatingRevenue()/100000000;
				revenue2 = fs.getProfitstatements().get("20171231").getOperatingRevenue()/100000000;
				revenue3 = fs.getProfitstatements().get("20181231").getOperatingRevenue()/100000000;
				
				System.out.println(item.getName() + "(" + item.getCode() + ")" + "," + revenue1 + "," + revenue2 + "," + revenue3);
				
			}
		}
		
	}
}
