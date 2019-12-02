package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.rhb.istock.fdata.FinancialStatement;
import com.rhb.istock.fdata.FinancialStatementService;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class BluechipServiceTest {
	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;

	@Autowired
	@Qualifier("financialStatementServiceImp")
	FinancialStatementService financialStatementService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	
	//@Test
	public void generateBluechip() {
		bluechipService.generateBluechip();
	}
	
	@Test
	public void showBluechips() {
		Item item;

		LocalDate date = LocalDate.now();
		List<String> ids = bluechipService.getBluechipIDs(date);
		System.out.println(ids.size());
		for(String id : ids) {
			item = itemService.getItem(id);
			if(item!=null) {
				System.out.println(item.getItemID() + "," + item.getName());			
			}
		}
		System.out.println(ids.size());
	}
}
