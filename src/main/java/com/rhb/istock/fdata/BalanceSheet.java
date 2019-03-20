package com.rhb.istock.fdata;


public class BalanceSheet{
	private String period = "";
	private Double cash = 0.0;       			//资产负债表.货币资金
	private Double inventories = 0.0;  			//资产负债表.存货净额
	private Double accountsReceivable = 0.0; 	//资产负债表.应收账款净额
	private Double notesReceivable = 0.0;  		//资产负债表.应收票据
	private Double payables = 0.0;      		//资产负债表.预收帐款
	private Double debt = 0.0; //资产负债表.负债合计
	private Double assets = 0.0; // //资产负债表.资产总计
	

	public Double getDAR(){
		return this.assets.intValue()==0 ? 0.0 : this.debt/this.assets;
	}
	
	public Double getDebt() {
		return debt;
	}

	public void setDebt(Double debt) {
		this.debt = debt;
	}

	public Double getAssets() {
		return assets;
	}

	public void setAssets(Double assets) {
		this.assets = assets;
	}

	@Override
	public String toString() {
		return "BalanceSheet [period=" + period + ", cash=" + cash/100000000
				+ ", inventories=" + inventories/100000000 + ", accountsReceivable="
				+ accountsReceivable/100000000 + ", notesReceivable=" + notesReceivable/100000000
				+ ", payables=" + payables/100000000 + ", debt=" + debt/100000000 + ", assets="
				+ assets/100000000 + "]";
	}

	public String getPeriod() {
		return period;
	}

	public void setPeriod(String period) {
		this.period = period;
	}

	public Double getCash() {
		return cash;
	}
	public void setCash(Double cash) {
		this.cash = cash;
	}
	public Double getInventories() {
		return inventories;
	}
	public void setInventories(Double inventories) {
		this.inventories = inventories;
	}

	public Double getAccountsReceivable() {
		return accountsReceivable;
	}

	public void setAccountsReceivable(Double accountsReceivable) {
		this.accountsReceivable = accountsReceivable;
	}

	public Double getNotesReceivable() {
		return notesReceivable;
	}
	public void setNotesReceivable(Double notesReceivable) {
		this.notesReceivable = notesReceivable;
	}

	public Double getPayables() {
		return payables;
	}
	public void setPayables(Double payables) {
		this.payables = payables;
	}
	
}
