package com.rhb.istock.selector.drum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Dimension;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.DimensionView;

@Service("drumService")
public class DrumService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Value("${drumFile}")
	private String drumFile;	
	
	protected static final Logger logger = LoggerFactory.getLogger(DrumService.class);

	public void generateDrums(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		StringBuffer sb = new StringBuffer();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, "  generateDrums: " + date.toString());
			sb.append(date.toString() + ":" + this.getDrums(date) + "\n");
		}
		FileTools.writeTextFile(drumFile, sb.toString(), false);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("generateDrums 用时：" + used + "秒");          
	}
	
	public void generateDrums(LocalDate endDate, Integer days) {
		List<LocalDate> dates = kdataService.getMusterDates(days, endDate);
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : dates) {
			sb.append(date.toString() + ":" + this.getDrums(date) + "\n");
		}
		FileTools.writeTextFile(drumFile, sb.toString(), false);
	}
	
	public List<String> getDrums() {
		List<LocalDate> dates = kdataService.getLastMusterDates();
		LocalDate date = dates.get(dates.size()-1);
		return this.getDrums(date);
	}
	
	private Map<String,Taoyans> getDimensions(LocalDate endDate){
		Dimension industry_a = new Dimension();
		Dimension area_a = new Dimension();
		Dimension market_a = new Dimension();
		Dimension topic_a = new Dimension();
		
		List<String> ids = this.getDrums(endDate);
		Item item;
		for(String id: ids) {
			item = itemService.getItem(id);
			industry_a.put(item.getIndustry(), id, item.getName());
			area_a.put(item.getArea(), id, item.getName());
			market_a.put(item.getMarket(), id, item.getName());
			topic_a.put(itemService.getTopic(item.getItemID()).split("，"), id, item.getName());
		}

		Map<String,Dimension> dimensions = itemService.getDimensions();

		Dimension industry_b = dimensions.get("industry");
		Dimension area_b = dimensions.get("area");
		Dimension market_b = dimensions.get("market");
		Dimension topic_b = dimensions.get("topic");

		Taoyans industry = new Taoyans(industry_a,industry_b);
		Taoyans area = new Taoyans(area_a,area_b);
		Taoyans market = new Taoyans(market_a,market_b);
		Taoyans topic = new Taoyans(topic_a,topic_b);
		
		Map<String, Taoyans> ds = new HashMap<String,Taoyans>();
		ds.put("topic", topic);
		ds.put("industry", industry);
		ds.put("area", area);
		ds.put("market", market);

		return ds;
	}
	
	public List<DimensionView> getDimensionView(LocalDate endDate){
		Map<String,Taoyans> dimensions = this.getDimensions(endDate);

		Taoyans topics = dimensions.get("topic");
		DimensionView topicView = this.getDimensionView("topic", "概念", topics.getResult());

		Taoyans industrys = dimensions.get("industry");
		DimensionView industryView = this.getDimensionView("industry", "行业", industrys.getResult());
		
		Taoyans areas = dimensions.get("area");
		DimensionView areaView = this.getDimensionView("area", "省市", areas.getResult());

		Taoyans markets = dimensions.get("market");
		DimensionView marketView = this.getDimensionView("market", "市场", markets.getResult());

		
		List<DimensionView> views = new ArrayList<DimensionView>();
		views.add(topicView);
		views.add(industryView);
		views.add(areaView);
		views.add(marketView);
		
		return views;
	}
	
	private DimensionView getDimensionView(String code, String name, List<Taoyan> ty) {
		DimensionView view = new DimensionView(code, name);
		//List<Taoyan> ty = industrys.getResult();
		Map<String,String> ids;
		for(Taoyan t : ty) {
			view.addBoard(t.getName(), t.getName(), t.getRatio());
			ids = t.getDrum();
			for(Map.Entry<String, String> entry : ids.entrySet()) {
				view.addItem(t.getName(), entry.getKey(), entry.getValue());
			}
		}
		return view;
	}
	
	public List<String> getDrumsOfTopDimensions(LocalDate endDate){
		Map<String,Taoyans> dimensions = this.getDimensions(endDate);
		
		Taoyans industry = dimensions.get("industry");
		Taoyans topic = dimensions.get("topic");
		
		
		Integer ratio = 55;
		Set<String> oks = new HashSet<String>();
		oks.addAll(industry.getHighRatioIDs(ratio));
		oks.addAll(topic.getHighRatioIDs(ratio));
		
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		List<Muster> ok_musters = new ArrayList<Muster>();
		for(String id : oks) {
			ok_musters.add(musters.get(id));
		}
		
		Collections.sort(ok_musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				//return o1.getLatestPrice().compareTo(o2.getLatestPrice());
				
				if(o1.getHLGap().compareTo(o2.getHLGap())==0){
					return o1.getLNGap().compareTo(o2.getLNGap());
				}else {
					return o1.getHLGap().compareTo(o2.getHLGap());
				}
			}
		});	
		
		List<String> results = new ArrayList<String>();
		for(Muster m : ok_musters) {
			results.add(m.getItemID());
		}
		
		return results;
	}
	
	public List<String> getDrums(LocalDate endDate) {
		List<String> ids = new ArrayList<String>();
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		Map<String,Muster> b_musters = kdataService.getMusters(kdataService.getMusterDates(89, endDate).get(0));
		
		Integer previous_period = 13;
		if(musters!=null) {
			List<LocalDate> previousDates = kdataService.getMusterDates(previous_period, endDate);
			if(previousDates!=null) {
				List<Map<String,Muster>> previous = new ArrayList<Map<String,Muster>>();
				for(LocalDate date : previousDates) {
					previous.add(kdataService.getMusters(date));
				}
				
				List<Muster>  ms = new ArrayList<Muster>(musters.values());
				
				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						//return o1.getLatestPrice().compareTo(o2.getLatestPrice());

						if(o1.getHLGap().compareTo(o2.getHLGap())==0){
							return o1.getLNGap().compareTo(o2.getLNGap());
						}else {
							return o1.getHLGap().compareTo(o2.getHLGap());
						}
					}
				});
				
				Integer sseiRatio = kdataService.getSseiRatio(endDate, previous_period);
				Integer ratio;
				Muster m,p,b;
				int j=1;
				for(int i=0; i<ms.size(); i++) {
					Progress.show(ms.size(), j++, "");
					m = ms.get(i);
					if(m!=null) {
						p = previous.get(0).get(m.getItemID());
						b = b_musters.get(m.getItemID());
						if(p!=null && b!=null) {
							ratio = this.getRatio(previous,m.getItemID(),m.getLatestPrice());
							if(ratio >= sseiRatio
								&& ratio >0
								&& m.isUpAve(21)
								&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1
								&& m.getAveragePrice21().compareTo(b.getAveragePrice21())==1
								) {
								ids.add(m.getItemID());
							}
						}
					}
				}
			}
		}
		
		//logger.info(sb.toString());
		
		return ids;
	}
	
	private Integer getRatio(List<Map<String,Muster>> musters, String itemID, BigDecimal price) {
		Integer ratio = 0;
		BigDecimal lowest=null;
		Muster m;
		for(Map<String,Muster> ms : musters) {
			m = ms.get(itemID);
			if(m!=null) {
				lowest = (lowest==null || lowest.compareTo(m.getLatestPrice())==1) ? m.getLatestPrice() : lowest;
				//logger.info(String.format("%s, date=%s, price=%.2f", itemID, m.getDate().toString(), m.getLatestPrice()));
			}
			
		}
		
		if(lowest!=null && lowest.compareTo(BigDecimal.ZERO)>0) {
			ratio = Functions.growthRate(price, lowest);
		}
		
		//logger.info(String.format("%s, lowest=%.2f, price=%.2f, ratio=%d", itemID, lowest, price,ratio));

		return ratio;
	}
	
	class Taoyans{
		List<Taoyan> ts = new ArrayList<Taoyan>();
		public Taoyans(Dimension a, Dimension b) {
			Map<String, Map<String,String>> ma = a.getResult();
			Map<String, Map<String,String>> mb = b.getResult();
			for(Map.Entry<String, Map<String,String>> entry : ma.entrySet()) {
				ts.add(new Taoyan(entry.getKey(),mb.get(entry.getKey()),entry.getValue()));
			}			
		}
		
		public List<Taoyan> getResult(){
			this.sort();
			
			return this.ts;
		}
		
		public void print() {
			this.sort();
			for(Taoyan t : ts) {
				System.out.println(t);
			}
		}
		
		private void sort() {
			Collections.sort(ts, new Comparator<Taoyan>() {

				@Override
				public int compare(Taoyan o1, Taoyan o2) {
					return o2.getRatio().compareTo(o1.getRatio());
				}
				
			});
		}
		
		public Set<String> getTopIDs(Integer top){
			this.sort();
			Set<String> ids = new HashSet<String>();
			for(int i=0; i<top; i++) {
				ids.addAll(this.ts.get(i).getDrum().keySet());
			}
			return ids;
		}
		
		public Set<String> getHighRatioIDs(Integer ratio){
			Set<String> ids = new HashSet<String>();
			for(int i=0; i<ts.size(); i++) {
				if(this.ts.get(i).getRatio() > ratio) {
					ids.addAll(this.ts.get(i).getDrum().keySet());
				}
			}
			return ids;
		}
	}
	
	class Taoyan{
		private String name;
		private Map<String,String> total;
		private Map<String,String> drum;
		
		public Taoyan(String name, Map<String,String> total, Map<String,String> drum) {
			super();
			this.name = name;
			this.total = total;
			this.drum = drum;
		}
		
		public String getName() {
			return this.name;
		}
		
		public Map<String,String> getDrum(){
			return this.drum;
		}

		public Integer getRatio() {
			Double a = drum.size()*1.0/total.size() * 100;
			return a.intValue();
		}

		@Override
		public String toString() {
			return String.format("%s total=%d drum=%d ratio=%.2f", name, total.size(), drum.size(), getRatio());
		}
		
	}

}
