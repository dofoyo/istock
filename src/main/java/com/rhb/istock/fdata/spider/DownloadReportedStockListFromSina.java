package com.rhb.istock.fdata.spider;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.HttpDownload;
import com.rhb.istock.comm.util.ParseString;

@Service("downloadReportedStockListFromSina")
public class DownloadReportedStockListFromSina implements DownloadReportedStockList {

	@Override
	public Map<String,String> go(String year) {
		Map<String,String> codes = new HashMap<String,String>();
		String page = "1";
		String url = "http://finance.sina.com.cn/realstock/income_statement/"+year+"-12-31/issued_pdate_de_"+page+".html";
		String result = HttpDownload.getResult(url);
		
		codes.putAll(getCodes(result));
		
		Integer pages = getPages(result);
		for(int i=2; i<=pages; i++){
			url =  "http://finance.sina.com.cn/realstock/income_statement/"+year+"-12-31/issued_pdate_de_"+Integer.toString(i)+".html";
			result = HttpDownload.getResult(url);
			//System.out.println(i);
			codes.putAll(getCodes(result));
		}
		
		System.out.println("there are " + pages + " pages, "+ codes.size() +" reported stocks.");
		
		return codes;
		
	}
	
	
	private Map<String,String> getCodes(String result){
		Map<String,String> codes = new HashMap<String,String>();
		
		String code;
		String reportDate;
		
		List<String> tds;
		List<String> trs = getTrs1(result);
		trs.addAll(getTrs2(result));
		for(String tr : trs){
			tds = getTds(tr);
			if(tds!=null && tds.size()>3 && tds.get(0)!=null && !tds.get(0).equals("null")){			
				code = getCode(tds.get(0));
				reportDate = tds.get(2);
				//System.out.println(code + "," + reportDate + "," + code.indexOf("60"));
				if(code.indexOf("60")==0 || code.indexOf("00")==0 || code.indexOf("30")==0) {
					codes.put(code, reportDate);
				}
			}
		}

		
		return codes;

	}
	
	private Integer getPages(String str){
		Integer i = 0;
		String regexp = "<a href=\'issued_pdate_de_.*?.html\'>|</a>";
		List<String> list = ParseString.subStrings(str,regexp);
		for(String s : list){
			//System.out.println(s);
			if(i < ParseString.toInteger(s)){
				i = ParseString.toInteger(s);
			}
		}
		
		return i;
	}

	private List<String> getTds(String str){
		String regexp = "<td>|</td>";
		List<String> list = ParseString.subStrings(str,regexp);
		return list;
	}
	
	private List<String> getTrs2(String str){
		String regexp = "<tr style='background:#F1F6FC;'>|</tr>";
		List<String> list = ParseString.subStrings(str,regexp);
/*		for(String code : list) {
			System.out.println(code);
		}*/
		
		return list;
	}

	
	private List<String> getTrs1(String str){
		//System.out.println(str);
		String regexp = "<tr>|</tr>";
		List<String> list = ParseString.subStrings(str,regexp);
		return list;
	}

	
	private String getCode(String str){
		String regexp = "nc.shtml\">|</a>";
		String code = ParseString.subString(str,regexp);
		return code;
	}
	

}
