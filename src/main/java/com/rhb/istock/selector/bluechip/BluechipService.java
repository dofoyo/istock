package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.List;

import com.rhb.istock.selector.bluechip.api.BluechipView;

public interface BluechipService {
	public List<BluechipDto> getBluechips();
	
	public BluechipDto getBluechips(String stockcode);
	
	public List<BluechipDto> getBluechips(LocalDate date);
	
	public List<String> getBluechipIDs(LocalDate date);
	
	public List<BluechipView> getBluechipViews(LocalDate date);
	
	public List<BluechipView> getBluechipViews();
	
	public boolean inGoodPeriod(String stockcode, LocalDate date);
	
	public void generateBluechip();
	
	public void init();


}
