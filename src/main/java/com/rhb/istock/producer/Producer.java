package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/*
 * 
 */
public interface Producer {
	public List<String> produce(LocalDate date, boolean write);
	public Map<LocalDate,List<String>> produce(LocalDate bDate, LocalDate eDate);
	public Map<LocalDate,List<String>> getResults(LocalDate bDate, LocalDate eDate);
	public List<String> getResults(LocalDate date);
}
