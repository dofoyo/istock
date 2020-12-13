package com.rhb.istock.index.tushare;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
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
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;

@Service("indexServiceTushare")
public class IndexServiceTushare {
	@Autowired
	@Qualifier("indexRepositoryTushare")
	IndexRepositoryTushare indexRepositoryTushare;
	

	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;
	
	
	protected static final Logger logger = LoggerFactory.getLogger(IndexServiceTushare.class);
	
	public void init() {
		indexRepositoryTushare.evictIndexDatasCache();
	}
	
	public Set<LocalDate> getSseiDates(LocalDate endDate, Integer period){
		String ts_code = "000001.SH";
		IndexData data = this.getIndexDatas(ts_code, endDate, period);
		//System.out.println(data);
		return data.getDates();
	
	}
	
	public IndexBasic getIndexBasic(String ts_code) {
		Set<IndexBasic> basics = indexRepositoryTushare.getIndexBasics();
		for(IndexBasic basic : basics) {
			if(basic.getTs_code().equals(ts_code)) {
				return basic;
			}
		}
		return null;
	}
	
	public Integer[] getSseiGrowthRate(LocalDate endDate, Integer period) {
		String ts_code = "000001.SH";
		IndexData data = this.getIndexDatas(ts_code, endDate, period);
		return data.growthRate();
	}
	
	public Integer[] getGrowthRate(String ts_code,LocalDate endDate, Integer period) {
		//logger.info(String.format("itemID=%s, date=%s", itemID, endDate.toString()));
		
		Set<LocalDate> dates = this.getSseiDates(endDate, period);
		IndexData data = this.getIndexDatas(ts_code, dates);
		if(data==null) {
			return new Integer[] {-1,-1};
		}else {
			return data.growthRate();
		}
	}
	
	public void generateIndex() {
		StringBuffer sb = new StringBuffer();
		LocalDate date = LocalDate.now();
		//date = LocalDate.parse("2020/03/16",DateTimeFormatter.ofPattern("yyyy/MM/dd"));
		Integer period = 13;
		Integer[] rate;
		Set<IndexBasic> basics = indexRepositoryTushare.getIndexBasics();
		Set<IndexWeight> weights;
		int i=1;
		for(IndexBasic basic : basics) {
			Progress.show(basics.size(),i++, " generatorIndex: " + basic.getTs_code());//进度条
			weights = indexRepositoryTushare.getIndexWeights(basic.getTs_code());
			if(weights.size()>0) {
				rate = this.getGrowthRate(basic.getTs_code(), date, period);
				if(rate[1]>=0) {
					sb.append(basic.getTs_code());
					sb.append(",");
				}			
			}			
		}
		if(sb.length()>0) {
			sb.deleteCharAt(sb.length()-1);
		}
		
		indexRepositoryTushare.saveIndexFile(sb.toString());
	}
	
	public TreeMap<Integer[], Set<String>> getGrowthRate(LocalDate endDate, Integer period) {
		TreeMap<Integer[], Set<String>> indexs = new TreeMap<Integer[], Set<String>>();
		String[] allIndexs = indexRepositoryTushare.getTsCodes();
		Integer[] ssei = this.getSseiGrowthRate(endDate, period);
		Integer[] rate;
		Integer i=1;
		Set<String> tmp;
		for(String code : allIndexs) {
			rate = this.getGrowthRate(code, endDate, period);
			if(rate[0]>ssei[0] || rate[1]>ssei[1]) {
				tmp = indexs.get(rate);
				if(tmp==null) {
					tmp = new HashSet<String>();
					indexs.put(rate, tmp);
				}
				tmp.add(code);
			}
			Progress.show(allIndexs.length, i++, "  getGrowthRate, " + code + ", rate=" + rate);
		}
		
		System.out.println("ssei ratio = " + ssei);
		return indexs;
	}
	
	public Map<String,Set<IndexWeight>> getIndexWeights(){
		return indexRepositoryTushare.getIndexWeights();
	}
	
	public Set<String> getItemIDsFromTopGrowthRateIndex(LocalDate endDate, Integer period, Integer top){
		Set<String> ids = new HashSet<String>();
		
		Map<String,Set<IndexWeight>> members = this.getIndexWeights();
		Set<IndexWeight> ms;
		
		TreeMap<Integer[],Set<String>> indexs = this.getGrowthRate(endDate, period);
		NavigableSet<Integer[]> keys = indexs.descendingKeySet();
		Set<String> codes;
		IndexBasic basic;
		int i = 0 ;
		StringBuffer sb = new StringBuffer(endDate.toString()+ "\n");
		for(Integer[] key : keys) {
			if(++i > top) {
				break;
			}

			codes = indexs.get(key);
			for(String code : codes) {
				basic = this.getIndexBasic(code);
				if(basic!=null) {
					sb.append("rate=" + key.toString() + " - " + endDate.toString() + " - " + basic.getName() + ": ");
				}
				ms = members.get(code);
				if(ms!=null) {
					for(IndexWeight iw : ms) {
						ids.add(iw.getItemID());
						sb.append(iw.getItemID() + ",");
						//System.out.print(iw.getCon_code() + ",");
					}
				}
				//System.out.println("");
				sb.append("\n");
			}
		}
		//logger.info(sb.toString());
		
		return ids;
	}
	
	private IndexData getIndexDatas(String ts_code, LocalDate endDate, Integer count) {
		IndexData newData = new IndexData(ts_code);
		
		IndexData allData = indexRepositoryTushare.getIndexDatas(ts_code);
		
		if(!allData.isExist(endDate)) {
			IndexBar latestBar = this.downloadLatestBar(allData.getItemID());
			if(latestBar!=null) {
				if(endDate.equals(latestBar.getDate())) {
					allData.addBar(latestBar);
				}
			}
		}
		
		LocalDate date = endDate;
		IndexBar bar;
		for(int i=0,j=0; i<count && j<allData.getBarSize(); j++) {
			bar = allData.getBar(date);
			if(bar!=null) {
				newData.addBar(date,bar);
				i++;
			}
			date = date.minusDays(1);
		}
		return newData;
	}
	
	private IndexBar downloadLatestBar(String itemID) {
		IndexBar bar = null;
		LocalDate theLatestBarDate = null;
		Map<String,String> latestBar = kdataRealtimeSpider.getLatestMarketData(itemID);
		if(latestBar!=null) {
			theLatestBarDate = LocalDate.parse(latestBar.get("dateTime"), DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			bar = new IndexBar(
					theLatestBarDate,
						latestBar.get("close"), 
						latestBar.get("open"), 
						latestBar.get("high"), 
						latestBar.get("low"), 
						latestBar.get("preClose"), 
						latestBar.get("quantity"), 
						latestBar.get("amount")
						);
		}
		return bar;
	}
	
	private IndexData getIndexDatas(String ts_code, Set<LocalDate> dates) {
		IndexData newData = new IndexData(ts_code);
		
		IndexData allData = indexRepositoryTushare.getIndexDatas(ts_code);
		IndexBar bar, latestBar;
		for(LocalDate date : dates) {
			bar = allData.getBar(date);
			if(bar!=null) {
				newData.addBar(date,bar);
			}else {
				latestBar = this.downloadLatestBar(allData.getItemID());
				if(latestBar!=null && date.equals(latestBar.getDate())) {
					newData.addBar(latestBar);
				}else {
					return null;  //如果为null, 说明该指数不全，不能用
				}
			}
		}
		return newData;
	}	
}
