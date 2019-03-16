package com.rhb.istock.trade.turtle.simulation;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class DailyItem {
	private LocalDate beginDate=null, endDate=null;
	private Map<LocalDate, Set<String>> itemIDs = new HashMap<LocalDate,Set<String>>();
	
	public Set<String> getItemIDs(LocalDate date){
		return itemIDs.get(date);
	}
	
	public void putItemID(LocalDate date, String itemid) {
		if(beginDate==null || beginDate.isAfter(date)) {
			beginDate = date;
		}
		
		if(endDate==null || endDate.isBefore(date)) {
			endDate = date;
		}
		
		Set<String> ids = itemIDs.get(date);
		if(ids==null) {
			ids = new HashSet<String>();
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
