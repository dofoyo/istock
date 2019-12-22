package com.rhb.istock.fdata;

public class ProfitStatement {
	private String period = "";

	private double allOperatingRevenue = 0.0;  	//总营业收入
	private double operatingRevenue = 0.0;  	//主营业务收入
	private double allOperatingCost = 0.0; 		//总营业成本
	private double operatingCost = 0.0; 		//主营业务成本
	private double operatingExpense = 0.0;  	//管理费用
	private double salesExpense = 0.0; 			//销售费用
	private double financeExpense = 0.0; 		//财务费用
	private double tax = 0.0; 					//营业税金及附加
	private double searchExpense = 0.0;			//研发费用
	
	public double getProfit(){
		return operatingRevenue - operatingCost - operatingExpense - salesExpense - financeExpense - tax - searchExpense; 
	}
	
	public double getSearchExpense() {
		return searchExpense;
	}

	public void setSearchExpense(double searchExpense) {
		this.searchExpense = searchExpense;
	}

	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public double getAllOperatingRevenue() {
		return allOperatingRevenue;
	}
	public void setAllOperatingRevenue(double allOperatingRevenue) {
		this.allOperatingRevenue = allOperatingRevenue;
	}
	public double getOperatingRevenue() {
		return operatingRevenue;
	}
	public void setOperatingRevenue(double operatingRevenue) {
		this.operatingRevenue = operatingRevenue;
	}
	public double getAllOperatingCost() {
		return allOperatingCost;
	}
	public void setAllOperatingCost(double allOperatingCost) {
		this.allOperatingCost = allOperatingCost;
	}
	public double getOperatingCost() {
		return operatingCost;
	}
	public void setOperatingCost(double operatingCost) {
		this.operatingCost = operatingCost;
	}
	public double getOperatingExpense() {
		return operatingExpense;
	}
	public void setOperatingExpense(double operatingExpense) {
		this.operatingExpense = operatingExpense;
	}
	public double getSalesExpense() {
		return salesExpense;
	}
	public void setSalesExpense(double salesExpense) {
		this.salesExpense = salesExpense;
	}
	public double getFinanceExpense() {
		return financeExpense;
	}
	public void setFinanceExpense(double financeExpense) {
		this.financeExpense = financeExpense;
	}
	public double getTax() {
		return tax;
	}
	public void setTax(double tax) {
		this.tax = tax;
	}
	@Override
	public String toString() {
		return "ProfitStatement [period=" + period + ", profit="+ this.getProfit()/100000000 +",allOperatingRevenue="
				+ allOperatingRevenue/100000000 + ", operatingRevenue="
				+ operatingRevenue/100000000 + ", allOperatingCost=" + allOperatingCost/100000000
				+ ", operatingCost=" + operatingCost/100000000 + ", operatingExpense="
				+ operatingExpense/100000000 + ", salesExpense=" + salesExpense/100000000
				+ ", financeExpense=" + financeExpense/100000000 + ", tax=" + tax + "]";
	}

}
