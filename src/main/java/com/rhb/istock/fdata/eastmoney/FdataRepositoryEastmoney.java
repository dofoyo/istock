package com.rhb.istock.fdata.eastmoney;

import java.math.BigDecimal;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;

@Service("fdataRepositoryEastmoney")
public class FdataRepositoryEastmoney {
	@Value("${eastmoneyFataPath}")
	private String eastmoneyFataPath;
	
	public Integer getCAGR(String itemID) {
		Integer cagr = null;
		TreeMap<String,BigDecimal> forcasts = new TreeMap<String,BigDecimal>();
		String fdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		if(FileTools.isExists(fdataFile)) {
			JSONArray items = (new JSONObject(FileTools.read(fdataFile,"UTF-8"))).getJSONObject("yctj").getJSONArray("data");
			if(items.length()>0) {
				JSONObject item;
				String rq, yylr;
				boolean flag = false;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONObject(i);
					rq = item.get("rq").toString();
					yylr = item.get("yylr").toString();
					
					if(rq.contains("预测") && !yylr.equals("--")) {
						forcasts.put(rq.substring(0, 4), this.getBigDecimal(yylr));
						flag = true;
					}
				}
				
				if(flag) {
					BigDecimal a = forcasts.lastEntry().getValue();
					BigDecimal b = forcasts.firstEntry().getValue();
					cagr = Functions.cagr(a, b, forcasts.size()-1);
				}
			}
		}
		
		return cagr;
		
	}
	
	public	Map<String,String[]> getForcasts(String itemID){
		Map<String,String[]> forcasts = new TreeMap<String,String[]>();
		
		String fdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		if(FileTools.isExists(fdataFile)) {
			JSONArray items = (new JSONObject(FileTools.read(fdataFile,"UTF-8"))).getJSONObject("yctj").getJSONArray("data");
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
					number = String.format("%.2f", b);
				}
			}else {
				number = str.substring(0,i);
			}			
		}
		

		return number;
	}
}
