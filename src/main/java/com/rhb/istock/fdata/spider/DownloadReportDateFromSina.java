package com.rhb.istock.fdata.spider;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rhb.istock.comm.util.HttpDownload;
import com.rhb.istock.comm.util.ParseString;
import com.rhb.istock.fdata.repository.ReportDateEntity;

@Service("downloadReportDateFromSina")
public class DownloadReportDateFromSina implements DownloadReportDate {
	@Value("${fdataPath}")
	private String fdataPath;

	@Override
	public void go() {

		List<ReportDateEntity> reportDates = this.go0912();
		reportDates.addAll(this.go1317());
		
		String jsonfile = fdataPath + "reportdates.json";
		writeToFile(jsonfile,reportDates);
		
	}

	public List<ReportDateEntity> go1317() {
		List<String[]> urls = new ArrayList<String[]>();

		Map<String,Integer> years = new HashMap<String,Integer>();
		years.put("2013", 14);
		years.put("2014", 14);
		years.put("2015", 15);
		years.put("2016", 17);
		years.put("2017", 3);
		
		for(Map.Entry<String, Integer> entry : years.entrySet()){
			for(int page=1; page<=entry.getValue(); page++){
				String[] url = new String[2];
				url[1] = entry.getKey();
				url[0] =  "http://finance.sina.com.cn/realstock/income_statement/"+url[1]+"-12-31/issued_pdate_de_"+Integer.toString(page)+".html";
				urls.add(url);
			}
		}
		
/*		for(int year=2013; year<2017; year++){
			for(int page=1; page<=10; page++){
				String[] url = new String[2];
				url[0] =  "http://finance.sina.com.cn/realstock/income_statement/"+year+"-12-31/issued_pdate_de_"+Integer.toString(page)+".html";
				url[1] = Integer.toString(year);
				urls.add(url);
			}
		}
		
		urls.add(new String[]{"http://finance.sina.com.cn/realstock/income_statement/2017-12-31/issued_pdate_de_1.html","2017"});
		urls.add(new String[]{"http://finance.sina.com.cn/realstock/income_statement/2017-12-31/issued_pdate_de_2.html","2017"});
		urls.add(new String[]{"http://finance.sina.com.cn/realstock/income_statement/2017-12-31/issued_pdate_de_3.html","2017"});
*/		
		String result;
		List<String> trs;
		List<String> tds;
		List<ReportDateEntity> reportDates = new LinkedList<ReportDateEntity>();
		for(String[] url : urls){
			System.out.println(url[0]);
			result = HttpDownload.getResult(url[0]);
			trs = getTrs1(result);
			trs.addAll(getTrs2(result));
			for(String tr : trs){
				tds = getTds(tr);
				if(tds!=null && tds.size()>3 && tds.get(0)!=null && !tds.get(0).equals("null")){
					ReportDateEntity pde = new ReportDateEntity();
					pde.setStockCode(getCode(tds.get(0)));
					pde.setYear(Integer.parseInt(url[1]));
					pde.setReportdate(tds.get(2));
					reportDates.add(pde);
					System.out.println(pde.getStockCode() + "," + pde.getReportdate());
				}
			}
			try {
				Thread.sleep(1000); //避免反扒措施
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return reportDates;
	}

	
	public List<ReportDateEntity> go0912() {
		List<String[]> urls = new ArrayList<String[]>();
		Map<String,Integer> years = new HashMap<String,Integer>();
		years.put("2009", 10);
		years.put("2010", 13);
		years.put("2011", 15);
		years.put("2012", 13);
		for(Map.Entry<String, Integer> entry : years.entrySet()){
			for(int page=1; page<=entry.getValue(); page++){
				String[] url = new String[2];
				url[1] = entry.getKey();
				url[0] =  "http://finance.sina.com.cn/realstock/income_statement/"+url[1]+"-12-31/issued_pdate_de_"+Integer.toString(page)+".html";
				urls.add(url);
			}
		}

		
		
		String result;
		List<String> trs;
		List<String> tds;
		List<ReportDateEntity> reportDates = new LinkedList<ReportDateEntity>();
		for(String[] url : urls){
			System.out.println(url[0]);
			result = HttpDownload.getResult(url[0]);
			trs = getTrs1(result);
			trs.addAll(getTrs2(result));
			for(String tr : trs){
				tds = getTds(tr);
				if(tds!=null && tds.size()>3 && tds.get(1)!=null && !tds.get(1).equals("null")){
					ReportDateEntity pde = new ReportDateEntity();
					pde.setStockCode(getCode(tds.get(1)));
					pde.setYear(Integer.parseInt(url[1]));
					pde.setReportdate(tds.get(3));
					reportDates.add(pde);
					
					System.out.println(pde.getStockCode() + "," + pde.getReportdate());

				}
			}
			try {
				Thread.sleep(1000); //避免反扒措施
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
		return reportDates;
	}
	
	
	private void writeToFile(String jsonfile, List<ReportDateEntity> object){
		ObjectMapper mapper = new ObjectMapper();
    	try {
			mapper.writeValue(new File(jsonfile),object);
		} catch (JsonGenerationException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private List<String> getTds(String str){
		String regexp = "<td>|</td>";
		List<String> list = ParseString.subStrings(str,regexp);
		return list;
	}
	
	private List<String> getTrs2(String str){
		String regexp = "<tr style='background:#F1F6FC;'>|</tr>";
		List<String> list = ParseString.subStrings(str,regexp);
		return list;
	}

	
	private List<String> getTrs1(String str){
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
