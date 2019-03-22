package com.rhb.istock.selector; 

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.selector.hold.HoldEntity;

public interface SelectorService{
	public List<HoldEntity> getHolds();

	public List<String> getHighLowTops(Integer top);
	public void generateHighLowTops();
	
	public List<String> getAverageAmountTops(Integer top);
	public void generateAverageAmountTops();
	
	public void generateBluechip();
	public List<String> getBluechipIDs(LocalDate date);

	public List<String> getDailyAmountTops(Integer top);

	public Map<String,String> getFavors();

}