package com.rhb.istock.selector.bluechip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.rhb.istock.fdata.BalanceSheet;
import com.rhb.istock.fdata.CashFlow;
import com.rhb.istock.fdata.ProfitStatement;

public class FinancialReport {
	private String stockcode;
	
	private Map<String,BalanceSheet> balancesheets;
	private Map<String,CashFlow> cashflows;
	private Map<String,ProfitStatement> profitstatements;
	
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
	
	public String getGoodPeriod(Integer year){
		StringBuffer sb = new StringBuffer();
		
		if(this.isOK(year) || this.isOK(year-1) || this.isOK(year-3)){ //近三年中有一次OK，就为good
			sb.append(year);
			sb.append(",");
		}
		return sb.toString();
	}
	
}
