package com.rhb.istock.trade.balloon.operation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.SelectorService;
import com.rhb.istock.trade.balloon.domain.Balloon;
import com.rhb.istock.trade.balloon.domain.Bfeature;
import com.rhb.istock.trade.balloon.operation.api.BluechipView;

@Service("balloonOperationServiceImp")
public class BalloonOperationServiceImp implements BalloonOperationService {
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
	Balloon balloon = null;
	
	@Override
	public List<BluechipView> getBluechips() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("getting bluechips ......");
		
		List<BluechipView> views = new ArrayList<BluechipView>();
		BluechipView view;
		Item item;
		Bfeature feature;
		
		List<String> itemIDs = selectorService.getBluechipIDs(LocalDate.now());
		System.out.println(itemIDs.size());
		for(String itemID : itemIDs) {
			itemID = itemID.replaceAll("\r|\n", "");
			
			setLatestKdata(itemID);
			item = itemService.getItem(itemID);
			feature = balloon.getFeature(itemID);
			if(item != null) {
				view = new BluechipView();
				view.setItemID(itemID);
				view.setCode(item.getCode());
				view.setName(item.getName());
				view.setUps(feature.getUps());
				view.setBiasOfBaseLine(feature.getBiasBaseLine());
				view.setBiasOfGolden(feature.getBiasOfGolden());
				view.setSlips(feature.getSlips());
				view.setStatus(feature.getStatus());
				
				views.add(view);				
			}
		}
		
		
		System.out.println("getting bluechips done. there are " + views.size() + " bluechips.");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		return views;
	}

	
	private void setDailyKdata(String itemID) {
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		boolean byCache = true;  
		
		Kbar kbar;
		Kdata kdata = kdataService.getKdata(itemID, endDate, balloon.getMidDuration(), byCache);
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			balloon.addDailyData(itemID,date,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
	}
	
	private boolean setLatestKdata(String itemID) {
		if(balloon==null) {
			balloon = new Balloon();
		}
		
		if(balloon.noData(itemID)) {
			setDailyKdata(itemID);
		}
		
		boolean flag = false;
		
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		
		Kbar kbar = kdataService.getLatestMarketData(itemID);
		if(kbar!=null) {
			balloon.addDailyData(itemID, endDate,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
			flag = true;
		}
		
		return flag;
	}
	
}
