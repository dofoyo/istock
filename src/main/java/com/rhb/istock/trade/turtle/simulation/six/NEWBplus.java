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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Muster;
/*
 * 高价或低价创新高
 * 
 * 操作策略
 * 买入：全部股票,按价格排序，选出价格最高或最低的55个，突破89日高点
 * 卖出：跌破21日均线
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class NEWBplus {
	protected static final Logger logger = LoggerFactory.getLogger(HLB_try.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 21;
	private Integer top = 1;
	private Integer type = 1;// 1 - 高价， 0 - 低价
	
	public NEWBplus(BigDecimal initCash, Integer type) {
		account = new Account(initCash);
		this.initCash = initCash;
		this.type = type;
	}
	
	public void doIt(Map<String,Muster> musters,List<Map<String,Muster>> previous, LocalDate date, Integer sseiFlag, Integer sseiRatio) {
		Muster muster, pre;
		account.setLatestDate(date);
		Integer ratio;
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		
		//卖出跌破dropline的股票
		boolean droped = false;
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			pre = previous.get(0).get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				//跌破21日均线就卖
				if(muster.isDropAve(21)) { 		
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
					droped=true;
				}
				
/*				//走势弱于大盘
				ratio = this.getRatio(previous, itemID, muster.getLatestPrice());
				if(ratio < sseiRatio) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
					droped = true;
				}*/
				
				//高位回落超过8%
				account.dropFallOrder(itemID, -8,"3");
			}
		}
		
		//行情好，才买入
		//if(sseiFlag==1) {
			holdItemIDs = account.getItemIDsOfHolds();
			
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
			
			//确定突破走势的股票
			List<Muster> breakers = this.getBreakers(new ArrayList<Muster>(musters.values()),holdItemIDs);
			//breakers.addAll(keeps.getUps(musters));
			
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
			
			//先卖后买，完成市值平均
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
			//}
		//}

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
	
	private List<Muster> getBreakers(List<Muster> musters, Set<String> holds){
		List<Muster> breakers = new ArrayList<Muster>();

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
		
		List<Muster> ms = musters.subList(0, musters.size()>=pool ? pool : musters.size());
		
		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					return o1.getLNGap().compareTo(o2.getLNGap());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());
				}
			}
		});
		
		Muster m;
		for(int i=0; i<ms.size() && breakers.size()<top; i++) {
			m = ms.get(i);
			if(m!=null 
					&& !m.isUpLimited() 
					&& m.isUpBreaker() 
					&& m.getHLGap()<=55
					&& !holds.contains(m.getItemID())
					) {
				breakers.add(m);
			}
		}
		
		return breakers;
	}
	
	private Integer getRatio(List<Map<String,Muster>> musters, String itemID, BigDecimal price) {
		Integer ratio = 0;
		BigDecimal lowest=null;
		Muster m;
		for(Map<String,Muster> ms : musters) {
			m = ms.get(itemID);
			if(m!=null) {
				lowest = (lowest==null || lowest.compareTo(m.getLatestPrice())==1) ? m.getLatestPrice() : lowest;
			}
		}
		
		if(lowest!=null && lowest.compareTo(BigDecimal.ZERO)>0) {
			ratio = Functions.growthRate(price, lowest);
		}
		
		//logger.info(String.format("%s, lowest=%.2f, price=%.2f, ratio=%d", itemID, lowest, price,ratio));

		return ratio;
	}
	
}
