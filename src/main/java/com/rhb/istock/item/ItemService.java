package com.rhb.istock.item;

import java.util.List;

public interface ItemService {
	public List<Item> getItems();
	public Item getItem(String itemID);
	public void download();
	public String getTopic(String itemID);
	public String[] getTopicTops(Integer count);
}
