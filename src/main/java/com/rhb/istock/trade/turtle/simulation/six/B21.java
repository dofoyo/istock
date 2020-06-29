package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;
/*
 * 突破21日线
 * 
 * 操作策略
 * 筛选：全部股票,按价格排序,选出最高或最低的55个。
 * 买入：突破21日均线，同时横盘、放量、升势、强于大盘这四个条件中，同时满足越多越好
 * 卖出：跌破21日均线
 * 仓位：满仓，每只股票的均衡市值
 *
 */
public class B21 {
	protected static final Logger logger = LoggerFactory.getLogger(HLB_try.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 21;
	private Integer top = 1;
	private Integer type = 1;  // 1 - 高价， 0 - 低价

	public B21(BigDecimal initCash, Integer type) {
		account = new Account(initCash);
		this.initCash = initCash;
		this.type = type;
	}
	
	public void doIt(Map<String,Muster> musters,Map<String,Muster> previous, LocalDate date, Integer sseiRatio) {
		Muster muster;
		account.setLatestDate(date);
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
			}
		}
		
		//卖出跌破dropline的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				//跌破21日均线就卖
				if(muster.isDropAve(21) && !muster.isDownLimited()) { 		
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}
				
/*				//涨幅超过21%，则跌破8日线要卖出
				if(account.getUpRatio(itemID)>=21 && muster.isDropAve(8) && !muster.isDownLimited()) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}*/
				
			}
		}
		
		holdItemIDs = account.getItemIDsOfHolds();
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定突破走势的股票
		List<Muster> breakers = this.getBreakers(musters, previous, sseiRatio);
		
		breakers_sb.append(date.toString() + ",");
		StringBuffer sb = new StringBuffer();
		for(Muster breaker : breakers) {
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
		// 当cash不够买入新股时，要卖出市值高与平均值的股票。
		//此举可避免高位加仓的现象出现
		if(!dds.isEmpty()) {
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
	
	private List<Muster> getBreakers(Map<String,Muster> ms,Map<String,Muster> previous, Integer sseiRatio){
		List<Muster> breakers = new ArrayList<Muster>();
		
		List<Muster> musters = new ArrayList<Muster>(ms.values());
		
		if(this.type == 0) {
			Collections.sort(musters, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //价格小到大排序
				}
			});
		}else {
			Collections.sort(musters, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o2.getLatestPrice().compareTo(o1.getLatestPrice()); //价格大到小排序
				}
			});
		}
		
		
		Muster m,p;
		Integer goals, ratio;
		Selector selector = new Selector();
		for(int i=0; i<musters.size() && i<this.pool; i++) {
			m = musters.get(i);
			p = previous.get(m.getItemID());
			goals = 0;
			if(m!=null && p!=null && !m.isUpLimited() 
					&& m.isJustBreaker(8)  //刚刚突破21日线，同时21日线在89日线上放不超过8%
					) {
				
				if(m.getAverageGap()<8) {    //均线在8%范围内纠缠
					goals++;
				}
				
				if(m.getAveragePrice21().compareTo(p.getAveragePrice21())==1) { //上升趋势
					goals++;
				}
				
				if(m.getAverageAmount().compareTo(p.getAverageAmount())==1) { // 放量
					goals++;
				}
				
				/*ratio = Functions.growthRate(m.getClose(),p.getClose());
				if(ratio >= sseiRatio) {  //强于大盘
					goals++;
				}*/
				
				if(goals>0) {
					selector.add(goals, m.getItemID());
				}
			}
		}
		
		List<String> results = selector.getResults();
		if(results!=null) {
			for(int i=0; i<this.top && i<results.size(); i++) {
				breakers.add(ms.get(results.get(i)));
			}
		}
		
		return breakers;
	}
	
	class Selector {
		TreeMap<Integer, List<String>> ids = new TreeMap<Integer, List<String>>();
		public void add(Integer i, String id) {
			List<String> ss = ids.get(i);
			if(ss == null) {
				ss = new ArrayList<String>();
				ids.put(i, ss);
			}
			ss.add(id);
		}
		
		public List<String> getResults() {
			if(ids.lastEntry()!=null) {
				return ids.lastEntry().getValue();
			}else {
				return null;
			}
		}
		
	}
}
