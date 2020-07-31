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

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Muster;
/*
 * 强力突破21日线
 * 
 * 操作策略
 * 筛选：全部股票,按涨幅和价格排序,选出1个。
 * 买入：突破21日均线，同时涨幅5%以上
 * 卖出：跌破21日均线
 * 仓位：满仓，每只股票的均衡市值
 *
 */
public class B21plus {
	protected static final Logger logger = LoggerFactory.getLogger(HLB_try.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	//private Integer pool = 21;
	private Integer top = 2;
	//private Integer type = 1;  // 1 - 高价， 0 - 低价

	public B21plus(BigDecimal initCash, Integer type) {
		account = new Account(initCash);
		this.initCash = initCash;
		//this.type = type;
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

		Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓

		//行情好，才选股，准备买入
		if(sseiFlag==1) {
		
			//确定突破走势的股票
			List<Muster> breakers = this.getBreakers(musters, previous, sseiRatio,holdItemIDs);
			
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
		}

		
		//卖出股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			pre = previous.get(0).get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
/*				//跌破21日均线就卖
				if(muster.isDropAve(21)) { 		
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}
				
				//走势弱于大盘
				ratio = this.getRatio(previous, itemID, muster.getLatestPrice());
				if(ratio < sseiRatio) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}*/
				
				//最高点跌5%就卖
				if(account.getFallRate(itemID)<-5
						//&& !account.isLost(itemID)
						) {
					account.dropWithTax(itemID, "3", muster.getLatestPrice());
				}
				
			}
		}

		//先卖后买，完成调仓和开仓
		//此举可避免高位加仓的现象出现
		if(!dds.isEmpty()) {
/*			Set<Integer> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null) {
					for(Integer holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "0", muster.getLatestPrice());   //先卖
						dds.add(muster);						
					}
				}
			}*/
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
	
	private List<Muster> getBreakers(Map<String,Muster> ms,List<Map<String,Muster>> previous, Integer sseiRatio, Set<String> holds){
		List<Muster> breakers = new ArrayList<Muster>();
		
		List<Muster> musters = new ArrayList<Muster>(ms.values());
		
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o2.getMaxRate().equals(o1.getMaxRate())) {
					return o1.getHLGap().compareTo(o2.getHLGap()); //价格波动幅度小到大排序
				}else {
					return o2.getMaxRate().compareTo(o1.getMaxRate());
				}
			}
		});
		
		
		Muster m,p;
		for(int i=0; i<musters.size() && breakers.size()<this.top; i++) {
			m = musters.get(i);
			//p = getPreviousMuster(previous, m.getItemID());
			if(m!=null 
					//&& p!=null 
					&& !m.isUpLimited() 
					&& m.isJustBreaker() 		//突破21
					&& m.isUpThan(5)        //大幅
					//&& m.getHLGap()<=34 		//股价还没飞涨
					&& m.isAboveAverageAmount()  //放量
					) {
				
					breakers.add(m);
			}
		}
		
		return breakers;
	}
	
	private boolean isAbsolutlyDownAve(List<Map<String,Muster>> previous, String id) {
		boolean flag = false;
		Muster m = null;
		for(int i=previous.size()-1; i>=0; i--) {
			m = previous.get(i).get(id);
			if(m!=null && m.isAbsolutlyDownAve(21)) {
				flag = true;
				break;
			}
		}
		
		return flag;
	}
	
	private Muster getPreviousMuster(List<Map<String,Muster>> previous, String id) {
		Muster m=null;
		for(Map<String,Muster> ms : previous) {
			m = ms.get(id);
			if(m!=null) {
				break;
			}
		}
		return m;
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
