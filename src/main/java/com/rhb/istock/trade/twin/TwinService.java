package com.rhb.istock.trade.twin;

import java.time.LocalDate;
import java.util.List;

public interface TwinService {
	public void generateTradeList();
	public List<String> getOpenList(LocalDate date);
	//public List<String> getDropList(LocalDate date);
}
