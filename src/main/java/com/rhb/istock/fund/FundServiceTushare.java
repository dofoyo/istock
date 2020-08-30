package com.rhb.istock.fund;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.tushare.FdataRepositoryTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;

@Service("fundServiceTushare")
public class FundServiceTushare {
	@Autowired
	@Qualifier("fundRepositoryTushare")
	FundRepositoryTushare fundRepositoryTushare;
	

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataServiceImp;
	
	
	protected static final Logger logger = LoggerFactory.getLogger(FundServiceTushare.class);
	
	public List<ItemPortfolio> getItemPortfolioes(String period) {
		Map<String, ItemPortfolio> tmps = fundRepositoryTushare.getItemPortfolioes(period);
		
		List<ItemPortfolio> itemPortfolioes = new ArrayList<ItemPortfolio>(tmps.values());
		Collections.sort(itemPortfolioes, new Comparator<ItemPortfolio>() {
			@Override
			public int compare(ItemPortfolio o1, ItemPortfolio o2) {
				return o2.getStk_mkv_ratio().compareTo(o1.getStk_mkv_ratio());
			}});
		
		return itemPortfolioes;
	}
	
}
