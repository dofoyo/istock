package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.account.Account;
import com.rhb.istock.kdata.Muster;

/*
 * 放量新高后回调
 *
 */
public class AVB2 {
	protected static final Logger logger = LoggerFactory.getLogger(AVB2.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 21;
	private Integer top = 3;
	private Map<String,Integer> breakers = new HashMap<String,Integer>();
	
	public AVB2(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, LocalDate date, Integer sseiFlag) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Integer value;
		Map.Entry<String, Integer> entry;
		for (Iterator<Map.Entry<String, Integer>> iterator = this.breakers.entrySet().iterator(); iterator.hasNext();){
		    entry = iterator.next();
			value = entry.getValue();
			if(value>=13) {    //突破后13日内回调有效
				iterator.remove();
			}else {
				entry.setValue(value++);
			}
		}

/*		if(sseiFlag==1) {
			this.top = 8;
		}else {
			this.top = 1;
		}*/
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		
		//卖出跌破dropline或lowest的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				if(muster.isDropAve(21) && !muster.isDownLimited()) { 		//跌破21日均线就卖
					account.drop(itemID, "1", muster.getLatestPrice());
				}
/*				if(muster.isDropLowest(21) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "跌破lowest", muster.getLatestPrice());
				}*/
/*				if(sseiFlag==0 && muster.isDropLowest(13) && !muster.isDownLimited()) { 		//跌破13日均线就卖
					account.drop(itemID, "1", muster.getLatestPrice());
				}				
				if(sseiFlag==1 && muster.isDropLowest(34) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "2", muster.getLatestPrice());
				}*/
			}
		}				
		
		//行情好，才买入
			holdItemIDs = account.getItemIDsOfHolds();
			
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
			
			//确定突破走势的股票
			//List<Muster> breakers = this.getTops(new ArrayList<Muster>(musters.values()));
			this.setBreakers(new ArrayList<Muster>(musters.values()));
			List<Muster> bs = this.getBreakers(musters);

			
			breakers_sb.append(date.toString() + ",");
			StringBuffer sb = new StringBuffer();
			for(Muster breaker : bs) {
				if(!holdItemIDs.contains(breaker.getItemID())) {
					dds.add(breaker);
					sb.append(breaker.getItemName());
					sb.append(",");
				}
				breakers_sb.append(breaker.getItemID());
				breakers_sb.append(",");
			}
			breakers_sb.deleteCharAt(breakers_sb.length()-1);
			breakers_sb.append("\n");
			
			//先卖后买，完成调仓和开仓
			//logger.info("先卖后买，完成调仓");
			if(!dds.isEmpty()) {
				holdItemIDs = account.getItemIDsOfHolds();
				Set<Integer> holdOrderIDs;
				for(String itemID: holdItemIDs) {
					holdOrderIDs = 	account.getHoldOrderIDs(itemID);
					muster = musters.get(itemID);
					if(muster!=null) {
						for(Integer holdOrderID : holdOrderIDs) {
							account.dropByOrderID(holdOrderID, "0", muster.getLatestPrice());   //先卖
							dds.add(muster);						
						}
					}
				}
				
				//System.out.println(dds.size());
				account.openAll(dds);			//后买
			}

		dailyAmount_sb.append(account.getDailyAmount() + "\n");
	}
	
	public Map<String,String> result() {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", account.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
		result.put("dailyAmount", dailyAmount_sb.toString());
		result.put("breakers", breakers_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}

	private List<Muster> getBreakers(Map<String,Muster> musters){
		List<Muster> bs = new ArrayList<Muster>();
		String id;
		Integer days;
		Muster m;
		Map.Entry<String, Integer> entry;
		for (Iterator<Map.Entry<String, Integer>> iterator = this.breakers.entrySet().iterator(); iterator.hasNext();){
		    entry = iterator.next();
			id = entry.getKey();
			days = entry.getValue();
			if(musters.containsKey(id)) {
				m = musters.get(id);
				if(m.getLatestPrice().compareTo(m.getAveragePrice13())<0
						&& m.getLatestPrice().compareTo(m.getAveragePrice21())>=0
						) {  //突破后跌破13日线
					bs.add(musters.get(id));
					iterator.remove();
				}else if(m.getLatestPrice().compareTo(m.getAveragePrice21())<0) {
					iterator.remove();
				}
			}
		}
		
		return bs;
	}

	
	private void setBreakers(List<Muster> musters){
		//List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
/*				if(o2.getPrviousAverageAmountRatio().equals(o1.getPrviousAverageAmountRatio())) {
					return o1.getLatestPrice().compareTo(o2.getLatestPrice());
				}else {
					return o2.getPrviousAverageAmountRatio().compareTo(o1.getPrviousAverageAmountRatio()); //Z-A
				}*/
				return o2.getAverageAmount().compareTo(o1.getAverageAmount()); //Z-A
			}
		});

		Muster m;
		for(int i=0; i<musters.size() && i<pool && breakers.size()<top; i++) {
			m = musters.get(i);
			if(m!=null 
					&& !m.isUpLimited() 
					&& !m.isDownLimited() 
					&& m.isUpBreaker()
					//&& m.isAboveAverageAmount()
					//&& m.isUp(21)
					//&& m.cal_volume_ratio().compareTo(new BigDecimal(2))==1
					&& !breakers.containsKey(m.getItemID())
					) {
				breakers.put(m.getItemID(),0);
			}
		}
		
	}
	
}
