package com.rhb.istock.item.spider;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.HttpClient;

@Service("itemSpiderTushare")
public class ItemSpiderTushare implements ItemSpider {
	@Value("${tushareItemsFile}")
	private String itemsFile;
	
	@Override
	public void download() throws Exception {
		System.out.println("download items...");
		
		String url = "http://api.tushare.pro";
		JSONObject params = new JSONObject();
		params.put("api_name", "stock_basic");
		params.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		params.put("fields", "symbol,name,area,industry,list_date");
		
		String str = HttpClient.doPostJson(url, params.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		FileUtil.writeTextFile(itemsFile, data.toString(), false);

		System.out.println("download items done!");
	}

}
