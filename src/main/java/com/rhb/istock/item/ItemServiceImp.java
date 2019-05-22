package com.rhb.istock.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.HttpClient;
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
	
	private Map<String,String> topics = new HashMap<String,String>();
	
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
		//System.out.println(itemRepository.getItemIDs().size());
		List<ItemEntity> entities = itemRepository.getItemEntities();
		//System.out.println(entities.size());
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

	@Override
	public String getTopic(String itemID) {
		if(!topics.containsKey(itemID)) {
			topics.put(itemID, itemSpider.getTopic(itemID));
			
			//为避免被反扒工具禁止，需要暂停一下
			HttpClient.sleep(5);
		}
		return topics.get(itemID);
	}

}
