package com.rhb.istock.item;

import java.util.List;

public interface ItemService {
	public List<Item> getItems();
	public Item getItem(String itemID);
	
}