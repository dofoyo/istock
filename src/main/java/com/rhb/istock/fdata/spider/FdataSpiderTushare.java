package com.rhb.istock.fdata.spider;

import java.io.File;
import java.util.List;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.HttpClient;
import com.rhb.istock.comm.util.Progress;

@Service("fdataSpiderTushare")
public class FdataSpiderTushare {
	@Value("${tushareFdataPath}")
	private String fdataPath;

	protected static final Logger logger = LoggerFactory.getLogger(FdataSpiderTushare.class);
	
	public boolean isExist(String itemID) {
		String fdataFile = fdataPath + "/" + itemID + "_indicator.json";
		File file = new File(fdataFile);
		return file.exists();

	}
	
	public void downIndicator(String itemID) throws Exception {
		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "fina_indicator");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("ts_code", tushareID);
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject data = (new JSONObject(str)).getJSONObject("data");
		
		String kdataFile = fdataPath + "/" + itemID + "_indicator.json";
		FileTools.writeTextFile(kdataFile, data.toString(), false);
		
	}

	public void downIndicators(List<String> ids) throws Exception {
		long beginTime=System.currentTimeMillis(); 
		logger.info("KdataSpiderTushare downBasics...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			this.downIndicator(id);
			Thread.sleep(1000); 
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
		
	}

}
