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
import com.rhb.istock.fund.Account2;
import com.rhb.istock.kdata.Muster;


/*
 * 强于大盘
 * 
 * 
 * 买入:
 * 1. 大盘上升趋势形成
 * 2. 近期走势强于大盘的最低价股
 * 
 * 卖出:
 * 跌破21日均线
 * 
 *
 */
public class Drum2 {
	protected static final Logger logger = LoggerFactory.getLogger(Drum.class);

	private Account2 account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	private BigDecimal valueRatio = new BigDecimal(3);  //每只股票不能超过市值的1/3
	//private Integer pool = 55;
	private Integer top = 1;
	private Integer type = 1;// 1 - 高价， 0 - 低价

	public Drum2(BigDecimal initCash, Integer type) {
		account = new Account2(initCash);
		this.initCash = initCash;
		this.type = type;
	}
	
	public void doIt(Map<String,Muster> musters,List<Map<String,Muster>> previous, LocalDate date, Integer sseiFlag, Integer sseiRatio) {
		Muster muster;
		account.setLatestDate(date);
		Integer ratio;
		
		//卖出
		Set<String> holdIDs = account.getItemIDsOfHolds();
		for(String itemID: holdIDs) {
			muster = musters.get(itemID);
			//pre = previous.get(0).get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());

				//跌破21日均线
				if(muster.isDropAve(21) 
						//&& muster.getAverageAmount5().compareTo(muster.getAverageAmount())==-1
						) { 		
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}

				//走势弱于大盘
				ratio = this.getRatio(previous, itemID, muster.getLatestPrice());
				if(ratio < sseiRatio) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}
			}
		}
		holdIDs = account.getItemIDsOfHolds();
		
		//确定待买入的股票
		Set<Muster> dds = new HashSet<Muster>();
		//if(sseiFlag==1) {  //行情好，才选股
			List<Muster> breakers = this.getBreakers(musters,previous,date,sseiRatio,holdIDs);
			breakers_sb.append(date.toString() + ",");
			StringBuffer sb = new StringBuffer();
			for(Muster breaker : breakers) {
				if(!holdIDs.contains(breaker.getItemID())) {  //已持有，就不开新仓
					dds.add(breaker);
					sb.append(breaker.getItemName());
					sb.append(",");
				}
				breakers_sb.append(breaker.getItemID());
				breakers_sb.append(",");
			}
			breakers_sb.deleteCharAt(breakers_sb.length()-1);
			breakers_sb.append("\n");
		//}

		//调仓，建仓
		if(!dds.isEmpty()) {
			holdIDs = account.getItemIDsOfHolds();
			BigDecimal total = account.getTotal();
			BigDecimal ave = total.divide(new BigDecimal(holdIDs.size() + dds.size()), BigDecimal.ROUND_HALF_DOWN);
			
			BigDecimal ave_cash = account.getCash().divide(new BigDecimal(dds.size()),BigDecimal.ROUND_HALF_DOWN);
			if(ave_cash.compareTo(ave)==-1) {
				account.dropToAve(ave);   //调仓
			}
			account.open(dds);  //建仓
		}
		
		//加仓
		
		
		
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
		//result.put("lostIndustrys", account.getLostIndustrys());
		//result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}
	
	private List<Muster> getBreakers(Map<String,Muster> musters,List<Map<String,Muster>> previous, LocalDate date, Integer sseiRatio, Set<String> holds){
		List<Muster>  ms = new ArrayList<Muster>(musters.values());

		if(this.type == 0) {
			Collections.sort(ms, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //价格小到大排序
				}
			});
		}else {
			Collections.sort(ms, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o2.getLatestPrice().compareTo(o1.getLatestPrice()); //价格大到小排序
				}
			});
		}
		
		List<Muster>  breakers = new ArrayList<Muster>();
		Muster m,p;
		Integer ratio, r;
		StringBuffer sb = new StringBuffer(date.toString() + ":");
		for(int i=0; i<ms.size() && breakers.size()<this.top; i++) {
			m = ms.get(i);
			p = getPreviousMuster(previous, m.getItemID());
			if(m!=null && p!=null) {
				ratio = Functions.growthRate(m.getAveragePrice21(), m.getAveragePrice());
				r = this.getRatio(previous,m.getItemID(),m.getLatestPrice());
				if(!m.isUpLimited() && !m.isDownLimited()
					&& r>0
					&& r >= sseiRatio   // 强于大盘
					&& m.getHLGap()<=55
					&& m.isUpAve(21)
					&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1  //上升趋势
					&& !holds.contains(m.getItemID())
					) {
				breakers.add(m);
				sb.append(m.getItemID() + "(" + i + ")" +",");
				}
			}
		}
		
		//logger.info(date.toString());
		
		return breakers;
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
				//logger.info(String.format("%s, date=%s, price=%.2f", itemID, m.getDate().toString(), m.getLatestPrice()));
			}
			
		}
		
		if(lowest!=null && lowest.compareTo(BigDecimal.ZERO)>0) {
			ratio = Functions.growthRate(price, lowest);
		}
		
		//logger.info(String.format("%s, lowest=%.2f, price=%.2f, ratio=%d", itemID, lowest, price,ratio));

		return ratio;
	}
	
}
