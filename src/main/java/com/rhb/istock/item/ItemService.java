package com.rhb.istock.item;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ItemService {
	public void init();
	public Map<String, Item> getItems();
	public List<String> getItemIDs();
	public Item getItem(String itemID);
	public void downItems();
	public String getTopic(String itemID);
	public void downTopics();
	public String[] getTopicTops(Integer count);
	public Set<String> getIndustrys();
	public Map<String,Dimension> getDimensions();
	public List<String> getSz50(LocalDate date);
	public List<String> getHs300(LocalDate date);
	public void cacheEvict();
}
