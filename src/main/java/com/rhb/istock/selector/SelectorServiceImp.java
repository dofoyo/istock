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
import com.rhb.istock.selector.agt.AmountGapTopService;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.selector.breaker.BreakerService;
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
	@Qualifier("amountGapTopServiceImp")
	AmountGapTopService amountGapTopService;
	
	@Autowired
	@Qualifier("potentialService")
	PotentialService potentialService;

	@Autowired
	@Qualifier("breakerService")
	BreakerService breakerService;
	
	@Override
	public List<HoldEntity> getHolds() {
		return holdService.getHolds();
	}

	@Override
	public List<String> getLatestHighLowTops(Integer top) {
		return hlt.getLatestHighLowTops(top);
	}

	@Override
	public void generateLatestHighLowTops() {
		hlt.generateLatestHighLowTops();
	}

	@Override
	public List<String> getLatestAverageAmountTops(Integer top) {
		return aat.getLatestAverageAmountTops(top);
	}

	@Override
	public void generateLatestAverageAmountTops() {
		aat.generateLatestAverageAmountTops();
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
	public void generateDailyAmountTops() {
		dat.generateDailyAmountTops();
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
	public void generateAmountGaps() {
		amountGapTopService.generateAmountGaps();
		
	}

	@Override
	public List<String> getAmountGapTops(Integer top) {
		return amountGapTopService.getAmountGapTops(top);
	}

	@Override
	public void generateBreakers() {
		breakerService.generateBreakers();
		
	}

	@Override
	public TreeMap<LocalDate, List<String>> getBreakers(LocalDate beginDate, LocalDate endDate) {
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		
		Map<LocalDate, List<String>> breaks = breakerService.getBreakerIDs();
		
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			if(breaks.containsKey(date)) {
				ids.put(date, breaks.get(date));
			}
		}
		
		return ids;
	}

	@Override
	public void generateLatestPotentials() {
		potentialService.generateLatestPotentials();
	}

	@Override
	public List<Potential> getLatestPotentials() {
		return potentialService.getLatestPotentials();
	}

	@Override
	public void generateTmpLatestPotentials() {
		potentialService.generateTmpLatestPotentials();		
	}

	@Override
	public List<String> getLatestPotentialIDs() {
		return potentialService.getLatestPotentialIDs();
	}

}
