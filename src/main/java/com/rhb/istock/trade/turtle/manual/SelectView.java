package com.rhb.istock.trade.turtle.manual;

import java.time.LocalDate;

public class SelectView {
	private String itemID;
	private String name;
	private LocalDate date;
	private String type;
	
	public SelectView(String itemID, String name, LocalDate date, String type) {
		this.itemID = itemID;
		this.name = name;
		this.date = date;
		this.type = type;
	}
	
	public String getItemID() {
		return itemID;
	}
	public String getName() {
		return name;
	}

	public LocalDate getDate() {
		return date;
	}

	public String getType() {
		return type;
	}
	
}
