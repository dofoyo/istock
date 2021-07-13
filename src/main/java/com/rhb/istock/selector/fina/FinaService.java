package com.rhb.istock.selector.fina;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.eastmoney.FdataRepositoryEastmoney;
import com.rhb.istock.fdata.tushare.FdataRepositoryTushare;
import com.rhb.istock.fdata.tushare.FdataServiceTushare;
import com.rhb.istock.fdata.tushare.FdataSpiderTushare;
import com.rhb.istock.fdata.tushare.FinaForecast;
import com.rhb.istock.fdata.tushare.FinaIndicator;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("finaService")
public class FinaService {
	@Autowired
	@Qualifier("fdataRepositoryTushare")
	FdataRepositoryTushare fdataRepositoryTushare;
	
	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;

	@Autowired
	@Qualifier("fdataSpiderTushare")
	FdataSpiderTushare fdataSpiderTushare;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("fdataRepositoryEastmoney")
	FdataRepositoryEastmoney fdataRepositoryEastmoney;
	
	
	@Value("${quarterCompareFile}")
	private String quarterCompareFile;	
	
	protected static final Logger logger = LoggerFactory.getLogger(FinaService.class);

	public Integer getRecommendationCount(String itemID, LocalDate date) {
		return fdataRepositoryEastmoney.getRecommendations(itemID, date);
	}
	
	public List<String> getHighRecommendations(LocalDate date, Integer top, Integer count){
		List<String> results = new ArrayList<String>();
		
		List<Recommendation> res = new ArrayList<Recommendation>();
		Map<String, Integer> ids = fdataRepositoryEastmoney.getRecommendations(date);
		for(Map.Entry<String, Integer> entry : ids.entrySet()) {
			if(entry.getValue()>count) {  //有多少次以上的买入推荐
				res.add(new Recommendation(entry.getKey(),entry.getValue()));
			}
		}
		
		Collections.sort(res, new Comparator<Recommendation>() {
			@Override
			public int compare(Recommendation o1, Recommendation o2) {
				return o2.getCount().compareTo(o1.getCount());
			}
			
		});
		
		int i=0;
		for(Recommendation re : res) {
			results.add(re.getId());
			if(i++ >= top) {
				break;
			}
		}
		
		return results;
	}
	
	class Recommendation{
		private String id;
		private Integer count;
		public Recommendation(String id, Integer count) {
			this.id = id;
			this.count = count;
		}
		public String getId() {
			return id;
		}
		public Integer getCount() {
			return count;
		}
	}
	
	public List<String> getHighCAGR(Integer top){
		List<String> results = new ArrayList<String>();
		List<Item> tmp = new ArrayList<Item>();
		
		Map<String,Item> items = itemService.getItems();
		for(Item item : items.values()) {
			if(item.getCagr()!=null && item.getCagr()>20) {
				tmp.add(item);
			}
		}
		Collections.sort(tmp, new Comparator<Item>() {
			@Override
			public int compare(Item o1, Item o2) {
				return o2.getCagr().compareTo(o1.getCagr());
			}
			
		});

		int i=0;
		for(Item item : tmp) {
			results.add(item.getItemID());
			if(i++ >= top) {
				break;
			}
		}
		
		return results;
	}
	
	public Map<String,QuarterCompare> getQuarterCompares(){
		Map<String,QuarterCompare> qcs = new HashMap<String,QuarterCompare>();
		QuarterCompare qc;
		String results = FileTools.readTextFile(quarterCompareFile);
		String[] lines = results.split("\n");
		for(int i=1; i<lines.length; i++) {
			qc = new QuarterCompare(lines[i]);
			qcs.put(qc.getItemID(),qc);
		}
		
		return qcs;
	}
	
	public Map<String,QuarterCompare> getForecasts(){
		Map<String,QuarterCompare> qcs = new HashMap<String,QuarterCompare>();
		QuarterCompare qc;
		String results = FileTools.readTextFile(quarterCompareFile);
		String[] lines = results.split("\n");
		for(int i=1; i<lines.length; i++) {
			qc = new QuarterCompare(lines[i]);
			if(qc.getItemID().startsWith("sz")) {  //只有深市才强制业绩预告
				qcs.put(qc.getItemID(),qc);
			}
		}
		
		return qcs;
	}
	
	public QuarterCompare buildQuarterCompare(String itemID, String date){
		QuarterCompare qc = null;
		fdataRepositoryTushare.init();
		Map<String,FinaIndicator> indicators = fdataRepositoryTushare.getIndicators(itemID, LocalDate.now());
		FinaIndicator indicator = indicators.get(date);
		if(indicator!=null) {
			qc = new QuarterCompare();
			qc.setItemID(itemID);
			qc.setPrevious_netprofit_yoy(indicator.getNetprofit_yoy().intValue());
			qc.setPrevious_dt_netprofit_yoy(indicator.getDt_netprofit_yoy().intValue());
			qc.setPrevious_or_yoy(indicator.getOr_yoy().intValue());
			
			//System.out.format("%s %s, ratio is %d\n", itemID,date,qc.getRatio());
		}
		return qc;
	}
	
	public List<QuarterCompare> buildQuarterCompares(String date){
		List<QuarterCompare> qcs = new ArrayList<QuarterCompare>();
		List<String> ids = itemService.getItemIDs();
		int i=1;
		QuarterCompare qc;
		for(String id : ids) {
			Progress.show(ids.size(),i++, id);//进度条
			qc = this.buildQuarterCompare(id, date);
			if(qc!=null) {
				qcs.add(qc);
			}
		}
		
/*		Collections.sort(qcs, new Comparator<QuarterCompare>() {
			@Override
			public int compare(QuarterCompare o1, QuarterCompare o2) {
				return o2.getNetprofit_yoy().compareTo(o1.getNetprofit_yoy());
			}
			
		});*/
		
		return qcs;
	}
	
	public void generateQuarterCompares() {
		Integer ratio = 100;
		String indicator_date = null;
		String forecast_date = null;
		LocalDate today = LocalDate.now();
		int y = today.getYear();
		int m = today.getMonthValue();
		int d = today.getDayOfMonth();
		if(m>=3 && (m<=4 && d<=15)){
			indicator_date = Integer.toString(y-1) + "1231";
			forecast_date = Integer.toString(y) + "0331";
		}else if(m>=6 && (m<=7 && d<=15)){
			indicator_date = Integer.toString(y) + "0331";
			forecast_date = Integer.toString(y) + "0630";
		}else if(m>=9 && (m<=10 && d<=15)){
			indicator_date = Integer.toString(y) + "0630";
			forecast_date = Integer.toString(y) + "0930";
		}else if(m==12 || m==1){
			indicator_date = Integer.toString(y) + "0930";
			forecast_date = Integer.toString(y+1) + "1231";
		}
		
		logger.info("indicator_date: " + indicator_date + ", forecast_date:" + forecast_date );
		
		this.generateQuarterCompares(indicator_date, forecast_date, ratio);
		
	}	
	/*
	 * indicator_date: 上一季度的实际业绩日期,如20200331
	 * forecast_date: 这一季度的业绩预告日期，如20200630
	 * 
	 * 目的是找出上一季度业绩优良，新一季度的业绩(预告/报告)还没发布的股票，以便提前埋伏进去
	 * 
	 */
	public void generateQuarterCompares(String indicator_date, String forecast_date, Integer ratio) {
		List<QuarterCompare> qcs = this.buildQuarterCompares(indicator_date);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
		Map<String,Muster> musters = null;

		StringBuffer sb = new StringBuffer("代码,上季净利润同比,上季非经净利润同比,上季主营同比,预告日期,预告净利润同比,预告日涨跌,报告日期,上季净利润同比,上季非经净利润同比,报告日涨跌\n");
		int i=1;
		Muster muster;
		FinaIndicator indicator;
		FinaForecast forecast;
		for(QuarterCompare qc : qcs) {
			Progress.show(qcs.size(),i++, qc.getItemID());//进度条
			if(qc!=null 
					&& qc.getPrevious_netprofit_yoy()!=null 
					&& qc.getPrevious_netprofit_yoy()>=ratio
					//&& qc.getPrevious_netprofit_yoy()!=null
					//&& qc.getPrevious_dt_netprofit_yoy()>0
					//&& qc.getPrevious_or_yoy()!=null
					//&& qc.getPrevious_or_yoy()>0
					) {
				forecast = getForecast(qc.getItemID(),forecast_date);
				if(forecast!=null) {
					qc.setForecast_date(forecast.getAnn_date());
					qc.setForecast_netprofit_yoy_max(forecast.getP_change_max().intValue());
					
					musters = kdataService.getMustersOfTheDayAfter(LocalDate.parse(forecast.getAnn_date(),dtf ));
					muster = musters.get(qc.getItemID());
					if(muster!=null) {
						qc.setForecast_price_up_max(muster.getMaxRate());
					}
				}	
				
				indicator = getIndicator(qc.getItemID(),forecast_date);
				if(indicator!=null) {
					qc.setIndicator_date(indicator.getAnn_date());
					qc.setIndicator_dt_netprofit_yoy(indicator.getDt_netprofit_yoy().intValue());
					qc.setIndicator_netprofit_yoy(indicator.getNetprofit_yoy().intValue());
					musters = kdataService.getMustersOfTheDayAfter(LocalDate.parse(indicator.getAnn_date(),dtf ));
					muster = musters.get(qc.getItemID());
					if(muster!=null) {
						qc.setIndicator_price_up_max(muster.getMaxRate());
					}				
				}
				
				sb.append(qc.getTxt());

				try {Thread.sleep(500);} catch (InterruptedException e) {} 
			}
		}
		
		FileTools.writeTextFile(quarterCompareFile, sb.toString(), false);
	}
	
	/*
	 * 业绩预告
	 */
	public FinaForecast getForecast(String itemID, String date) {
		try {
			fdataSpiderTushare.downForecast(itemID);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Map<String,FinaForecast> forecasts = fdataRepositoryTushare.getForecasts(itemID);
		if(forecasts.containsKey(date)) {
			return forecasts.get(date);
		}else {
			return null;
		}
	}
	
	/*
	 * 业绩报告
	 */
	public FinaIndicator getIndicator(String itemID, String date) {
		/*try {
			fdataSpiderTushare.downIndicator(itemID);
		} catch (Exception e) {
			e.printStackTrace();
		}*/
		Map<String,FinaIndicator> indicators = fdataRepositoryTushare.getIndicators(itemID, LocalDate.now());
		if(indicators.containsKey(date)) {
			return indicators.get(date);
		}else {
			return null;
		}
	}
	
	public List<NewPE> generateNewPE(String date) {
		List<NewPE> results = new ArrayList<NewPE>();
		Map<String, Muster> musters = kdataService.getLatestMusters();
		List<String> ids = itemService.getItemIDs();
		FinaIndicator fina;
		Muster muster;
		BigDecimal profit_dedt,total_mv,rate;
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			fina = this.getIndicator(id, date);
			muster = musters.get(id);
			if(fina!=null && muster!=null && fina.getProfit_dedt().compareTo(BigDecimal.ZERO)==1) {
				profit_dedt = fina.getProfit_dedt();
				total_mv = muster.getTotal_mv();
				rate = total_mv.divide(profit_dedt,BigDecimal.ROUND_HALF_UP);
				results.add(new NewPE(id, rate));
			}
		}
		
		Collections.sort(results, new Comparator<NewPE>() {

			@Override
			public int compare(NewPE o1, NewPE o2) {
				return o1.getRate().compareTo(o2.getRate());
			}
			
		});
		
		return results;
	}
}
