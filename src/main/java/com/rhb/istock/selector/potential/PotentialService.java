package com.rhb.istock.selector.potential;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.KdataService;

@Service("potentialService")
public class PotentialService {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	public Map<String,Potential> getLatestPotentials(){
		Map<String,Potential> potentials = new HashMap<String,Potential>();
		Potential potential;
		
		Map<String,Muster> musters = kdataService.getLatestMusters();
		
		for(Muster item : musters.values()) {
			potential =  new Potential(item.getItemID(),
					item.getAmount(),
					item.getAverageAmount(),
					item.getHighest(),
					item.getLowest(),
					item.getClose(),
					item.getLatestPrice());
			if(potential.getHNGap()<10) {
				//System.out.println(potential);
				potentials.put(potential.getItemID(),potential);	
			}					
		}

		return potentials;
	}
	
	public Map<String,Potential> getPotentials(LocalDate date){
		Map<String,Potential> potentials = new HashMap<String,Potential>();
		Potential potential;
		
		Map<String,Muster> musters = kdataService.getMusters(date);
		
		for(Muster item : musters.values()) {
			potential =  new Potential(item.getItemID(),
					item.getAmount(),
					item.getAverageAmount(),
					item.getHighest(),
					item.getLowest(),
					item.getClose(),
					item.getLatestPrice());
			if(potential.getHNGap()<10) {
				//System.out.println(potential);
				potentials.put(potential.getItemID(),potential);	
			}					
		}

		return potentials;
	}
	
	public List<String> getPowerIDs(){
		List<LocalDate> dates = kdataService.getLastMusterDates();

		Map<String,Potential> potentials = new HashMap<String,Potential>();
		
		Map<String,Muster> latests = kdataService.getLatestMusters();
		Map<String,Muster> musters;
		
		Map<String,Potential> ps;
		Muster muster;
		int i=1;
		for(LocalDate date : dates) {
			Progress.show(dates.size(), i++, date.toString());
			ps = this.getPotentials(date);
			musters = kdataService.getMusters(date);
			for(Map.Entry<String, Potential> entry : ps.entrySet()) {
				if(potentials.containsKey(entry.getKey())) {
					muster = musters.get(entry.getKey());
					if(muster!=null) {
						potentials.get(entry.getKey()).updateHighest(muster.getLatestPrice());
					}
				}else if(entry.getValue().isBreaker() && entry.getValue().isUpLimited()) {
					entry.getValue().setDate(date);
					potentials.put(entry.getKey(),entry.getValue());
				}
			}
		}
		
		List<String> ids = new ArrayList<String>();
		
		for(Map.Entry<String, Potential> entry : potentials.entrySet()) {
			muster = latests.get(entry.getKey());
			if(muster!=null && entry.getValue().getLatestPrice().compareTo(muster.getLatestPrice())==1) {
				if(entry.getValue().getHNGap()<=10 && entry.getValue().getHLGap()<=55) {
					ids.add(entry.getKey());
				}
			}
		}
		
		return ids;
	}
	
}
