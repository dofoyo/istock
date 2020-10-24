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
import com.rhb.istock.comm.util.ParseString;

@Service("fdataRepositoryEastmoney")
public class FdataRepositoryEastmoney {
	@Value("${eastmoneyFataPath}")
	private String eastmoneyFataPath;
	
	public Integer getCAGR(String itemID) {
		Integer cagr = null;
		TreeMap<String,BigDecimal> forcasts = new TreeMap<String,BigDecimal>();
		String fdataFile = eastmoneyFataPath + "/" + itemID + ".json";
		//System.out.println(fdataFile);
		if(FileTools.isExists(fdataFile)) {
			JSONArray items = (new JSONObject(FileTools.read(fdataFile,"UTF-8"))).getJSONObject("yctj").getJSONArray("data");
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
