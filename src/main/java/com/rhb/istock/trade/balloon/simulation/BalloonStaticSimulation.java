package com.rhb.istock.trade.balloon.simulation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.balloon.domain.Balloon;
import com.rhb.istock.trade.turtle.simulation.DailyItem;

/*
 * 所谓static，即每天要交易的item是确定的，如上证50、每日交易量top50、日均交易量top50、等
 */
@Service("balloonStaticSimulation")
public class BalloonStaticSimulation implements BalloonSimulation{
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	boolean byCache = true;
	
	LocalDate beginDate = null;
	LocalDate endDate = null;

	Balloon balloon = null;
	
	@Override
	public Map<String, String> simulate(DailyItem dailyItem, Option option) {
		this.beginDate = dailyItem.getBeginDate();
		this.endDate = dailyItem.getEndDate();
		
		if(option==null) {
			balloon = new Balloon();			
		}else {
		}
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		
		Set<String> itemIDs;
		int i = 0;
		for(LocalDate date=beginDate; date.isBefore(endDate); date=date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			//balloon.clearDatas(); //开始前清除历史记录，当某个item停牌几天，原记录可能会缺失
			
			itemIDs = dailyItem.getItemIDs(date);
			if(itemIDs!=null) {
				itemIDs.addAll(balloon.getItemIDsOfHolds());//加入在手的ID
				for(String itemID : itemIDs) {
					if(balloon.noData(itemID)){
						setDailyKdata(itemID, date); //放入beginDate之前的历史记录
					}
					setLatestKdata(itemID, date); //放入当前记录
				}				
				System.out.println("");
				balloon.doIt();
			}
		}
		
		Map<String, String> result = balloon.result();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("CAGR: " + result.get("cagr"));
		System.out.println("winRatio: " + result.get("winRatio"));
		FileUtil.writeTextFile(reportPath + "/one_item_simulation_" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);
		return result;
	}

	private void setDailyKdata(String itemID, LocalDate theDate) {
		Kbar kbar;
		Kdata kdata = kdataService.getDailyKdata(itemID, theDate, balloon.getMidDuration(), byCache);
		
		List<LocalDate> dates = kdata.getDates();
		//System.out.println(dates);
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			//System.out.println("\n" + date + "," + kbar);
			balloon.addDailyData(itemID,date,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
	}
	
	private Kbar setLatestKdata(String itemID, LocalDate theDate) {
		Kbar kbar = kdataService.getKbar(itemID, theDate, byCache);
		if(kbar!=null) {
			balloon.addDailyData(itemID,theDate ,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
		return kbar;
	}
}