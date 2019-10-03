package com.rhb.istock.selector.aat;

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

@Service("averageAmountTopServiceImp")
public class AverageAmountTopServiceImp implements AverageAmountTopService{
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Override
	public List<String> getLatestTops(Integer top) {
		List<String>  tops = new ArrayList<String>();

		List<Muster> musters = new ArrayList<Muster>(kdataService.getLatestMusters().values());
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.getAverageAmount().compareTo(o1.getAverageAmount());
			}
		});
		
		for(int i=0; i<top && i<musters.size(); i++) {
			//System.out.println(musters.get(i).getItemID() + ": " + musters.get(i).getAverageAmount());
			tops.add(musters.get(i).getItemID());
		}
		
		return tops;
	}
	
	@Override
	public Map<String,Integer> getLatestAverageAmountTops(Integer top) {
		Map<String,Integer>  tops = new HashMap<String,Integer> ();

		List<Muster> musters = new ArrayList<Muster>(kdataService.getLatestMusters().values());
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.getAverageAmount().compareTo(o1.getAverageAmount());
			}
		});
		
		for(int i=0; i<top && i<musters.size(); i++) {
			//System.out.println(musters.get(i).getItemID() + ": " + musters.get(i).getAverageAmount());
			tops.put(musters.get(i).getItemID(),i);
		}
		
		return tops;
	}

}
