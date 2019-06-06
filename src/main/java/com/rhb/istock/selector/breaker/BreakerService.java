package com.rhb.istock.selector.breaker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.aat.AverageAmountTopService;
import com.rhb.istock.selector.breaker.repository.BreakerRepository;
import com.rhb.istock.selector.dat.DailyAmountTopService;
import com.rhb.istock.selector.hlt.HighLowTopService;

@Service("breakerService")
public class BreakerService {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("breakerRepository")
	BreakerRepository breakerRepository;

	@Autowired
	@Qualifier("highLowTopServiceImp")
	HighLowTopService highLowTopService;

	@Autowired
	@Qualifier("dailyAmountTopServiceImp")
	DailyAmountTopService dailyAmountTopService;

	@Autowired
	@Qualifier("averageAmountTopServiceImp")
	AverageAmountTopService averageAmountTopService;
	
	Integer duration = 55;
	
	public void generateBreakersSortByAV(LocalDate beginDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateBreakersSortByAV ......");
		
		boolean cache = false;
		TreeMap<LocalDate, List<String>> breakerIDs = this.getBreakerIDs();
		int i=1;
		for(Map.Entry<LocalDate, List<String>> entry : breakerIDs.entrySet()) {
			Progress.show(breakerIDs.size(),i++, entry.getKey().toString());

			if(entry.getKey().isAfter(beginDate)) {
				breakerIDs.put(entry.getKey(), averageAmountTopService.sort(entry.getValue(), entry.getKey(), duration, cache));
			}
		}
		
		breakerRepository.saveBreakersSortByAV(breakerIDs);

		System.out.println("generateBreakersSortByAV done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	public void generateBreakersSortByDT(LocalDate beginDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateBreakersSortByDT ......");
		
		TreeMap<LocalDate, List<String>> breakerIDs = this.getBreakerIDs();
		int i=1;
		for(Map.Entry<LocalDate, List<String>> entry : breakerIDs.entrySet()) {
			Progress.show(breakerIDs.size(),i++, entry.getKey().toString());
			
			if(entry.getKey().isAfter(beginDate)) {
				breakerIDs.put(entry.getKey(), dailyAmountTopService.sort(entry.getValue(), entry.getKey(), duration, false));
			}
		}
		
		//System.out.println(soted);
		
		breakerRepository.saveBreakersSortByDT(breakerIDs);

		System.out.println("generateBreakersSortByDT done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	public void generateBreakersSortByHL(LocalDate beginDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateBreakersSortByHL ......");
		
		TreeMap<LocalDate, List<String>> breakerIDs = this.getBreakerIDs();
		int i=1;
		for(Map.Entry<LocalDate, List<String>> entry : breakerIDs.entrySet()) {
			Progress.show(breakerIDs.size(),i++, entry.getKey().toString());

			if(entry.getKey().isAfter(beginDate)) {
				breakerIDs.put(entry.getKey(), highLowTopService.sort(entry.getValue(), entry.getKey(), duration, false));
			}
		}
		
		breakerRepository.saveBreakersSortByHL(breakerIDs);

		System.out.println("generateBreakersSortByHL done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	public TreeMap<LocalDate, List<String>> getBreakersSortByAV(Integer top, LocalDate beginDate, LocalDate endDate){
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids.put(date, this.getBreakersSortByAV(top,date));
		}
		return ids;
	}

	public List<String> getBreakersSortByAV(Integer top, LocalDate date) {
		List<String> tops = new ArrayList<String>();
		List<String> all = breakerRepository.getBreakersSortByAV().get(date);
		if(all != null && all.size()>0) {
			for(int i=0; i<top && i<all.size(); i++) {
				tops.add(all.get(i));
			}
		}
		return tops;
	}
	
	public TreeMap<LocalDate, List<String>> getBreakersSortByDT(){
		return breakerRepository.getBreakersSortByDT();
	}
	
	public TreeMap<LocalDate, List<String>> getBreakersSortByDT(Integer top, LocalDate beginDate, LocalDate endDate){
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids.put(date, this.getBreakersSortByDT(top,date));
		}
		return ids;
	}
	
	public List<String> getBreakersSortByDT(Integer top, LocalDate date) {
		List<String> tops = new ArrayList<String>();
		List<String> all = breakerRepository.getBreakersSortByDT().get(date);
		if(all != null && all.size()>0) {
			for(int i=0; i<top && i<all.size(); i++) {
				tops.add(all.get(i));
			}
		}
		return tops;
	}
	
	
	public TreeMap<LocalDate, List<String>> getBreakersSortByHL(){
		return breakerRepository.getBreakersSortByHL();
	}
	
	public TreeMap<LocalDate, List<String>> getBreakersSortByHL(Integer top, LocalDate beginDate, LocalDate endDate){
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids.put(date, this.getBreakersSortByHL(top,date));
		}
		return ids;
	}
	
	public List<String> getBreakersSortByHL(Integer top, LocalDate date) {
		List<String> tops = new ArrayList<String>();
		List<String> all = breakerRepository.getBreakersSortByHL().get(date);
		if(all != null && all.size()>0) {
			for(int i=0; i<top && i<all.size(); i++) {
				tops.add(all.get(i));
			}
		}
		return tops;
	}
	
	
	public TreeMap<LocalDate, List<String>> getBreakerIDs() {
		return breakerRepository.getBreakerIDs();
	}
	
	public void generateBreakers() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate breakers ......");

		boolean byCache = false;
		List<LocalDate> dates;
		Kdata kdata;
		Box box;
		TreeMap<LocalDate, List<String>> breakers = new TreeMap<LocalDate,List<String>>();
		
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(), byCache);
			dates = kdata.getDates();
			box = new Box();
			for(LocalDate date : dates) {
				if(box.addBar(kdata.getBar(date))) {
					if(!breakers.containsKey(date)) {
						breakers.put(date, new ArrayList<String>());
					}
					breakers.get(date).add(item.getItemID());
				}
			}
		}
		
		breakerRepository.saveBreakers(breakers);
		
		
		System.out.println("generate breakers done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	class Box {
		boolean redo = false;
		Integer duration = 55;
		BigDecimal highestPrice = new BigDecimal(0);
		LocalDate highestDate = null;
		List<Kbar> bars = new ArrayList<Kbar>();
		public boolean addBar(Kbar bar) {
			boolean bk = isBreaker(bar.getClose(), bar.getDate());
			
			bars.add(bar);
			
			if(highestPrice.compareTo(bar.getHigh())==-1) {
				highestPrice = bar.getHigh();
				highestDate = bar.getDate();
			}
			
			if(bars.size()>duration) {
				if(highestPrice.compareTo(bars.get(0).getHigh())==0) {
					redo = true;
				}
				bars.remove(0);
			}
			
			if(redo) {
				redo = false;
				highestPrice = new BigDecimal(0);
				for(Kbar b : bars) {
					if(highestPrice.compareTo(b.getHigh())==-1) {
						highestPrice = b.getHigh();
						highestDate = b.getDate();
					}
				}
			}
			
			return bk;
		}
		
		private boolean isBreaker(BigDecimal price, LocalDate date) {
			if(bars.size() < duration) {
				return false;
			}
			//return (price.compareTo(highestPrice)==1 && date.toEpochDay()-highestDate.toEpochDay()>3) ? true : false;
			return (price.compareTo(highestPrice)==1) ? true : false;
		}
		
	}
	
}
