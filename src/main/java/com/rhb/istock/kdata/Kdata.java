package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class Kdata {
	private String itemID;
	private TreeMap<LocalDate,Kbar> bars;

	Map<String,BigDecimal> features = null;
	Map<String,BigDecimal> averagePrices = null;
	
	public Kdata(String itemID) {
		this.itemID = itemID;
		this.bars = new TreeMap<LocalDate,Kbar>();
	}
	
	public Integer getSize() {
		return this.bars.size();
	}
	
	public Map<String,BigDecimal> getFeatures(){
		if(this.features == null) {
			this.features = new HashMap<String,BigDecimal>();

			BigDecimal highest = new BigDecimal(0);
			BigDecimal lowest = new BigDecimal(10000);
			BigDecimal lowest21 = new BigDecimal(10000);
			BigDecimal lowest34 = new BigDecimal(10000);
			BigDecimal totalAmount = new BigDecimal(0);
			BigDecimal totalPrice = new BigDecimal(0);
			BigDecimal total_turnover_rate_f = new BigDecimal(0);
			BigDecimal total_volume_ratio = new BigDecimal(0);
			
			int j = bars.size()-21;
			int k = bars.size()-34;
			int i = 0;
			for(Kbar kbar : bars.values()) {
				highest = highest.compareTo(kbar.getHigh())==-1 ? kbar.getHigh() : highest;
				lowest = lowest.compareTo(kbar.getLow())==1 ? kbar.getLow() : lowest;
				totalAmount = totalAmount.add(kbar.getAmount());
				totalPrice = totalPrice.add(kbar.getClose());
				total_turnover_rate_f = total_turnover_rate_f.add(kbar.getTurnover_rate_f());
				total_volume_ratio = total_volume_ratio.add(kbar.getVolume_ratio());
						
				if(i>=j && lowest21.compareTo(kbar.getLow())==1) {
					lowest21 =  kbar.getLow();
				}

				if(i>=k && lowest34.compareTo(kbar.getLow())==1) {
					lowest34 =  kbar.getLow();
				}
				
				i++;
				
			}
				
			BigDecimal averageAmount = totalAmount.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP);
			BigDecimal averagePrice = totalPrice.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP);
			BigDecimal average_turnover_rate_f = total_turnover_rate_f.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP);
			BigDecimal average_volume_ratio = total_volume_ratio.divide(new BigDecimal(this.bars.size()),BigDecimal.ROUND_HALF_UP);

			features.put("highest", highest);
			features.put("lowest", lowest);
			features.put("lowest21", lowest21);
			features.put("lowest34", lowest34);
			features.put("averageAmount", averageAmount);
			features.put("averagePrice", averagePrice);
			features.put("average_turnover_rate_f", average_turnover_rate_f);
			features.put("average_volume_ratio", average_volume_ratio);
		}
		
		return this.features;
	}	
	
	public boolean isUp() {
		Map<String, BigDecimal> ap = this.getAveragePrices();
		Map<String, BigDecimal> fe = this.getFeatures(); 
		return  //latestPrice.compareTo(averagePrice8)==1 &&
				ap.get("a8").compareTo(ap.get("a13"))==1 &&
						ap.get("a13").compareTo(ap.get("a21"))==1 &&
								ap.get("a21").compareTo(ap.get("a34"))==1 &&
										ap.get("a34").compareTo(fe.get("averagePrice"))==1;
	}
	
	public Map<String,BigDecimal> getAveragePrices(){
		if(this.averagePrices == null) {
			this.averagePrices = new HashMap<String,BigDecimal>();
			
			BigDecimal totalPrice8 = new BigDecimal(0);
			BigDecimal totalPrice13 = new BigDecimal(0);
			BigDecimal totalPrice21 = new BigDecimal(0);
			BigDecimal totalPrice34 = new BigDecimal(0);

			Integer a8 = this.bars.size() - 8;
			Integer a13 = this.bars.size() - 13;
			Integer a21 = this.bars.size() - 21;
			Integer a34 = this.bars.size() - 34;
			Integer i=0;
			for(Kbar kbar : bars.values()) {
				totalPrice8  = i>= a8  ? totalPrice8.add(kbar.getClose()) : totalPrice8;
				totalPrice13 = i>= a13 ? totalPrice13.add(kbar.getClose()) : totalPrice13;
				totalPrice21 = i>= a21 ? totalPrice21.add(kbar.getClose()) : totalPrice21;
				totalPrice34 = i>= a34 ? totalPrice34.add(kbar.getClose()) : totalPrice34;

				i++;
			}
				
			BigDecimal averagePrice8 = totalPrice8.divide(new BigDecimal(8),BigDecimal.ROUND_HALF_UP);
			BigDecimal averagePrice13 = totalPrice13.divide(new BigDecimal(13),BigDecimal.ROUND_HALF_UP);
			BigDecimal averagePrice21 = totalPrice21.divide(new BigDecimal(21),BigDecimal.ROUND_HALF_UP);
			BigDecimal averagePrice34 = totalPrice34.divide(new BigDecimal(34),BigDecimal.ROUND_HALF_UP);

			averagePrices.put("a8", averagePrice8);
			averagePrices.put("a13", averagePrice13);
			averagePrices.put("a21", averagePrice21);
			averagePrices.put("a34", averagePrice34);
		}
		
		return this.averagePrices;
	}
	
	public boolean isAboveAveragePrice(Integer days) {
		if(bars.size() == 0) {
			return false;
		}		
		BigDecimal price = this.bars.lastEntry().getValue().getClose();
		BigDecimal avaragePrice = null;
		if(days == 8) {
			avaragePrice = this.getAveragePrices().get("a8");
		}else if(days == 21) {
			avaragePrice = this.getAveragePrices().get("a21");
		}else if(days == 34) {
			avaragePrice = this.getAveragePrices().get("a34");
		}else{
			avaragePrice = this.getFeatures().get("averagePrice");
		}
		
		return price.compareTo(avaragePrice)==1;
	}
	
	public Kbar getLastBar() {
		return this.bars.lastEntry().getValue();
	}

	public void addBar(LocalDate date,Kbar bar) {
		if(bar!=null) {
			this.bars.put(date, bar);
		}
	}
	
	public void addBar(LocalDate date,
			BigDecimal open,
			BigDecimal high,
			BigDecimal low,
			BigDecimal close,
			BigDecimal amount,
			BigDecimal quantity,
			BigDecimal turnover_rate_f,
			BigDecimal volume_ratio,
			BigDecimal total_mv,
			BigDecimal circ_mv,
			BigDecimal total_share,
			BigDecimal float_share,
			BigDecimal free_share
			) {
		this.bars.put(date, new Kbar(open, high, low, close, amount, quantity,date, 
				turnover_rate_f, volume_ratio,total_mv,circ_mv,total_share,float_share,free_share));
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
	
}
