package com.rhb.istock.fdata.eastmoney;

import java.time.LocalDate;
import java.util.List;

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
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@Service("fdataSpiderEastmoney")
public class FdataSpiderEastmoney {
	@Value("${eastmoneyFataPath}")
	private String eastmoneyFataPath;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	protected static final Logger logger = LoggerFactory.getLogger(FdataSpiderEastmoney.class);
	
	//数据来自页面：http://f10.eastmoney.com/ProfitForecast/Index?type=web&code=SH601633#
	public void downProfitForecast(String itemID) {
		String url = "http://f10.eastmoney.com/ProfitForecast/ProfitForecastAjax?code=" + itemID;
		JSONObject args = new JSONObject();
		String str = HttpClient.doPostJson(url, args.toString());
		
		String fdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		FileTools.writeTextFile(fdataFile, str, false);		

	}
	
	public void downProfitForecasts() {
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String itemID : ids) {
			Progress.show(ids.size(),i++, " down all: " + itemID);//进度条
			this.downProfitForecast(itemID);
			HttpClient.sleep(2);
		}
	}
	
	public void downRecommendations() {
		List<Item> items = itemService.getItems();
		//System.out.println(items.size());
		String year="", fdataFile;
		JSONArray reports;
		int i=1;
		for(Item item : items) {
			if(item.getIpo()!=null) {
				year = item.getIpo().substring(0, 4);
				reports = this.downRecommendations(item.getCode(), Integer.parseInt(year));
				if(reports.length()>0) {
					fdataFile = eastmoneyFataPath + "/" + item.getItemID() + "_yb.json";
					FileTools.writeTextFile(fdataFile, reports.toString(), false);		
				}
				HttpClient.sleep(3);
			}
			Progress.show(items.size(), i++, item.getItemID() + ", ipo=" + year);
		}
	}
	
	public JSONArray downRecommendations(String code, Integer year) {
		JSONArray reports = new JSONArray();
		JSONArray rs;
		LocalDate today = LocalDate.now();
		Integer end = today.getYear();
		for(int i = year; i<=end; i++) {
			//Progress.show(end-year+1, i, "");
			rs = this.downRecommendations(code, Integer.toString(i));
			rs.forEach(obj -> reports.put(obj));
		}
		return reports;
	}
	
	private JSONArray downRecommendations(String code, String year) {
		String url = "http://reportapi.eastmoney.com/report/list?cb=datatable8515095&pageNo=1&pageSize=200&code="+code+"&beginTime="+year+"-01-01&endTime="+year+"-12-31&qType=0&_=1603757059836";
		JSONObject args = new JSONObject();
		String str = HttpClient.doGet(url);
		String ss = str.substring(17,str.length()-1);
		JSONArray reports = (new JSONObject(ss)).getJSONArray("data");

		return reports;
	}
	
	
}
