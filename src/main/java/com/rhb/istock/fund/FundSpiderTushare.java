package com.rhb.istock.fund;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.HttpClient;

@Service("fundSpiderTushare")
public class FundSpiderTushare {
	@Value("${fundsFile}")
	private String fundsFile;

	@Value("${fundsPath}")
	private String fundsPath;
	
	@Value("${tushareUrl}")
	private String url;	
	
	protected static final Logger logger = LoggerFactory.getLogger(FundSpiderTushare.class);
	
	public void downFundBasic() {
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "fund_basic");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		FileTools.writeTextFile(fundsFile, data.toString(), false);		
	}
	
	public void downFundPortfolio(String ts_code) {
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "fund_portfolio");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", ts_code);
		args.put("params", params);
	
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		JSONArray items = data.getJSONArray("items");

		if(items!=null && items.length()>0) {
			String dataFile = fundsPath + "/" + ts_code + ".json";
			FileTools.writeTextFile(dataFile, data.toString(), false);		
		}
	}
}
