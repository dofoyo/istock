package com.rhb.istock.fdata.spider;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.HttpDownload;

@Service("downloadFinancialStatementsFromSina")
public class DownloadFinancialStatementsFromSina implements
		DownloadFinancialStatements {
	
	@Value("${fdataPath}")
	private String fdataPath;
	
	private String subPath = "\\fina\\sina\\";
	
	public void downloadBalanceSheet(String stockid) {
		//http://money.finance.sina.com.cn/corp/go.php/vDOWN_BalanceSheet/displaytype/4/stockid/300022/ctrl/all.phtml
		String url = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_BalanceSheet/displaytype/4/stockid/"+stockid+"/ctrl/all.phtml";
		String pathAndfileName = fdataPath + subPath + stockid + "_balancesheet.xls";
		HttpDownload.saveToFile(url, pathAndfileName);
		System.out.println(stockid + "_balancesheet.xls have downloaded!");		
	}

	public void downloadCashFlow(String stockid) {
		//http://money.finance.sina.com.cn/corp/go.php/vDOWN_CashFlow/displaytype/4/stockid/300022/ctrl/all.phtml
		String url = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_CashFlow/displaytype/4/stockid/"+stockid+"/ctrl/all.phtml";
		String pathAndfileName = fdataPath + subPath + stockid + "_cashflow.xls";
		HttpDownload.saveToFile(url, pathAndfileName);
		System.out.println(stockid + "_cashflow.xls have downloaded!");		
		
	}

	public void downloadProfitStatement(String stockid) {
		//http://money.finance.sina.com.cn/corp/go.php/vDOWN_ProfitStatement/displaytype/4/stockid/300022/ctrl/all.phtml
		String url = "http://money.finance.sina.com.cn/corp/go.php/vDOWN_ProfitStatement/displaytype/4/stockid/"+stockid+"/ctrl/all.phtml";
		String pathAndfileName = fdataPath + subPath + stockid + "_profitstatement.xls";
		HttpDownload.saveToFile(url, pathAndfileName);
		System.out.println(stockid + "_profitstatement.xls have downloaded!");		
		
	}
	
	@Override
	public String downloadBalanceSheetUrl(String stockid) {
		return "http://money.finance.sina.com.cn/corp/go.php/vDOWN_BalanceSheet/displaytype/4/stockid/"+stockid+"/ctrl/all.phtml";
	}

	@Override
	public String downloadCashFlowUrl(String stockid) {
		return "http://money.finance.sina.com.cn/corp/go.php/vDOWN_CashFlow/displaytype/4/stockid/"+stockid+"/ctrl/all.phtml";
	}

	@Override
	public String downloadProfitStatementUrl(String stockid) {
		return "http://money.finance.sina.com.cn/corp/go.php/vDOWN_ProfitStatement/displaytype/4/stockid/"+stockid+"/ctrl/all.phtml";
	}

	@Override
	public void down(Map<String,String> urls) {
		int i = 0;
		for(Map.Entry<String, String> entry : urls.entrySet()){
			HttpDownload.saveToFile(entry.getValue(), fdataPath + subPath + entry.getKey());
			//System.out.print(i++ + "/" + urls.size() + "\r");

			//System.out.print(entry.getKey() + " have downloaded!");		
			
			//为避免被反扒工具禁止，需要暂停一下
			try {
				Thread.sleep(5000);  //5秒
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
			} 
			
		}
		//System.out.print("共下载了 " + urls.size() + " 份。");

	}

}
