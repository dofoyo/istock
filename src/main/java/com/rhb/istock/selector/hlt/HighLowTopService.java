package com.rhb.istock.selector.hlt;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import com.rhb.istock.kdata.Muster;

public interface HighLowTopService {
	public Map<String,Integer> getHighLowTops(Integer top);
	public List<String> getTops(Integer top);

	}
