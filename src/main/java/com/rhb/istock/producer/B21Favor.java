package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.selector.favor.FavorService;

/*
 * favor  + 向上破21日线
 * 
 */

@Service("b21Favor")
public class B21Favor implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(B21Favor.class);
	
	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorService;

	@Autowired
	@Qualifier("b21")
	Producer b21;
		
	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		System.out.println("do NOT used!!");
		return null;
	}

	@Override
	public Map<LocalDate, List<String>> getResults(LocalDate bDate, LocalDate eDate) {
		System.out.println("do NOT used!!");
		return null;
	}

	@Override
	public List<String> getResults(LocalDate date) {
		List<String> results = new ArrayList<String>();
		Set<String> favors = favorService.getFavors().keySet();
		if(favors!=null && favors.size()>0) {
			List<String> b21s = b21.getResults(date);
			if(b21s!=null && b21s.size()>0) {
				for(String id : b21s) {
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
