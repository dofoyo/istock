package com.rhb.istock.trade.twin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;


/**
 * status: 
 * 	1 -- 表示做多
 * -1 -- 表示做空
 * 
 */
public class Wfeature implements Comparable<Wfeature>{
	private String itemID;
	private LocalDate date;
	private Integer duration;
	private BigDecimal nowPrice;
	private BigDecimal nowLongLinePrice;
	private BigDecimal nowShortLinePrice;
	private Integer nowDarkDays = 0; 	//在最近的一个duration中，shortLine在longLine之下的天数
	private BigDecimal preLongLinePrice;
	private BigDecimal preShortLinePrice;
	private Integer preDarkDays = 0; 	//在最近的一个duration中，shortLine在longLine之下的天数
	private Integer status = 0;
	
	public Integer getStatus() {	
		return status;
	}

	public Wfeature(String itemID, Integer duration) {
		this.itemID = itemID;
		this.duration = duration;
	}
	
	public Integer getBiasOfNowPriceAndShortLine() {
		return getBias(nowPrice,nowShortLinePrice);
	}

	public Integer getBiasOfShortAndLong() {
		return getBias(nowShortLinePrice,nowLongLinePrice);
	}
	
	private Integer getBias(BigDecimal price1, BigDecimal price2) {
		BigDecimal i = new BigDecimal(0);
		if(price1!=null && price2!=null){
			i = (price1.subtract(price2)).divide(price2,2,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).abs();
		}
		return i.intValue();
	}
	
	
	public LocalDate getDate() {
		return date;
	}

	public void setDate(LocalDate date) {
		this.date = date;
	}

	public Integer getDuration() {
		return duration;
	}

	public void setDuration(Integer duration) {
		this.duration = duration;
	}

	public String getItemID() {
		return itemID;
	}

	public BigDecimal getNowPrice() {
		return nowPrice;
	}
	public void setNowPrice(BigDecimal nowPrice) {
		this.nowPrice = nowPrice;
	}
	public BigDecimal getNowLongLinePrice() {
		return nowLongLinePrice;
	}
	public void setNowLongLinePrice(BigDecimal nowLongLinePrice) {
		this.preLongLinePrice = this.nowLongLinePrice;
		this.nowLongLinePrice = nowLongLinePrice;
	}
	public BigDecimal getNowShortLinePrice() {
		return nowShortLinePrice;
	}
	public void setNowShortLinePrice(BigDecimal nowShortLinePrice) {
		this.preShortLinePrice = this.nowShortLinePrice;
		this.nowShortLinePrice = nowShortLinePrice;
	}
	public BigDecimal getPreLongLinePrice() {
		return preLongLinePrice;
	}
	public void setPreLongLinePrice(BigDecimal preLongLinePrice) {
		this.preLongLinePrice = preLongLinePrice;
	}
	public BigDecimal getPreShortLinePrice() {
		return preShortLinePrice;
	}
	public void setPreShortLinePrice(BigDecimal preShortLinePrice) {
		this.preShortLinePrice = preShortLinePrice;
	}

	public Integer getNowDarkDays() {
		return nowDarkDays;
	}
	
	public void setPrices(BigDecimal nowPrice, BigDecimal nowShortLinePrice, BigDecimal nowLongLinePrice) {
		this.nowPrice = nowPrice;
		this.preShortLinePrice = this.nowShortLinePrice;
		this.preLongLinePrice = this.nowLongLinePrice;
		this.nowShortLinePrice = nowShortLinePrice;
		this.nowLongLinePrice = nowLongLinePrice;
		this.status = nowShortLinePrice.compareTo(nowLongLinePrice)==1 ? 1 : -1;
	}

	public void setNowDarkDays(Integer nowDarkDays) {
		this.preDarkDays = this.nowDarkDays;
		this.nowDarkDays = nowDarkDays;
		if(nowDarkDays == preDarkDays-1) {
			status = 2;
		}
		if(nowDarkDays == preDarkDays + 1) {
			status = -2;
		}
	}

	public Integer getPreDarkDays() {
		return preDarkDays;
	}

	public void setPreDarkDays(Integer preDarkDays) {
		this.preDarkDays = preDarkDays;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Wfeature [itemID=" + itemID + ", date=" + date + ", duration=" + duration + ", nowPrice=" + nowPrice
				+ ", nowLongLinePrice=" + nowLongLinePrice + ", nowShortLinePrice=" + nowShortLinePrice
				+ ", nowDarkDays=" + nowDarkDays + ", preLongLinePrice=" + preLongLinePrice + ", preShortLinePrice="
				+ preShortLinePrice + ", preDarkDays=" + preDarkDays + ", status=" + status + ", getStatus()="
				+ getStatus() + ", getBiasOfNowPriceAndShortLine()=" + getBiasOfNowPriceAndShortLine()
				+ ", getBiasOfShortAndLong()=" + getBiasOfShortAndLong() + "]";
	}

	@Override
	public int compareTo(Wfeature o) {
		return o.getNowPrice().compareTo(this.getNowPrice());  //倒叙
	}

}
