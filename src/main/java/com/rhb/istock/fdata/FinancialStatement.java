package com.rhb.istock.fdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FinancialStatement {

	private Map<String,BalanceSheet> balancesheets;
	private Map<String,CashFlow> cashflows;
	private Map<String,ProfitStatement> profitstatements;
	
	//private String[] periods={"20151231","20141231","20131231"};

	//private Set<Integer> goodPeriods = new TreeSet<Integer>();

/*	public void refreshGoodPeriods(LocalDate date){
		//this.fs = new FinancialStatements(this.stockId,dataPath);
		
		int endYear = this.getLastPeriod(date);
		int beginYear = endYear - 2;

		this.goodPeriods = new TreeSet<Integer>();
		for(int year=beginYear; year<=endYear; year++){
			if(this.isOK(year)){
				this.goodPeriods.add(year);
			}
		}
	}*/
	

	//销售收入	销售收入持续增长，且年均增长率大于20%(二年增长大于44%)
	//利润		利润持续增长，且年均增长率大于20%
	//现金流		经营活动现金流为正，且持续增长，且年均增长率大于20%
	//应收账款		应收账款的增长率小于销售收入的增长率
	//现金流与利润的比率大于1
	//应收占比销售额的比例小于20%
	public boolean isOK(int year){
		//this.setYear(year);
		boolean flag = false;
		if(this.getRateOfOperatngRevenue(year)>0.44 //销售收入持续增长，且年均增长率大于20%(二年增长大于44%)
			&& this.getRateOfProfit(year)>0.44  //利润持续增长，且年均增长率大于20%(二年增长大于44%)
			&& this.getRateOfCashflow(year)>0.44  //经营活动现金流为正，且持续增长，且年均增长率大于20%(二年增长大于44%)
			//&& this.getRateOfOperatngRevenue(year)>this.getRateOfAccountsReceivable(year) //应收账款的增长率小于销售收入的增长率
			&& this.getCPR(year)>=1  //现金流与利润的比率大于1
			&& this.getReceivableRatio(year)<=0.2  //应收占比销售额的比例小于20%
			){
			flag = true;
			//System.out.println();
		}
		
		return flag;
	}
	
	
	public void setBalancesheets(Map<String, BalanceSheet> balancesheets) {
		this.balancesheets = balancesheets;
		//System.out.println("there are " + this.balancesheets.size() + " balancesheets.");
	}

	public void setCashflows(Map<String, CashFlow> cashflows) {
		this.cashflows = cashflows;
	}

	public void setProfitstatements(Map<String, ProfitStatement> profitstatements) {
		this.profitstatements = profitstatements;
	}

	/*
	 * 当年只能得到上一年的年报，如2018年只能得到2017年的年报
	 * 在2018年5月以前，有些公司还没有发布最新年报，只能得到2016年的年报
	 */
	public int getLastPeriod(){
		int p = 0;
		for(String period : balancesheets.keySet()){
			int i = Integer.parseInt(period.substring(0, 4));
			if(i > p){
				p = i;
			}
		}
		return p;
	}
	
	public int getFirstPeriod(){
		int p = 100000;
		for(String period : balancesheets.keySet()){
			int i = Integer.parseInt(period.substring(0, 4));
			if(i < p){
				p = i;
			}
		}
		return p;		
	}
	
	public List<Integer> getPeriods(){
		List<Integer> years = new ArrayList<Integer>();
		for(String period : balancesheets.keySet()){
			years.add(Integer.parseInt(period.substring(0, 4)));
		}
		Collections.sort(years);
		return years;
	}
	
	public boolean exists(String period){
		boolean flag = false;
		if(this.balancesheets.containsKey(period)
			&& this.cashflows.containsKey(period)
			&& this.profitstatements.containsKey(period)){
			flag = true;
		}
		return flag;
	}
	
/*	public void setYear(int year){
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
	}*/
	
/*	public String getCashflowString(){
		StringBuffer sb = new StringBuffer();
		if(this.cashflows.containsKey(periods[0])
				&& this.cashflows.containsKey(periods[1])
				&& this.cashflows.containsKey(periods[2])){

			sb.append(((CashFlow)this.cashflows.get(periods[0])).toString());
			sb.append("\n");
			sb.append(((CashFlow)this.cashflows.get(periods[1])).toString());
			sb.append("\n");
			sb.append(((CashFlow)this.cashflows.get(periods[2])).toString());
			
		}
		return sb.toString();
	}
*/	
	public Double getRateOfCashflow(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		Double rate = 0.0;
		if(this.cashflows.containsKey(periods[0])
				&& this.cashflows.containsKey(periods[1])
				&& this.cashflows.containsKey(periods[2])){

			double or1 = ((CashFlow)this.cashflows.get(periods[0])).getNetCashFlow();
			double or2 = ((CashFlow)this.cashflows.get(periods[1])).getNetCashFlow();
			double or3 = ((CashFlow)this.cashflows.get(periods[2])).getNetCashFlow();
			
			if(or1>or2 && or2>or3 && or3>0){
				rate = (or1-or3)/or3;
			}
			
		}
			
		return rate;
	}
	
/*	public String getBalanceSheetString(){
		StringBuffer sb = new StringBuffer();
		if(this.balancesheets.containsKey(periods[0])
				&& this.balancesheets.containsKey(periods[1])
				&& this.balancesheets.containsKey(periods[2])){

			sb.append(((BalanceSheet)this.balancesheets.get(periods[0])).toString());
			sb.append("\n");
			sb.append(((BalanceSheet)this.balancesheets.get(periods[1])).toString());
			sb.append("\n");
			sb.append(((BalanceSheet)this.balancesheets.get(periods[2])).toString());
			
		}
		return sb.toString();
	}*/
	
	public Double getRateOfAccountsReceivable(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		Double rate = 0.0;
		if(this.balancesheets.containsKey(periods[0])
				&& this.balancesheets.containsKey(periods[1])
				&& this.balancesheets.containsKey(periods[2])){

			double or1 = ((BalanceSheet)this.balancesheets.get(periods[0])).getAccountsReceivable();
			//double or2 = ((BalanceSheet)this.balancesheets.get(periods[1])).getAccountsReceivable();
			double or3 = ((BalanceSheet)this.balancesheets.get(periods[2])).getAccountsReceivable();
			
			rate = (or1-or3)/or3;
		}
		return rate;
	}
	
/*	public String getProfitStatementString(){
		StringBuffer sb = new StringBuffer();
		if(this.profitstatements.containsKey(periods[0])
				&& this.profitstatements.containsKey(periods[1])
				&& this.profitstatements.containsKey(periods[2])){

			sb.append(((ProfitStatement)this.profitstatements.get(periods[0])).toString());
			sb.append("\n");
			sb.append(((ProfitStatement)this.profitstatements.get(periods[1])).toString());
			sb.append("\n");
			sb.append(((ProfitStatement)this.profitstatements.get(periods[2])).toString());
			
		}
		return sb.toString();
	}*/
		
	public Double getRateOfProfit(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		Double rate = 0.0;
		if(this.profitstatements.containsKey(periods[0])
				&& this.profitstatements.containsKey(periods[1])
				&& this.profitstatements.containsKey(periods[2])){

		
			double or1 = ((ProfitStatement)this.profitstatements.get(periods[0])).getProfit();
			double or2 = ((ProfitStatement)this.profitstatements.get(periods[1])).getProfit();
			double or3 = ((ProfitStatement)this.profitstatements.get(periods[2])).getProfit();
			
			if(or1>or2 && or2>or3 && or3>0){
				rate = (or1-or3)/or3;
			}
		}
		return rate;
	}
	
	public Double getRateOfOperatngRevenue(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";

		Double rate = 0.0;
		if(this.profitstatements.containsKey(periods[0])
				&& this.profitstatements.containsKey(periods[1])
				&& this.profitstatements.containsKey(periods[2])){
			double or1 = ((ProfitStatement)this.profitstatements.get(periods[0])).getOperatingRevenue();
			double or2 = ((ProfitStatement)this.profitstatements.get(periods[1])).getOperatingRevenue();
			double or3 = ((ProfitStatement)this.profitstatements.get(periods[2])).getOperatingRevenue();
					
			if(or1>or2 && or2>or3 && or3>0){
				rate = (or1-or3)/or3;
			}
		}
		return rate;
	}
	
	public Double getTurnoverRatioOfReceivable(int year){
		String period = Integer.toString(year) + "1231";
		double d = 0.0;
		
		BalanceSheet bs = (BalanceSheet)this.balancesheets.get(period);
		ProfitStatement ps = (ProfitStatement)this.profitstatements.get(period);
		if(bs != null && ps != null){
			double accountsReceivable = bs.getAccountsReceivable();
			double operatingRevenue = ps.getOperatingRevenue();
			d = operatingRevenue/accountsReceivable;
		}
		
		return d;
	}
	
	//资产负债率  debt to assets ratio
	public Double getDAR(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		return ((BalanceSheet)this.balancesheets.get(periods[0])).getDAR();
	}
	
	//利润现金含量  cashflow to profit ratio
	public Double getCPR(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		Double cash = ((CashFlow)this.cashflows.get(periods[0])).getNetCashFlow();
		Double profit = ((ProfitStatement)this.profitstatements.get(periods[0])).getProfit();
		return profit.intValue()==0 ? 0.0 : cash/profit;
	}
	
	//应收占比 Receivable Ratio
	public Double getReceivableRatio(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		BalanceSheet bs = (BalanceSheet)this.balancesheets.get(periods[0]);
		ProfitStatement ps = (ProfitStatement)this.profitstatements.get(periods[0]);
		
		Double operating = ps.getOperatingRevenue();
		Double receivable = 0.0;
		if(bs!=null){
			receivable = bs.getAccountsReceivable();
		}
		
		return operating.intValue()==0 ? 0.0 : receivable/operating;
		
	}
	
/*	public boolean isGood(){
		return this.goodPeriods.size()>1 || this.goodPeriods.contains(this.getLastPeriod(LocalDate.now()));
	}
	
	
	public int getGoodTimes(){
		return this.goodPeriods.size();
	}
	*/
	
/*	public String getGoodPeriod(Integer year){
		StringBuffer sb = new StringBuffer();
		boolean goodLastPeriod = false;
		for(int i=year; i>year-3; i--){
			if(this.isOK(i)){
				sb.append(i);
				sb.append(",");
				if(i==year || i==year-1){
					goodLastPeriod = true;
				}
			}
		}
	
		int goodtimes = StringUtils.countOccurrencesOf(sb.toString(), ",");
		
		return (goodtimes>1 || goodLastPeriod) ? sb.substring(0, sb.length()) : "";
	}*/
	
	
	public String getGoodPeriod(Integer year){
		StringBuffer sb = new StringBuffer();
		
		//if(this.isOK(year)){ //当年ok才能为good
		if(this.isOK(year) || this.isOK(year-1) || this.isOK(year-3)){ //近三年中有一次OK，就为good
			sb.append(year);
			sb.append(",");
		}
		
/*		if(this.isOK(year-1) && this.isOK(year-2)){
			sb.append(year-1);
			sb.append(",");
			sb.append(year-2);
			sb.append(",");
		}*/
		
		
		return sb.toString();
	}
	

/*	public String getRateOfFinancialStatements(int year,String stockName){
		this.setYear(year);
		StringBuffer sb = new StringBuffer();
		sb.append(stockName);
		sb.append(",");
		sb.append("RateOfOperatngRevenue = ");
		sb.append(this.getRateOfOperatngRevenue());
		sb.append(",");
		sb.append("RateOfProfit = ");
		sb.append(this.getRateOfProfit());
		sb.append(",");
		sb.append("RateOfCashflow = ");
		sb.append(this.getRateOfCashflow());
		sb.append(",");
		sb.append("RateOfAccountsReceivable = ");
		sb.append(this.getRateOfAccountsReceivable());
		
		return sb.toString();
	}*/
/*	
	public String getDetailOfFinancialStatements(int year){
		this.setYear(year);
		StringBuffer sb = new StringBuffer();
		sb.append(this.getBalanceSheetString());
		sb.append("\n");
		sb.append(this.getProfitStatementString());
		sb.append("\n");
		sb.append(this.getCashflowString());
		return sb.toString();

	}*/

	
}
