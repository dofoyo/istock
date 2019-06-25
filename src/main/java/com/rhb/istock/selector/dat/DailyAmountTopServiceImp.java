package com.rhb.istock.selector.dat;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.kdata.spider.KdataRealtimeSpider;

@Service("dailyAmountTopServiceImp")
public class DailyAmountTopServiceImp implements DailyAmountTopService {
	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;
	
	@Override
	public List<String> getLatestDailyAmountTops(Integer top) {
		return kdataRealtimeSpider.getLatestDailyTop(top);
	}

}
