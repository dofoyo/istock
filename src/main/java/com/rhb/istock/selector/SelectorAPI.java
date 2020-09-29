package com.rhb.istock.selector;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.rhb.istock.comm.api.ResponseContent;
import com.rhb.istock.comm.api.ResponseEnum;
import com.rhb.istock.selector.drum.DrumService;

@RestController
public class SelectorAPI {
	
	@Autowired
	@Qualifier("drumService")
	DrumService drumService;
	
	@GetMapping("/selector/dimension/{date}")
	public ResponseContent<List<DimensionView>> getDiemension(
			@PathVariable(value="date") String endDate
			) {
		
		//System.out.println(endDate);
		
		LocalDate theEndDate = null;
		if(endDate!=null && !endDate.isEmpty()) {
			theEndDate = LocalDate.parse(endDate, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
		}
		
/*		DimensionView industry = new DimensionView("industry", "行业");
		industry.addBoard("bank", "银行", 30);
		industry.addItem("bank", "sh600016", "民生银行");
		industry.addItem("bank", "sh601398", "工商银行");

		industry.addBoard("证券", "证券", 40);
		industry.addItem("证券", "sh600030", "中信证券");

		DimensionView topic = new DimensionView("topic", "概念");
		topic.addBoard("5G", "5G", 50);
		topic.addItem("5G", "sz000063", "中兴通讯");
		
		DimensionView market = new DimensionView("market", "市场");
		market.addBoard("创业板", "创业板", 20);
		market.addItem("创业板", "sz300033", "同花顺");
		
		DimensionView area = new DimensionView("area", "地区");
		area.addBoard("深圳", "深圳", 60);
		area.addItem("深圳", "sz300033", "同花顺");

		List<DimensionView> views = new ArrayList<DimensionView>();
		views.add(industry);
		views.add(topic);
		views.add(market);
		views.add(area);*/
		
		List<DimensionView> views = drumService.getDimensionView(theEndDate);
		
		return new ResponseContent<List<DimensionView>>(ResponseEnum.SUCCESS, views);
	}
}
