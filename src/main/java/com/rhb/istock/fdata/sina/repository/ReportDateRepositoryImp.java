package com.rhb.istock.fdata.sina.repository;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service("reportDateRepositoryImp")
public class ReportDateRepositoryImp implements ReportDateRepository{
	@Value("${fdataPath}")
	private String fdataPath;
	
	private static final String filename = "reportdates.json";
	
	private Map<String,Map<Integer,String>> codeYearDates = null;  //  stockcode--year--reportdate
	
	@Override
	public void init(){
		System.out.println("ReportDateRepositoryImp init....");
		codeYearDates = new HashMap<String,Map<Integer,String>>();
		
		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
		JavaType javaType = mapper.getTypeFactory().constructParametricType(List.class, ReportDateEntity.class); 
		try {
			List<ReportDateEntity> reportDates  = mapper.readValue(new File(fdataPath+filename), javaType);
			for(ReportDateEntity pde : reportDates){
				if(codeYearDates.containsKey(pde.getStockCode())){
					codeYearDates.get(pde.getStockCode()).put(pde.getYear(), pde.getReportdate());
				}else{
					Map<Integer,String> map = new HashMap<Integer,String>();
					map.put(pde.getYear(), pde.getReportdate());
					codeYearDates.put(pde.getStockCode(), map);
				}
			}
			//reportDates = mapper.readValue(dataPath+filename, new TypeReference<List<ReportDateEntity>>(){});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		System.out.println("there are " + codeYearDates.size() + " stocks have reported.");

		System.out.println("........ReportDateRepositoryImp end");

	}
	

	@Override
	public String getReportDate(String code, Integer year) {
		if(codeYearDates == null){
			this.init();
		}
		String date = null;
		
		Map<Integer,String> yearDates = codeYearDates.get(code);
		if(yearDates!=null && yearDates.get(year)!=null){
			date = yearDates.get(year);
		}
		
		return date;
	}



	@Override
	public Map<Integer, String> getReportDates(String stockcode) {
		if(codeYearDates == null){
			this.init();
		}
		
		if(codeYearDates.get(stockcode)==null){
			//System.out.println(stockcode + " has no reportDate!!!");
			return null;
		}else {
			return codeYearDates.get(stockcode);
		}
	}


	@Override
	public void saveReportDates(Map<String,String> codes, Integer year) {
		if(codeYearDates == null){
			this.init();
		}
		
		List<ReportDateEntity> reportDates = new ArrayList<ReportDateEntity>();
		Map<Integer,String> yearDates;
		for(Map.Entry<String,Map<Integer,String>> codeYearDate : codeYearDates.entrySet()) {
			yearDates = codeYearDate.getValue();
			for(Map.Entry<Integer, String> yearDate : yearDates.entrySet()) {
				ReportDateEntity pde = new ReportDateEntity();
				pde.setStockCode(codeYearDate.getKey());
				pde.setYear(yearDate.getKey());				
				pde.setReportdate(yearDate.getValue());
				reportDates.add(pde);
			}
		}
		
			
		for(Map.Entry<String, String> entry : codes.entrySet()) {
			ReportDateEntity pde = new ReportDateEntity();
			pde.setStockCode(entry.getKey());
			pde.setReportdate(entry.getValue());
			pde.setYear(year);
			reportDates.add(pde);
			
			
			if(codeYearDates.containsKey(pde.getStockCode())){
				codeYearDates.get(pde.getStockCode()).put(pde.getYear(), pde.getReportdate());
			}else{
				Map<Integer,String> map = new HashMap<Integer,String>();
				map.put(pde.getYear(), pde.getReportdate());
				codeYearDates.put(pde.getStockCode(), map);
			}
			
		}
		
		
		writeToFile(fdataPath+filename, reportDates);
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
}
