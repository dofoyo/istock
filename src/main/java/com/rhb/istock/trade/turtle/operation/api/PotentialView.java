package com.rhb.istock.trade.turtle.operation.api;

public class PotentialView {
	private String itemID;
	private String itemName;
	
	public PotentialView(String itemID, String name) {
		this.itemID = itemID;
		this.itemName = name;
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

	
	@Override
	public String toString() {
		return "PotentialView [itemID=" + itemID + ", itemName=" + itemName + "]";
	}

}
