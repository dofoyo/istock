package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;

public class FinaIncome {
	private BigDecimal revenue;
	public boolean isValid() {
		return this.revenue!=null && !this.revenue.equals(BigDecimal.ZERO);
	}
	public FinaIncome(JSONArray item) {
		this.revenue  = item.get(9).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(9);
	}
	
	public BigDecimal getRevenue() {
		return revenue;
	}
	public void setRevenue(BigDecimal revenue) {
		this.revenue = revenue;
	}

	@Override
	public String toString() {
		return "FinaIncome [revenue=" + revenue + "]";
	}
	
}
