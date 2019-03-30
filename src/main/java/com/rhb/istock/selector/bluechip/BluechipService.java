package com.rhb.istock.selector.bluechip;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;

public interface BluechipService {
	public void generateBluechip();
	public List<String> getBluechipIDs(LocalDate date);
	public TreeMap<LocalDate,List<String>> getBluechipIDs(LocalDate beginDate, LocalDate endDate);
	public List<String> getLatestBluechipIDs();

}
