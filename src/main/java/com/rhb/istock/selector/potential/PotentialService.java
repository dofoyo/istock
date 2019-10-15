package com.rhb.istock.selector.potential;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

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
	
	public List<Potential> getPotentials_hlb(LocalDate date, Integer tops){
		List<Potential> potentials = new ArrayList<Potential>();
		
		List<Muster> musters = new ArrayList<Muster>(kdataService.getMusters(date).values());

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
/*				if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
					return o2.getLatestPrice().compareTo(o1.getLatestPrice());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				}	
*/				return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
			}
		});

		Muster muster;
		for(int i=0; i<musters.size() && i<tops; i++) {
			muster = musters.get(i);
			if(muster.isPotential()){
				potentials.add(new Potential(muster.getItemID(),
						muster.getItemName(),
						muster.getIndustry(),
						muster.getAmount(),
						muster.getAverageAmount(),
						muster.getHighest(),
						muster.getLowest(),
						muster.getClose(),
						muster.getLatestPrice()));
			}
		}
		
		Map<String,Integer> industryHots = new HashMap<String,Integer>();
		Integer hot;
		for(Muster m : musters) {
			if(m.isPotential()) {
				hot = industryHots.get(m.getIndustry());
				if(hot == null) {
					hot = 1;
				}else {
					hot = hot + 1;
				}						
				industryHots.put(m.getIndustry(), hot);
			}
		}
		
		for(Potential p : potentials) {
			p.setIndustryHot(industryHots.get(p.getIndustry()));
		}
		
/*		Collections.sort(potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
					return o2.getIndustryHot().compareTo(o1.getIndustryHot());
			}
		});*/
		
		return potentials;
	}
	
	public Map<String,Potential> getLatestPotentials(){
		Map<String,Potential> potentials = new HashMap<String,Potential>();
		Potential potential;
		
		Map<String,Muster> musters = kdataService.getLatestMusters();
		
		for(Muster item : musters.values()) {
			//if(item.getHLGap()<50 && item.getHNGap()<10) {
			if(item.getHNGap()<10) {
				potential =  new Potential(item.getItemID(),
						item.getItemName(),
						item.getIndustry(),
						item.getAmount(),
						item.getAverageAmount(),
						item.getHighest(),
						item.getLowest(),
						item.getClose(),
						item.getLatestPrice());
				
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
			//if(item.getHLGap()<50 && item.getHNGap()<10) {
			if(item.isPotential()) {
				potential =  new Potential(item.getItemID(),
						item.getItemName(),
						item.getIndustry(),
						item.getAmount(),
						item.getAverageAmount(),
						item.getHighest(),
						item.getLowest(),
						item.getClose(),
						item.getLatestPrice());
				
				potentials.put(potential.getItemID(),potential);	
			}
		}

		return potentials;
	}
	
	public Map<String,TreeSet<Potential>> getIndustryPotentials(LocalDate date){
		Map<String,TreeSet<Potential>> potentials = new HashMap<String,TreeSet<Potential>>();
		Potential potential;
		TreeSet<Potential> ps;
		
		Map<String,Muster> musters = kdataService.getMusters(date);
		
		for(Muster item : musters.values()) {
			if(item.isPotential()) {
				potential =  new Potential(item.getItemID(),
						item.getItemName(),
						item.getIndustry(),
						item.getAmount(),
						item.getAverageAmount(),
						item.getHighest(),
						item.getLowest(),
						item.getClose(),
						item.getLatestPrice());
				ps = potentials.get(item.getIndustry());
				if(ps == null) {
					ps = new TreeSet<Potential>();
					potentials.put(item.getIndustry(), ps);
				}
				ps.add(potential);
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
