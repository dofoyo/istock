package com.rhb.istock.trade.turtle.manual;

import java.time.LocalDate;

public class SelectView {
	private String itemID;
	private String name;
	private LocalDate date;
	
	public SelectView(String itemID, String name, LocalDate date) {
		this.itemID = itemID;
		this.name = name;
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

	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}
	
}
