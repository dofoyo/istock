package com.rhb.istock.index.tushare;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.HttpClient;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;

@Service("indexSpiderTushare")
public class IndexSpiderTushare {
	@Value("${tushareKdataPath}")
	private String kdataPath;

	@Value("${tushareUrl}")
	private String url;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	protected static final Logger logger = LoggerFactory.getLogger(IndexSpiderTushare.class);

	public JSONObject downIndex_basic() {
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "index_basic");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/index/basic.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);		
		
		return data;
	}
	
	public void downIndex_weight() {
		JSONObject data = this.downIndex_basic();
		if(data!=null) {
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String index_code;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					index_code = item.getString(0);
					this.downIndex_weight(index_code);
					
					
					try {
						Thread.sleep(1000);  //一分钟200个	
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
			}			
		}
	}
		
	public void downIndex_weight(String index_code) {
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "index_weight");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");

		JSONObject params = new JSONObject();
		params.put("index_code", index_code);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/index/"+index_code+".json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);		
	}

	public void downIndex_Daily() {
		JSONObject data = this.downIndex_basic();
		if(data!=null) {
			JSONArray items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String index_code;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					index_code = item.getString(0);

					Progress.show(items.length(),i++, " downIndex_Daily: " + index_code);//进度条
					
					this.downIndex_Daily(index_code);
					
					try {
						Thread.sleep(1000);  //一分钟200个	
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 
				}
			}			
		}
	}
	
	public void downIndex_Daily(String ts_code) {
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "index_daily");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", ts_code);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/index/" + ts_code + "_kdatas.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);
	}
}
