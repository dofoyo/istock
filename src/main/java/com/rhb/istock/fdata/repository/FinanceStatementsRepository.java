package com.rhb.istock.fdata.repository;

import java.util.Map;
import java.util.Set;

import com.rhb.istock.fdata.BalanceSheet;
import com.rhb.istock.fdata.CashFlow;
import com.rhb.istock.fdata.ProfitStatement;


public interface FinanceStatementsRepository {

	public Map<String,BalanceSheet> getBalanceSheets(String stockid);
	public Map<String,CashFlow> getCashFlows(String stockid);
	public Map<String,ProfitStatement> getProfitStatements(String stockid);
	
	public Set<String> getReportedStockcode();
	
}
