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
 * 操作策略
 *
 * 买入：突破21日均线
 * 卖出：跌破21日均线
 * 筛选范围：突破21日均线,
 * 筛选依据：当日成交量，越高越好
 * 仓位控制：满仓，每只股票的均衡市值
 *
 */
public class BAV3 {
	protected static final Logger logger = LoggerFactory.getLogger(BAV3.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	
	private BigDecimal valueRatio = new BigDecimal(3);  //每只股票不能超过市值的1/3
	private Integer pool = 55;
	private Integer top = 1;

	public BAV3(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters,Map<String,Muster> previous, LocalDate date, Integer sseiFlag) {
		Muster muster;
		account.setLatestDate(date);

		if(sseiFlag==1) {
			this.top = 8;
		}else {
			this.top = 1;
		}

		
		//卖出
		Set<String> holdIDs = account.getItemIDsOfHolds();
		for(String itemID: holdIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
				
				//跌破21日均线
				if(muster.isDropAve(21) && !muster.isDownLimited()) { 		
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}
				
				/*//涨幅超过21%，则跌破8日线
				if(account.getUpRatio(itemID)>=21 && muster.isDropAve(8) && !muster.isDownLimited()) {
					account.dropWithTax(itemID, "up "+account.getUpRatio(itemID).toString()+" and drop_ave8", muster.getLatestPrice());
				}*/
				
				/*if(sseiFlag==0 && muster.isDropLowest(21) && !muster.isDownLimited()) { 		//跌破21日均线就卖
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}				
				if(sseiFlag==1 && muster.isDropLowest(34) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.dropWithTax(itemID, "2", muster.getLatestPrice());
				}*/	
			}
		}
		
		//行情好，才买入
		//if(sseiFlag==1) {
			//确定突破走势的股票
			Set<String> holdItemIDs = account.getItemIDsOfHolds();
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			
			List<Muster> breakers = this.getBreakers(musters,previous,1);
			if(!breakers.isEmpty()) {
				breakers = this.getBreakers(musters, previous, 2);
			}
			
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
	
	private List<Muster> getBreakers(Map<String,Muster> musters, Map<String,Muster> previous, Integer type){
		List<Muster>  ms = new ArrayList<Muster>(musters.values());
		
		if(type==1) {
			Collections.sort(ms, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
				}
			});
		}else if(type==2) {
			Collections.sort(ms, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					return o2.getLatestAmount().compareTo(o1.getLatestAmount()); //Z-A
				}
			});
		}
		
		List<Muster>  breakers = new ArrayList<Muster>();
		Muster m,p;
		Integer ratio=8;
		BigDecimal previousAverageAmount;
		for(int i=0; i<ms.size() && i<pool && breakers.size()<top; i++) {
			m = ms.get(i);
			p = previous.get(m.getItemID());
			if(p==null) {
				previousAverageAmount = BigDecimal.ZERO;
			}else {
				previousAverageAmount = p.getAverageAmount();
			}
			if(m!=null 
					&& m.getPe().compareTo(BigDecimal.ZERO)>0 && m.getPe().compareTo(new BigDecimal(233))<0
					&& !m.isUpLimited() 
					&& !m.isDownLimited() 
					&& m.isJustBreaker()
					&& m.getAverageAmount().compareTo(previousAverageAmount)==1
					) {
				breakers.add(m);
			}
		}
		
		return breakers;
	}
}
