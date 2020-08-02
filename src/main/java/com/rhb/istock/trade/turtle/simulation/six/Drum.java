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
public class Drum {
	protected static final Logger logger = LoggerFactory.getLogger(Drum.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	private BigDecimal valueRatio = new BigDecimal(3);  //每只股票不能超过市值的1/3
	private Integer pool = 21;
	private Integer top = 1;
	private Integer type = 1;// 1 - 高价， 0 - 低价

	public Drum(BigDecimal initCash, Integer type) {
		account = new Account(initCash);
		this.initCash = initCash;
		this.type = type;
	}
	
	public void doIt(Map<String,Muster> musters,List<Map<String,Muster>> previous, LocalDate date, Integer sseiFlag, Integer sseiRatio) {
		Muster muster;
		account.setLatestDate(date);
		Integer ratio;
		
		//卖出
		boolean droped = false;
		Set<String> holdIDs = account.getItemIDsOfHolds();
		for(String itemID: holdIDs) {
			muster = musters.get(itemID);
			//pre = previous.get(0).get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());

				//跌破21日均线
				if(muster.isDropAve(21) 
						//&& muster.getAverageAmount5().compareTo(muster.getAverageAmount())==-1
						) { 		
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
					droped = true;
				}
/*
				//走势弱于大盘
				ratio = this.getRatio(previous, itemID, muster.getLatestPrice());
				if(ratio < sseiRatio) {
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
					droped = true;
				}*/
				
				//高位快速回落超过13%
				//account.dropFallOrder(itemID, -13,"3");
			}
		}
		
		//行情好，才买入
		//if(sseiFlag==1) {
			//确定突破走势的股票
			Set<String> holdItemIDs = account.getItemIDsOfHolds();
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			List<Muster> breakers = this.getBreakers(musters,previous,date,sseiRatio,holdItemIDs);
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
			//logger.info("先卖后买，完成调仓");
			if(!dds.isEmpty() || droped) {
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
	
	private Integer getQuantity(BigDecimal cash, BigDecimal total,BigDecimal price) {
		BigDecimal dd = total.divide(valueRatio,BigDecimal.ROUND_DOWN);
		BigDecimal ee = dd.compareTo(cash)<=0 ? dd : cash;
		return ee.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
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
		for(int i=0; i<ms.size() && breakers.size()<this.top && i<this.pool; i++) {
			m = ms.get(i);
			p = previous.get(0).get(m.getItemID());
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
