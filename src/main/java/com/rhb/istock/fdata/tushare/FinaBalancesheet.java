package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;

public class FinaBalancesheet {
	private BigDecimal adv_receipts;  //预收款项
	private Map<String, Integer> fields;
	
	public boolean isValid() {
		return this.adv_receipts!=null && !this.adv_receipts.equals(BigDecimal.ZERO);
	}
	
	public FinaBalancesheet(JSONArray item, JSONArray fields) {
		int i = this.getPosition(fields, "adv_receipts");
		this.adv_receipts  = item.get(i).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(i);
	}
	
	public BigDecimal getAdv_receipts() {
		return adv_receipts;
	}

	public void setAdv_receipts(BigDecimal adv_receipts) {
		this.adv_receipts = adv_receipts;
	}
	
	public Integer getPosition(JSONArray fields,String fieldName) {
		if(this.fields==null) {
			this.fields = this.getFields(fields);
		}
		return this.fields.get(fieldName);
	}
	
	public Map<String, Integer> getFields(JSONArray fields) {
		Map<String, Integer> fs = new HashMap<String, Integer>();
		for(int i=0; i<fields.length()-1; i++) {
			fs.put(fields.getString(i), i);
		}		
		
		return fs;
	}
	

	@Override
	public String toString() {
		return "FinaBalancesheet [adv_receipts=" + adv_receipts  + "]";
	}


}
