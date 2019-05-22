package com.rhb.istock.item;

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
	
	
	@Test
	public void getTopic() {
		String itemID = "sz300022";
		String topic = itemService.getTopic(itemID);
		System.out.println(topic);
	}
	
}
