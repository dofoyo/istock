package com.rhb.istock.trade.a3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.fina.FinaService;

public class A3 {
	KdataService kdataService;
	FinaService finaService;
	IndexServiceTushare indexServiceTushare;
	LocalDate bDate, eDate;

	public void setConfig(
			KdataService kdataService,
			FinaService finaService,
			IndexServiceTushare indexServiceTushare,
			LocalDate bDate, LocalDate eDate
			) {
		this.kdataService = kdataService;
		this.finaService = finaService;
		this.indexServiceTushare = indexServiceTushare;
		this.bDate = bDate;
		this.eDate = eDate;
		
	}
	
	Map<String,Muster> musters, tmps;
	Muster muster;
	List<Map<String,Muster>> previous = new ArrayList<Map<String,Muster>>();
	Integer previous_period  = 13; //历史纪录区间，主要用于后面判断
	List<String> recommendations = null;
	List<Muster> ls;
	Integer pool = 21;
	
	Map<String,Muster> b21s = new HashMap<String,Muster>();
	Map<String,Muster> tigers = new HashMap<String,Muster>();
	Map<String,Muster> bnews = new HashMap<String,Muster>();
	
	public Map<LocalDate, Map<String,List<String>>> generateOperations() {
		Map<LocalDate, Map<String,List<String>>> results = new TreeMap<LocalDate,Map<String,List<String>>>();
		
		Map<String,List<String>> ms;
		List<String> buys, sells;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date=bDate; date.isBefore(eDate) || date.equals(eDate); date = date.plusDays(1)) {
			buys = new ArrayList<String>();
			sells = new ArrayList<String>();
			
			musters = kdataService.getMusters(date);
			Progress.show((int)days, i++, "  generateOperations: " + date.toString() + ", musters.size()=" + musters.size() + " \n");
			
			if(musters!=null && musters.size()>0) {
				//卖出操作
				Iterator<Map.Entry<String,Muster>> it = b21s.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Muster> entry = it.next();
					muster = musters.get(entry.getKey());
					if(muster!=null && muster.isDropAve(21)) {
						it.remove();
						tigers.remove(muster.getItemID());
						bnews.remove(muster.getItemID());
						sells.add(entry.getKey());
					}
				}
				
				//买入操作
				previous.add(musters);
				if(previous.size()>previous_period) {
					previous.remove(0);
				}
				
				recommendations = finaService.getHighRecommendations(date, 10000, 13); //推荐买入的顺序是从大到小
				ls = new ArrayList<Muster>();
				for(String id : recommendations) {
					muster = musters.get(id);
					if(muster!=null) {
						ls.add(muster);
					}
				}
				
				Collections.sort(ls, new Comparator<Muster>(){
					@Override
					public int compare(Muster o1, Muster o2) {
						return o2.getLatestPrice().compareTo(o1.getLatestPrice());
					}
					
				});
				
				for(int j=0; j<ls.size() && j<pool; j++) {
					muster = ls.get(j);
					if(muster!=null) {
						if(this.addB21(muster)) {
							buys.add(muster.getItemID());
						}
						
						if(this.addTiger(muster, previous, date)) {
							buys.add(muster.getItemID());
						}
						
						if(this.addBnew(muster)) {
							buys.add(muster.getItemID());
						}
					}
				}
			}
			
			if(sells.size()>0 || buys.size()>0) {
				ms = new HashMap<String,List<String>>();
				ms.put("sells", sells);
				ms.put("buys", buys);
				
				results.put(date, ms);
			}
		}
		return results;
	}
	
	public JSONObject generateOperations1() {
		JSONObject results = new JSONObject(true);
		
		JSONObject ms;
		JSONArray buys, sells;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date=bDate; date.isBefore(eDate) || date.equals(eDate); date = date.plusDays(1)) {
			buys = new JSONArray();
			sells = new JSONArray();
			
			musters = kdataService.getMusters(date);
			Progress.show((int)days, i++, "  generateOperations: " + date.toString() + ", musters.size()=" + musters.size() + " \n");
			
			if(musters!=null && musters.size()>0) {
				//卖出操作
				Iterator<Map.Entry<String,Muster>> it = b21s.entrySet().iterator();
				while(it.hasNext()) {
					Map.Entry<String, Muster> entry = it.next();
					muster = musters.get(entry.getKey());
					if(muster!=null && muster.isDropAve(21)) {
						it.remove();
						tigers.remove(muster.getItemID());
						bnews.remove(muster.getItemID());
						sells.put(entry.getKey());
					}
				}
				
				//买入操作
				previous.add(musters);
				if(previous.size()>previous_period) {
					previous.remove(0);
				}
				
				recommendations = finaService.getHighRecommendations(date, 10000, 13); //推荐买入的顺序是从大到小
				ls = new ArrayList<Muster>();
				for(String id : recommendations) {
					muster = musters.get(id);
					if(muster!=null) {
						ls.add(muster);
					}
				}
				
				Collections.sort(ls, new Comparator<Muster>(){
					@Override
					public int compare(Muster o1, Muster o2) {
						return o2.getLatestPrice().compareTo(o1.getLatestPrice());
					}
					
				});
				
				for(int j=0; j<ls.size() && j<pool; j++) {
					muster = ls.get(j);
					if(muster!=null) {
						if(this.addB21(muster)) {
							buys.put(muster.getItemID());
						}
						
						if(this.addTiger(muster, previous, date)) {
							buys.put(muster.getItemID());
						}
						
						if(this.addBnew(muster)) {
							buys.put(muster.getItemID());
						}
					}
				}
			}
			
			if(sells.length()>0 || buys.length()>0) {
				ms = new JSONObject();
				ms.put("sells", sells);
				ms.put("buys", buys);
				
				results.put(date.toString(), ms);
			}
		}
		return results;
	}
	
	public boolean addB21(Muster muster) {
		boolean flag = false;
		if(muster.isJustBreaker() && muster.getHLGap()<=55 && !b21s.containsKey(muster.getItemID())) {
			b21s.put(muster.getItemID(), muster);
			//System.out.println("b21: " + muster.getItemID() + "," + muster.getItemName());  //buy1
			flag = true;
		}
		return flag;
	}

	public boolean addTiger(Muster muster, List<Map<String,Muster>> previous, LocalDate date) {
		boolean flag = false;
		Muster pmuster = previous.get(0).get(muster.getItemID());
		Integer[] sseiRatio = indexServiceTushare.getSseiGrowthRate(date, 21);
		if(pmuster!=null && b21s.containsKey(muster.getItemID()) && !tigers.containsKey(muster.getItemID())) {
			Integer[] ratio = this.getRatio(previous,muster.getItemID(),muster.getLatestPrice());
			if(ratio[0] > 0
					&& (ratio[0]>=sseiRatio[0] || ratio[1]>=sseiRatio[1])   // 强于大盘
				&& muster.getHLGap()<=55
				&& muster.isUpAve(21)
				&& muster.getAveragePrice21().compareTo(pmuster.getAveragePrice21())==1  //上升趋势
				) {
				tigers.put(muster.getItemID(), muster);
				//System.out.println("tiger: " + muster.getItemID() + "," + muster.getItemName());  //buy2
				flag = true;
			}
		}
		return flag;
	}
	
	public boolean addBnew(Muster muster) {
		boolean flag = false;
		if(muster.isUpBreaker() 
				&& muster.getHLGap()<=55 
				&& b21s.containsKey(muster.getItemID()) 
				&& tigers.containsKey(muster.getItemID()) 
				&& !bnews.containsKey(muster.getItemID())) {
			bnews.put(muster.getItemID(), muster);
			//System.out.println("bnew: " + muster.getItemID() + "," + muster.getItemName());  // buy3
			flag = true;
		}
		return flag;
	}
	
	private Integer[] getRatio(List<Map<String,Muster>> musters, String itemID, BigDecimal price) {
		Integer[] ratio = new Integer[] {0,0};
		BigDecimal lowest=null, begin=null;
		Muster m;
		for(Map<String,Muster> ms : musters) {
			m = ms.get(itemID);
			if(m!=null) {
				lowest = (lowest==null || lowest.compareTo(m.getLatestPrice())==1) ? m.getLatestPrice() : lowest;
				//logger.info(String.format("%s, date=%s, price=%.2f", itemID, m.getDate().toString(), m.getLatestPrice()));
				if(begin==null) {
					begin = m.getLatestPrice();
				}
			}
		}
		
		if(lowest!=null && begin!=null && lowest.compareTo(BigDecimal.ZERO)>0) {
			ratio[0] = Functions.growthRate(price, begin);
			ratio[1] = Functions.growthRate(price, lowest);
		}
		
		//logger.info(String.format("%s, lowest=%.2f, price=%.2f, ratio=%d", itemID, lowest, price,ratio));

		return ratio;
	}
}
