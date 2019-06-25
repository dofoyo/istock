package com.rhb.istock.selector.aat;

import java.util.List;
import java.util.Map;

public interface AverageAmountTopService {
	public Map<String,Integer> getLatestAverageAmountTops(Integer top);
	public List<String> getLatestTops(Integer top);
	
}
