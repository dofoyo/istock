package com.rhb.istock.fdata.tushare;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

@Service("fdataSpiderTushare")
public class FdataSpiderTushare {
	@Value("${tushareFdataPath}")
	private String fdataPath;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	protected static final Logger logger = LoggerFactory.getLogger(FdataSpiderTushare.class);
	
	public boolean isExistIndicator(String itemID) {
		return isExist(itemID,"fina_indicator");
	}

	public boolean isExistCashflow(String itemID) {
		return isExist(itemID,"cashflow");
	}
	
	public boolean isExistIncome(String itemID) {
		return isExist(itemID,"income");
	}
	
	private boolean isExist(String itemID, String type) {
		String fdataFile = fdataPath + "/" + itemID + "_"+type+".json";
		File file = new File(fdataFile);
		return file.exists();
	}
	
	private void down(String itemID, String type) {
		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", type);
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", tushareID);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = fdataPath + "/" + itemID + "_"+type+".json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);		
	}

	public void downIncome(String itemID) throws Exception {
		this.down(itemID, "income");
	}
	
	public void downCashflow(String itemID) throws Exception {
		this.down(itemID, "cashflow");
	}
	
	public void downIndicator(String itemID) throws Exception {
		this.down(itemID, "fina_indicator");
	}
	
	public void downForecast(String itemID) throws Exception {
		this.down(itemID, "forecast");
	}

	public void downFloatholder(String itemID) throws Exception {
		this.down(itemID, "top10_floatholders");
	}
	
	public void downForecast(LocalDate date) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");

		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "forecast");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ann_date", date.format(df));
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = fdataPath + "/" + date.format(df) + "_forecast.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);		
	}

	public void downAll() {
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String itemID : ids) {
			Progress.show(ids.size(),i++, " down all: " + itemID);//进度条
			this.down(itemID, "income");
			this.down(itemID, "cashflow");
			this.down(itemID, "fina_indicator");
			this.down(itemID, "forecast");
			this.down(itemID, "top10_floatholders");
			try {
				Thread.sleep(1000);  //一分钟200个	
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void downFloatholders(String period) {
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(),i++, " downFloatholders: " + id);//进度条
			this.downFloatholders(id, period);
			try {
				Thread.sleep(1000);  //一分钟200个	
			} catch (InterruptedException e) {
				e.printStackTrace();
			} 
		}
	}
	
	public void downFloatholders(String itemID, String period) {
		DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMdd");
		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";

		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "top10_floatholders");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", tushareID);
		params.put("period", period);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = fdataPath + "/" + itemID + "_floatholders.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);		
	}
	
}
