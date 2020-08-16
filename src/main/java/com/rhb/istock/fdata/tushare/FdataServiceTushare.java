package com.rhb.istock.fdata.tushare;

import java.time.LocalDate;
import java.util.ArrayList;
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
	
	@Value("${makersFile}")
	private String makersFile;
	
	private Map<String,Map<String, Integer>> makers;
	private Set<String> makerNames;
	private Map<Integer,Set<String>> oks = null;
	
	protected static final Logger logger = LoggerFactory.getLogger(FdataServiceTushare.class);

	public Map<Integer,Set<String>> getOks() {
		if(oks!=null) {
			return oks;
		}
		oks = new HashMap<Integer,Set<String>>();
		List<String> ids = itemService.getItemIDs();
		int i=1;
		Set<Integer> years;
		Set<String> okIDs;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			years = this.getOKs(id);
			for(Integer year : years) {
				okIDs = oks.get(year);
				if(okIDs == null) {
					okIDs = new HashSet<String>();
				}
				okIDs.add(id);
				oks.put(year, okIDs);
			}
		}
		
		return oks;
	}

	private Set<Integer> getOKs(String itemID){
		Set<Integer> years = new HashSet<Integer>();
		Map<String,FinaIndicator> indicators = fdataRepositoryTushare.getIndicators(itemID);
		FinaIndicator i1,i2,i3;
		for(int year=2020; year>=2009; year--) {
			String date1 = Integer.toString(year-3) + "1231";
			String date2 = Integer.toString(year-2) + "1231";
			String date3 = Integer.toString(year-1) + "1231";
			
			i1 = indicators.get(date1);
			i2 = indicators.get(date2);
			i3 = indicators.get(date3);
			
			if(i1!=null && i2!=null && i3!=null
					&& i1.isOK() && i2.isOK() && i3.isOK()
					) {
				years.add(year);
			}			
		}
		return years;
	}
	
	public Map<String,Integer[]> getFinaGrowthRate(String itemID){
		Map<String,Integer[]> growthRates = new TreeMap<String,Integer[]>();
		Map<String,Fina> finas = fdataRepositoryTushare.getFinas(itemID);
		
		
		return growthRates;
	}
	
	public Set<String> getMakerNames() {
		Set<String> names = new HashSet<String> ();
		String source = FileTools.readTextFile(makersFile);
		String[] lines = source.split("\n");
		for(String line : lines) {
			names.add(line);
		}
		return names;
	}
	
	public Map<String,Map<String, Integer>> getFloatholders(){
		Set<String> mns = this.getMakerNames();
		if(this.makers==null || mns.size()!=makerNames.size()) {
			this.makerNames = mns;
			this.makers = this.getFloatholders(this.makerNames);
		}
		
		return this.makers;
		
	}
	
	/*
	 * 返回一只股票,包含了几个机构,每个机构有几个理财项目
	 * 
	 * 
	 */
	public Map<String,Map<String, Integer>> getFloatholders(Set<String> names){
		Map<String,Map<String, Integer>> holders = new HashMap<String,Map<String, Integer>>();
		Set<Floatholder> hs;
		String holderName;
		Integer count;
		Map<String,Integer> holder_count; 
		String[] end_dates = this.getEndDates();
		List<String> ids = itemService.getItemIDs();
/*		List<String> ids = new ArrayList<String>();
		ids.add("sz002428");
		ids.add("sh603718");
		ids.add("sh603399");
*/		int i=1;
		for(String id : ids) {
			//Progress.show(ids.size(),i++, " getFloatholders: " + id);//进度条
			hs = fdataRepositoryTushare.getFloatholders(id,end_dates);
			for(Floatholder h : hs) {
				holderName = h.getHolder_name();
				//System.out.println(holderName);
				for(String name : names) {
					if(holderName.contains(name)) {
						//System.out.println(id + "'s holder " + name);
						holder_count = holders.get(id);
						if(holder_count==null) {
							holder_count = new HashMap<String,Integer>();
							holder_count.put(name, 1);
							//System.out.println(holder_count);
						}else {
							//System.out.println(holder_count);
							count = holder_count.get(name);
							//System.out.println("count = " + count);
							count = count==null? 1 : ++count;
							holder_count.put(name, count);
						}

						holders.put(id, holder_count);
					}
				}
			}
		}
		
		return holders;
	}
	
	public Map<String,String> getFinaGrowthRatioInfo(Set<String> ids){
		Map<String, String> info =  new HashMap<String,String>();
		
		String[] endDate = this.getEndDates();
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
	
	private String[] getEndDates() {
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
			endDate[1] = Integer.toString(y) + "0331";
		}else if(m>=10){
			endDate[0] = Integer.toString(y) + "0930";
			endDate[1] = Integer.toString(y) + "0630";
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
