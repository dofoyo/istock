package com.rhb.istock.kdata.spider;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
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
	public void downKdata(String id)  throws Exception {
		String tushareID = id.indexOf("sh")==0 ? id.substring(2)+".SH" : id.substring(2)+".SZ";

		downloadKdata(tushareID);
		downloadFactor(tushareID);
	}
	
	private void downloadKdata(String id) {
		
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "daily");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", id);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/" + id + ".json";
		FileUtil.writeTextFile(kdataFile, data.toString(), false);
	}
	
	private void downloadFactor(String id) {
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "adj_factor");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", id);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = kdataPath + "/" + id + "_factor.json";
		FileUtil.writeTextFile(kdataFile, data.toString(), false);
	}

	@Override
	public void downKdata(LocalDate date) throws Exception {
		downloadKdata(date);
		downloadFactor(date);
	}

	@Override
	public void downloadKdata(LocalDate date) {
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
		FileUtil.writeTextFile(kdataFile, str, false);

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
		FileUtil.writeTextFile(kdataFile, str, false);
	}
	
	@Override
	public void downloadFactor(LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("KdataSpiderTushare.downloadFactor of " + date + "....");
		
		String result = this.downFactor(date);
		
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
				data = new JSONObject(FileUtil.readTextFile(kdataFile));
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
			FileUtil.writeTextFile(kdataFile, data.toString(), false);
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
				data = new JSONObject(FileUtil.readTextFile(factorFile));
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
			FileUtil.writeTextFile(factorFile, data.toString(), false);
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
	public String downloadLatestFactors(LocalDate date) throws Exception {
		return this.downFactor(date);
	}
	
	
	private String downFactor(LocalDate date) {
		String result = null;
		
		String kdataFile = kdataPath + "/" + date + "_factor.json";
		
		File factorFile = new File(kdataFile);
		if(factorFile.exists()) {
			System.out.println(kdataFile + " has been downloaded! pass!");
		}else {
			String url = "http://api.tushare.pro";
			JSONObject args = new JSONObject();
			args.put("api_name", "adj_factor");
			args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
			
			JSONObject params = new JSONObject();
			params.put("trade_date", date.format(dtf));
			
			args.put("params", params);
			
			result = HttpClient.doPostJson(url, args.toString());
			
			FileUtil.writeTextFile(kdataFile, result, false);
		}
		
		return result;
	}



}
