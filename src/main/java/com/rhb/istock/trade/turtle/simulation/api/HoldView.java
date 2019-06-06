package com.rhb.istock.trade.turtle.simulation.api;

public class HoldView {
	private String itemID;
	private String name;
	private Integer status; // 1--buy, -1--sell, 0--hold
	
	public HoldView(String itemID, String name, Integer status) {
		this.itemID = itemID;
		this.name = name;
		this.status = status;
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "HoldView [itemID=" + itemID + ", name=" + name + ", status=" + status + "]";
	}
	
	
}
