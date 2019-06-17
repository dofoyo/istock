package com.rhb.istock.selector;

import java.time.LocalDate;
import java.util.ArrayList;
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
	public List<String> getLatestHighLowTops(Integer top) {
		return hlt.getLatestHighLowTops(top);
	}

	@Override
	public List<String> getLatestAverageAmountTops(Integer top) {
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
	public List<String> getLatestDailyAmountTops(Integer top) {
		return dat.getLatestDailyAmountTops(top);
	}

	@Override
	public Map<String, String> getFavors() {
		return favorService.getFavors();
	}

	@Override
	public List<String> getDailyAmountTops(Integer top, LocalDate date) {
		return dat.getDailyAmountTops(top, date);
	}

	@Override
	public List<String> getAverageAmountTops(Integer top, LocalDate date) {
		return aat.getAverageAmountTops(top, date);
	}

	@Override
	public void generateAverageAmountTops() {
		aat.generateAverageAmountTops();
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
	public TreeMap<LocalDate, List<String>> getDailyAmountTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		return dat.getDailyAmountTops(top, beginDate, endDate);
	}

	@Override
	public TreeMap<LocalDate, List<String>> getAverageAmountTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		return aat.getAverageAmountTops(top, beginDate, endDate);
	}

	@Override
	public List<String> getHighLowTops(Integer top, LocalDate date) {
		return hlt.getHighLowTops(top, date);
	}

	@Override
	public TreeMap<LocalDate, List<String>> getHighLowTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		return hlt.getHighLowTops(top, beginDate, endDate);
	}

	@Override
	public void generateHighLowTops() {
		hlt.generateHighLowTops();
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
}
