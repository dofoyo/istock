package com.rhb.istock.kdata.spider;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.HttpClient;
import com.rhb.istock.comm.util.Progress;

/*
* 注意季度交接日
* 最好每天23点前完成下载
*/

@Service("kdataSpiderTushare")
public class KdataSpiderTushare implements KdataSpider {
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public void downKdata(String itemID) {
		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "daily");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", tushareID);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/" + tushareID + ".json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);
	}
	
	@Override
	public void downFactor(String itemID) {
		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "adj_factor");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", tushareID);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/" + tushareID + "_factor.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);
	}

	@Override
	public void downKdatas(LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("KdataSpiderTushare.downloadKdata of " + date + "....");

		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "daily");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("trade_date", date.format(dtf));
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());

		String kdataFile = kdataPath + "/" + date + ".json";
		FileTools.writeTextFile(kdataFile, str, false);

		JSONArray items = (new JSONObject(str)).getJSONObject("data").getJSONArray("items");	
		
		if(items.length()>0) {
			JSONArray item;
			for(int i=0; i<items.length(); i++) {
				item = items.getJSONArray(i);

				Progress.show(items.length(),i,item.getString(0));

				setKdata(item);
			}
		}
		System.out.println("KdataSpiderTushare.downloadKdata of " + date + " done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		FileTools.writeTextFile(kdataFile, str, false);
	}
	
	@Override
	public void downFactors(LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("KdataSpiderTushare.downloadFactor of " + date + "....");
		
		String result = null;
		
		String kdataFile = kdataPath + "/" + date + "_factor.json";
		
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "adj_factor");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("trade_date", date.format(dtf));
		
		args.put("params", params);
		
		result = HttpClient.doPostJson(url, args.toString());
		
		FileTools.writeTextFile(kdataFile, result, false);
		
		if(result!=null) {
			JSONArray items = (new JSONObject(result)).getJSONObject("data").getJSONArray("items");	
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					Progress.show(items.length(),i,item.getString(0));
					
					setFactor(item);
				}
			}			
		}
		
		System.out.println("KdataSpiderTushare.downloadFactor of " + date + " done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	private void setKdata(JSONArray item) {
		if(item!=null && item.length()>0) {
			JSONObject data;
			JSONArray dataItems;
			String id = item.getString(0);
			String kdataFile = kdataPath + "/" + id+ ".json";
			File file = new File(kdataFile);
			if(file.exists()) {
				data = new JSONObject(FileTools.readTextFile(kdataFile));
				dataItems = data.getJSONArray("items");
			}else {
				//"fields":["ts_code","trade_date","open","high","low","close","pre_close","change","pct_chg","vol","amount"],
				data = new JSONObject();
				
				JSONArray dataFields = new JSONArray();
				dataFields.put("ts_code");
				dataFields.put("trade_date");
				dataFields.put("open");
				dataFields.put("high");
				dataFields.put("low");
				dataFields.put("close");
				dataFields.put("pre_close");
				dataFields.put("change");
				dataFields.put("pct_chg");
				dataFields.put("vol");
				dataFields.put("amount");
				data.put("fields",dataFields);
				
				//data.put("fields", "[\"ts_code\",\"trade_date\",\"open\",\"high\",\"low\",\"close\",\"pre_close\",\"change\",\"pct_chg\",\"vol\",\"amount\"]");
				
				dataItems = new JSONArray();
				data.put("items", dataItems);
			}
			dataItems.put(item);
			FileTools.writeTextFile(kdataFile, data.toString(), false);
		}
	}
	
	private void setFactor(JSONArray item) {
		if(item!=null && item.length()>0) {
			JSONObject data;
			JSONArray dataItems;
			String id = item.getString(0);
			String factorFile = kdataPath + "/" + id+ "_factor.json";
			File file = new File(factorFile);
			if(file.exists()) {
				data = new JSONObject(FileTools.readTextFile(factorFile));
				dataItems = data.getJSONArray("items");
			}else {
				//"fields":["ts_code","trade_date","adj_factor"],
				data = new JSONObject();
				
				JSONArray dataFields = new JSONArray();
				dataFields.put("ts_code");
				dataFields.put("trade_date");
				dataFields.put("adj_factor");
				data.put("fields",dataFields);
				
				dataItems = new JSONArray();
				data.put("items", dataItems);
			}
			dataItems.put(item);
			FileTools.writeTextFile(factorFile, data.toString(), false);
		}
		
	}

	@Override
	public void downKdata(List<String> ids) throws Exception {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("KdataSpiderTushare downKdata...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			this.downKdata(id);
			Thread.sleep(300); //一分钟200次
		}
		
		System.out.println("KdataSpiderTushare downKdata done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}

	@Override
	public void downFactors(List<String> ids) throws Exception {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("KdataSpiderTushare downFactors...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			this.downFactor(id);
			Thread.sleep(300); //一分钟200次
		}
		
		System.out.println("KdataSpiderTushare downFactors done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}

}
