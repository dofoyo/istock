package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;

@Service("kdataRepositoryTushare")
public class KdataRepositoryTushare implements KdataRepository{
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	@Override
	@CacheEvict(value="dailyKdatas",allEntries=true)
	public void EvictDailyKDataCache() {}
	
	@Override
	//@Cacheable("dailyKdatas")
	public KdataEntity getDailyKdata(String itemID) {
		//System.out.println("itemID: " + itemID);

		KdataEntity kdata = new KdataEntity(itemID);

		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String kdataFile = kdataPath + "/" + tushareID + ".json";
		String factorFile = kdataPath + "/" + tushareID + "_factor.json";

		if(FileUtil.isExists(kdataFile) && FileUtil.isExists(factorFile)) {
			JSONObject factor = new JSONObject(FileUtil.readTextFile(factorFile));
			Map<LocalDate,BigDecimal> factors = new HashMap<LocalDate,BigDecimal>();
			JSONArray items = factor.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					factors.put(LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd")),item.getBigDecimal(2));
				}
			}

			LocalDate date;
			BigDecimal open,high,low,close,amount,quantity;
			BigDecimal preFactor=null,nowFactor;
			JSONObject data = new JSONObject(FileUtil.readTextFile(kdataFile));
			items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					date = LocalDate.parse(item.getString(1),DateTimeFormatter.ofPattern("yyyyMMdd"));
					nowFactor = factors.get(date);
					if(i==0) {
						preFactor = nowFactor;
					}
					
					open = getPrice(item.getBigDecimal(2),nowFactor,preFactor);
					high = getPrice(item.getBigDecimal(3),nowFactor,preFactor);
					low = getPrice(item.getBigDecimal(4),nowFactor,preFactor);
					close = getPrice(item.getBigDecimal(5),nowFactor,preFactor);
					amount = item.getBigDecimal(10);
					quantity = item.getBigDecimal(9);

					kdata.addBar(date,open,high,low,close,amount,quantity);
					
					preFactor = nowFactor;
				}
			}
			
		}

		return kdata;
	}
	
	private BigDecimal getPrice(BigDecimal price, BigDecimal nowFactor, BigDecimal preFactor) {
		return price.multiply(nowFactor).divide(preFactor,BigDecimal.ROUND_HALF_UP);
	}
}
