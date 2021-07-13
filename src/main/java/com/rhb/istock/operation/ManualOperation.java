package com.rhb.istock.operation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;


/*
 * 手动买入卖出操作
 * 
 * 买入：根据传入的buyList清单买入
 * 卖出：根据传入的sellList清单买入
 */
@Scope("prototype")
@Service("manualOperation")
public class ManualOperation implements Operation {
	protected static final Logger logger = LoggerFactory.getLogger(ManualOperation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	private StringBuffer dailyHolds_sb;
	private StringBuffer dailyAmount_sb;
	private StringBuffer breakers_sb;
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,Map<LocalDate, List<String>> sellList,LocalDate beginDate, LocalDate endDate, String label, int top, boolean isAveValue, Integer quantityType) {
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		//logger.info(buyList.toString());
		
		dailyHolds_sb = new StringBuffer("date,itemID,itemName,open,close,quantity,profit,days\n");
		dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
		breakers_sb = new StringBuffer();
		
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++," " + label +  " manualOperation run:" + date.toString());
			this.doIt(date, account, buyList.get(date), sellList.get(date), top, isAveValue,quantityType);
		}
		return this.result(account);
	}
	
	private void doIt(LocalDate date,Account account, List<String> buyList, List<String> sellList, int top, boolean isAveValue, Integer quantityType) {
		//logger.info(date.toString());

		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters==null || musters.size()==0) return;
		
		Muster muster;
		account.setLatestDate(date);
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		dailyHolds_sb.append(account.getHoldStateString());
		
		//卖出
		if(sellList!=null) {
			for(String itemID: sellList) {
				muster = musters.get(itemID);
				if(muster!=null) {
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}
			}
		}
		
		//买入清单
		Set<Muster> dds = new HashSet<Muster>();  
		if(buyList!=null) {
			for(String id : buyList) {
				muster = musters.get(id); 
				if(muster!=null) {
					dds.add(muster);
				}
			}
		}
		
		breakers_sb.append(date.toString() + ",");
		for(Muster m : dds) {
			breakers_sb.append(m.getItemID());
			breakers_sb.append(",");
		}				
		breakers_sb.deleteCharAt(breakers_sb.length()-1);
		breakers_sb.append("\n");

		if(isAveValue && dds.size()>0 && account.isAve(dds.size())) {
			Set<Integer> holdOrderIDs;
			for(String itemID: holdItemIDs) {
				holdOrderIDs = 	account.getHoldOrderIDs(itemID);
				muster = musters.get(itemID);
				if(muster!=null && !muster.isUpLimited() && !muster.isDownLimited()) {
					for(Integer holdOrderID : holdOrderIDs) {
						account.dropByOrderID(holdOrderID, "0", muster.getLatestPrice());   //先卖
						dds.add(muster);						
					}
				}
			}					
		}

		//logger.info("dds after ave " + dds.size());

		if(quantityType==0) {
			account.openAll(dds);			//后买
		}else if(quantityType==1) {
			account.openAllWithFixAmount(dds);
		}else {
			account.openAllWithFixQuantity(dds);
		}
			
		dailyAmount_sb.append(account.getDailyAmount() + "\n");
	}
	
	private Map<String,String> result(Account account) {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", account.getCSV());
		result.put("initCash", account.getInitCash().toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
		result.put("dailyAmount", dailyAmount_sb.toString());
		result.put("breakers", breakers_sb.toString());
		result.put("lostIndustrys", account.getLostIndustrys());
		result.put("winIndustrys", account.getWinIndustrys());
		result.put("dailyHolds", dailyHolds_sb.toString());
		return result;
	}
}
