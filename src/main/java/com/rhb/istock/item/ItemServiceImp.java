package com.rhb.istock.item;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.HttpClient;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.repository.Component;
import com.rhb.istock.item.repository.ComponentRepository;
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
	
	@Autowired
	@Qualifier("componentRepositoryImp")
	ComponentRepository componentRepository;
	
	private Map<String,String> topics = new HashMap<String,String>();
	protected static final Logger logger = LoggerFactory.getLogger("");

	@Override
	public Map<String, Item> getItems() {
		Map<String, Item> items = new HashMap<String, Item>();
		
		Item item;
		List<ItemEntity> entities = itemRepository.getItemEntities();
		for(ItemEntity entity : entities) {
			item = new Item();
			item.setItemID(entity.getItemId());
			item.setCode(entity.getCode());
			item.setName(entity.getName());
			item.setIndustry(entity.getIndustry());
			item.setArea(entity.getArea());
			item.setIpo(entity.getIpo());
			item.setCagr(entity.getCagr());
			items.put(entity.getItemId(),item);
		}
		return items;
	}

	@Override
	public Item getItem(String itemID) {
		//System.out.println(itemID);
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
				item.setIpo(entity.getIpo());
				item.setCagr(entity.getCagr());
				break;
			}
		}
		
		//System.out.println(item);

		return item;
	}

	@Override
	public void downItems() {
		try {
			itemSpider.downItems();
			itemRepository.cacheEvict();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	@Override
	public String getTopic(String itemID) {
		if(!topics.containsKey(itemID)) {
			String topic = itemSpider.getTopic(itemID);
			topics.put(itemID, topic);
			itemRepository.saveTopic(itemID, topic);
			//为避免被反扒工具禁止，需要暂停一下
			HttpClient.sleep(3);
		}
		return topics.get(itemID);
	}

	@Override
	public String[] getTopicTops(Integer count) {
		return itemSpider.getTopicTops(count);
	}

	@Override
	public List<String> getItemIDs() {
		List<String> ids = new ArrayList<String>();
		for(Item item : this.getItems().values()) {
			ids.add(item.getItemID());
		}
		return ids;
	}

	@Override
	public void init() {
		logger.info("itemService init...");
		topics.putAll(itemRepository.getTopics());
		logger.info("there are " + topics.size() + " topics ready!");
	}

	@Override
	public Set<String> getIndustrys() {
		Set<String> industrys = new HashSet<String>();
		Map<String,Item> items = this.getItems();
		for(Item item : items.values()) {
			industrys.add(item.getIndustry());
		}
		
		return industrys;
	}

	@Override
	public List<String> getSz50(LocalDate date) {
		List<String> items = new ArrayList<String>();
		Item item;
		List<Component> components = componentRepository.getSz50Components();
		for(Component component : components) {
			if((date.isAfter(component.getBeginDate()) || date.isEqual(component.getBeginDate()))
					&& (date.isBefore(component.getEndDate()) || date.isEqual(component.getEndDate()))
				) {
				items.add(component.getItemID());
			}
		}
		return items;
	}

	@Override
	public List<String> getHs300(LocalDate date) {
		List<String> items = new ArrayList<String>();
		Item item;
		List<Component> components = componentRepository.getHs300Components();
		for(Component component : components) {
			if((date.isAfter(component.getBeginDate()) || date.isEqual(component.getBeginDate()))
					&& (date.isBefore(component.getEndDate()) || date.isEqual(component.getEndDate()))
				) {
				items.add(component.getItemID());
			}
		}
		return items;
	}

	@Override
	public void cacheEvict() {
		itemRepository.cacheEvict();		
	}

	@Override
	public void downTopics() {
		long beginTime=System.currentTimeMillis(); 
		
		itemRepository.emptyTopic();
		
		List<String> ids = this.getItemIDs();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, "  downTopic: " + id);
			
			String topic = itemSpider.getTopic(id);
			itemRepository.saveTopic(id, topic);
			HttpClient.sleep(3);

		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("downTopics 用时：" + used + "秒");          
	}

	@Override
	@Cacheable("dimensions")
	public Map<String,Dimension> getDimensions() {
		Map<String,Dimension> dimensions = new HashMap<String,Dimension>();
		Dimension industry = new Dimension();
		Dimension area = new Dimension();
		Dimension market = new Dimension();
		Dimension topic = new Dimension();
		Dimension average = new Dimension();
		
		Map<String,Item> items = this.getItems();
		for(Item item : items.values()) {
			industry.put(item.getIndustry(), item.getItemID(), item.getName());
			area.put(item.getArea(), item.getItemID(), item.getName());
			market.put(item.getMarket(), item.getItemID(), item.getName());
			topic.put(this.getTopic(item.getItemID()).split("，"), item.getItemID(), item.getName());
			average.put("average", item.getItemID(), item.getName());
		}

		dimensions.put("industry", industry);
		dimensions.put("area", area);
		dimensions.put("market", market);
		dimensions.put("topic", topic);
		dimensions.put("average", average);

		return dimensions;
	}
}
