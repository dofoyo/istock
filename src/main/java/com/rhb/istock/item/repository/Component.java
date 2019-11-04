package com.rhb.istock.item.repository;

import java.time.LocalDate;

public class Component {
	private String itemID;
	private String itemName;
	private LocalDate beginDate;
	private LocalDate endDate;
	
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
	public LocalDate getBeginDate() {
		return beginDate;
	}
	public void setBeginDate(LocalDate beginDate) {
		this.beginDate = beginDate;
	}
	public LocalDate getEndDate() {
		return endDate;
	}
	public void setEndDate(LocalDate endDate) {
		this.endDate = endDate;
	}
	@Override
	public String toString() {
		return "Component [itemID=" + itemID + ", itemName=" + itemName + ", beginDate=" + beginDate + ", endDate="
				+ endDate + "]";
	}
	
}
