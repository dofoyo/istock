package com.rhb.istock.item.repository;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("itemRepositoryTushare")
public class ItemRepositoryTushare implements ItemRepository{
	@Value("${tushareItemsFile}")
	private String itemsFile;
	
	@Override
	public List<ItemEntity> getItemEntities() {
		List<ItemEntity> entities = new ArrayList<ItemEntity>();
		try {
			JSONObject data = new JSONObject(FileUtil.readTextFile(itemsFile));
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				ItemEntity entity;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					entity = new ItemEntity();
					entity.setCode(item.getString(0));
					entity.setName(item.getString(1));
					entity.setArea(item.getString(2));
					entity.setIndustry(item.getString(3));
					entity.setIpo(item.getString(4));
					entities.add(entity);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return entities;
	}

	@Override
	public ItemEntity getItemEntity(String itemID) {
		ItemEntity itemEntity = null;
		try {
			JSONObject data = new JSONObject(FileUtil.readTextFile(itemsFile));
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					//System.out.println(itemID.substring(0) + "," + item.getString(1));
					if(itemID.substring(2).equals(item.getString(0))) {
						itemEntity = new ItemEntity();
						itemEntity.setItemId(itemID);
						itemEntity.setCode(item.getString(0));
						itemEntity.setName(item.getString(1));
						itemEntity.setArea(item.getString(2));
						itemEntity.setIndustry(item.getString(3));
						itemEntity.setIpo(item.getString(4));
						break;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//System.out.println(itemEntity);
		return itemEntity;
	}

	@Override
	public List<String> getItemIDs() {
		List<String> ids = new ArrayList<String>();
		try {
			JSONObject data = new JSONObject(FileUtil.readTextFile(itemsFile));
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String code;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					code = item.getString(0);
					ids.add(code.indexOf("60")==0 ? "sh"+code : "sz"+code);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return ids;
	}

}
