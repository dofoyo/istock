package com.rhb.istock.item;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.item.repository.ItemEntity;
import com.rhb.istock.item.repository.ItemRepository;

@Service("itemServiceImp")
public class ItemServiceImp implements ItemService {
	@Autowired
	@Qualifier("itemRepositoryTushare")
	ItemRepository itemRepository;

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
		ItemEntity entity = itemRepository.getItemEntity(itemID);
		if(entity!=null) {
			item = new Item();
			item.setItemID(entity.getItemId());
			item.setCode(entity.getCode());
			item.setName(entity.getName());
			item.setIndustry(entity.getIndustry());
			item.setArea(entity.getArea());
		}
		
		//System.out.println(item);
		return item;
	}

}
