package com.rhb.istock.unlock;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("unlockDataRepositoryTushare")
public class UnlockDataRepositoryTushare{
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	protected static final Logger logger = LoggerFactory.getLogger(UnlockDataRepositoryTushare.class);

	public List<UnlockDataEntity> getUnlockKdata(String itemID) {
		List<UnlockDataEntity> datas = new ArrayList<UnlockDataEntity>();
		
		String kdataFile = kdataPath + "/unlock/" + itemID + ".json";
		
		System.out.println(kdataFile);
		
		UnlockDataEntity kdata;
		if(FileTools.isExists(kdataFile)) {
			try {
				JSONObject basicObject = new JSONObject(FileTools.readTextFile(kdataFile));
				JSONArray items = basicObject.getJSONArray("items");
				
				if(items.length()>0) {
					JSONArray item;
					LocalDate floatDate;
					LocalDate endDate = LocalDate.now().plusYears(1);
					for(int i=0; i<items.length(); i++) {
						item = items.getJSONArray(i);
						floatDate = LocalDate.parse(item.getString(2), DateTimeFormatter.ofPattern("yyyyMMdd"));
						if(item.getString(6).equals("定增股份") && floatDate.isBefore(endDate)) {
							try {
								kdata = new UnlockDataEntity();
								kdata.setTs_code(item.getString(0));
								kdata.setAnn_date(LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd")));
								kdata.setFloat_date(floatDate);
								kdata.setFloat_share(item.getBigDecimal(3));
								kdata.setFloat_ratio(item.getBigDecimal(4));
								kdata.setHolder_name(item.getString(5));
								kdata.setShare_type(item.getString(6));
								
								datas.add(kdata);						
							}catch(Exception e) {
								logger.error("read " + item.getString(0) + " ERROR, maybe ann date is null");
							}
						}
					}
				}
				
			}catch(Exception e) {
				logger.error("read " + kdataFile + " ERROR");
			}
		}
		System.out.println("there are " + datas.size() + " records");
		return datas;
	}
	

}
