package com.rhb.istock.fdata;

import java.util.Map;

public class FinancialStatement {

	private Map<String,BalanceSheet> balancesheets;
	private Map<String,CashFlow> cashflows;
	private Map<String,ProfitStatement> profitstatements;
	
	public Map<String, BalanceSheet> getBalancesheets() {
		return balancesheets;
	}

	public Map<String, CashFlow> getCashflows() {
		return cashflows;
	}

	public Map<String, ProfitStatement> getProfitstatements() {
		return profitstatements;
	}

	public void setBalancesheets(Map<String, BalanceSheet> balancesheets) {
		this.balancesheets = balancesheets;
	}

	public void setCashflows(Map<String, CashFlow> cashflows) {
		this.cashflows = cashflows;
	}

	public void setProfitstatements(Map<String, ProfitStatement> profitstatements) {
		this.profitstatements = profitstatements;
	}
	
}
