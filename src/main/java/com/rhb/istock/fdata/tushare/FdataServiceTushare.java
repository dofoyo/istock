package com.rhb.istock.fdata.tushare;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;

@Service("fdataServiceTushare")
public class FdataServiceTushare {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;

	protected static final Logger logger = LoggerFactory.getLogger(FdataServiceTushare.class);
	
	public Map<String,String> getFinaGrowthRatioInfo(Set<String> ids){
		Map<String, String> info =  new HashMap<String,String>();
		
		String[] endDate = this.getEndDate();
		Map<String,FinaIndicator> indicators;
		Map<String,FinaForecast> forecasts;
		FinaIndicator indicator;
		FinaForecast forecast;
		
		for(String id : ids) {
			indicators = fdataRepositoryTushare.getIndicators(id);
			indicator = indicators.get(endDate[0]);
			if(indicator!=null) {
				info.put(id, indicator.getInfo());
			}else {
				forecasts = fdataRepositoryTushare.getForecasts(id);
				forecast = forecasts.get(endDate[0]);
				if(forecast != null) {
					info.put(id, forecast.getInfo());
				}else {
					indicator = indicators.get(endDate[1]);
					if(indicator!=null) {
						info.put(id, indicator.getInfo());
					}
				}
			}
		}
		
		return info;
	}
	
	private String[] getEndDate() {
		String[] endDate = new String[2];
		LocalDate today = LocalDate.now();
		int y = today.getYear();
		int m = today.getMonthValue();
		int d = today.getDayOfMonth();
		if(m>=1 && m<5){
			endDate[0] = Integer.toString(y-1) + "1231";
			endDate[1] = Integer.toString(y-1) + "0930";
		}else if(m>=5 && m<7){
			endDate[0] = Integer.toString(y) + "0331";
			endDate[1] = Integer.toString(y-1) + "1231";
		}else if(m>=7 && m<10){
			endDate[0] = Integer.toString(y) + "0630";
			endDate[1] = Integer.toString(y-1) + "0331";
		}else if(m>=10){
			endDate[0] = Integer.toString(y) + "0930";
			endDate[1] = Integer.toString(y-1) + "0630";
		}
		
		return endDate;
	}
	
	public List<String> getGrowModels(String b_date, String e_date,Integer n) {
		long beginTime=System.currentTimeMillis(); 

		List<String> models = new ArrayList<String>(); 

		GrowModel model;
		
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, " getGrowModels: " + item.getItemID());//进度条
			model = this.getGrowModel(item.getItemID(),item.getName(), b_date, e_date,n);
			if(model!=null && model.isOK()) {
				models.add(item.getItemID());
				System.out.println(model);
			}
		}
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          

		return models;
		
	}
	
	public GrowModel getGrowModel(String itemID, String itemName, String b_date, String e_date,Integer n) {
		Fina b_fina = fdataRepositoryTushare.getFina(itemID, b_date);
		Fina e_fina = fdataRepositoryTushare.getFina(itemID, e_date);
		fdataRepositoryTushare.init();
		
		if(b_fina!=null && e_fina!=null) {
			return new GrowModel(itemID, itemName,b_fina, e_fina,n);
		}else {
			return null;
		}
	}
}
