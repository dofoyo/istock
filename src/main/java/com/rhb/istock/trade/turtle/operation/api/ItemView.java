package com.rhb.istock.trade.turtle.operation.api;

import java.util.List;
import java.util.Map;

public class ItemView {
	private String itemID;
	private String code;
	private String name;
	private String status;
	private String area;
	private String industry;
	private String topic;
	private String label;
	private String fina;
	private String price;

	public ItemView(Map<String, String> map) {
		//System.out.println(map);
		this.itemID = map.get("itemID");
		this.code = map.get("code");
		this.name = map.get("name");
		this.status = map.get("status");
		this.industry = map.get("industry");
		this.area = map.get("area");	
		this.fina = map.get("fina");
		this.topic = map.get("topic");
		this.label = map.get("label");
		this.price = map.get("price");
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getLabel() {
		return label;
	}

	public void addLabel(String label) {
		if(label==null || label.isEmpty()) return;
				
		if(this.label==null || this.label.isEmpty()) {
			this.label = label;
		}else {
			this.label = this.label + "，" + label;
		}
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public void setLabels(List<String> ss) {
		StringBuffer sb = new StringBuffer();
		for(String s : ss) {
			sb.append(s);
			sb.append(",");
		}
		if(sb.length()>0) sb.deleteCharAt(sb.length()-1);
		this.label = sb.toString();
	}

	public String getLine() {
		return "";
	}

	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
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

	public String getFina() {
		return fina;
	}

	public void setFina(String fina) {
		this.fina = fina;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
}
