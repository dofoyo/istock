package com.rhb.istock.selector;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.selector.aat.AverageAmountTopService;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.selector.dat.DailyAmountTopService;
import com.rhb.istock.selector.favor.FavorService;
import com.rhb.istock.selector.hlt.HighLowTopService;
import com.rhb.istock.selector.hold.HoldEntity;
import com.rhb.istock.selector.hold.HoldService;
import com.rhb.istock.selector.potential.Potential;
import com.rhb.istock.selector.potential.PotentialService;

@Service("selectorServiceImp")
public class SelectorServiceImp implements SelectorService{
	@Autowired
	@Qualifier("dailyAmountTopServiceImp")
	DailyAmountTopService dat;

	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;
	
	@Autowired
	@Qualifier("averageAmountTopServiceImp")	
	AverageAmountTopService aat;
	
	@Autowired
	@Qualifier("highLowTopServiceImp")
	HighLowTopService hlt;

	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorService;

	@Autowired
	@Qualifier("holdServiceImp")
	HoldService holdService;

	@Autowired
	@Qualifier("potentialService")
	PotentialService potentialService;

	@Override
	public List<HoldEntity> getHolds() {
		return holdService.getHolds();
	}

	@Override
	public Map<String,Integer> getHighLowTops(Integer top) {
		return hlt.getHighLowTops(top);
	}

	@Override
	public Map<String,Integer>  getLatestAverageAmountTops(Integer top) {
		return aat.getLatestAverageAmountTops(top);
	}

	@Override
	public void generateBluechip() {
		bluechipService.generateBluechip();		
	}

	@Override
	public List<String> getBluechipIDs(LocalDate date) {
		return bluechipService.getBluechipIDs(date);
	}

	@Override
	public Map<String,Integer> getLatestDailyAmountTops(Integer top) {
		Map<String,Integer> tops = new HashMap<String,Integer>();
		List<String> ids = dat.getLatestDailyAmountTops(top);
		int i=0;
		for(String id: ids) {
			tops.put(id, i++);
		}
		return tops;
	}

	@Override
	public Map<String, String> getFavors() {
		return favorService.getFavors();
	}

	@Override
	public List<String> getLatestBluechipIDs() {
		return bluechipService.getLatestBluechipIDs();
	}

	@Override
	public TreeMap<LocalDate,List<String>> getBluechipIDs(LocalDate beginDate, LocalDate endDate) {
		return bluechipService.getBluechipIDs(beginDate, endDate) ;
	}

	@Override
	public List<String> getHoldIDs() {
		List<HoldEntity> holds  = getHolds();
		List<String> ids = new ArrayList<String>();
		for(HoldEntity hold : holds) {
			ids.add(hold.getItemID());
		}
		return ids;
	}


	@Override
	public Map<String,Potential> getLatestPotentials() {
		return potentialService.getLatestPotentials();
	}

	@Override
	public Map<String, Potential> getPotentials(LocalDate date) {
		return potentialService.getPotentials(date);
	}

	@Override
	public List<String> getPowerIDs() {
		return potentialService.getPowerIDs();
	}

	@Override
	public List<Potential> getPotentials_hlb(LocalDate date, Integer tops) {
		return potentialService.getPotentials_hlb(date, tops);
	}

	@Override
	public List<Potential> getPotentials_avb(LocalDate date, Integer tops) {
		return potentialService.getPotentials_avb(date, tops);
	}
}
