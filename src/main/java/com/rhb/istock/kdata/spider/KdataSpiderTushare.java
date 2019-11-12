package com.rhb.istock.kdata.spider;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

	protected static final Logger logger = LoggerFactory.getLogger(KdataSpiderTushare.class);
	
	@Override
	public void downKdatas(String itemID) {
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
		
		String kdataFile = kdataPath + "/daily/" + tushareID + "_kdatas.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);
	}
	
	@Override
	public void downFactors(String itemID) {
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
		
		String kdataFile = kdataPath + "/factor/" + tushareID + "_factors.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);
	}

	@Override
	public void downKdatas(LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("KdataSpiderTushare.downloadKdata of " + date + "....");

		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "daily");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("trade_date", date.format(dtf));
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());

		String kdataFile = kdataPath + "/daily/kdatas_" + date + ".json";
		FileTools.writeTextFile(kdataFile, str, false);

		JSONArray items = (new JSONObject(str)).getJSONObject("data").getJSONArray("items");	
		
		if(items.length()>0) {
			JSONArray item;
			for(int i=0; i<items.length(); i++) {
				item = items.getJSONArray(i);

				Progress.show(items.length(),i, " KdataSpiderTushare.downloadKdata.setKdata " + item.getString(0));

				setKdata(item);
			}
		}
		logger.info("\nKdataSpiderTushare.downloadKdata of " + date + " done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
		
		//FileTools.writeTextFile(kdataFile, str, false);
	}
	
	@Override
	public void downFactors(LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("KdataSpiderTushare.downloadFactor of " + date + "....");
		
		String result = this.downloadFactors(date);
		JSONArray items = (new JSONObject(result)).getJSONObject("data").getJSONArray("items");	
		while(items.length()==0) {
			logger.error("\n factors file is NULL!!!!!!");
			
			try {
				Thread.sleep(3000);  //等待3秒钟
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			result = this.downloadFactors(date);
			items = (new JSONObject(result)).getJSONObject("data").getJSONArray("items");	
		}
		
		JSONArray item;
		for(int i=0; i<items.length(); i++) {
			item = items.getJSONArray(i);
			
			Progress.show(items.length(),i, " KdataSpiderTushare.downloadFactor.setFactor " + item.getString(0));
			
			setFactor(item);
		}
		
		
		logger.info("\nKdataSpiderTushare.downloadFactor of " + date + " done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
	}
	
	private String downloadFactors(LocalDate date) {
		String result = null;
		
		String kdataFile = kdataPath + "/factor/factors_" + date + ".json";
		
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "adj_factor");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("trade_date", date.format(dtf));
		
		args.put("params", params);
		
		result = HttpClient.doPostJson(url, args.toString());
		
		FileTools.writeTextFile(kdataFile, result, false);
		return result;
		
	}
	
	private void setKdata(JSONArray item) {
		if(item!=null && item.length()>0) {
			JSONObject data;
			JSONArray dataItems;
			String id = item.getString(0);
			String kdataFile = kdataPath + "/daily/" + id+ "_kdatas.json";
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
			String factorFile = kdataPath + "/factor/" + id+ "_factors.json";
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
	public void downKdatas(List<String> ids) throws Exception {
		long beginTime=System.currentTimeMillis(); 
		logger.info("KdataSpiderTushare downKdata...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			this.downKdatas(id);
			Thread.sleep(300); //一分钟200次
		}
		
		logger.info("\nKdataSpiderTushare downKdata done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
		
	}

	@Override
	public void downFactors(List<String> ids) throws Exception {
		long beginTime=System.currentTimeMillis(); 
		logger.info("KdataSpiderTushare downFactors...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			this.downFactors(id);
			Thread.sleep(300); //一分钟200次
		}
		
		logger.info("\nKdataSpiderTushare downFactors done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
		
	}

}
