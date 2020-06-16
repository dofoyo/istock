package com.rhb.istock.selector.bluechip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import com.rhb.istock.fdata.sina.BalanceSheet;
import com.rhb.istock.fdata.sina.CashFlow;
import com.rhb.istock.fdata.sina.ProfitStatement;

public class FinancialReport {
	private Map<String,BalanceSheet> balancesheets;
	private Map<String,CashFlow> cashflows;
	private Map<String,ProfitStatement> profitstatements;
	
	//销售收入	销售收入持续增长，且年均增长率大于20%
	//利润		利润持续增长，且年均增长率大于20%
	//现金流		经营活动现金流为正，且持续增长
	//应收账款		应收账款的增长率小于销售收入的增长率
	//现金流与利润的比率大于1
	//应收占比销售额的比例小于20%
	public boolean isOK(int year){
		//this.setYear(year);
		boolean flag = false;
		if(this.getRateOfProfit(year)>=0.2  //利润持续增长，且每年(不是年均)增长率大于20%
			//&& this.getRateOfOperatngRevenue(year)>=0.2 //销售收入持续增长，且年均增长率大于20%
			//&& this.getGrossProfitMargin(year)>0.3 //毛利率大于30% 
			//&& this.getRateOfCashflow(year)>0.0  //经营活动现金流为正，且持续增长
			//&& this.getRateOfOperatngRevenue(year)>this.getRateOfAccountsReceivable(year) //应收账款的增长率小于销售收入的增长率
			//&& this.getCPR(year)>=1  //现金流与利润的比率大于1
			//&& this.aboveZeroOfCashflow(year)    //现金流大于零
			//&& this.getReceivableRatio(year)<=0.1  //应收占比销售额的比例小于10%
			&& this.getDAR(year)<=0.4             //资产负债率
			&& this.getGoodwillRate(year)<=0.05    //资产商誉率
			){
			flag = true;
			//System.out.println();
		}
		
		return flag;
	}
	
	//利润现金含量  cashflow to profit ratio
	public Double getCPR(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		if(this.cashflows.get(periods[0])!=null && this.profitstatements.get(periods[0])!=null) {
			Double cash = ((CashFlow)this.cashflows.get(periods[0])).getNetCashFlow();
			Double profit = ((ProfitStatement)this.profitstatements.get(periods[0])).getProfit();
			return profit.intValue()==0 ? 0.0 : cash/profit;
		}else {
			return 0.0;
		}
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
		String[] periods= new String[5];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		periods[3] = Integer.toString(year-3) + "1231";
		periods[4] = Integer.toString(year-4) + "1231";
		Double rate = 0.0;
		if(this.cashflows.containsKey(periods[0])
				&& this.cashflows.containsKey(periods[1])
				&& this.cashflows.containsKey(periods[2])
				&& this.cashflows.containsKey(periods[3])
				&& this.cashflows.containsKey(periods[4])
				){

			double or0 = ((CashFlow)this.cashflows.get(periods[0])).getNetCashFlow();
			double or1 = ((CashFlow)this.cashflows.get(periods[1])).getNetCashFlow();
			double or2 = ((CashFlow)this.cashflows.get(periods[2])).getNetCashFlow();
			double or3 = ((CashFlow)this.cashflows.get(periods[3])).getNetCashFlow();
			double or4 = ((CashFlow)this.cashflows.get(periods[4])).getNetCashFlow();
			
			
			
			if(or0>or1 && or1>or2 && or2>or3 && or3>or4 && or4>0){
				//rate = (or1-or3)/or3;
				//rate = Math.sqrt(or1/or2)-1;
				rate = Math.pow(or1/or4, 1.0/4)-1;
			}
			//System.out.println("");
			//System.out.println(String.format("%d: %f, %f, %f, %f", year,or1,or2,or3,rate));
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
			
			//rate = (or1-or3)/or3;
			rate = Math.sqrt(or1/or3)-1;
			//rate = Math.pow(or1/or3, 1.0/2)-1;
		}
		return rate;
	}
	
	public Double getRateOfProfit(Integer year){
		String[] periods= new String[5];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		periods[3] = Integer.toString(year-3) + "1231";
		periods[4] = Integer.toString(year-4) + "1231";
		Double rate = 0.0;
		if(this.profitstatements.containsKey(periods[0])
				&& this.profitstatements.containsKey(periods[1])
				&& this.profitstatements.containsKey(periods[2])
				&& this.profitstatements.containsKey(periods[3])
				&& this.profitstatements.containsKey(periods[4])
				){

		
			double or0 = ((ProfitStatement)this.profitstatements.get(periods[0])).getProfit();
			double or1 = ((ProfitStatement)this.profitstatements.get(periods[1])).getProfit();
			double or2 = ((ProfitStatement)this.profitstatements.get(periods[2])).getProfit();
			double or3 = ((ProfitStatement)this.profitstatements.get(periods[3])).getProfit();
			double or4 = ((ProfitStatement)this.profitstatements.get(periods[4])).getProfit();
			
			TreeSet<Double> rates = new TreeSet<Double>();
			rates.add(or0/or1-1.0);
			rates.add(or1/or2-1.0);
			rates.add(or2/or3-1.0);
			rates.add(or3/or4-1.0);
			rate = rates.first();
/*			if(or0>or1 && or1>or2 && or2>or3 && or3>or4 && or4>0){
				//rate = (or1-or3)/or3;
				//rate = Math.sqrt(or1/or3)-1;
				rate = Math.pow(or1/or4, 1.0/4)-1;
			}
*/		    
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
				//rate = (or1-or3)/or3;
				rate = Math.sqrt(or1/or3)-1;
				//rate = Math.pow(or1/or3, 1.0/2)-1;
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

	//商誉  Goodwill to assets ratio
	public Double getGoodwillRate(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		//periods[1] = Integer.toString(year-1) + "1231";
		//periods[2] = Integer.toString(year-2) + "1231";
		//System.out.println(year);
		return ((BalanceSheet)this.balancesheets.get(periods[0])).getGoodwillRate();
	}
	
	//资产负债率  debt to assets ratio
	public Double getDAR(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		//periods[1] = Integer.toString(year-1) + "1231";
		//periods[2] = Integer.toString(year-2) + "1231";
		return ((BalanceSheet)this.balancesheets.get(periods[0])).getDAR();
	}
	
	//利润现金含量  cashflow to profit ratio
	public boolean aboveZeroOfCashflow(Integer year){
		String[] periods= new String[3];
		periods[0] = Integer.toString(year) + "1231";
		periods[1] = Integer.toString(year-1) + "1231";
		periods[2] = Integer.toString(year-2) + "1231";
		
		double cash=0.0,cash1=0.0,cash2=0.0,profit=0.0,profit1=0.0,profit2=0.0;
		
		//double d = 0.0;
		/*if(this.cashflows.get(periods[0])!=null && this.profitstatements.get(periods[0])!=null &&
				this.cashflows.get(periods[1])!=null && this.profitstatements.get(periods[1])!=null &&
				this.cashflows.get(periods[2])!=null && this.profitstatements.get(periods[2])!=null
				){
			cash = ((CashFlow)this.cashflows.get(periods[0])).getNetCashFlow();
			cash1 = ((CashFlow)this.cashflows.get(periods[1])).getNetCashFlow();
			cash2 = ((CashFlow)this.cashflows.get(periods[2])).getNetCashFlow();
			profit = ((ProfitStatement)this.profitstatements.get(periods[0])).getProfit();
			profit1 = ((ProfitStatement)this.profitstatements.get(periods[1])).getProfit();
			profit2 = ((ProfitStatement)this.profitstatements.get(periods[2])).getProfit();
			//d =  profit.intValue()==0 ? 0.0 : cash/profit;
		}*/
		if(this.cashflows.get(periods[0])!=null && 
				this.cashflows.get(periods[1])!=null &&
				this.cashflows.get(periods[2])!=null 
				){
			cash = ((CashFlow)this.cashflows.get(periods[0])).getNetCashFlow();
			cash1 = ((CashFlow)this.cashflows.get(periods[1])).getNetCashFlow();
			cash2 = ((CashFlow)this.cashflows.get(periods[2])).getNetCashFlow();
		}
		
		return cash2>0 & cash>cash2;
	}
	
	//毛利率
	public Double getGrossProfitMargin(Integer year){
		String period = Integer.toString(year) + "1231";
		double d = 0.0;
		ProfitStatement ps = (ProfitStatement)this.profitstatements.get(period);
		if(ps!=null) {
			Double revenue = ps.getOperatingRevenue();
			Double profit = ps.getProfit();
			
			d = revenue.intValue()==0 ? 0.0 : profit/revenue;
		}
		
		return d;
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
