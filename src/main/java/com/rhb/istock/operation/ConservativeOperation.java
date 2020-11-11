package com.rhb.istock.operation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;


/*
 * 保守型操作模式　conservative
 * 
 * 买入：满仓　＋　市值平均　＋　单只股票不加仓  + 行情好（大盘在21日均线上方）
 * 卖出：跌破21日线  + 大盘走坏  + 回落超过8%
 */
@Scope("prototype")
@Service("conservativeOperation")
public class ConservativeOperation implements Operation{
	protected static final Logger logger = LoggerFactory.getLogger(ConservativeOperation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;
	
	private StringBuffer dailyAmount_sb = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers_sb = new StringBuffer();
	private Integer top = 3;
	Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
	
	public Map<String,String> run(Account account, Map<LocalDate, List<String>> buyList,LocalDate beginDate, LocalDate endDate, String label) {
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {

			Progress.show((int)days, i++, " " + label +  " run:" + date.toString());
			
			this.doIt(date, account, buyList);
		}
		
		return this.result(account);

	}
	
	public void doIt(LocalDate date,Account account, Map<LocalDate, List<String>> buyList) {
		Map<String,Muster> musters = kdataService.getMusters(date);
		if(musters==null || musters.size()==0) return;

		Integer sseiFlag = kdataService.getSseiFlag(date);
		//Integer sseiRatio = indexServiceTushare.getSseiGrowthRate(date, 21);
		Integer sseiTrend = kdataService.getSseiTrend(date, previous_period);
		
		Muster muster;
		account.setLatestDate(date);
		
		Set<String> holdItemIDs = account.getItemIDsOfHolds();
		for(String itemID : holdItemIDs) {
			muster = musters.get(itemID);
			if(muster != null) {
				account.refreshHoldsPrice(itemID, muster.getLatestPrice(), muster.getLatestHighest());
			}
		}
		
		//卖出
		for(String itemID: holdItemIDs) {
			muster = musters.get(itemID);
			if(muster!=null && !muster.isDownLimited()) {
				//跌破21日均线就卖
				if(muster.isDropAve(21)) { 		
					account.dropWithTax(itemID, "1", muster.getLatestPrice());
				}
				
				//大盘下降通道走坏,所持股跟随下跌
				if(sseiFlag==0 && sseiTrend<0 && muster.getClose().compareTo(muster.getLatestPrice())>0) {
					account.dropWithTax(itemID, "3", muster.getLatestPrice());
				}
				
				//高位快速回落超过8%
				account.dropFallOrder(itemID, -8,"2");
			}
		}
		
		//买入
		//if(sseiFlag==1) {  //行情好，才买入
			holdItemIDs = account.getItemIDsOfHolds();
			
			Set<Muster> dds = new HashSet<Muster>();  //用set，无重复，表示不可加仓
			//List<Muster> dds = new ArrayList<Muster>();  //用list，有重复，表示可以加仓
			
			//选择股票
			List<String> ids = buyList.get(date);
			if(ids!=null && ids.size()>0) {
				breakers_sb.append(date.toString() + ",");
				String id;
				for(int i=0, j=0; i<ids.size() && j<top; i++) {
					id = ids.get(i);
					if(!holdItemIDs.contains(id)) {
						muster = musters.get(id); 
						if(muster!=null && !muster.isUpLimited()) {
							dds.add(muster);
							j++;
							breakers_sb.append(id);
							breakers_sb.append(",");
						}
					}
				}
				breakers_sb.deleteCharAt(breakers_sb.length()-1);
				breakers_sb.append("\n");
			}
			
			//市值平均
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
	//	}

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
		return result;
	}
	
}
