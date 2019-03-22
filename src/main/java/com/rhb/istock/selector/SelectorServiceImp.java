package com.rhb.istock.selector;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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
	
	@Override
	public List<HoldEntity> getHolds() {
		return holdService.getHolds();
	}

	@Override
	public List<String> getHighLowTops(Integer top) {
		return hlt.getHighLowTops(top);
	}

	@Override
	public void generateHighLowTops() {
		hlt.generateHighLowTops();
	}

	@Override
	public List<String> getAverageAmountTops(Integer top) {
		return aat.getAverageAmountTops(top);
	}

	@Override
	public void generateAverageAmountTops() {
		aat.generateAverageAmountTops();
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
	public List<String> getDailyAmountTops(Integer top) {
		return dat.getDailyAmountTops(top);
	}

	@Override
	public Map<String, String> getFavors() {
		return favorService.getFavors();
	}

}
