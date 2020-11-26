package com.rhb.istock.selector.drum;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.rhb.istock.item.Dimension;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.DdatasView;
import com.rhb.istock.selector.DimensionView;
import com.rhb.istock.selector.favor.FavorService;
import com.rhb.istock.selector.fina.FinaService;

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

	@Value("${drumsPath}")
	private String drumsPath;
	
	@Autowired
	@Qualifier("finaService")
	FinaService finaService;

	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorService;
	
	@Value("${dimensionsFile}")
	private String dimensionsFile;
	
	LocalDate tmp_taoyans_date = null;
	Map<String, Taoyans> tmp_taoyans = null;
	
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
	
	public Map<LocalDate, Integer> getDimension(String name){
		Map<LocalDate, Integer> result = new TreeMap<LocalDate, Integer>();
		String file = drumsPath + "/" + name + ".txt";
		String[] lines = FileTools.readTextFile(file).split("\n");

		String[] cols;
		LocalDate date;
		Integer ratio;
		for(String line : lines) {
			cols = line.split(",");
			date = LocalDate.parse(cols[0]);
			ratio = Integer.parseInt(cols[1]);
			result.put(date, ratio);
		}
		
		return result;
	}
	
	public void generateDimensions() {
		String itemID = "sh000001";
		LocalDate date = kdataService.getLatestMarketDate(itemID);
		this.generateDimensions(date);
	}
	
	public void generateDimensions(LocalDate date) {
		Map<String,Taoyans> dimensions = this.getDimensions(date);
		this.writeDimension(dimensions.get("industry").getResult(),date);
		this.writeDimension(dimensions.get("area").getResult(),date);
		this.writeDimension(dimensions.get("market").getResult(),date);
		this.writeDimension(dimensions.get("topic").getResult(),date);
		this.writeDimension(dimensions.get("average").getResult(),date);
	}
	
	private void writeDimension(List<Taoyan> taoyans, LocalDate date) {
		String file;
		String content;
		for(Taoyan t : taoyans) {
			file = drumsPath + "/" + t.getName() + ".txt";
			content = date.toString() + "," + t.getRatio().toString() + "\n";
			FileTools.writeTextFile(file, content, true);
		}
	}
	
	public Set<String> getDimensionNames(Set<String> itemIDs){
		Set<String> dns = new HashSet<String>();
		if(itemIDs==null) {
			return dns;
		}
		Item item;
		String[] topics;
		for(String id : itemIDs) {
			item = itemService.getItem(id);
			dns.add(item.getIndustry());
			dns.add(item.getArea());
			dns.add(item.getMarket());
			topics = itemService.getTopic(item.getItemID()).split("，");
			dns.addAll(Arrays.asList(topics));
		}
		return dns;
	}
	
	public Set<String> getDimensions(LocalDate date, String itemID){
		Set<String> results = new HashSet<String>();
		List<Taoyan> taoyans;
		Map<String,String> ids;
		String str;
		Map<String,Taoyans> dimensions = this.getThinDimensions(date);
		for(Map.Entry<String, Taoyans> entry : dimensions.entrySet()) {
			taoyans = entry.getValue().getResult();
			for(Taoyan taoyan : taoyans) {
				ids = taoyan.getAll();
				if(ids.containsKey(itemID)) {
					str = String.format("%s(%d)", taoyan.getName(),taoyan.getRatio());
					results.add(str);
				}
			}
		}
		return results;
	}
	
	private Map<String,Taoyans> getThinDimensions(LocalDate endDate){
		if(endDate.equals(this.tmp_taoyans_date)) {
			return this.tmp_taoyans;
		}
		
		Dimension industry_a = new Dimension();
		Dimension topic_a = new Dimension();
		
		List<String> ids = this.getDrums(endDate);
		//System.out.println(ids.size());
		Item item;
		for(String id: ids) {
			item = itemService.getItem(id);
			industry_a.put(item.getIndustry(), id, item.getNameWithCAGR());
			topic_a.put(itemService.getTopic(item.getItemID()).split("，"), id, item.getNameWithCAGR());
		}

		Map<String,Dimension> dimensions = itemService.getDimensions();

		Dimension industry_b = dimensions.get("industry");
		Dimension topic_b = dimensions.get("topic");

		Taoyans industry = new Taoyans(industry_a,industry_b);
		Taoyans topic = new Taoyans(topic_a,topic_b);

		//System.out.println("industry");
		//industry.print();
		//System.out.println("topic");
		//topic.print();
		
		this.tmp_taoyans = new HashMap<String,Taoyans>();
		this.tmp_taoyans_date = endDate;
		tmp_taoyans.put("topic", topic);
		tmp_taoyans.put("industry", industry);

		return tmp_taoyans;
	}
	
	private Map<String,Taoyans> getDimensions(LocalDate endDate){
		Dimension industry_a = new Dimension();
		Dimension area_a = new Dimension();
		Dimension market_a = new Dimension();
		Dimension topic_a = new Dimension();
		Dimension average_a = new Dimension();
		
		List<String> ids = this.getDrums(endDate);
		Integer recommendationCount;
		Item item;
		for(String id: ids) {
			item = itemService.getItem(id);
			
			recommendationCount = finaService.getRecommendationCount(id, endDate);
			item.setRecommendations(recommendationCount);
			
			industry_a.put(item.getIndustry(), id, item.getNameWithCAGR());
			area_a.put(item.getArea(), id, item.getNameWithCAGR());
			market_a.put(item.getMarket(), id, item.getNameWithCAGR());
			topic_a.put(itemService.getTopic(item.getItemID()).split("，"), id, item.getNameWithCAGR());
			average_a.put("average", id, item.getNameWithCAGR());
			
		}

		Map<String,Dimension> dimensions = itemService.getDimensions();

		Dimension industry_b = dimensions.get("industry");
		Dimension area_b = dimensions.get("area");
		Dimension market_b = dimensions.get("market");
		Dimension topic_b = dimensions.get("topic");
		Dimension average_b = dimensions.get("average");

		Taoyans industry = new Taoyans(industry_a,industry_b);
		Taoyans area = new Taoyans(area_a,area_b);
		Taoyans market = new Taoyans(market_a,market_b);
		Taoyans topic = new Taoyans(topic_a,topic_b);
		Taoyans average = new Taoyans(average_a,average_b);
		
		Map<String, Taoyans> ds = new HashMap<String,Taoyans>();
		ds.put("topic", topic);
		ds.put("industry", industry);
		ds.put("area", area);
		ds.put("market", market);
		ds.put("average", average);

		return ds;
	}
	
	public DdatasView getDdatasView(String name, LocalDate date){
		Map<LocalDate, Integer> datas = this.getDimension(name);
		Map<LocalDate, Integer> averages = this.getDimension("average");

		DdatasView dv = new DdatasView(name);
		for(Map.Entry<LocalDate, Integer> data : datas.entrySet()) {
			if(data.getKey().isBefore(date) || data.getKey().equals(date)) {
				dv.add(data.getKey().toString(), data.getValue().toString(), averages.get(data.getKey()).toString());
			}
		}
		
		return dv;
	}
	
	private Set<String> getDefaultDeimensions(){
		Set<String> ss = new HashSet<String>();
		String[] lines = FileTools.readTextFile(dimensionsFile).split("\n");
		for(String line : lines) {
			ss.add(line);
		}
		return ss;
	}
	
	public List<DimensionView> getDimensionView(LocalDate endDate, Set<String> holds, Integer ratio){
		Map<String,Taoyans> dimensions = this.getDimensions(endDate);
		Set<String> holdDimensions = this.getDimensionNames(holds);
		//List<Taoyan> averages = dimensions.get("average").getResult();
		//Integer ratio = averages.get(averages.size()-1).getRatio();
		//Integer ratio = 34;
		
		Set<String> defaultDimensions = this.getDefaultDeimensions();
		
		Taoyans topics = dimensions.get("topic");
		DimensionView topicView = this.getDimensionView("topic", "概念", topics.getResult(ratio, defaultDimensions), holds, holdDimensions);

		Taoyans industrys = dimensions.get("industry");
		DimensionView industryView = this.getDimensionView("industry", "行业", industrys.getResult(ratio, defaultDimensions), holds, holdDimensions);
		
		//Taoyans areas = dimensions.get("area");
		//DimensionView areaView = this.getDimensionView("area", "省市", areas.getResult(), holds, holdDimensions);

		//Taoyans markets = dimensions.get("market");
		//DimensionView marketView = this.getDimensionView("market", "市场", markets.getResult(), holds, holdDimensions);

		
		List<DimensionView> views = new ArrayList<DimensionView>();
		views.add(topicView);
		views.add(industryView);
		//views.add(areaView);
		//views.add(marketView);
		
		return views;
	}
	
	private DimensionView getDimensionView(String code, String name, List<Taoyan> ty, Set<String> holds, Set<String> holdDimensionNames) {
		DimensionView view = new DimensionView(code, name);
		//List<Taoyan> ty = industrys.getResult();
		Map<String,String> ids;
		for(Taoyan t : ty) {
			view.addBoard(t.getName(), t.getName(), t.getRatio(), holdDimensionNames.contains(t.getName())? "1" : "0");
			ids = t.getDrum();
			for(Map.Entry<String, String> entry : ids.entrySet()) {
				view.addItem(t.getName(), entry.getKey(), entry.getValue(),holds.contains(entry.getKey())? "1" : "0");
			}
		}
		return view;
	}
	
	private List<String> getLowest(LocalDate endDate, Integer top){
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		List<Muster>  ms = new ArrayList<Muster>(musters.values());
		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getLatestPrice().compareTo(o2.getLatestPrice());
			}
		});
		
		List<String> ids = new ArrayList<String>();
		for(int i=0; i<ms.size() && i<top; i++) {
			ids.add(ms.get(i).getItemID());
		}
		return ids;
	}

	public List<String> getDrumsOfLowest(LocalDate endDate, Integer top){
		List<String> results = new ArrayList<String>();
		List<String> ss = this.getDrums(endDate);
		if(ss!=null && ss.size()>0) {
			for(int i=0; i<ss.size() && i<top; i++) {
				results.add(ss.get(i));
			}
		}
		
/*		List<String> ids = this.getLowest(endDate, top);
		for(String id : ss) {
			if(ids.contains(id)) {
				results.add(id);
			}
		}*/
		return results;
		
		
	}
	
	public List<String> getDrumsOfHighRecommendations(LocalDate endDate, Integer top){
		List<String> results = new ArrayList<String>();
		List<String> ids = finaService.getHighRecommendations(endDate, top, 13);
		List<String> ss = this.getDrums(endDate);
		for(String id : ss) {
			if(ids.contains(id)) {
				results.add(id);
			}
		}
		//System.out.format("there are %d drums and %d high cagrs --> %d high_cagr_drums\n", ss.size(),ids.size(),results.size());
		return results;
	}
	
	public List<String> getDrumsOfHighCAGR(LocalDate endDate, Integer top){
		List<String> results = new ArrayList<String>();
		List<String> ids = finaService.getHighCAGR(top);
		List<String> ss = this.getDrums(endDate);
		for(String id : ss) {
			if(ids.contains(id)) {
				results.add(id);
			}
		}
		//System.out.format("there are %d drums and %d high cagrs --> %d high_cagr_drums\n", ss.size(),ids.size(),results.size());
		return results;
	}
	
	public Set<String> getDrumsOfDimensions(LocalDate endDate, Integer lRatio, Integer hRatio){
		Map<String,Taoyans> dimensions = this.getThinDimensions(endDate);
		Set<String> industrys = dimensions.get("industry").getHighRatioIDs(lRatio, hRatio);
		//System.out.println("strong industrys.size=" + industrys.size());
		Set<String> topics = dimensions.get("topic").getHighRatioIDs(lRatio, hRatio);
		//System.out.println("strong topics.size=" + topics.size());
		Set<String> oks = new HashSet<String>();
		oks.addAll(industrys);
		oks.addAll(topics);
		return oks;
	}
	
	public List<String> getDrumsOfTopDimensions(LocalDate endDate,Set<String> holds, Integer lRatio, Integer hRatio, boolean newb){
		Set<String> holdDimensions = this.getDimensionNames(holds);
		Map<String,Taoyans> dimensions = this.getDimensions(endDate);
		
		//List<Taoyan> averages = dimensions.get("average").getResult();
		//Integer ratio = averages.get(averages.size()-1).getRatio();
		//Integer ratio = 34;
		
		Set<String> industrys = dimensions.get("industry").getHighRatioIDs(lRatio, hRatio,holdDimensions);
		Set<String> topics = dimensions.get("topic").getHighRatioIDs(lRatio, hRatio,holdDimensions);
		//Set<String> markets = dimensions.get("market").getHighRatioIDs(ratio);
		//Set<String> areas = dimensions.get("area").getHighRatioIDs(ratio);

		//Set<String> favors = favorService.getFavors().keySet();
		
		Set<String> oks = new HashSet<String>();
		oks.addAll(industrys);
		oks.addAll(topics);
		//oks.addAll(favors);
/*		for(String id : industrys) {
			if(topics.contains(id) && markets.contains(id) && areas.contains(id)) {
				oks.add(id);
			}
		}*/
		
		List<String> recommendations = finaService.getHighRecommendations(endDate, 10000, 13); //推荐买入
		Map<String,Muster> musters = kdataService.getMusters(endDate);
		List<Muster> ok_musters = new ArrayList<Muster>();
		Muster muster;
		for(String id : oks) {
			muster = musters.get(id);
			if(muster!=null 
					&& recommendations.contains(id)) {
				ok_musters.add(muster);
			}
		}
		
		Collections.sort(ok_musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				//return o1.getLatestPrice().compareTo(o2.getLatestPrice());
				
				if(o1.getLNGap().compareTo(o2.getLNGap())==0){
					return o1.getLatestPrice().compareTo(o2.getLatestPrice());
				}else {
					return o1.getLNGap().compareTo(o2.getLNGap());
				}
			}
		});	
		
		List<String> results = new ArrayList<String>();
		for(Muster m : ok_musters) {
			if(newb) {
				if(m.isUpBreaker()) {
					results.add(m.getItemID());
				}
			}else {
				results.add(m.getItemID());
			}
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
				
/*				Collections.sort(ms, new Comparator<Muster>() {
					@Override
					public int compare(Muster o1, Muster o2) {
						//return o1.getLatestPrice().compareTo(o2.getLatestPrice());

						if(o1.getN21Gap().compareTo(o2.getN21Gap())==0){
							return o1.getHLGap().compareTo(o2.getHLGap());
						}else {
							return o1.getN21Gap().compareTo(o2.getN21Gap());
						}
					}
				});*/
				
				Integer sseiRatio = kdataService.getSseiRatio(endDate, previous_period);
				Integer ratio;
				Muster m,p,b;
				for(int i=0; i<ms.size(); i++) {
					m = ms.get(i);
					//Progress.show(ms.size(), j++, m.getItemID());
					if(m!=null) {
						p = previous.get(0).get(m.getItemID());
						b = b_musters.get(m.getItemID());
						if(p!=null && b!=null) {
							ratio = this.getRatio(previous,m.getItemID(),m.getLatestPrice());
							if(ratio >= sseiRatio
								&& ratio >0
								&& m.getHLGap()<=55
								&& m.isUpAve(21)
								&& m.getAveragePrice21().compareTo(p.getAveragePrice21())==1
								//&& m.getAveragePrice21().compareTo(b.getAveragePrice21())==1
								) {
								ids.add(m.getItemID());
							}
							//logger.info(String.format("itemID=%s,sseiRatio=%d, ratio=%d, m.avp=%.2f, p.avp=%.2f, b.avp=%.2f,flag=%b", m.getItemID(),sseiRatio, ratio,m.getAveragePrice21(),p.getAveragePrice21(),b.getAveragePrice21(),flag));
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
		
		public List<Taoyan> getResult(Integer ratio, Set<String> defaultDimensions){
			this.sort();
			
			List<Taoyan> result = new ArrayList<Taoyan>();
			for(int i=0; i<this.ts.size(); i++) {
				if(this.ts.get(i).getRatio()>=ratio || defaultDimensions.contains(this.ts.get(i).getName())) {
					result.add(this.ts.get(i));
				}
			}
			
			return result;
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
					return (o2.getRatio()).compareTo(o1.getRatio());
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
		
		public Set<String> getHighRatioIDs(Integer lRatio, Integer hRatio){
			Set<String> ids = new HashSet<String>();
			for(int i=0; i<ts.size(); i++) {
				if(this.ts.get(i).getRatio()>=lRatio && this.ts.get(i).getRatio()<=hRatio) {
					ids.addAll(this.ts.get(i).getDrum().keySet());
				}
			}
			//System.out.println("ts.size=" + ts.size());
			//System.out.println(">" + ratio + "的个数" + ids.size());
			return ids;
		}
		
		public Set<String> getHighRatioIDs(Integer lRatio, Integer hRatio, Set<String> excludes){
			Set<String> ids = new HashSet<String>();
			Taoyan ty;
			for(int i=0; i<ts.size(); i++) {
				ty = this.ts.get(i);
				if(!excludes.contains(ty.getName()) && ty.getRatio()>=lRatio && ty.getRatio()<=hRatio) {
					ids.addAll(ty.getDrum().keySet());
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
		
		public Map<String,String> getAll(){
			return this.total;
		}

		public Integer getRatio() {
			Double a = drum.size()*1.0/total.size() * 100;
			return a.intValue();
		}
		
		public Integer getSize() {
			return total.size();
		}

		@Override
		public String toString() {
			return String.format("%s total=%d drum=%d ratio=%d, drums:[%s]", name, total.size(), drum.size(), getRatio(),drum.toString());
		}
		
	}

}
