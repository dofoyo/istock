package com.rhb.istock.item;

import java.time.LocalDate;
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
public class ItemServiceTest {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	//@Test
	public void downTopics() {
		//itemService.downTopics();
		itemService.downItems();
	}
	
	//@Test
	public void getTopic() {
		String itemID = "sz300022";
		String topic = itemService.getTopic(itemID);
		System.out.println(topic);
	}
	
	@Test
	public void getItem() {
		String itemID = "sz000858";
		Item item = itemService.getItem(itemID);
		System.out.println(item);
	}
	
	//@Test
	public void getItems() {
		Map<String,Item> items = itemService.getItems();
		for(Item item : items.values()) {
			System.out.println(item.getItemID() + "," + item.getName() + "," + item.getIndustry());
			
		}
	}
	
	//@Test
	public void getSz50() {
		LocalDate date = LocalDate.parse("2019-11-01");
		List<String> items = itemService.getSz50(date);
		for(String item : items) {
			System.out.println(item);
		}
	}
	
	//@Test
	public void getHs300() {
		LocalDate date = LocalDate.parse("2005-04-08");
		List<String> items = itemService.getHs300(date);
		for(String item : items) {
			System.out.println(item);
		}
	}
	
}
