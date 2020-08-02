package com.rhb.istock.index.tushare;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("indexRepositoryTushare")
public class IndexRepositoryTushare {
	@Value("${tushareKdataPath}")
	private String kdataPath;

	public	Set<IndexBasic> getIndexBasic(){
		Set<IndexBasic> ics = new HashSet<IndexBasic>();
		
		String fdataFile = kdataPath + "/index/basic.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				IndexBasic basic;
				String  ts_code,name,market,publisher,category,base_date,base_point,list_date;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					ts_code = item.get(0).toString();
					name = item.get(1).toString();
					market = item.get(2).toString();
					publisher = item.get(3).toString();
					category = item.get(4).toString();
					base_date = item.get(5).toString();
					base_point = item.get(6).toString();
					list_date = item.get(7).toString();
					basic = new IndexBasic(ts_code,name,market,publisher,category,base_date,base_point,list_date);
					ics.add(basic);
				}
			}
		}
		
		return ics;
	}
	
	
	public Map<String,Set<IndexWeight>> getIndexWeights(){
		Map<String,Set<IndexWeight>> members = new HashMap<String,Set<IndexWeight>>();
		Set<IndexBasic> basics = this.getIndexBasic();
		Set<IndexWeight> ms;
		for(IndexBasic basic : basics) {
			ms = this.getIndexWeights(basic.getTs_code());
			members.put(basic.getTs_code(), ms);
		}
		
		return members;
	}
	
	
	public	Set<IndexWeight> getIndexWeights(String ts_code){
		Set<IndexWeight> members = new HashSet<IndexWeight>();
		
		String fdataFile = kdataPath + "/index/"+ts_code+".json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				IndexWeight member;
				String con_code,trade_date,weight, previous_trade_date=null;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					ts_code = item.getString(0);
					con_code = item.getString(1);
					trade_date = item.get(2).toString();
					weight = item.get(3).toString();
					
					if(previous_trade_date!=null && !trade_date.equals(previous_trade_date)) {
						break;
					}
					
					member = new IndexWeight(ts_code,con_code,trade_date,weight);
					members.add(member);

					previous_trade_date = trade_date;
				}
			}
		}
		
		return members;
	}
	
	public IndexData getIndexDatas(String ts_code) {
		IndexData kdata = new IndexData(ts_code);

		String kdataFile = kdataPath + "/index/" + ts_code + "_kdatas.json";

		if(FileTools.isExists(kdataFile)) {
			String trade_date,close,open,high,low,pre_close,change,pct_chg,vol,amount;
			JSONObject data = new JSONObject(FileTools.readTextFile(kdataFile));
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					trade_date = item.get(1).toString();
					close = item.get(2).toString();
					open = item.get(3).toString();
					high = item.get(4).toString();
					low = item.get(5).toString();
					pre_close = item.get(6).toString();
					change = item.get(7).toString();
					pct_chg = item.get(8).toString();
					vol = item.get(8).toString();
					amount = item.get(10).toString();
						
					kdata.addBar(trade_date,close, open, high, low, pre_close, change, pct_chg, vol, amount);
				}
			}
		}
		
		return kdata;
	}
	
	public IndexData getIndexDatas(String ts_code, LocalDate endDate, Integer count) {
		IndexData newData = new IndexData(ts_code);
		
		LocalDate date = endDate.minusDays(1);
		IndexData allData = this.getIndexDatas(ts_code);
		IndexBar bar;
		for(int i=0,j=0; i<count && j<allData.getBarSize(); j++) {
			bar = allData.getBar(date);
			if(bar!=null) {
				newData.addBar(date,bar);
				i++;
			}
			date = date.minusDays(1);
		}
		return newData;
	}
	
	public IndexData getIndexDatas(String ts_code, Set<LocalDate> dates) {
		IndexData newData = new IndexData(ts_code);
		
		IndexData allData = this.getIndexDatas(ts_code);
		IndexBar bar;
		for(LocalDate date : dates) {
			bar = allData.getBar(date);
			if(bar!=null) {
				newData.addBar(date,bar);
			}else {
				return null;  //如果为null, 说明该指数不全，不能用
			}
		}
		return newData;
	}	
	
}
