package com.rhb.istock.selector.aat;

import java.math.BigDecimal;
import java.time.LocalDate;

public class AverageAmount  implements Comparable<AverageAmount>{
	@Override
	public String toString() {
		return "BarEntity [date=" + date + ", itemID=" + itemID + ", amount=" + amount + "]";
	}

	private LocalDate date;
	private String itemID;
	private BigDecimal amount = new BigDecimal(0);
	
	public AverageAmount(String itemID) {
		this.itemID = itemID;
	}
	
	public AverageAmount(LocalDate date,String itemID, BigDecimal amount) {
		this.date = date;
		this.itemID = itemID;
		this.amount = amount;
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

	public BigDecimal getAmount() {
		return amount;
	}
	
	public Integer getAmountInt() {
		return amount.divide(new BigDecimal(100000),BigDecimal.ROUND_HALF_UP).intValue();
	}

	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}
	
	@Override
	public int compareTo(AverageAmount o) {
		return o.getAmount().compareTo(this.getAmount()); //倒叙
	}
	
}
