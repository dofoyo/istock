package com.rhb.istock.fdata.repository;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.ParseString;
import com.rhb.istock.fdata.BalanceSheet;
import com.rhb.istock.fdata.CashFlow;
import com.rhb.istock.fdata.ProfitStatement;
import com.rhb.istock.fdata.spider.DownloadFinancialStatements;

@Service("financeStatementsRepositoryFromSina")
public class FinanceStatementsRepositoryFromSina implements FinanceStatementsRepository {
	@Value("${fdataPath}")
	private String fdataPath;
	
	private String subPath = "\\fina\\sina\\";
	
	private static final Integer theYear = 2007; //2006年后才有现金流报表
	
	private String out = "000527,600840,002710,600631,000522,601206,600005,600263";
	
	@Autowired
	@Qualifier("downloadFinancialStatementsFromSina")
	DownloadFinancialStatements downloadFinancialStatements;
	
	
	@Override
	public Map<String,BalanceSheet> getBalanceSheets(String stockid) {
		Map<String,BalanceSheet> balancesheets = new TreeMap<String,BalanceSheet>();
		
		String pf = fdataPath + subPath + stockid + "_balancesheet.xls";
		
		if(!FileTools.isExists(pf)){
			System.out.println(pf + " do NOT exist!!");
			downloadFinancialStatements.downloadBalanceSheet(stockid);
			System.out.println(pf + " DONW!");
		}
		
		String str = FileTools.readTextFile(pf);
		
		if(str.trim().isEmpty()){
			System.out.println(pf + " is EMPTY!!");
			return balancesheets;
		}
		
		String[] lines = str.split("\n");
		String[] columns = lines[0].split("\t");
		String[][] cells = new String[columns.length][lines.length];
		
		int i = 0;
		int j = 0;
		for(String line : lines){
			columns = line.split("\t");
			j=0;
			for(String cell : columns){
				cells[j][i] = cell;
				j++;
			}
			i++;
		}
		
		/*
		for(int m=0; m<j; m++){
			for(int n=0; n<i; n++){
				System.out.println("cell["+m+"]["+n+"]=" + cells[m][n]);
			}
		}
		*/
		
		for(int m=1; m<j; m++){
			String period = cells[m][0];
			Integer year = Integer.parseInt(period.substring(0, 4));
			if(period.contains("1231") && year>=theYear){ //2006年后才有现金流分析
				BalanceSheet bs = new BalanceSheet();
				bs.setPeriod(cells[m][0]);
				if(lines.length>100){
					bs.setCash(ParseString.toDouble(cells[m][3]));
					bs.setInventories(ParseString.toDouble(cells[m][22]));
					bs.setAccountsReceivable(ParseString.toDouble(cells[m][9]));
					bs.setNotesReceivable(ParseString.toDouble(cells[m][8]));
					bs.setPayables(ParseString.toDouble(cells[m][10]));
					bs.setAssets(ParseString.toDouble(cells[m][55]));
					bs.setDebt(ParseString.toDouble(cells[m][98]));
				}else if(lines.length > 85){   // 银行
					bs.setCash(ParseString.toDouble(cells[m][3]));
					bs.setInventories(0.0);
					bs.setAccountsReceivable(0.0);
					bs.setNotesReceivable(0.0);
					bs.setPayables(0.0);
					bs.setAssets(ParseString.toDouble(cells[m][41]));
					bs.setDebt(ParseString.toDouble(cells[m][71]));
				}else{ // 券商
					bs.setCash(ParseString.toDouble(cells[m][3]));
					bs.setInventories(0.0);
					bs.setAccountsReceivable(0.0);
					bs.setNotesReceivable(0.0);
					bs.setPayables(0.0);
					bs.setAssets(ParseString.toDouble(cells[m][24]));
					bs.setDebt(ParseString.toDouble(cells[m][44]));
				}
				balancesheets.put(period,bs);
			}
		}
		
/*		for(Map.Entry<String, BalanceSheet> entry : balancesheets.entrySet()){
			System.out.println(entry.getValue());
		}
*/		
		return balancesheets;
		
	}
	
	@Override
	public Map<String,CashFlow> getCashFlows(String stockid) {
		Map<String,CashFlow> cashflows = new TreeMap<String,CashFlow>();
		
		String pf = fdataPath + subPath + stockid + "_cashflow.xls";
		if(!FileTools.isExists(pf)){
			System.out.println(pf + " do NOT exist!!");
			downloadFinancialStatements.downloadCashFlow(stockid);
			System.out.println(pf + " DONW!");
		}
		
		String str = FileTools.readTextFile(pf);
		if(str.trim().isEmpty()){
			System.out.println(pf + " is EMPTY!!");
			return cashflows;
		}
		
		String[] lines = str.split("\n");
		String[] columns = lines[0].split("\t");
		String[][] cells = new String[columns.length][lines.length];
		
		int i = 0;
		int j = 0;
		for(String line : lines){
			columns = line.split("\t");
			j=0;
			for(String cell : columns){
				cells[j][i] = cell;
				j++;
			}
			i++;
		}
		
		/*
		for(int m=0; m<j; m++){
			for(int n=0; n<i; n++){
				System.out.println("cell["+m+"]["+n+"]=" + cells[m][n]);
			}
		}
		*/
		
		for(int m=1; m<j; m++){
			String period = cells[m][0];
			Integer year = Integer.parseInt(period.substring(0, 4));
			if(period.contains("1231") && year>=theYear){
				CashFlow fs = new CashFlow();
				fs.setPeriod(period);
				if(lines.length>90){
					fs.setPurchaseAssets(ParseString.toDouble(cells[m][36]));
					fs.setNetCashFlow(ParseString.toDouble(cells[m][27]));
					fs.setDepreciationAssets(ParseString.toDouble(cells[m][66]) + ParseString.toDouble(cells[m][67]) + ParseString.toDouble(cells[m][68]));					
				}else if(lines.length > 76){  //银行
					fs.setPurchaseAssets(ParseString.toDouble(cells[m][20]));
					fs.setNetCashFlow(ParseString.toDouble(cells[m][12]));
					fs.setDepreciationAssets(ParseString.toDouble(cells[m][48]));					
				}else{  //券商
					fs.setPurchaseAssets(ParseString.toDouble(cells[m][23]));
					fs.setNetCashFlow(ParseString.toDouble(cells[m][15]));
					fs.setDepreciationAssets(ParseString.toDouble(cells[m][47]));					
				}
				cashflows.put(period,fs);
			}
		}

/*		for(Map.Entry<String, CashFlow> entry : cashflows.entrySet()){
			System.out.println(entry.getValue());
		}
*/		
		return cashflows;
		
	}

	@Override
	public Map<String, ProfitStatement> getProfitStatements(String stockid) {
		Map<String,ProfitStatement> profitstatements = new TreeMap<String,ProfitStatement>();
		
		String pf = fdataPath + subPath + stockid + "_profitstatement.xls";
		if(!FileTools.isExists(pf)){
			System.out.println(pf + " do NOT exist!!");
			downloadFinancialStatements.downloadProfitStatement(stockid);
			System.out.println(pf + " DONW!");
		}
		
		String str = FileTools.readTextFile(pf);
		if(str.trim().isEmpty()){
			System.out.println(pf + " is EMPTY!!");
			return profitstatements;
		}
		
		
		String[] lines = str.split("\n");
		String[] columns = lines[0].split("\t");
		String[][] cells = new String[columns.length][lines.length];
		
		int i = 0;
		int j = 0;
		for(String line : lines){
			columns = line.split("\t");
			j=0;
			for(String cell : columns){
				cells[j][i] = cell;
				j++;
			}
			i++;
		}
		
		/*
		for(int m=0; m<j; m++){
			for(int n=0; n<i; n++){
				System.out.println("cell["+m+"]["+n+"]=" + cells[m][n]);
			}
		}
		*/
		
		for(int m=1; m<j; m++){
			String period = cells[m][0];
			Integer year = Integer.parseInt(period.substring(0, 4));

			if(period.contains("1231") && year>=theYear){
				ProfitStatement fs = new ProfitStatement();
				fs.setPeriod(period);
				if(lines.length > 50){
					fs.setAllOperatingRevenue(ParseString.toDouble(cells[m][2]));
					fs.setOperatingRevenue(ParseString.toDouble(cells[m][3]));
					fs.setAllOperatingCost(ParseString.toDouble(cells[m][9]));
					fs.setOperatingCost(ParseString.toDouble(cells[m][10]));
					fs.setOperatingExpense(ParseString.toDouble(cells[m][23]));
					fs.setFinanceExpense(ParseString.toDouble(cells[m][24]));
					fs.setSalesExpense(ParseString.toDouble(cells[m][22]));
					fs.setTax(ParseString.toDouble(cells[m][39]));
				}else if(lines.length > 35){ //券商
					fs.setAllOperatingRevenue(ParseString.toDouble(cells[m][2]));
					fs.setOperatingRevenue(ParseString.toDouble(cells[m][2]));
					fs.setAllOperatingCost(ParseString.toDouble(cells[m][15]));
					fs.setOperatingCost(0.00);
					fs.setOperatingExpense(0.00);
					fs.setFinanceExpense(0.00);
					fs.setSalesExpense(0.00);
					fs.setTax(ParseString.toDouble(cells[m][24]));
				}else if(lines.length > 20){	//银行
					fs.setAllOperatingRevenue(ParseString.toDouble(cells[m][2]));
					fs.setOperatingRevenue(ParseString.toDouble(cells[m][3]));
					fs.setAllOperatingCost(ParseString.toDouble(cells[m][4]));
					fs.setOperatingCost(ParseString.toDouble(cells[m][5]));
					fs.setOperatingExpense(ParseString.toDouble(cells[m][8]));
					fs.setFinanceExpense(ParseString.toDouble(cells[m][9]));
					fs.setSalesExpense(ParseString.toDouble(cells[m][7]));
					fs.setTax(ParseString.toDouble(cells[m][20]));
				}
				profitstatements.put(period,fs);
			}
		}
		
/*		for(Map.Entry<String, ProfitStatement> entry : profitstatements.entrySet()){
			System.out.println(entry.getValue());
		}
*/		
		return profitstatements;
		
	}

	@Override
	public Set<String> getReportedStockcode() {
		Set<String> codes = new HashSet<String>();
		String path = fdataPath + subPath;
	    File[] files = new File(path).listFiles();

	    String filename;
//		List<File> files  = FileUtil.getFiles(path, ".xls", false);
		for(File file : files){
			filename = file.getName().substring(0,6);
			if(out.indexOf(filename)==-1){
				codes.add(filename);
			}
		}
		
		return codes;
	}

}
