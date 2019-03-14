package com.rhb.istock.item.repository;

import java.util.List;

public interface ItemRepository {
	public List<String> getItemIDs();
	public List<ItemEntity> getItemEntities();
	public ItemEntity getItemEntity(String itemID);
}
