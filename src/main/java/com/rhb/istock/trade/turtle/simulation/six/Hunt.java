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
 * 买入：21日趋势形成
 * 卖出：跌破21日均线
 * 筛选范围：全部股票选3个
 * 筛选依据：89天的高点和低点形成的通道越窄、价格越低
 * 仓位控制：每只股票不能超过市值的1/3
 */
public class Hunt {
	protected static final Logger logger = LoggerFactory.getLogger(Hunt.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer potentials_sb = new StringBuffer();

	private BigDecimal valueRatio = new BigDecimal(3);  //每只股票不能超过市值的1/3
	private Integer top = 3;

	public Hunt(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, LocalDate date) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		
		//卖出跌破dropline的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
				if(muster.isDrop(21) && !muster.isDownLimited()){
					account.drop(itemID, "跌破deadline", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
				
				if(muster.isDropLowest(21) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "跌破lowest", muster.getLatestPrice());
					account.dropHoldState(itemID);
				}	
			}
		}				

		//买入潜力股
		List<Muster> potentials = this.getHLs(new ArrayList<Muster>(musters.values()));
		potentials_sb.append(date.toString() + ",");
		holdItemIDs = account.getItemIDsOfHolds();
		for(Muster potential : potentials) {
			if(!holdItemIDs.contains(potential.getItemID()) && !potential.isUpLimited()) {
				account.refreshHoldsPrice(potential.getItemID(), potential.getLatestPrice());
				account.open(potential.getItemID(),potential.getItemName(), potential.getIndustry(), this.getQuantity(account.getCash(),account.getTotal(),potential.getLatestPrice()), "", potential.getLatestPrice());
			}
			potentials_sb.append(potential.getItemID());
			potentials_sb.append(",");
		}
		potentials_sb.deleteCharAt(potentials_sb.length()-1);
		potentials_sb.append("\n");
		
		dailyAmount_sb.append(account.getDailyAmount() + "\n");

		
/*		potentials_sb.append(date.toString() + ",");
		Set<Muster> dds = new HashSet<Muster>();
		holdItemIDs = account.getItemIDsOfHolds();
		for(Muster must : potentials) {
			if(!holdItemIDs.contains(must.getItemID())) {
				dds.add(must);
				potentials_sb.append(must.getItemID());
				potentials_sb.append(",");
			}
		}
		potentials_sb.deleteCharAt(potentials_sb.length()-1);
		potentials_sb.append("\n");
		
		//先卖后买，完成调仓和开仓
		//logger.info("先卖后买，完成调仓");
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
*/	}
	
	private Integer getQuantity(BigDecimal cash, BigDecimal total,BigDecimal price) {
		BigDecimal dd = total.divide(valueRatio,BigDecimal.ROUND_DOWN);
		BigDecimal ee = dd.compareTo(cash)<=0 ? dd : cash;
		return ee.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
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
		result.put("breakers", potentials_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}
	
	private List<Muster> getHLs(List<Muster> musters){
		List<Muster> hls = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
					return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}
				
/*				if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					if(o1.getLNGap().compareTo(o2.getLNGap())==0){  
						return o1.getLatestPrice().compareTo(o2.getLatestPrice()); //a-z
					}else {
						return o1.getLNGap().compareTo(o2.getLNGap()); //a-z
					}
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}
*/				

			}
		});

		Muster m;
		for(int i=0; i<musters.size(); i++) {
			m = musters.get(i);
			if(!m.isUpLimited() && !m.isDownLimited() && m.isUp()){
				hls.add(m);
			}
			if(hls.size()>=top) {
				break;
			}
		}
		
		return hls;
	}
	
}
