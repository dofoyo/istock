package com.rhb.istock.selector.hlt.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HighLowTopRepository {
	public Map<LocalDate,List<String>> getHighLowTops();
}
