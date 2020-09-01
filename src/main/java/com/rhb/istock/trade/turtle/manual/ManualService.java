package com.rhb.istock.trade.turtle.manual;

import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.stereotype.Service;

@Service("manualService")
public class ManualService {
	private Map<LocalDate, String> selects = new TreeMap<LocalDate, String>();

	public void addSelects(LocalDate date, String itemID) {
		this.selects.put(date, itemID);
	}
	
	public void deleteSelects(LocalDate date) {
		this.selects.remove(date);
	}
	
	public Map<LocalDate, String> getSelects(){
		return this.selects;
	}
}
