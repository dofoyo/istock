package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;

@Service("kdataRepositoryTushare")
public class KdataRepositoryTushare implements KdataRepository{
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	@Value("${avarageAmountTopsFile}")
	private String avarageAmountTopsFile;
	
	@Override
	@CacheEvict(value="tushareDailyKdatas",allEntries=true)
	public void evictDailyKDataCache() {}
	
	@Override
	public KdataEntity getDailyKdata(String itemID) {
		//System.out.println("itemID: " + itemID);

		KdataEntity kdata = new KdataEntity(itemID);

		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String kdataFile = kdataPath + "/" + tushareID + ".json";
		String factorFile = kdataPath + "/" + tushareID + "_factor.json";

		//System.out.println(kdataFile);//----------------
		BigDecimal roof = null;
		if(FileUtil.isExists(kdataFile) && FileUtil.isExists(factorFile)) {
			BigDecimal f = null;
			JSONObject factor = new JSONObject(FileUtil.readTextFile(factorFile));
			Map<LocalDate,BigDecimal> factors = new TreeMap<LocalDate,BigDecimal>();
			JSONArray items = factor.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					f = item.getBigDecimal(2);
					if(roof==null || roof.compareTo(f)==-1) roof = f;
					factors.put(LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd")),f);
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
					if(preFactor==null) preFactor = nowFactor;
					
					//System.out.println(item);
					//System.out.println(nowFactor);
					//System.out.println(preFactor);
					
					open = getPrice(item.getBigDecimal(2),nowFactor,roof);
					high = getPrice(item.getBigDecimal(3),nowFactor,roof);
					low = getPrice(item.getBigDecimal(4),nowFactor,roof);
					close = getPrice(item.getBigDecimal(5),nowFactor,roof);
					amount = item.getBigDecimal(10);
					quantity = item.getBigDecimal(9);

					kdata.addBar(date,open,high,low,close,amount,quantity);
				}
			}
		}

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
	public LocalDate getLatestDate() {
		String itemID1="sh601398"; //工商银行
		String itemID2="sh601288"; //农业银行
		LocalDate date1 = getDailyKdata(itemID1).getLatestDate();
		LocalDate date2 = getDailyKdata(itemID2).getLatestDate();
		return date1.isAfter(date2) ? date1 : date2;
	}

	@Override
	public List<String> getDailyAverageAmountTops() {
		String[] ids = FileUtil.readTextFile(avarageAmountTopsFile).split(",");
		return Arrays.asList(ids);
	}

	@Override
	public void generateDailyAverageAmountTops(List<String> itemIDs, Integer duration) {
		TreeSet<Amount> tops = new TreeSet<Amount>();
		Amount amount = null;
		KdataEntity kdata;
		BigDecimal av;
		int d = 1;
		for(String itemID : itemIDs) {
			Progress.show(itemIDs.size(), d++, itemID);
			kdata = this.getDailyKdata(itemID);
			av = kdata.getAvarageAmount(duration);
			amount = new Amount(itemID,av);
			tops.add(amount);
		}
		StringBuffer sb = new StringBuffer();
		for(Iterator<Amount> i = tops.iterator() ; i.hasNext();) {
			amount = i.next();
			sb.append(amount.getCode());
			sb.append(",");
		}
		sb.deleteCharAt(sb.length()-1);
		
		FileUtil.writeTextFile(avarageAmountTopsFile, sb.toString(), false);
	}
	
	class Amount  implements Comparable<Amount>{
		@Override
		public String toString() {
			return "BarEntity [code=" + code + ", amount=" + amount + "]";
		}

		private String code;
		private BigDecimal amount = new BigDecimal(0);
		
		public Amount(String code) {
			this.code = code;
		}
		
		public Amount(String code, BigDecimal amount) {
			this.code = code;
			this.amount = amount;
		}

		public String getCode() {
			return code;
		}

		public void setCode(String code) {
			this.code = code;
		}

		public BigDecimal getAmount() {
			return amount;
		}
		
		public Integer getAmountInt() {
			return amount.divide(new BigDecimal(100000),BigDecimal.ROUND_HALF_UP).intValue();
		}

		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
		
		@Override
		public int compareTo(Amount o) {
			return o.getAmount().compareTo(this.getAmount()); //倒叙
		}
		
	}
}
