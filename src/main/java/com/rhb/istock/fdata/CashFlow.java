package com.rhb.istock.fdata;

public class CashFlow {
	private String period = "";
	private double purchaseAssets = 0.0;  		//购建固定资产、无形资产及其他长期资产所支付的现金
	private double depreciationAssets = 0.0; 	//固定资产折旧+无形资产摊销+递延资产摊销+长期待摊费用摊销
	private double netCashFlow = 0.0; 			//经营活动现金流量净额
	
		
	public String getPeriod() {
		return period;
	}
	public void setPeriod(String period) {
		this.period = period;
	}
	public double getPurchaseAssets() {
		return purchaseAssets;
	}
	public void setPurchaseAssets(double purchaseAssets) {
		this.purchaseAssets = purchaseAssets;
	}
	public double getDepreciationAssets() {
		return depreciationAssets;
	}
	public void setDepreciationAssets(double depreciationAssets) {
		this.depreciationAssets = depreciationAssets;
	}
	public double getNetCashFlow() {
		return netCashFlow;
	}
	public void setNetCashFlow(double netCashFlow) {
		this.netCashFlow = netCashFlow;
	}
	@Override
	public String toString() {
		return "CashFlow [period=" + period + ", purchaseAssets="
				+ purchaseAssets/100000000 + ", depreciationAssets=" + depreciationAssets/100000000
				+ ", netCashFlow=" + netCashFlow/100000000 + "]";
	}

	
}
