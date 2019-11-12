package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("kdataRepositoryTushare")
public class KdataRepositoryTushare implements KdataRepository{
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	//private Map<String,KdataEntity> kdatas = new HashMap<String,KdataEntity>();;
	
	protected static final Logger logger = LoggerFactory.getLogger(KdataRepositoryTushare.class);

	@Override
	@CacheEvict(value="tushareDailyKdatas",allEntries=true)
	public void evictKDataCache() {}
	
	@Override
	public KdataEntity getKdata(String itemID) {
		//System.out.println("itemID: " + itemID);
		
		//if(kdatas.containsKey(itemID)) return kdatas.get(itemID);
		
		KdataEntity kdata = new KdataEntity(itemID);

		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String kdataFile = kdataPath + "/daily/" + tushareID + "_kdatas.json";
		String factorFile = kdataPath + "/factor/" + tushareID + "_factors.json";

		//System.out.println(kdataFile);//----------------
		BigDecimal roof = null;
		if(FileTools.isExists(kdataFile) && FileTools.isExists(factorFile)) {
			BigDecimal f = null;
			JSONObject factor = new JSONObject(FileTools.readTextFile(factorFile));
			TreeMap<LocalDate,BigDecimal> factors = new TreeMap<LocalDate,BigDecimal>();
			JSONArray items = factor.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					f = item.getBigDecimal(2);
					factors.put(LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd")),f);
				}
				roof = factors.lastEntry().getValue();
			}
			
			LocalDate date;
			BigDecimal open,high,low,close,amount,quantity;
			BigDecimal nowFactor = null;
			JSONObject data = new JSONObject(FileTools.readTextFile(kdataFile));
			items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					date = LocalDate.parse(item.getString(1),DateTimeFormatter.ofPattern("yyyyMMdd"));
					nowFactor = factors.get(date);
					//System.out.println("date=" + date + ", nowFactor=" + nowFactor);
					//if(preFactor==null) preFactor = nowFactor;
					
					if(nowFactor!=null && roof!=null) {
						open = getPrice(item.getBigDecimal(2),nowFactor,roof);
						high = getPrice(item.getBigDecimal(3),nowFactor,roof);
						low = getPrice(item.getBigDecimal(4),nowFactor,roof);
						close = getPrice(item.getBigDecimal(5),nowFactor,roof);
						amount = item.getBigDecimal(10);
						quantity = item.getBigDecimal(9);						
						kdata.addBar(date,open,high,low,close,amount,quantity);
					}
					
					if(nowFactor==null){
						logger.error("The nowFactor of " + itemID + " on " + date.toString() + " is NULL.");
					}

					if(roof==null){
						logger.error("The roof of " + itemID + " is NULL.");
					}
				}
			}
		}
		
		//kdatas.put(itemID, kdata);
		//System.out.println("kdatas.size()=" + kdatas.size());

		return kdata;
	}
	
	private BigDecimal getPrice(BigDecimal price, BigDecimal nowFactor, BigDecimal preFactor) {
		return price.multiply(nowFactor).divide(preFactor,3,BigDecimal.ROUND_HALF_DOWN);
	}

	@Override
	@Cacheable("tushareDailyKdatas")
	public KdataEntity getKdataByCache(String itemID) {
		return this.getKdata(itemID);
	}

	@Override
	public LocalDate getLastDate(String itemID) {
		KdataEntity kdata = this.getKdata(itemID);
		if(kdata != null) {
			return kdata.getLastDate();
		}else {
			return null;
		}
	}


}
