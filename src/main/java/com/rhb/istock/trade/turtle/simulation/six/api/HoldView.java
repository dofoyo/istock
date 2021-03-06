package com.rhb.istock.trade.turtle.simulation.six.api;

import java.time.LocalDate;

public class HoldView {
	private String itemID;
	private String name;
	private Integer status; // 1--buy, -1--sell, 0--hold
	private LocalDate date;
	
	public HoldView(String itemID, String name, Integer status, LocalDate date) {
		this.itemID = itemID;
		this.name = name;
		this.status = status;
		this.date = date;
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
	
	public String getColor() {
		return this.status>0 ? "red" : "green";
	}

	@Override
	public String toString() {
		return "HoldView [itemID=" + itemID + ", name=" + name + ", status=" + status + "]";
	}
	
	
}
