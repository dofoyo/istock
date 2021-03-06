package com.rhb.istock.item.spider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.HttpClient;
import com.rhb.istock.comm.util.ParseString;

@Service("itemSpiderTushare")
public class ItemSpiderTushare implements ItemSpider {
	@Value("${tushareItemsFile}")
	private String itemsFile;
	
	@Value("${tushareUrl}")
	private String url;	
	
	protected static final Logger logger = LoggerFactory.getLogger(ItemSpiderTushare.class);
	
	@Override
	public void downItems() throws Exception {
		logger.info("download items...");
		
		//String url = "http://api.tushare.pro";

		JSONObject args = new JSONObject();
		args.put("api_name", "stock_basic");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");

		
		JSONObject params = new JSONObject();
		//params.put("list_status", "L");
		//params.put("fields", "symbol,name,area,industry,list_date");
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());

/*		Map<String,String> params = new HashMap<String,String>();
		params.put("api_name", "stock_basic");
		params.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		params.put("list_status", "L");
		params.put("fields", "symbol,name,area,industry,list_date");
		
		String str = HttpClient.doPost(url, params);*/
		
		//logger.info(str);
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		FileTools.writeTextFile(itemsFile, data.toString(), false);

		logger.info("download items done!");
	}

	@Override
	public String getTopic(String itemID) {
		//System.out.print(" getting topic... ");

		String code = itemID.substring(2);
		String strUrl = "http://stockpage.10jqka.com.cn/"+code+"/";
		String result = HttpClient.doGet(strUrl);
		String topic = ParseString.subString(result, "<dd title=\"|\">");
		if(topic==null) {
			return "other";
		}
		topic = topic.replaceAll("概念",	 "");
		
		//Set<String> topics = new HashSet<String>();
		
		
		//System.out.println(strUrl);
		//System.out.println(topic);
		
		return topic;
	}

	@Override
	public String[] getTopicTops(Integer count) {
		List<JSONObject> tops = new ArrayList<JSONObject>();
		
		String strUrl = "http://q.10jqka.com.cn/gn/";
		String result = HttpClient.doGet(strUrl);
		String data = ParseString.subString(result, "id=\"gnSection\" value=\'|\'>");
		
		JSONObject topics = new JSONObject(data);
		JSONObject topic;
		for(int i=0; i<topics.length(); i++) {
			if(topics.has(Integer.toString(i))) {
				topic = topics.getJSONObject(Integer.toString(i));
				tops.add(topic);
			}
		}
		
		Collections.sort(tops, new Comparator<JSONObject>() {
			@Override
			public int compare(JSONObject o1, JSONObject o2) {
				return o2.getBigDecimal("199112").compareTo(o1.getBigDecimal("199112"));
			}
		});
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<tops.size() && i<count; i++) {
			topic = tops.get(i);
			sb.append(topic.getString("platename").replace("概念",""));
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);

		return sb.toString().split(",");
	}

}
