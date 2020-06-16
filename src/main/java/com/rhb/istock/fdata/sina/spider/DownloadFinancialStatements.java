package com.rhb.istock.fdata.sina.spider;

import java.util.Map;

public interface DownloadFinancialStatements {

	public void down(Map<String,String> urls);
	
	public String downloadBalanceSheetUrl(String stockid);
	public String downloadCashFlowUrl(String stockid);
	public String downloadProfitStatementUrl(String stockid);
	
	public void downloadBalanceSheet(String stockid);
	public void downloadCashFlow(String stockid);
	public void downloadProfitStatement(String stockid);
}
