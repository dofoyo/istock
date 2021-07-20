package com.rhb.istock.trade.turtle.simulation.six.api;

import java.time.LocalDate;

public class BriefView {
	private String itemID;
	private String name;
	private Integer profit; 
	private LocalDate date;
	private boolean isHold;
	
	public BriefView(String itemID, String name, Integer profit, LocalDate date, boolean isHold) {
		this.itemID = itemID;
		this.name = name;
		this.profit = profit;
		this.date = date;
		this.isHold = isHold;
	}
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public String getName() {
		return isHold ? "*"+this.name : this.name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getProfit() {
		return profit;
	}

	public void setProfit(Integer profit) {
		this.profit = profit;
	}

	public String getColor() {
		return this.profit>0 ? "red" : "green";
	}

	@Override
	public String toString() {
		return "BriefView [itemID=" + itemID + ", name=" + name + ", profit=" + profit + ", date=" + date + "]";
	}
	
	
}
