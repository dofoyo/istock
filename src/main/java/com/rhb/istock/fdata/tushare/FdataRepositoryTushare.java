package com.rhb.istock.fdata.tushare;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("fdataRepositoryTushare")
public class FdataRepositoryTushare {
	@Value("${tushareFdataPath}")
	private String fdataPath;
	
	Map<String,FinaCashflow> cashflows = null;
	Map<String,FinaIncome> incomes = null;
	Map<String,FinaIndicator> indicators = null;
	Map<String,FinaBalancesheet> balancesheets = null;
	
	public void init() {
		this.cashflows = null;
		this.incomes = null;
		this.indicators = null;
		this.balancesheets = null;
	}
	
	public Map<String,Fina> getFinas(String itemID){
		Map<String,Fina> finas = new TreeMap<String,Fina>();
		Set<String> dates = this.getDates(itemID);
		Fina fina;
		for(String date : dates) {
			if(date.contains("1231")) {
				fina = this.getFina(itemID, date);
				finas.put(date, fina);
			}
		}
		return finas;
		
	}
	
	public Set<String> getDates(String itemID){
		if(this.cashflows == null) {
			this.cashflows = this.getCashflows(itemID);
		}
		
		return this.cashflows.keySet();
	}
	
	public Fina getFina(String itemID, String end_date) {
		//if(this.cashflows == null || this.cashflows.size()==0) {
			this.cashflows = this.getCashflows(itemID);
		//}
		//if(this.incomes == null || this.incomes.size()==0) {
			this.incomes = this.getIncomes(itemID);
		//}
		//if(this.indicators == null || this.indicators.size()==0) {
			this.indicators = this.getIndicators(itemID, LocalDate.now());
		//}
		//if(this.balancesheets == null || this.balancesheets.size()==0) {
			this.balancesheets = this.getBalancesheets(itemID);
		//}

		
		Fina fina = null;
		//if(this.cashflows!=null && this.incomes!=null && this.indicators!=null && this.balancesheets!=null) {
			FinaCashflow cashflow = this.cashflows.get(end_date);
			FinaIncome income = this.incomes.get(end_date);
			FinaIndicator indicator = this.indicators.get(end_date);
			FinaBalancesheet balancesheet = this.balancesheets.get(end_date);
			if(cashflow!=null && income!=null && indicator!=null && balancesheet!=null) {
				fina = new Fina(end_date,cashflow,income,indicator,balancesheet);
			}
		//}
		
		//System.out.println(itemID);
		//System.out.println(end_date);
		//System.out.println(fina);
		
		return fina;
	}
	
	public	Map<String,FinaBalancesheet> getBalancesheets(String itemID){
		Map<String,FinaBalancesheet> balancesheets = new HashMap<String,FinaBalancesheet>();
		
		String fdataFile = fdataPath + "/" + itemID + "_balancesheet.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			JSONArray fields = basicObject.getJSONArray("fields");
			if(items.length()>0) {
				JSONArray item;
				FinaBalancesheet balancesheet;
				String end_date;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(3);
					balancesheet = new FinaBalancesheet(item, fields);
					balancesheets.put(end_date, balancesheet);
				}
			}
		}
		
		return balancesheets;
	}
	
	public	Map<String,FinaCashflow> getCashflows(String itemID){
		Map<String,FinaCashflow> cashflows = new HashMap<String,FinaCashflow>();
		
		String fdataFile = fdataPath + "/" + itemID + "_cashflow.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				FinaCashflow cashflow;
				String end_date;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(3);
					cashflow = new FinaCashflow(item);
					cashflows.put(end_date, cashflow);
				}
			}
		}
		
		return cashflows;
	}
	
	
	public	Map<String,FinaIncome> getIncomes(String itemID){
		Map<String,FinaIncome> incomes = new HashMap<String,FinaIncome>();
		
		String fdataFile = fdataPath + "/" + itemID + "_income.json";
		//System.out.println(fdataFile);
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String end_date;
				FinaIncome income;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(3);
					income = new FinaIncome(item);
					incomes.put(end_date, income);
				}
			}
		}else {
			System.out.println("File DO NOT exists! " + fdataFile);
		}
		//System.out.println(incomes);
		return incomes;
	}
	
	public	Map<String,FinaIndicator> getIndicators(String itemID,LocalDate date){
		Map<String,FinaIndicator> indicators = new TreeMap<String,FinaIndicator>();
		
		String fdataFile = fdataPath + "/" + itemID + "_fina_indicator.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String end_date;
				LocalDate ann_date;
				FinaIndicator indicator;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(2);
					//if(end_date.endsWith("1231") && !"null".equals(item.get(1).toString())) {
					if(!"null".equals(item.get(1).toString())) {
						//System.out.println(item);
						//System.out.println(item.get(1));
						ann_date = LocalDate.parse(item.get(1).toString(),DateTimeFormatter.ofPattern("yyyyMMdd"));
						if(ann_date.equals(date) || ann_date.isBefore(date)) {
							indicator = new FinaIndicator(itemID,item);
							indicators.put(end_date, indicator);
						}
					}
				}
			}
		}
		
		return indicators;
	}
	
	public	Map<String,FinaForecast> getForecasts(String itemID){
		Map<String,FinaForecast> indicators = new HashMap<String,FinaForecast>();
		
		String fdataFile = fdataPath + "/" + itemID + "_forecast.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String end_date;
				FinaForecast forecast;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(2);
					forecast = new FinaForecast(item);
					indicators.put(end_date, forecast);
				}
			}
		}
		
		return indicators;
	}
	
	public	Set<Floatholder> getFloatholders(String itemID, String[] periods){
		Set<Floatholder> holders = new HashSet<Floatholder>();
		
		String fdataFile = fdataPath + "/" + itemID + "_top10_floatholders.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				Floatholder holder;
				String end_date;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(2);
					if(end_date.equals(periods[0])) {
						holder = new Floatholder(item);
						holders.add(holder);
					}
				}
				
				if(holders.size()==0) {
					for(int i=0; i<items.length()-1; i++) {
						item = items.getJSONArray(i);
						end_date = item.getString(2);
						if(end_date.equals(periods[1])) {
							holder = new Floatholder(item);
							holders.add(holder);
						}
					}
					
				}
			}
		}
		
		return holders;
	}
	
}
