package com.rhb.istock.item.repository;

public class ItemEntity {
	private String itemID;
	private String code;
	private String name;
	private String area;
	private String industry;
	private String ipo;
	
	public String getItemId() {
		return itemID;
	}

	public void setItemId(String itemID) {
		this.code = itemID.substring(2, 8);
		this.itemID = itemID;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.itemID = code.indexOf("60")==0 ? "sh"+code : "sz"+code;
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getIndustry() {
		return industry;
	}

	public void setIndustry(String industry) {
		this.industry = industry;
	}
	
	

	public String getIpo() {
		return ipo;
	}

	public void setIpo(String ipo) {
		this.ipo = ipo;
	}

	@Override
	public String toString() {
		return "ItemEntity [itemID=" + itemID + ", code=" + code + ", name=" + name + ", area=" + area + ", industry="
				+ industry + ", ipo=" + ipo + "]";
	}
	
	
}
