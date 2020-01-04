package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
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
public class Hua {
	protected static final Logger logger = LoggerFactory.getLogger(Hua.class);

	private Account account = null;
	private BigDecimal initCash = null;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer potentials_sb = new StringBuffer();

	private BigDecimal valueRatio = new BigDecimal(3);  //每只股票不能超过市值的1/3

	public Hua(BigDecimal initCash) {
		account = new Account(initCash);
		this.initCash = initCash;
	}
	
	public void doIt(Map<String,Muster> musters, Set<String> ids, LocalDate date, Integer sseiFlag) {
		//logger.info(date.toString());
		Muster muster;
		account.setLatestDate(date);

		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		
		//卖出跌破dropline的股票
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice());
				if(muster.isDropLowest(8) && !muster.isDownLimited()){
					account.drop(itemID, "1", muster.getLatestPrice()); 
					account.dropHoldState(itemID);
				}
				
/*				if(muster.isDropLowest(8) && !muster.isDownLimited()) { 		//跌破*日低点就卖
					account.drop(itemID, "跌破lowest", muster.getLatestPrice());
					account.dropHoldState(itemID);
				}	*/
				
/*				if(sseiFlag==0 && muster.isDropAve(13) && !muster.isDownLimited()) { 		//跌破13日均线就卖
					account.drop(itemID, "1", muster.getLatestPrice());
				}				
				if(sseiFlag==1 && muster.isDropLowest(21) && !muster.isDownLimited()) { 		//跌破21日低点就卖
					account.drop(itemID, "2", muster.getLatestPrice());
				}*/
			}
		}				

		//买入
		//if(sseiFlag==1) {
			List<Muster> potentials = this.getHuaItems(musters, ids);
			
			potentials_sb.append(date.toString() + ",");
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
		result.put("breakers", potentials_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		return result;
	}
	
	private List<Muster> getHuaItems(Map<String,Muster> musters,Set<String> ids){
		List<Muster>  ms = new ArrayList<Muster>();
		for(String id : ids) {
			ms.add(musters.get(id));
		}
		return ms;
	}

}
