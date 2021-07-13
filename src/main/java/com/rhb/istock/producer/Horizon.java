package com.rhb.istock.producer;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.fina.FinaService;

/*
 * newbPlus（创新高，89个交易日的涨幅在55%内） + 买入评级 + 业绩增涨 + HLGAP低到高排序
 * 
 */

@Service("horizon")
public class Horizon implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(Horizon.class);
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Value("${operationsPath}")
	private String operationsPath;
	
	private String fileName  = "Horizon.txt";
	
	private Integer cagr = 21;  //业绩年增长率
	
	private Integer reco = 5;  //推荐买入的机构数量
	
	private Integer hlgap = 55; //最大涨幅

	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> breakers;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date = bDate; (date.isBefore(eDate) || date.equals(eDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, fileName + ", " + date.toString());
			breakers = this.produce(date,false);
			if(breakers!=null && breakers.size()>0) {
				results.put(date, breakers);
			}
		}
		
		FileTools.writeMapFile(this.getFileName(), results, false);
		
		return results;
	}

	@Override
	public Map<LocalDate, List<String>> getResults(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		LocalDate date;
		for(Map.Entry<LocalDate, List<String>> entry : all.entrySet()) {
			date = entry.getKey();
			if((date.isAfter(bDate) || date.equals(bDate))
					&& (date.isBefore(eDate) || date.equals(eDate))) {
				results.put(date, entry.getValue());
			}
		}
		
		return results;
	}
	
	private String getFileName() {
		return operationsPath + fileName;
	}

	@Override
	public List<String> getResults(LocalDate date) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		if(all.get(date)!=null) {
			return all.get(date);
		}else {
			return this.produce(date, false);
		}
	}

	@Override
	public List<String> produce(LocalDate date, boolean write) {
		List<String> breakers = new ArrayList<String>();
		
		List<Muster> ms;
		Muster muster;
		Muster previous;
		List<String> recommendations;
		Item item;

		Map<String,Muster> musters = kdataService.getMusters(date);
		List<LocalDate> dates = kdataService.getMusterDates(89, date);
		Map<String,Muster> previous_musters = kdataService.getMusters(dates.get(0));
		
		Map<String,Item> items = itemService.getItems();
		if(musters!=null && musters.size()>0
				//&& previous_musters!=null && previous_musters.size()>0
				) {
			recommendations = finaService.getHighRecommendations(date, 10000, reco); //推荐买入
			ms = new ArrayList<Muster>();
			for(String id : recommendations) {
				muster = musters.get(id);
				item = items.get(id);
				if(muster!=null && item!=null && item.getCagr()!=null && item.getCagr()>=cagr) {
					ms.add(muster);
				}
			}
			
			Collections.sort(ms, new Comparator<Muster>() {
				@Override
				public int compare(Muster o1, Muster o2) {
					if(o1.getN21Gap().compareTo(o2.getN21Gap())==0){            //横盘波动
						return o1.getHLGap().compareTo(o2.getHLGap());
					}else {
						return o1.getN21Gap().compareTo(o2.getN21Gap());
					}
					//return o1.getLatestPrice().compareTo(o2.getLatestPrice());
					//return o2.getTotal_mv().compareTo(o1.getTotal_mv());
				}
			});
			
			String s;
			Integer ratio;
			for(Muster m : ms) {
				previous = previous_musters.get(m.getItemID());
				if(m!=null 
						&& previous!=null
						) {
					ratio = Functions.growthRate(m.getAveragePrice21(), previous.getAveragePrice21());
					if(ratio>-8 && ratio<=8
							//&& m.isDownBreaker()
							) {
/*						s = String.format("%s, %s, p21=%.2f, m21=%.2f, rate=%d", 
								date.toString(),
								m.getItemID(),
								previous.getAveragePrice21(),
								m.getAveragePrice21(),
								ratio);
						logger.info(s);*/
						breakers.add(m.getItemID());
					}
				}
			}
			
			if(breakers.size()>0 && write) {
				Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
				results.put(date, breakers);
				FileTools.writeMapFile(this.getFileName(), results, true);
			}

		}	
		return breakers;
	}

}
