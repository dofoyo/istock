package com.rhb.istock.item.repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("itemRepositoryTushare")
public class ItemRepositoryTushare implements ItemRepository{
	@Value("${tushareItemsFile}")
	private String itemsFile;

	@Value("${topicsFile}")
	private String topicsFile;
	
	@Override
	@Cacheable("items")
	public List<ItemEntity> getItemEntities() {
		List<ItemEntity> entities = new ArrayList<ItemEntity>();
		JSONArray item = null;
		try {
			JSONObject data = new JSONObject(FileTools.readTextFile(itemsFile));
			JSONArray theItems = data.getJSONArray("items");
			if(theItems.length()>0) {
				ItemEntity entity;
				for(int i=0; i<theItems.length(); i++) {
					item = theItems.getJSONArray(i);
					entity = new ItemEntity();
					entity.setCode(item.get(0).toString());
					entity.setName(item.get(1).toString());
					entity.setArea(item.get(2).toString());
					entity.setIndustry(item.get(3).toString());
					entity.setIpo(item.get(4).toString());
					entities.add(entity);
				}
			}
		} catch (Exception e) {
			System.err.println(item);
			//e.printStackTrace();
		}
		
		return entities;
	}

	//@Override
	public ItemEntity getItemEntity(String itemID) {
		ItemEntity itemEntity = null;
		try {
			JSONObject data = new JSONObject(FileTools.readTextFile(itemsFile));
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
			JSONObject data = new JSONObject(FileTools.readTextFile(itemsFile));
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

	@Override
	@CacheEvict(value="items",allEntries=true)
	public void cacheEvict() {}

	@Override
	public void saveTopic(String itemID, String topic) {
		topic = topic.replaceAll(":", "：");
		
		FileTools.writeTextFile(topicsFile, itemID + ":" + topic + "\n", true);
	}

	@Override
	public Map<String, String> getTopics() {
		Map<String,String> topics = new HashMap<String,String>();
		String[] lines = FileTools.readTextFile(topicsFile).split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(":");
			topics.put(columns[0], columns.length>1 ? columns[1] : "");
		}
		return topics;
	}

}
