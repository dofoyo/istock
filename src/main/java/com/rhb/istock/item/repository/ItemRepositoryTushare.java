package com.rhb.istock.item.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.fdata.eastmoney.FdataRepositoryEastmoney;

@Service("itemRepositoryTushare")
public class ItemRepositoryTushare implements ItemRepository{
	@Value("${tushareItemsFile}")
	private String itemsFile;

	@Value("${topicsFile}")
	private String topicsFile;
	
	@Autowired
	@Qualifier("fdataRepositoryEastmoney")
	FdataRepositoryEastmoney fdataRepositoryEastmoney;
	
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
					entity.setCode(item.get(1).toString());
					entity.setName(item.get(2).toString());
					entity.setArea(item.get(3).toString());
					entity.setIndustry(item.get(4).toString());
					entity.setIpo(item.get(6).toString());
					entity.setCagr(fdataRepositoryEastmoney.getCAGR(entity.getItemId()));
					entities.add(entity);
					
					//System.out.println(entity);  //***************
				}
			}
		} catch (Exception e) {
			System.err.println(item);
			//e.printStackTrace();
		}
		
		Collections.sort(entities, new Comparator<ItemEntity>() {

			@Override
			public int compare(ItemEntity o1, ItemEntity o2) {
				return o1.getItemId().compareTo(o2.getItemId());
			}
			
		});
		
		return entities;
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

	@Override
	public void emptyTopic() {
		FileTools.writeTextFile(topicsFile, "", false);
	}

}
