package com.rhb.istock.trade.turtle.operation.api;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import com.rhb.istock.comm.util.Line;

public class TurtleView {
	private String itemID;
	private String code;
	private String name;
	private String high;
	private String low;
	private String pclose;
	private String now;
	private String drop;
	private String hlgap;
	private String nhgap;
	private String line;
	private String atr;
	private String status;
	private String area;
	private String industry;
	private String topic;
	private String label;
	private String note;

	public TurtleView() {
		
	}
	
	public TurtleView(Map<String, String> map) {
		//System.out.println(map);
		this.itemID = map.get("itemID");
		this.code = map.get("code");
		this.name = map.get("name");
		this.high = map.get("high");
		this.low = map.get("low");
		this.now = map.get("now");
		this.drop = map.get("drop");
		this.atr = map.get("atr");
		this.hlgap = map.get("hlgap");
		this.nhgap = map.get("nhgap");
		this.status = map.get("status");
		this.industry = map.get("industry");
		this.area = map.get("area");	
		this.note = map.get("note");
		this.topic = map.get("topic");
		this.label = map.get("label");

	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getLine() {
		Map<String, BigDecimal> prices = new HashMap<String, BigDecimal>();
		prices.put("now", new BigDecimal(this.now));
		prices.put("high", new BigDecimal(this.high));
		prices.put("low", new BigDecimal(this.low));
		if(this.drop!=null) prices.put("drop", new BigDecimal(this.drop));
		if(this.pclose!=null) prices.put("pclose", new BigDecimal(this.pclose));
		
		this.line = Line.draw(prices);

		return line;
	}

	public String getAtr() {
		return atr;
	}

	public void setAtr(String atr) {
		this.atr = atr;
	}

	public void setLine(String line) {
		this.line = line;
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

	public String getHigh() {
		return high;
	}

	public void setHigh(String high) {
		this.high = high;
	}

	public String getLow() {
		return low;
	}

	public String getPclose() {
		return pclose;
	}

	public void setPclose(String pclose) {
		this.pclose = pclose;
	}

	public void setLow(String low) {
		this.low = low;
	}

	public String getNow() {
		return now;
	}

	public void setNow(String now) {
		this.now = now;
	}

	public String getDrop() {
		return drop;
	}

	public void setDrop(String drop) {
		this.drop = drop;
	}

	public String getHlgap() {
		return hlgap;
	}

	public void setHlgap(String hlgap) {
		this.hlgap = hlgap;
	}

	public String getNhgap() {
		return nhgap;
	}

	public void setNhgap(String nhgap) {
		this.nhgap = nhgap;
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

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}
	
	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}

	@Override
	public String toString() {
		return "TurtleView [itemID=" + itemID + ", code=" + code + ", name=" + name + ", high=" + high + ", low=" + low
				+ ", now=" + now + ", drop=" + drop + ", hlgap=" + hlgap + ", nhgap=" + nhgap + ", line=" + line
				+ ", atr=" + atr + ", status=" + status + ", area=" + area + ", industry=" + industry + ", topic="
				+ topic + ", label=" + label + ", note=" + note + "]";
	}

}
