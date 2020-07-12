package com.rhb.istock.fdata.tushare;

import com.rhb.istock.comm.util.Functions;

public class GrowModel {
	private String itemID;
	private String itemName;
	
	private Fina b_fina;
	private Fina e_fina;
	
	private Integer revenueCAGR = 0;
	private Integer cashflowCAGR = 0;
	private Integer profitCAGR = 0;
	
	public GrowModel(String itemID, String itemName,Fina b_fina, Fina e_fina, Integer n) {
		this.itemID = itemID;
		this.itemName = itemName;
		this.b_fina = b_fina;
		this.e_fina = e_fina;
		//System.out.println(e_fina.getIncome());
		//System.out.println(b_fina.getIncome());
		if(b_fina!=null && e_fina!=null && b_fina.isValid() && e_fina.isValid()) {
			this.revenueCAGR = Functions.cagr(e_fina.getIncome().getRevenue(), b_fina.getIncome().getRevenue(),n);
			this.cashflowCAGR = Functions.cagr(e_fina.getCashflow().getN_cashflow_act(), b_fina.getCashflow().getN_cashflow_act(),n);
			this.profitCAGR	= Functions.cagr(e_fina.getIndicator().getProfit_dedt(), b_fina.getIndicator().getProfit_dedt(),n);
		}
	}
	
	public boolean isOK() {
		return revenueCAGR >= 20 
				&& cashflowCAGR > 0
				&& profitCAGR > 0
				&& e_fina.isOK()
				&& b_fina.isOK()
				;
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getItemName() {
		return itemName;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public Fina getB_fina() {
		return b_fina;
	}

	public void setB_fina(Fina b_fina) {
		this.b_fina = b_fina;
	}

	public Fina getE_fina() {
		return e_fina;
	}

	public void setE_fina(Fina e_fina) {
		this.e_fina = e_fina;
	}

	public Integer getRevenueCAGR() {
		return revenueCAGR;
	}

	public void setRevenueCAGR(Integer revenueCAGR) {
		this.revenueCAGR = revenueCAGR;
	}

	public Integer getCashflowCAGR() {
		return cashflowCAGR;
	}

	public void setCashflowCAGR(Integer cashflowCAGR) {
		this.cashflowCAGR = cashflowCAGR;
	}

	public Integer getProfitCAGR() {
		return profitCAGR;
	}

	public void setProfitCAGR(Integer profitCAGR) {
		this.profitCAGR = profitCAGR;
	}

	@Override
	public String toString() {
		return "GrowModel [itemID=" + itemID + ", itemName=" + itemName + ", b_fina=" + b_fina + ", e_fina=" + e_fina
				+ ", revenueCAGR=" + revenueCAGR + ", cashflowCAGR=" + cashflowCAGR + ", profitCAGR=" + profitCAGR
				+ "]";
	}


	
}
