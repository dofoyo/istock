package com.rhb.istock.fdata;

import java.util.Map;
import java.util.Set;

public interface FinancialStatementService {
	
	public FinancialStatement getFinancialStatement(String stockcode);
	
	public void downloadReports();
	
	public Set<String> getReportedStockCodes();
	public Map<Integer,String>getReportDates(String stockcode);

	
}
