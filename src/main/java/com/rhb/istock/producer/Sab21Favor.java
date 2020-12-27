package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.selector.favor.FavorService;

/*
 * sab + favor
 */

@Service("sab21Favor")
public class Sab21Favor implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(Sab21Favor.class);
	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorService;
	
	@Autowired
	@Qualifier("sab21")
	Producer sab;
		
	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		return this.getResults(bDate, eDate);
	}

	@Override
	public Map<LocalDate, List<String>> getResults(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> favors;
		for(LocalDate date=bDate; date.isBefore(eDate)||date.equals(eDate); date = date.plusDays(1)) {
			favors = this.getResults(date);
			if(favors!=null && favors.size()>0) {
				results.put(date, favors);
			}
		}
		
		return results;	
	}

	@Override
	public List<String> getResults(LocalDate date) {
		List<String> results = new ArrayList<String>();
		Set<String> favors = favorService.getFavors().keySet();
		if(favors!=null && favors.size()>0) {
			List<String> sabs = sab.getResults(date);
			if(sabs!=null && sabs.size()>0) {
				for(String id : sabs) {
					if(favors.contains(id)) {
						results.add(id);
					}
				}	
			}
		}
		return results;
	}

	@Override
	public List<String> produce(LocalDate date, boolean write) {
		System.out.println("do NOT used!!");
		return null;
	}
}
