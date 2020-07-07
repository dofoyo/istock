package com.rhb.istock.fdata.tushare;

import java.util.HashMap;
import java.util.List;
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
	
	public void init() {
		this.cashflows = null;
		this.incomes = null;
		this.indicators = null;
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
		if(this.cashflows == null) {
			this.cashflows = this.getCashflows(itemID);
		}
		if(this.incomes == null) {
			this.incomes = this.getIncomes(itemID);
		}
		if(this.indicators == null) {
			this.indicators = this.getIndicators(itemID);
		}
		
		Fina fina = null;
		if(this.cashflows!=null && this.incomes!=null && this.indicators!=null) {
			FinaCashflow cashflow = this.cashflows.get(end_date);
			FinaIncome income = this.incomes.get(end_date);
			FinaIndicator indicator = this.indicators.get(end_date);
			
			fina = new Fina(end_date,cashflow,income,indicator);
		}
		
		return fina;
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
		}
		
		return incomes;
	}
	
	public	Map<String,FinaIndicator> getIndicators(String itemID){
		Map<String,FinaIndicator> indicators = new HashMap<String,FinaIndicator>();
		
		String fdataFile = fdataPath + "/" + itemID + "_fina_indicator.json";
		if(FileTools.isExists(fdataFile)) {
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(fdataFile));
			JSONArray items = basicObject.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				String end_date;
				FinaIndicator indicator;
				for(int i=0; i<items.length()-1; i++) {
					item = items.getJSONArray(i);
					end_date = item.getString(2);
					indicator = new FinaIndicator(item);
					indicators.put(end_date, indicator);
				}
			}
		}
		
		return indicators;
	}
}
