package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;

public class Kdata {
	private String itemID;
	private TreeMap<LocalDate,Kbar> bars;

	public Kdata(String itemID) {
		this.itemID = itemID;
		this.bars = new TreeMap<LocalDate,Kbar>();
	}
	
	public Integer getSize() {
		return this.bars.size();
	}
	
	public BigDecimal[] getTotalAmounts() {
		Integer half = this.bars.size()/2;
		BigDecimal[] total = {new BigDecimal(0),new BigDecimal(0)};
		int i=0;
		for(Kbar bar : bars.values()) {
			if(i++ < half) {
				total[0] = total[0].add(bar.getAmount());
			}else {
				total[1] = total[1].add(bar.getAmount());
			}
		}
		return total;
	}
	
	public BigDecimal getAvarageAmount() {
		if(bars.size() == 0) {
			return new BigDecimal(0);
		}
		
		BigDecimal total = new BigDecimal(0);
		for(Kbar kbar : bars.values()) {
			total = total.add(kbar.getAmount());
		}
		

		return total.divide(new BigDecimal(bars.size()),BigDecimal.ROUND_HALF_UP);
	}
	
	public BigDecimal getAvaragePrice() {
		if(bars.size() == 0) {
			return new BigDecimal(0);
		}
		
		BigDecimal total = new BigDecimal(0);
		for(Kbar kbar : bars.values()) {
			total = total.add(kbar.getClose());
		}

		return total.divide(new BigDecimal(bars.size()),BigDecimal.ROUND_HALF_UP);
	}
	
	public Integer isAboveAvaragePrice() {
		if(bars.size() == 0) {
			return -1;
		}		
		BigDecimal price = this.bars.lastEntry().getValue().getClose();
		return price.compareTo(this.getAvaragePrice());
	}

	
	public Integer getHighLowGap() {
		BigDecimal high = new BigDecimal(0);
		BigDecimal low = new BigDecimal(10000);
		for(Kbar kbar : bars.values()) {
			high = high.compareTo(kbar.getHigh())==-1 ? kbar.getHigh() : high;
			low = low.compareTo(kbar.getLow())==1 ? kbar.getLow() : low;
		}
		BigDecimal rate = high.subtract(low).divide(low,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

		return rate.intValue();
	}
	
	public BigDecimal getAvarageAmount(Integer count) {
		BigDecimal total = new BigDecimal(0);
		NavigableSet<LocalDate> dates = this.bars.descendingKeySet();
		int i=0;
		for(LocalDate date : dates) {
			//System.out.println(date);
			if(i++ < count) {
				total = total.add(this.bars.get(date).getAmount());
			}else {
				break;
			}
		}
		
		return total.divide(new BigDecimal(count),BigDecimal.ROUND_HALF_UP);
	}
	
	public boolean isPotential(Integer count) {
		if(this.bars.size()<count || this.bars.lastEntry().getValue()==null) return false;
		
		BigDecimal now = this.bars.lastEntry().getValue().getClose();
		BigDecimal highest = now;
		
		NavigableSet<LocalDate> dates = this.bars.descendingKeySet();

		int i=0;
		for(LocalDate date : dates) {
			if(i++ < count) {
				highest = highest.compareTo(this.bars.get(date).getHigh())==-1 ? this.bars.get(date).getHigh() : highest;
			}else {
				break;
			}
			
/*			if(this.itemID.equals("sz000620")) {
				System.out.println();
				System.out.println("date: " + date + ", high=" + this.bars.get(date).getHigh() + ", highest=" + highest);
			}*/
		}
		
		BigDecimal ratio = highest.subtract(now).divide(now,BigDecimal.ROUND_HALF_UP);
		boolean isPotential = ratio.compareTo(new BigDecimal(0.1))<0;
		
		//System.out.println(", highest: " + highest + ", now: " + now + ", ratio: " + ratio + ", isBreaker=" + isBreaker);
		
		return isPotential;
	}

	
	public Integer getHighLowGap(Integer count) {
		BigDecimal high = new BigDecimal(0);
		BigDecimal low = new BigDecimal(10000);
		NavigableSet<LocalDate> dates = this.bars.descendingKeySet();
		int i=0;
		for(LocalDate date : dates) {
			if(i++ < count) {
				high = high.compareTo(this.bars.get(date).getHigh())==-1 ? this.bars.get(date).getHigh() : high;
				low = low.compareTo(this.bars.get(date).getLow())==1 ? this.bars.get(date).getLow() : low;
			}else {
				break;
			}
		}
		BigDecimal rate = high.subtract(low).divide(low,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100));

		return rate.intValue();
	}
	
	public void addBar(LocalDate date,Kbar bar) {
		if(bar!=null) {
			this.bars.put(date, bar);
		}
	}
	
	public void addBar(LocalDate date,BigDecimal open,BigDecimal high,BigDecimal low,BigDecimal close,BigDecimal amount,BigDecimal quantity) {
		this.bars.put(date, new Kbar(open, high, low, close, amount, quantity,date));
	}
	
	public void addBar(String date,String open,String high,String low,String close,String amount, String quantity) {
		this.bars.put(LocalDate.parse(date), new Kbar(new BigDecimal(open), new BigDecimal(high), new BigDecimal(low), new BigDecimal(close), new BigDecimal(amount), new BigDecimal(quantity),LocalDate.parse(date)));
	}
	
	public Kbar getBar(LocalDate date){
		return this.bars.get(date);
	}
	
	public List<LocalDate> getDates() {
		List<LocalDate> dates = new ArrayList<LocalDate>();
		for(Map.Entry<LocalDate, Kbar> entry : this.bars.entrySet()) {
			dates.add(entry.getKey());
		}
		return dates;
	}
	
	public String getItemID() {
		return itemID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}
	
	public String getString() {
		//System.out.println(bars.size());
		StringBuffer sb = new StringBuffer();
		sb.append("'"+itemID+"'	日线\n");
		sb.append("日期	开盘	最高	最低	收盘	成交量	成交额\n");
		for(Map.Entry<LocalDate, Kbar> entry : bars.entrySet()) {
			sb.append(entry.getKey());
			sb.append(" ");
			sb.append(entry.getValue().getOpen());
			sb.append(" ");
			sb.append(entry.getValue().getHigh());
			sb.append(" ");
			sb.append(entry.getValue().getLow());
			sb.append(" ");
			sb.append(entry.getValue().getClose());
			sb.append(" ");
			sb.append(entry.getValue().getQuantity());
			sb.append(" ");
			sb.append(entry.getValue().getAmount());
			sb.append("\n");
		}
		
		return sb.toString();
	}

	

}
