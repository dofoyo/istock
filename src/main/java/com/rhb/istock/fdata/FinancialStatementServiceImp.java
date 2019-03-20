package com.rhb.istock.fdata;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
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
	
	Map<String,FinancialStatement> financialStatements = new HashMap<String,FinancialStatement>();
	
	private String out = "000527,600840,002710,600631,000522,601206,600005";

	
	@Override
	public boolean isOk(String stockcode, Integer year) {
		if(out.indexOf(stockcode)!=-1){
			return false;
		}
		if(!financialStatements.containsKey(stockcode)){
			setFinancialStatement(stockcode);
		}
		
		boolean flag = false;
		
		FinancialStatement fs = financialStatements.get(stockcode);
		if(fs!=null){
			flag = fs.isOK(year);
		}		
		return flag;
	}
	
	@Override
	public List<Integer> getPeriods(String stockcode){
		if(!financialStatements.containsKey(stockcode)){
			setFinancialStatement(stockcode);
		}
		List<Integer> years = null;
		FinancialStatement fs = financialStatements.get(stockcode);
		if(fs!=null){
			years = fs.getPeriods();
		}
		
		return years;
		
	}
	
	
	private void setFinancialStatement(String stockcode) {
		//System.out.println("setFinancialStatement of  " + stockcode);
		FinancialStatement fs = new FinancialStatement();
		fs.setBalancesheets(financeStatementsRepository.getBalanceSheets(stockcode));
		fs.setCashflows(financeStatementsRepository.getCashFlows(stockcode));
		fs.setProfitstatements(financeStatementsRepository.getProfitStatements(stockcode));
		financialStatements.put(stockcode, fs);
		//stock.refreshFinancialStatements();

	}


	@Override
	public Map<Integer, String> getOks(String stockcode) {
		Map<Integer, String> oks  = new HashMap<Integer, String>();
		
		if(!financialStatements.containsKey(stockcode)){
			setFinancialStatement(stockcode);
		}
		
		FinancialStatement fs = financialStatements.get(stockcode);

				
		Map<Integer, String> reportdates = reportDateRepository.getReportDates(stockcode);
		for(Map.Entry<Integer, String> entry : reportdates.entrySet()){
			if(fs.isOK(entry.getKey())){
				oks.put(entry.getKey(), entry.getValue());
			}
		}
		
		return oks;
	}


	@Override
	public List<OkfinanceStatementDto> getOks() {
		List<OkfinanceStatementDto> dtos = new LinkedList<OkfinanceStatementDto>();
		List<Integer> years;
		Set<String> codes = financeStatementsRepository.getReportedStockcode();
		int i=0;
		for(String code : codes){
			System.out.print(i++ + "/" + codes.size() + " codes.size" + "\r");

			years = this.getPeriods(code);
			for(Integer year : years){
				if(this.isOk(code, year)){
					String reportDate = reportDateRepository.getReportDate(code, year);
					OkfinanceStatementDto dto = new OkfinanceStatementDto();
					dto.setStockcode(code);
					dto.setYear(year);
					dto.setReportdate(reportDate==null ? null : reportDate.toString());
					dtos.add(dto);
					//System.out.println(dto);
				}
			}
		}
		
		return dtos;
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
	

}
