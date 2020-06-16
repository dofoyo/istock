package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;

public class FinaIndicator {
	private BigDecimal profit_dedt;
	private BigDecimal grossprofit_margin;
	
	public boolean isValid() {
		return this.profit_dedt!=null && !this.profit_dedt.equals(BigDecimal.ZERO);
	}
	public FinaIndicator(JSONArray item) {
		this.profit_dedt  = item.get(11).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(11);
		this.grossprofit_margin  = item.get(49).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(49);
	}
	
	public BigDecimal getProfit_dedt() {
		return profit_dedt;
	}
	public void setProfit_dedt(BigDecimal profit_dedt) {
		this.profit_dedt = profit_dedt;
	}
	public BigDecimal getGrossprofit_margin() {
		return grossprofit_margin;
	}
	public void setGrossprofit_margin(BigDecimal grossprofit_margin) {
		this.grossprofit_margin = grossprofit_margin;
	}
	@Override
	public String toString() {
		return "FinaIndicator [profit_dedt=" + profit_dedt + ", grossprofit_margin=" + grossprofit_margin + "]";
	}

}
