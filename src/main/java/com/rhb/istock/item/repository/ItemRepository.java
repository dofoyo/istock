package com.rhb.istock.item.repository;

import java.time.LocalDate;
import java.util.List;

public interface ItemRepository {
	public List<String> getItemIDs();
	public List<ItemEntity> getItemEntities();
	public ItemEntity getItemEntity(String itemID);
}
