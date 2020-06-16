package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import org.json.JSONArray;

public class FinaCashflow {
	private BigDecimal n_cashflow_act;
	
	public boolean isValid() {
		return this.n_cashflow_act!=null && !this.n_cashflow_act.equals(BigDecimal.ZERO);
	}
	
	public FinaCashflow(JSONArray item) {
		this.n_cashflow_act  = item.get(33).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(33);
	}
	
	public BigDecimal getN_cashflow_act() {
		return n_cashflow_act;
	}
	public void setN_cashflow_act(BigDecimal n_cashflow_act) {
		this.n_cashflow_act = n_cashflow_act;
	}

	@Override
	public String toString() {
		return "FinaCashflow [n_cashflow_act=" + n_cashflow_act + "]";
	}


}
