package com.rhb.istock.selector.hlt;

import java.util.List;
import java.util.Map;

public interface HighLowTopService {
	public Map<String,Integer> getLatestHighLowTops(Integer top);
	public List<String> getLatestTops(Integer top);

	}
