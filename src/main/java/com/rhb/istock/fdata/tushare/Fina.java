package com.rhb.istock.fdata.tushare;

import java.math.BigDecimal;

import com.rhb.istock.comm.util.Functions;

public class Fina {
	private String end_date;
	private FinaCashflow cashflow;
	private FinaIncome income;
	private FinaIndicator indicator;
	
	public Fina(String end_date,FinaCashflow cashflow, FinaIncome income, FinaIndicator indicator) {
		this.end_date = end_date;
		this.cashflow = cashflow;
		this.income = income;
		this.indicator = indicator;
	}
	
	public boolean isValid() {
		return this.cashflow!=null 
				&& this.income!=null 
				&& this.indicator!=null 
				&& this.cashflow.isValid()
				&& this.income.isValid()
				&& this.indicator.isValid()
				;
	}
	
	public boolean isOK() {
		return this.cashflow.getN_cashflow_act().compareTo(BigDecimal.ZERO)==1
				&& this.indicator.getProfit_dedt().compareTo(BigDecimal.ZERO)==1
				&& this.getOperationMarginRatio()>=13  // 销售100元钱，净利润21元
				;
	}
	
	//营业利润率
	public Integer getOperationMarginRatio() {
		return Functions.rate(this.getIndicator().getProfit_dedt(),this.getIncome().getRevenue());
	}
	
	public String getEnd_date() {
		return end_date;
	}
	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}
	public FinaCashflow getCashflow() {
		return cashflow;
	}
	public void setCashflow(FinaCashflow cashflow) {
		this.cashflow = cashflow;
	}
	public FinaIncome getIncome() {
		return income;
	}
	public void setIncome(FinaIncome income) {
		this.income = income;
	}
	public FinaIndicator getIndicator() {
		return indicator;
	}
	public void setIndicator(FinaIndicator indicator) {
		this.indicator = indicator;
	}
	@Override
	public String toString() {
		return "Fina [end_date=" + end_date + ", cashflow=" + cashflow + ", income=" + income + ", indicator="
				+ indicator + ", isValid()=" + isValid() + ", isOK()=" + isOK() + ", getOperationMarginRatio()="
				+ getOperationMarginRatio() + "]";
	}
	
}
