package com.rhb.istock.selector.dat.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface DailyAmountTopRepository {
	public Map<LocalDate,List<String>> getDailyAmountTops();
}
