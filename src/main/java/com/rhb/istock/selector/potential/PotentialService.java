package com.rhb.istock.selector.potential;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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
	
}
