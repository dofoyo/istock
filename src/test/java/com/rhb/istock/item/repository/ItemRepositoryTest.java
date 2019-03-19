package com.rhb.istock.item.repository;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class ItemRepositoryTest {
	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;
	
	//@Test
	public void testGetItemEntities() {
		List<ItemEntity> items = itemRepository.getItemEntities();
		for(ItemEntity item : items) {
			System.out.println(item);
		}
	}
	
	//@Test
	public void test() {
		String itemID = "sz300571";
		ItemEntity item = itemRepository.getItemEntity(itemID);
		System.out.println(item);
	}
}
