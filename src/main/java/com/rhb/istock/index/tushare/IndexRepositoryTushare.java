package com.rhb.istock.index.tushare;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.repository.KdataEntity;

@Service("indexRepositoryTushare")
public class IndexRepositoryTushare {
	@Value("${tushareKdataPath}")
	private String kdataPath;

	@Value("${indexFile}")
	private String indexFile;
	
	public String[] getTsCodes(){
		return FileTools.readTextFile(indexFile).split(",");
	}
	
	public void saveIndexFile(String str) {
		FileTools.writeTextFile(indexFile, str, false);		
	}
	
	@Cacheable("indexBasics")
	public	Set<IndexBasic> getIndexBasics(){
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
		Set<IndexBasic> basics = this.getIndexBasics();
		Set<IndexWeight> ms;
		for(IndexBasic basic : basics) {
			ms = this.getIndexWeights(basic.getTs_code());
			members.put(basic.getTs_code(), ms);
		}
		
		return members;
	}
	
	@Cacheable("indexWeights")
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
	
	@CacheEvict(value="indexDatas",allEntries=true)
	public void evictIndexDatasCache() {}
	
	@Cacheable("indexDatas")
	public IndexData getIndexDatas(String ts_code) {
		IndexData kdata = new IndexData(ts_code);

		String kdataFile = kdataPath + "/index/" + ts_code + "_kdatas.json";

		if(FileTools.isExists(kdataFile)) {
			String trade_date,close,open,high,low,pre_close,vol,amount;
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
					vol = item.get(8).toString();
					amount = item.get(10).toString();
						
					kdata.addBar(trade_date,close, open, high, low, pre_close, vol, amount);
				}
			}
		}
		
		return kdata;
	}
	
	@Cacheable("indexKdatas")
	public KdataEntity getKdataByCache(String ts_code) {
		return this.getKdata(ts_code);
	}
	
	
	public KdataEntity getKdata(String ts_code) {
		KdataEntity kdata = new KdataEntity(ts_code);

		String kdataFile = kdataPath + "/index/" + ts_code + "_kdatas.json";

		if(FileTools.isExists(kdataFile)) {
			//String trade_date,close,open,high,low,pre_close,vol,amount;
			LocalDate date;
			BigDecimal open,high,low,close,amount,quantity;
			BigDecimal zero = BigDecimal.ZERO;

			JSONObject data = new JSONObject(FileTools.readTextFile(kdataFile));
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					date = LocalDate.parse(item.get(1).toString(),DateTimeFormatter.ofPattern("yyyyMMdd"));;
					close = item.getBigDecimal(2);
					open = item.getBigDecimal(3);
					high = item.getBigDecimal(4);
					low = item.getBigDecimal(5);
					//pre_close = item.getBigDecimal(6);
					quantity = item.getBigDecimal(8);
					amount = item.getBigDecimal(10);
					kdata.addBar(date,open,high,low,close,amount,quantity,zero,zero,zero,zero,zero,zero,zero,zero);
						
					//kdata.addBar(date,close, open, high, low, pre_close, vol, amount);
				}
			}
		}else {
			System.out.println(kdataFile + " NOT exist!");
		}
		
		//System.out.println(kdata.getBarSize());
		
		return kdata;
	}
	/*
	 * LocalDate date,
			BigDecimal open,
			BigDecimal high,
			BigDecimal low,
			BigDecimal close,
			BigDecimal amount,
			BigDecimal quantity,
			BigDecimal turnover_rate_f,
			BigDecimal volume_ratio,
			BigDecimal total_mv,
			BigDecimal circ_mv,
			BigDecimal total_share,
			BigDecimal float_share,
			BigDecimal free_share,
			BigDecimal pe
	 */
}
