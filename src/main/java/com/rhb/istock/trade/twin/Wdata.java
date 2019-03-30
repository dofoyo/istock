package com.rhb.istock.trade.twin;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 */
public class Wdata {
	protected static final Logger logger = LoggerFactory.getLogger(Wdata.class);
	
	private String itemID;
	private Integer duration; 	
	private Integer longLine;
	private Integer shortLine;
	private List<Bar> bars;
	private Wfeature feature;
	
	public Wdata(String itemID, 
			Integer duration, 
			Integer longLine, 
			Integer shortLine) {
		this.itemID = itemID;
		this.duration = duration;
		this.longLine = longLine;
		this.shortLine = shortLine;
		this.bars = new ArrayList<Bar>();
		this.feature = new Wfeature(itemID,duration);
	}
	
	public boolean isEmpty() {
		return this.bars.isEmpty();
	}
	
	public void addBar(LocalDate date, BigDecimal price) {
		Bar bar = new Bar(date,price);
		this.bars.add(bar);
		if(this.bars.size()>longLine) {
			this.bars.remove(0);
		}
		bar.setLongLinePrice(getLinePrice(longLine));
		bar.setShortLinePrice(getLinePrice(shortLine));
		
		feature.setDate(date);
		//feature.setNowPrice(price);
		//feature.setNowLongLinePrice(bar.getLongLinePrice());
		//feature.setNowShortLinePrice(bar.getShortLinePrice());
		feature.setPrices(price,bar.getShortLinePrice(),bar.getLongLinePrice());
		feature.setNowDarkDays(getDarkDays());
	}
	
	public Wfeature getFeature() {
		if(this.bars.size()<longLine) return null;

		return feature;
	}
	
	private Integer getDarkDays() {
		Integer total = 0;
		Integer fromIndex = this.bars.size()>duration ? this.bars.size()-duration : 0;
		List<Bar> sub = this.bars.subList(fromIndex, this.bars.size());
		for(Bar bar : sub) {
			if(bar.isDark()) {
				total ++;
			}
		}
		//System.out.format("from %s to %s,darkDays:%d\n",sub.get(0).getDate().toString(),sub.get(sub.size()-1).getDate().toString(), total);
		return total;
	}
	
	private BigDecimal getLinePrice(Integer line) {
		BigDecimal total = new BigDecimal(0);
		Integer fromIndex = this.bars.size()>line ? this.bars.size()-line : 0;
		List<Bar> sub = this.bars.subList(fromIndex, this.bars.size());
		for(Bar bar : sub) {
			total = total.add(bar.getPrice());
		}
		//System.out.println(sub);
		return total.divide(new BigDecimal(sub.size()), BigDecimal.ROUND_HALF_UP);
	}
	
	class Bar {
		private LocalDate date;
		private BigDecimal price;
		private BigDecimal longLinePrice;
		private BigDecimal shortLinePrice;
		
		public Bar(LocalDate date, BigDecimal price) {
			this.date = date;
			this.price = price;
		}
		
		public LocalDate getDate() {
			return date;
		}

		public void setDate(LocalDate date) {
			this.date = date;
		}

		public BigDecimal getPrice() {
			return price;
		}

		public void setPrice(BigDecimal price) {
			this.price = price;
		}

		public BigDecimal getLongLinePrice() {
			return longLinePrice;
		}

		public void setLongLinePrice(BigDecimal longLinePrice) {
			this.longLinePrice = longLinePrice;
		}

		public BigDecimal getShortLinePrice() {
			return shortLinePrice;
		}

		public void setShortLinePrice(BigDecimal shortLinePrice) {
			this.shortLinePrice = shortLinePrice;
		}

		public boolean isDark() {
			return shortLinePrice.compareTo(longLinePrice)<=0;
		}

		@Override
		public String toString() {
			return "Bar [date=" + date + ", price=" + price + "]";
		}
		
		
	}
}
