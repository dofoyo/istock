package com.rhb.istock.fdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.fdata.repository.FinanceStatementsRepository;
import com.rhb.istock.fdata.repository.ReportDateRepository;
import com.rhb.istock.fdata.spider.DownloadFinancialStatements;
import com.rhb.istock.fdata.spider.DownloadReportedStockList;

@Service("financialStatementServiceImp")
public class FinancialStatementServiceImp implements FinancialStatementService {
	@Value("${fdataPath}")
	private String fdataPath;

	@Autowired
	@Qualifier("financeStatementsRepositoryFromSina")
	FinanceStatementsRepository financeStatementsRepository;

	@Autowired
	@Qualifier("reportDateRepositoryImp")
	ReportDateRepository reportDateRepository;

	@Autowired
	@Qualifier("downloadReportedStockListFromSina")
	DownloadReportedStockList downloadReportedStockList;
	
	@Autowired
	@Qualifier("downloadFinancialStatementsFromSina")
	DownloadFinancialStatements downloadFinancialStatements;
	
	private String out = "000527,600840,002710,600631,000522,601206,600005";
	
	@Override
	public FinancialStatement getFinancialStatement(String stockcode) {
		if(out.indexOf(stockcode)!=-1){
			return null;
		}	
		
		FinancialStatement fs = new FinancialStatement();
		fs.setBalancesheets(financeStatementsRepository.getBalanceSheets(stockcode));
		fs.setCashflows(financeStatementsRepository.getCashFlows(stockcode));
		fs.setProfitstatements(financeStatementsRepository.getProfitStatements(stockcode));
		return fs;
	}
	
	@Override
	public void downloadReports() {
		LocalDate today = LocalDate.now();
		int month = today.getMonthValue();
		int year = today.getYear() - 1;
		if(month<6){
			System.out.println(LocalDateTime.now() +  "   " + Thread.currentThread().getName() + ":  下载年报任务开始.............");
			Map<String,String> codes = downloadReportedStockList.go(Integer.toString(year));
			
	    	Map<String,String> downloadUrls = new HashMap<String,String>();
	    	Map<String,String> downloadCodes = new HashMap<String,String>();
			for(Map.Entry<String, String> entry : codes.entrySet()){
				if(reportDateRepository.getReportDate(entry.getKey(), year) == null){
					downloadUrls.put(entry.getKey()+"_balancesheet.xls",downloadFinancialStatements.downloadBalanceSheetUrl(entry.getKey()));
					downloadUrls.put(entry.getKey()+"_cashflow.xls",downloadFinancialStatements.downloadCashFlowUrl(entry.getKey()));
					downloadUrls.put(entry.getKey()+"_profitstatement.xls",downloadFinancialStatements.downloadProfitStatementUrl(entry.getKey()));
					
					downloadCodes.put(entry.getKey(),entry.getValue());
				}
			}
			
	    	//开始下载年报
			downloadFinancialStatements.down(downloadUrls);
			
			reportDateRepository.saveReportDates(downloadCodes, year);
			
			//完成年报下载后，刷新内存中STOCK对象
			//stockService.setFinancialStatements(codes);
			
			System.out.println(Thread.currentThread().getName() + ":  下载年报任务完成.............");
		}		
	}

	@Override
	public Set<String> getReportedStockCodes() {
		return financeStatementsRepository.getReportedStockcode();
	}

	@Override
	public Map<Integer, String> getReportDates(String stockcode) {
		return reportDateRepository.getReportDates(stockcode);
	}

	//@Override
	public String getReportDate(String stockcode, Integer year) {
		return reportDateRepository.getReportDate(stockcode, year);
	}

}
