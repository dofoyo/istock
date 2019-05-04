package com.rhb.istock.item;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.item.repository.ItemEntity;
import com.rhb.istock.item.repository.ItemRepository;
import com.rhb.istock.item.spider.ItemSpider;

@Service("itemServiceImp")
public class ItemServiceImp implements ItemService {
	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;

	@Autowired
	@Qualifier("itemSpiderTushare")
	ItemSpider itemSpider;
	
	@Override
	public List<Item> getItems() {
		List<Item> items = new ArrayList<Item>();
		
		Item item;
		List<ItemEntity> entities = itemRepository.getItemEntities();
		for(ItemEntity entity : entities) {
			item = new Item();
			item.setItemID(entity.getItemId());
			item.setCode(entity.getCode());
			item.setName(entity.getName());
			item.setIndustry(entity.getIndustry());
			item.setArea(entity.getArea());
			
			items.add(item);
		}
		return items;
	}

	@Override
	public Item getItem(String itemID) {
		Item item = null;
		
		List<ItemEntity> entities = itemRepository.getItemEntities();
		for(ItemEntity entity : entities) {
			if(entity.getItemId().equals(itemID)) {
				item = new Item();
				item.setItemID(entity.getItemId());
				item.setCode(entity.getCode());
				item.setName(entity.getName());
				item.setIndustry(entity.getIndustry());
				item.setArea(entity.getArea());
				break;
			}
		}
		return item;
	}

	@Override
	public void download() {
		try {
			itemSpider.download();
			itemRepository.cacheEvict();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

}
