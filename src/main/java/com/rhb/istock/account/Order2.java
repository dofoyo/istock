package com.rhb.istock.account;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Order2 {
	private Integer orderID;
	private String itemID;
	private String itemName;
	private BigDecimal latestPrice;
	private Integer quantity;
	private List<Detail> details;

	public Integer getOrderID() {
		return orderID;
	}

	public Order2(Integer orderID, String itemID, String itemName,BigDecimal latestPrice) {
		this.orderID = orderID;
		this.itemID = itemID;
		this.itemName = itemName;
		this.latestPrice = latestPrice;
		this.quantity = 0;
		details = new ArrayList<Detail>();
	}
	
	class Detail{
		private LocalDate date;  
		private BigDecimal price;
		private Integer quantity;
		private BigDecimal fee;
		private Integer type;  // 1 - 买入， -1 - 卖出 
		private String note;
		
		public Detail(LocalDate date, BigDecimal price, Integer quantity, Integer type, String note, BigDecimal fee) {
			this.date = date;
			this.price = price;
			this.quantity = quantity;
			this.type = type;
			this.note = note;
			this.fee = fee;
		}

		public Integer getQuantity() {
			return quantity * type;
		}
		
		/*
		 * 买入时，quantity为正、amount为负
		 * 卖出时，quantity为负、amount为正
		 */
		public BigDecimal getAmount() {
			return this.price.multiply(new BigDecimal(quantity*type)).multiply(new BigDecimal(-1));
		}
		
		public String getCSV() {
			StringBuffer sb = new StringBuffer();
			sb.append(this.date);
			sb.append(",");
			sb.append(this.price);
			sb.append(",");
			sb.append(this.quantity * this.type);
			sb.append(",");
			sb.append(this.getAmount());
			sb.append(",");
			sb.append(this.fee);
			sb.append(",");
			sb.append(this.note);
			return sb.toString();
		}
	}
	
	public Integer getQuantity() {
		return this.quantity;
	}
	
	public String getItemID() {
		return this.itemID;
	}
	
	public void setLatestPrice(BigDecimal price) {
		this.latestPrice = price;
	}
	public BigDecimal getLatestPrice() {
		return this.latestPrice;
	}
	
	
	//买入
	public BigDecimal buy(LocalDate date, BigDecimal price, Integer quantity, String note) {
		this.quantity = this.quantity + quantity;
		BigDecimal amount = price.multiply(new BigDecimal(quantity));
		BigDecimal fee = amount.multiply(new BigDecimal(0.001));
		Detail detail = new Detail(date, price, quantity, 1, note, fee);
		this.details.add(detail);
		
		return amount.add(fee);
	}
	
	//卖出
	public BigDecimal sell(LocalDate date, BigDecimal price,Integer quantity, String note) {
		BigDecimal amount = price.multiply(new BigDecimal(quantity));
		BigDecimal fee = amount.multiply(new BigDecimal(0.002));
		Detail detail = new Detail(date, price, quantity, -1, note, fee);
		this.details.add(detail);
		
		this.quantity = this.quantity - quantity;

		return amount.subtract(fee);
	}
	
	//获得市值
	public BigDecimal getValue() {
		BigDecimal	amount = this.latestPrice.multiply(new BigDecimal(this.quantity));
		return amount;
		
	}
	
	//是否还持有该合同
	public boolean isHold() {
		return this.quantity==0 ? false : true;
	}
	
	public String getCSV(LocalDate endDate) {
		StringBuffer sb = new StringBuffer();
		for(Detail detail : this.details) {
			sb.append(this.orderID);
			sb.append(",");
			sb.append(this.itemID);
			sb.append(",");
			sb.append(this.itemName);
			sb.append(",");
			sb.append(detail.getCSV());
			sb.append("\n");
		}
		if(this.isHold()) {
			sb.append(this.orderID);
			sb.append(",");
			sb.append(this.itemID);
			sb.append(",");
			sb.append(this.itemName);
			sb.append(",");
			sb.append(endDate);
			sb.append(",");
			sb.append(this.latestPrice);
			sb.append(",");
			sb.append(this.quantity*-1);
			sb.append(",");
			sb.append(this.latestPrice.multiply(new BigDecimal(this.quantity)));
			sb.append(",");
			sb.append(0);
			sb.append(",");
			sb.append("hold");
			sb.append("\n");
		}
		return sb.toString();
	}
	
	public BigDecimal getProfit() {
		BigDecimal profit = BigDecimal.ZERO;
		for(Detail detail : details) {
			profit = profit.add(detail.getAmount()); //买入时amount为负，卖出时amount为正，所以，profit为正表示盈利
		}
		
		return profit.add(this.getValue());
	}
	
}
