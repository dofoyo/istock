package com.rhb.istock.item;

import java.util.List;

public interface ItemService {
	public void init();
	public List<Item> getItems();
	public List<String> getItemIDs();
	public Item getItem(String itemID);
	public void download();
	public String getTopic(String itemID);
	public String[] getTopicTops(Integer count);
}
