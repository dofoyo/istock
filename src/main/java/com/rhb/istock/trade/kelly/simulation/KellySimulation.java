package com.rhb.istock.trade.kelly.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.kelly.domain.Kelly;

/*
 * 
 */
@Service("kellySimulation")
public class KellySimulation{
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	boolean cache = true;
	
	Kelly kelly = null;
	
	public Map<String, String> simulate(TreeMap<LocalDate,List<String>> dailyItems, Keoption option, boolean cache) {
		if(dailyItems==null || dailyItems.size()==0) return null;
		
		this.cache = cache;
		
		if(option==null) {
			kelly = new Kelly();			
		}else {
			kelly = new Kelly(option.getDeficitFactor(),
					option.getOpenDuration(),
					option.getDropDuration(),
					option.getMaxOfLot(),
					option.getInitCash(),
					option.getStopStrategy(),
					option.getCancels(),
					option.getGap());
		}
		
		long days = dailyItems.lastKey().toEpochDay()- dailyItems.firstKey().toEpochDay();
		
		List<String> newIDs;
		Set<String> holdIDs;
		int i = 0;
		for(Map.Entry<LocalDate, List<String>> entry : dailyItems.entrySet()) {
			Progress.show((int)days, i++, entry.getKey().toString());
			
			kelly.clearDatas(); //开始前清除历史记录，当某个item停牌几天，原记录可能会缺失
			
			newIDs = entry.getValue();
			if(newIDs!=null && !newIDs.isEmpty()) {
				//1、处理在手
				holdIDs = kelly.getItemIDsOfHolds();
				if(holdIDs!=null && !holdIDs.isEmpty()) {
					for(String itemID : holdIDs) {
						setDailyKdata(itemID, entry.getKey()); //放入beginDate之前的历史记录
						setLatestKdata(itemID, entry.getKey()); //放入当前记录
					}			
					kelly.doStopOrDrop(holdIDs);				
				}
				
				//2、新开仓
				for(String itemID : newIDs) {
					if(!holdIDs.contains(itemID)) {  //之前已放入了数据，就不用再放入了
						setDailyKdata(itemID, entry.getKey()); //放入beginDate之前的历史记录
						setLatestKdata(itemID, entry.getKey()); //放入当前记录						
					}
				}
				kelly.doOpenOrReopen(newIDs); 
				
				
				//3、生成日报
				kelly.dailyReport();
				
			}
		}
		
		Map<String, String> result = kelly.result();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("CAGR: " + result.get("cagr"));
		System.out.println("winRatio: " + result.get("winRatio"));
		FileTools.writeTextFile(reportPath + "/kelly_simulation_detail" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);
		FileTools.writeTextFile(reportPath + "/kelly_simulation_dailyAmount" + System.currentTimeMillis() + ".csv", result.get("dailyAmount"), false);
		FileTools.writeTextFile(reportPath + "/kelly_simulation_breakers" + System.currentTimeMillis() + ".csv", result.get("breakers"), false);
		return result;
	}

	private void setDailyKdata(String itemID, LocalDate theDate) {
		Kbar kbar;
		Kdata kdata = kdataService.getKdata(itemID, theDate, kelly.getOpenDuration(), cache);
		
		List<LocalDate> dates = kdata.getDates();
		//System.out.println(dates);
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			//System.out.println(date + "," + kbar);
			kelly.addDailyData(itemID,date,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
	}
	
	private Kbar setLatestKdata(String itemID, LocalDate theDate) {
		Kbar kbar = kdataService.getKbar(itemID, theDate, cache);
		if(kbar!=null) {
			kelly.addLatestData(itemID,theDate ,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
		return kbar;
	}

}
