package com.rhb.istock.item;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface ItemService {
	public void init();
	public List<Item> getItems();
	public List<String> getItemIDs();
	public Item getItem(String itemID);
	public void download();
	public String getTopic(String itemID);
	public String[] getTopicTops(Integer count);
	public Set<String> getIndustrys();
	public List<String> getSz50(LocalDate date);
}
