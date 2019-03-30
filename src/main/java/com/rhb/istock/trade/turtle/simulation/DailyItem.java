package com.rhb.istock.trade.turtle.simulation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DailyItem {
	private LocalDate beginDate=null, endDate=null;
	private Map<LocalDate, List<String>> itemIDs = new HashMap<LocalDate,List<String>>();
	
	public List<String> getItemIDs(LocalDate date){
		return itemIDs.get(date);
	}
	
	public void putItemIDs(LocalDate date,List<String> ids) {
		itemIDs.put(date, ids);
	}
	
	public void putItemID(LocalDate date, String itemid) {
		if(beginDate==null || beginDate.isAfter(date)) {
			beginDate = date;
		}
		
		if(endDate==null || endDate.isBefore(date)) {
			endDate = date;
		}
		
		List<String> ids = itemIDs.get(date);
		if(ids==null) {
			ids = new ArrayList<String>();
			itemIDs.put(date, ids);
		}
		ids.add(itemid);
	}

	public LocalDate getBeginDate() {
		return beginDate;
	}

	public LocalDate getEndDate() {
		return endDate;
	}

	@Override
	public String toString() {
		return "DailyItem [beginDate=" + beginDate + ", endDate=" + endDate + ", itemIDs=" + itemIDs + "]";
	}
	
	
	
	

}
