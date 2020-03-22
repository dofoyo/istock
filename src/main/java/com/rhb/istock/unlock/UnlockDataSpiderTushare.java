package com.rhb.istock.unlock;

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

@Service("unlockDataSpiderTushare")
public class UnlockDataSpiderTushare{
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	protected static final Logger logger = LoggerFactory.getLogger(UnlockDataSpiderTushare.class);

	public void downUnlockDatas(List<String> ids){
		long beginTime=System.currentTimeMillis(); 
		logger.info("downUnlockDatas...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			try {
				this.downUnlockData(id);
				Thread.sleep(300); //一分钟200次
			} catch (InterruptedException e) {
				logger.error("down unlock data of " + id + " ERROR!");
			} 
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("\n downUnlockDatas done! 用时：" + used + "秒");          
		
	}
	
	public void downUnlockData(String itemID) {
		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "share_float");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", tushareID);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		//System.out.println(str);
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		JSONArray items = data.getJSONArray("items");
		if(items.length()>0) {
			String kdataFile = kdataPath + "/unlock/" + itemID + ".json";
			FileTools.writeTextFile(kdataFile, data.toString(), false);
		}
	}

}
