package com.rhb.istock.selector.aat.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface AverageAmountTopRepository {
	public Map<LocalDate,List<String>> getAverageAmountTops();

}
