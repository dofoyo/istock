package com.rhb.istock.fdata.eastmoney;

import java.io.File;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
	public void down(String itemID) {
		String url = "http://f10.eastmoney.com/ProfitForecast/ProfitForecastAjax?code=" + itemID;
		JSONObject args = new JSONObject();
		String str = HttpClient.doPostJson(url, args.toString());
		
		String kdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		FileTools.writeTextFile(kdataFile, str, false);		

	}
	
	public void downAll() {
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String itemID : ids) {
			Progress.show(ids.size(),i++, " down all: " + itemID);//进度条
			this.down(itemID);
			HttpClient.sleep(2);
		}
	}
	
	
}
