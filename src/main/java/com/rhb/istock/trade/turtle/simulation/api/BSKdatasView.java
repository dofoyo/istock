package com.rhb.istock.trade.turtle.simulation.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.rhb.istock.kdata.api.KdatasView;

public class BSKdatasView extends KdatasView {
	private List<BSView> buys = new ArrayList<BSView>();
	private List<BSView> sells = new ArrayList<BSView>();
	
	public List<BSView> getBuys() {
		return buys;
	}

	public void setBuys(List<BSView> buys) {
		this.buys = buys;
	}

	public List<BSView> getSells() {
		return sells;
	}

	public void setSells(List<BSView> sells) {
		this.sells = sells;
	}
	
	public void addBuys(Map<String,String> bs) {
		for(Map.Entry<String, String> entry : bs.entrySet()) {
			buys.add(new BSView(entry.getKey(),entry.getValue()));
		}
	}
	
	public void addSells(Map<String,String> ss) {
		for(Map.Entry<String, String> entry : ss.entrySet()) {
			sells.add(new BSView(entry.getKey(),entry.getValue()));
		}
	}

	class BSView{
		private String date;
		private String highest;
		private String price;
		
		public BSView(String date,String price) {
			this.date = date;
			this.highest = price;
			this.price = price;
		}
		
		public String getDate() {
			return date;
		}
		public void setDate(String date) {
			this.date = date;
		}
		public String getHighest() {
			return highest;
		}
		public void setHighest(String highest) {
			this.highest = highest;
		}
		public String getPrice() {
			return price;
		}
		public void setPrice(String price) {
			this.price = price;
		}
	}
	
}
