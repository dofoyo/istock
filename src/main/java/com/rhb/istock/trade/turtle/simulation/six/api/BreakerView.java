package com.rhb.istock.trade.turtle.simulation.six.api;

public class BreakerView {
	private String itemID;
	private String name;
	
	public BreakerView(String itemID, String name) {
		this.itemID = itemID;
		this.name = name;
	}
	
	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
