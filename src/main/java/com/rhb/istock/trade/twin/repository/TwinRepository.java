package com.rhb.istock.trade.twin.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import com.rhb.istock.trade.twin.Wfeature;


public interface TwinRepository {
	public void saveOpens(TreeMap<LocalDate,TreeSet<Wfeature>> result);
	public void saveDrops(TreeMap<LocalDate,TreeSet<Wfeature>> result);
	public TreeMap<LocalDate,List<String>> getOpens();
	public TreeMap<LocalDate,List<String>> getDrops();
}
