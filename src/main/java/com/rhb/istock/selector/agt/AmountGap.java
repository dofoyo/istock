package com.rhb.istock.selector.agt;

import java.math.BigDecimal;

public class AmountGap implements Comparable<AmountGap>{
	private String itemID;
	private BigDecimal[] total;
	
	public AmountGap(String itemID, BigDecimal[] total) {
		this.itemID = itemID;
		this.total = total;
	}
	
	public String getItemID() {
		return itemID;
	}

	public BigDecimal[] getTotal() {
		return total;
	}

	public void setTotal(BigDecimal[] total) {
		this.total = total;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	private BigDecimal getGap() {
		return total[0].divide(total[1],BigDecimal.ROUND_HALF_UP);
	}
	
	@Override
	public int compareTo(AmountGap o) {
		return this.getGap().compareTo(o.getGap());
	}
}
