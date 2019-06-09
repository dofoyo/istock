package com.rhb.istock.kdata.repository;

import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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

	@Value("${latestFactorsFile}")
	private String latestFactorsFile;
	
	@Value("${kdataMusterFile}")
	private String kdataMusterFile;
	
	//private Map<String,KdataEntity> kdatas = new HashMap<String,KdataEntity>();;
	
	@Override
	@CacheEvict(value="tushareDailyKdatas",allEntries=true)
	public void evictDailyKDataCache() {}
	
	@Override
	public KdataEntity getDailyKdata(String itemID) {
		//System.out.println("itemID: " + itemID);
		
		//if(kdatas.containsKey(itemID)) return kdatas.get(itemID);
		
		KdataEntity kdata = new KdataEntity(itemID);

		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String kdataFile = kdataPath + "/" + tushareID + ".json";
		String factorFile = kdataPath + "/" + tushareID + "_factor.json";

		//System.out.println(kdataFile);//----------------
		BigDecimal roof = null;
		if(FileUtil.isExists(kdataFile) && FileUtil.isExists(factorFile)) {
			BigDecimal f = null;
			JSONObject factor = new JSONObject(FileUtil.readTextFile(factorFile));
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
			BigDecimal preFactor=null,nowFactor;
			JSONObject data = new JSONObject(FileUtil.readTextFile(kdataFile));
			items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					date = LocalDate.parse(item.getString(1),DateTimeFormatter.ofPattern("yyyyMMdd"));
					nowFactor = factors.get(date);
					if(preFactor==null) preFactor = nowFactor;
					

					try {
						open = getPrice(item.getBigDecimal(2),nowFactor,roof);
						high = getPrice(item.getBigDecimal(3),nowFactor,roof);
						low = getPrice(item.getBigDecimal(4),nowFactor,roof);
						close = getPrice(item.getBigDecimal(5),nowFactor,roof);
						amount = item.getBigDecimal(10);
						quantity = item.getBigDecimal(9);						
						kdata.addBar(date,open,high,low,close,amount,quantity);
						
						//System.out.printf("%tF: item.getBigDecimal(5) * nowFactor / preFactor = %f * %f / %f = %f\n" , date, item.getBigDecimal(5),nowFactor,roof,close);

						
					}catch(Exception e) {
						System.err.println(item);
						System.err.println(nowFactor);
						System.err.println(preFactor);					}
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
	public KdataEntity getDailyKdataByCache(String itemID) {
		return this.getDailyKdata(itemID);
	}

	@Override
	public LocalDate getLastDate() {
		String itemID1="sh601398"; //工商银行
		String itemID2="sh601288"; //农业银行
		LocalDate date1 = getDailyKdata(itemID1).getLastDate();
		LocalDate date2 = getDailyKdata(itemID2).getLastDate();
		return date1.isAfter(date2) ? date1 : date2;
	}

	@Override
	public LocalDate getLatestMusterDate() {
		String source = FileUtil.readTextFile(kdataMusterFile);
		
		if(source==null || source.isEmpty()) {
			System.err.println("can NOT find " + kdataMusterFile + "! or the file is empty!");
			return null;
		}

		String[] lines = source.split("\n");
		
		return LocalDate.parse(lines[0].split(",")[0]);
	}

	@Override
	@Cacheable("kdataMusters")
	public List<KdataMusterEntity> getKdataMusters() {
		List<KdataMusterEntity> entities = new ArrayList<KdataMusterEntity>();
		
		String source = FileUtil.readTextFile(kdataMusterFile);

		String[] lines = source.split("\n");
		for(int i=1; i<lines.length; i++) {
			entities.add(new KdataMusterEntity(lines[i]));
		}
		
		return entities;
	}
	@Override
	@CacheEvict(value="kdataMusters",allEntries=true)
	public void evictKdataMustersCache() {}
	

	@Override
	public void saveLatestMusters(LocalDate date, List<KdataMusterEntity> entities, Integer period) {
		StringBuffer sb = new StringBuffer(date.toString() + "," + period + "\n");
		for(KdataMusterEntity entity : entities) {
			sb.append(entity.toText());
			sb.append("\n");
		}
		sb.deleteCharAt(sb.length()-1);
		
		FileUtil.writeTextFile(kdataMusterFile, sb.toString(), false);
	}

	@Override
	public TreeMap<LocalDate, BigDecimal> getFactors(String itemID) {
		TreeMap<LocalDate,BigDecimal> factors = new TreeMap<LocalDate,BigDecimal>();

		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String factorFile = kdataPath + "/" + tushareID + "_factor.json";
/*		File file = new File(factorFile);
		if(file.exists()) {
			System.out.println(factorFile + " is exist!");
		}else {
			System.out.println(factorFile + " is NOT exist!");
		}*/
		JSONObject factor_json = new JSONObject(FileUtil.readTextFile(factorFile));
		JSONArray items = factor_json.getJSONArray("items");
		if(items.length()>0) {
			BigDecimal factor = null;
			JSONArray item;
			for(int i=0; i<items.length(); i++) {
				item = items.getJSONArray(i);
				factor = item.getBigDecimal(2);
				factors.put(LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd")),factor);
			}
		}
		
		
		return factors;
	}

	@Override
	public void saveLatestFactors(Map<String, BigDecimal> factors) {
		StringBuffer sb = new StringBuffer();
		for(Map.Entry<String, BigDecimal> entry : factors.entrySet()) {
			sb.append(entry.getKey());
			sb.append(",");
			sb.append(entry.getValue());
			sb.append("\n");
		}
		FileUtil.writeTextFile(latestFactorsFile, sb.toString(), false);
	}

	@Override
	@CacheEvict(value="latestFactors",allEntries=true)
	public Map<String, BigDecimal> getLatestFactors() {
		Map<String,BigDecimal> latestFactors = new HashMap<String, BigDecimal>();

		String source = FileUtil.readTextFile(latestFactorsFile);
		String[] lines = source.split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(",");
			if(columns!=null && columns.length>1) {
				latestFactors.put(columns[0], new BigDecimal(columns[1]));
			}
		}
		return latestFactors;
	}

	@Override
	@CacheEvict(value="latestFactors",allEntries=true)
	public void evictLatestFactorsCache() {}

}
