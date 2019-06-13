package com.rhb.istock.kdata.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class KdatasView {
	private String itemID;
	private String code;
	private String name;
	
	List<Kdata> kdatas = new ArrayList<Kdata>();

	public String getItemID() {
		return itemID;
	}
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	public String getNameCode() {
		return name + "(" + code +")";
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

	public List<Kdata> getKdatas() {
		return kdatas;
	}

	public void addKdata(String date, String open, String high, String low, String close) {
		this.kdatas.add(new Kdata(date, open, high, low, close));
	}

	public void addKdata(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		this.kdatas.add(new Kdata(date, open, high, low, close));
	}
	
	
	
	@Override
	public String toString() {
		return "KdatasView [itemID=" + itemID + ", code=" + code + ", name=" + name + ", kdatas=" + kdatas + "]";
	}



	class Kdata {
		private String date;
		private String open;
		private String high;
		private String low;
		private String close;

		public Kdata(String date, String open, String high, String low, String close) {
			this.date = date;
			this.open = open;
			this.high = high;
			this.low = low;
			this.close = close;
		}
		
		public Kdata(LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
			this.date = date.toString();
			this.open = open.toString();
			this.high = high.toString();
			this.low = low.toString();
			this.close = close.toString();
		}

		public String getDate() {
			return date;
		}

		public void setDate(String date) {
			this.date = date;
		}

		public String getOpen() {
			return open;
		}

		public void setOpen(String open) {
			this.open = open;
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

		public void setLow(String low) {
			this.low = low;
		}

		public String getClose() {
			return close;
		}

		public void setClose(String close) {
			this.close = close;
		}

		@Override
		public String toString() {
			return "Kdata [date=" + date + ", open=" + open + ", high=" + high + ", low=" + low + ", close=" + close
					+ "]";
		}
	}

}
