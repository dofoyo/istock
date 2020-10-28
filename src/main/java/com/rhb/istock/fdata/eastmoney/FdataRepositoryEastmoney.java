package com.rhb.istock.fdata.eastmoney;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.comm.util.ParseString;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.selector.drum.DrumService;

@Service("fdataRepositoryEastmoney")
public class FdataRepositoryEastmoney {
	protected static final Logger logger = LoggerFactory.getLogger(DrumService.class);

	@Value("${eastmoneyFataPath}")
	private String eastmoneyFataPath;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	private Integer period = 180;
	private HashMap<LocalDate, Map<String,Integer>> recommendations = null;
	
	public Map<LocalDate,Integer> getRecommendationCount(String itemID, LocalDate date) {
		Map<LocalDate,Integer> count = new TreeMap<LocalDate,Integer>();
		
		TreeMap<LocalDate,Integer> records = this.getRecommendationRecords(itemID);
		TreeMap<LocalDate,Integer> tmp = new TreeMap<LocalDate, Integer>();
		Integer total = 0, ct;
		for(LocalDate pDate = records.firstKey(); pDate.isBefore(date)||pDate.equals(date); pDate = pDate.plusDays(1)) {
			ct = records.get(pDate);
			if(ct!=null) {
				total = total + ct;
			}else {
				ct = 0;
			}
			tmp.put(pDate, ct);
			if(tmp.size()>=period) {
				total = total - tmp.firstEntry().getValue();
				tmp.remove(tmp.firstKey());
			}
			if(ct>0) {
				count.put(pDate, total);
			}
		}

		
		return count;
	}
	
	private TreeMap<LocalDate,Integer> getRecommendationRecords(String itemID){
		TreeMap<LocalDate,Integer> records = new TreeMap<LocalDate,Integer>();

		String fdataFile = eastmoneyFataPath + "/" + itemID + "_yb.json";
		if(FileTools.isExists(fdataFile)) {
			JSONArray res = null;
			try {
				res = new JSONArray(FileTools.readTextFile(fdataFile));
			}catch(Exception e) {
				logger.error("building JSONArray Error: " + fdataFile);;
			}
			if(res!=null) {
				Integer dayCount;
				JSONObject obj;
				String publishDate;
				LocalDate pd;
				for(int i=0; i<res.length(); i++) {
					obj = res.getJSONObject(i);
					if("007".equals(obj.get("emRatingCode"))) {
						publishDate = obj.get("publishDate").toString();
						pd = LocalDate.parse(publishDate.subSequence(0, 10));
						dayCount = records.get(pd);  
						if(dayCount==null) { //日期没有保存过
							dayCount = 1;
						}else {
							dayCount++;
						}
						records.put(pd, dayCount);
					}
				}				
			}
		}
		
		return records;
	}
	
	
	
	public Map<String, Integer> getRecommendations(LocalDate date) {
		if(this.recommendations==null) this.generateRecommendation();
		
		Map<String, Integer> results = new HashMap<String, Integer>();
		
		Map<String, Integer> ids;
		Integer count;
		for(int i=0; i<180; i++) {
			ids = this.recommendations.get(date.minusDays(i));
			if(ids != null) {
				for(Map.Entry<String, Integer> entry : ids.entrySet()) {
					count = results.get(entry.getKey());
					if(count == null) {
						count = entry.getValue();
					}else {
						count = count + entry.getValue();
					}
					results.put(entry.getKey(), count);
				}
			}			
		}
		
		return results;
	}
	
	public Integer getRecommendations(String itemID, LocalDate date) {
		if(this.recommendations==null) this.generateRecommendation();
		
		Map<String, Integer> res;
		Integer count = 0, tmp;
		for(int i=0; i<period; i++) {
			res = this.recommendations.get(date.minusDays(i));
			if(res!=null) {
				tmp = res.get(itemID);
				if(tmp!=null) {
					count = count + tmp;
				}
			}			
		}
		
		return count;
	}
	
	public void generateRecommendation() {
		recommendations = new HashMap<LocalDate, Map<String,Integer>>();
		
		List<String> ids = itemService.getItemIDs();
		String fdataFile;
		Map<String,Integer> ss;
		Integer count;
		int j=1;
		for(String id : ids) {
			fdataFile = eastmoneyFataPath + "/" + id + "_yb.json";
			Progress.show(ids.size(), j++, fdataFile);
			if(FileTools.isExists(fdataFile)) {
				JSONArray res = null;
				try {
					res = new JSONArray(FileTools.readTextFile(fdataFile));
				}catch(Exception e) {
					logger.error("building JSONArray Error: " + fdataFile);;
				}
				if(res!=null) {
					JSONObject obj;
					String publishDate;
					LocalDate pd;
					
					for(int i=0; i<res.length(); i++) {
						obj = res.getJSONObject(i);
						if("007".equals(obj.get("emRatingCode"))) {
							publishDate = obj.get("publishDate").toString();
							pd = LocalDate.parse(publishDate.subSequence(0, 10));
							ss = recommendations.get(pd);  
							if(ss==null) { //日期没有保存过
								ss = new HashMap<String,Integer>();  
								count = 1;
								recommendations.put(pd, ss);
							}else {
								count = ss.get(id);
								if(count==null) {  //日期保存过，但没有该id的记录
									count = 1;
								}else {
									count++;
								}
							}
							ss.put(id, count);
							//System.out.println(pd);
						}
					}					
				}
			}
		}
		//System.out.println(recommendations);
	}
	
	public Integer getCAGR(String itemID) {
		Integer cagr = null;
		TreeMap<String,BigDecimal> forcasts = new TreeMap<String,BigDecimal>();
		String fdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		//System.out.println(fdataFile);
		if(FileTools.isExists(fdataFile)) {
			String charset = FileTools.getCharset(fdataFile);
			//JSONArray items = (new JSONObject(FileTools.readTextFile(fdataFile))).getJSONObject("yctj").getJSONArray("data");
			JSONArray items = (new JSONObject(FileTools.read(fdataFile,charset))).getJSONObject("yctj").getJSONArray("data");
			//System.out.println(items);
			if(items.length()>0) {
				JSONObject item;
				String rq, yylr;
				Integer p = null, count=0;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONObject(i);
					rq = item.get("rq").toString();
					yylr = item.get("yylr").toString();
					//System.out.println(yylr);
					
					if(p==null && rq.contains("预测")) {
						p=i-1;  //得到最后一个实际值的位置
					}
					
					if(rq.contains("预测") && !yylr.equals("--")) {
						forcasts.put(rq.substring(0, 4), this.getBigDecimal(yylr));
						count = this.getCount(yylr); 
					}
				}

				boolean flag = false;
				if(p!=null && p>=0 && forcasts.size()>0 && count>=3) {  //三个以上的机构预测
					item = items.getJSONObject(p);   //得到最后一个实际值
					yylr = item.get("yylr").toString();
					BigDecimal b = this.getBigDecimal(yylr);
					if(b.compareTo(new BigDecimal(1))==1) {  //最后一个实际值在一个亿以上
						for(Map.Entry<String, BigDecimal> entry : forcasts.entrySet()) {
							if(b.compareTo(entry.getValue())==-1) {
								flag = true;
							}else {
								flag = false;
								break;
							}
							b = entry.getValue();
						}
					}					
				}
				
				if(flag) {
					item = items.getJSONObject(p);   //得到最后一个实际值
					yylr = item.get("yylr").toString();
					BigDecimal b = this.getBigDecimal(yylr);
					BigDecimal a = forcasts.lastEntry().getValue();
					cagr = Functions.cagr(a, b, forcasts.size());
				}
			}
		}else {
			System.err.println("fdataFile do NOT exist!!");
		}
		
		return cagr;
		
	}
	
	public	Map<String,String[]> getForcasts(String itemID){
		Map<String,String[]> forcasts = new TreeMap<String,String[]>();
		
		String fdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		if(FileTools.isExists(fdataFile)) {
			String charset = FileTools.getCharset(fdataFile);
			JSONArray items = (new JSONObject(FileTools.read(fdataFile,charset))).getJSONObject("yctj").getJSONArray("data");
			if(items.length()>0) {
				JSONObject item;
				String rq, yyzsr, yylr;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONObject(i);
					rq = item.get("rq").toString();
					yyzsr = item.get("yyzsr").toString();
					yylr = item.get("yylr").toString();
					
					//System.out.println(rq + "," + yyzsr +  ", " + yylr );

					if(rq.contains("预测") && !yyzsr.equals("--")) {
						forcasts.put(rq.substring(0, 4)+"预", new String[] {this.getNumber(yyzsr),this.getNumber(yylr)});
					}
				}
			}
		}else {
			System.out.println(fdataFile + " NOT exist!!!");
		}
		//System.out.println(itemID + "," + forcasts.size());

		return forcasts;
	}
	
	private Integer getCount(String str) {
		int a = str.indexOf("(");
		int b = str.indexOf("家)");
		String count = str.substring(a+1, b);
		//System.out.println(count);
		return Integer.parseInt(count);
	}
	
	private BigDecimal getBigDecimal(String str) {
		String num = this.getNumber(str);
		if(num==null) {
			return BigDecimal.ZERO;
		}else{
			return new BigDecimal(num);
		}
	}
	
	private String getNumber(String str) {
		String number = null;
		
		Integer k = str.indexOf("万亿");
		if(k!=-1) {
			Double a = Double.valueOf(str.substring(0, k))*10000;
			number = String.format("%.0f", a);
		}else {
			Integer i = str.indexOf("亿");
			if(i==-1) {
				Integer j = str.indexOf("万");
				if(j!=-1) {
					Double b = Double.valueOf(str.substring(0, j))/10000;
					number = String.format("%.4f", b);
				}
			}else {
				number = str.substring(0,i);
			}			
		}
		

		return number;
	}
}
