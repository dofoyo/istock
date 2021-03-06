package com.rhb.istock.selector.hlt;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.KdataService;

@Service("highLowTopServiceImp")
public class HighLowTopServiceImp implements HighLowTopService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Override
	public List<String> getTops(Integer top) {
		List<String> tops = new ArrayList<String>();

		List<Muster> musters = new ArrayList<Muster>(kdataService.getLatestMusters().values());
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}
		});
		
		for(int i=0; i<top && i<musters.size(); i++) {
			//System.out.println(musters.get(i).getItemID() + ": " + musters.get(i).getHLGap());
			tops.add(musters.get(i).getItemID());
		}
		
		return tops;
	}
	
	@Override
	public Map<String,Integer> getHighLowTops(Integer top) {
		Map<String,Integer> tops = new HashMap<String,Integer>();
		
		List<Muster> musters = new ArrayList<Muster>(kdataService.getLatestMusters().values());
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
					return o1.getClose().compareTo(o2.getClose());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());
				}
			}
		});
		
		for(int i=0; i<top && i<musters.size(); i++) {
			//System.out.println(musters.get(i).getItemID() + ": " + musters.get(i).getHLGap());
			tops.put(musters.get(i).getItemID(),i);
		}
		
		return tops;
	}

	@Override
	public List<Muster> getTops(LocalDate date, Integer top) {
		List<Muster> musters = new ArrayList<Muster>(kdataService.getMusters(date).values());
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
			}
		});

		return musters.subList(0, musters.size()>top ? top : musters.size());
	}


}
