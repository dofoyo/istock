package com.rhb.istock.item.repository;

import java.util.List;
import java.util.Map;

public interface ItemRepository {
	public List<String> getItemIDs();
	public List<ItemEntity> getItemEntities();
	public void saveTopic(String itemID, String topic);
	public void emptyTopic();
	public Map<String,String> getTopics();
	public void cacheEvict();
}
