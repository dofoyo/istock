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

import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;
/*
 * 操作策略
 * 买入：突破89日高点
 * 卖出：跌破21日均线
 * 筛选范围：全部股票中筛选出21个，再从中最多选出5个突破的（可能会选不出来）
 * 筛选依据：89天的高点和低点形成的通道越窄、价格越低
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class HLB {
	protected static final Logger logger = LoggerFactory.getLogger(HLB.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer pool = 21;
	private Integer top = 5;

	
	public HLB(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, LocalDate date) {
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
			}
		}
		
		//卖出跌破dropline或lowest的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				if(muster.isDrop(21) && !muster.isDownLimited()) {
					account.drop(itemID, "跌破dropLine", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
				if(muster.isDropLowest(21) && !muster.isDownLimited()) {
					account.drop(itemID, "跌破lowest", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
			}
		}				
		
		holdItemIDs = account.getItemIDsOfHolds();
		
		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
		//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
		
		//确定突破走势的股票
		List<Muster> breakers = this.getTops(new ArrayList<Muster>(musters.values()));
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
		if(!dds.isEmpty()) {
			Set<String> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null) {
					for(String holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "调仓", muster.getLatestPrice());   //先卖
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
	
	private List<Muster> getTops(List<Muster> musters){
		List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
/*					
 					//303万
  					if(o1.getHLGap().compareTo(o2.getHLGap())==0){
							return o1.getLNGap().compareTo(o2.getLNGap()); //a-z
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
					}*/
					
/*					//391万
					if(o1.getHLGap().compareTo(o2.getHLGap())==0){
						if(o1.getN21Gap().compareTo(o2.getN21Gap())==0){  
							return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
						}else {
							return o1.getN21Gap().compareTo(o2.getN21Gap()); //a-z
						}
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
					}*/
					
					// 431万
					if(o1.getHLGap().compareTo(o2.getHLGap())==0){
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
					}
				
					//409万
					//return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
			}
		});

		Muster m;
		for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(!m.isUpLimited() && !m.isDownLimited() && m.isUpBreaker()) {
				breakers.add(m);
			}
			if(breakers.size()>=top) {
				break;
			}
		}
		
		return breakers;
	}


}
