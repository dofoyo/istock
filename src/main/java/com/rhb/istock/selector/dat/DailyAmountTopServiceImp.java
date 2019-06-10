package com.rhb.istock.selector.dat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;
import com.rhb.istock.selector.dat.repository.DailyAmountTopRepository;

@Service("dailyAmountTopServiceImp")
public class DailyAmountTopServiceImp implements DailyAmountTopService {
	@Value("${dailyAmountTopsFile}")
	private String dailyAmountTopsFile;
	
	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;	
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("dailyAmountTopRepositoryImp")
	DailyAmountTopRepository dailyAmountTopRepository;	
	
	@Override
	public List<String> getLatestDailyAmountTops(Integer top) {
		return kdataRealtimeSpider.getLatestDailyTop(top);
	}


	@Override
	public List<String> getDailyAmountTops(Integer top, LocalDate date) {
		List<String> tops = new ArrayList<String>();
		List<String> all = dailyAmountTopRepository.getDailyAmountTops().get(date);
		if(all != null && all.size()>0) {
			for(int i=0; i<top && i<all.size(); i++) {
				tops.add(all.get(i));
			}
		}
		return tops;
	}


	@Override
	public void generateDailyAmountTops() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate daily amount tops ......");

		Integer top=100;
		Map<LocalDate,TreeSet<DailyAmount>> tops = new HashMap<LocalDate,TreeSet<DailyAmount>>();
		DailyAmount amount = null;
		TreeSet<DailyAmount> amounts;

		boolean byCache = false;
		Kdata kdata;
		List<LocalDate> dates;
		
		List<Item> items = itemService.getItems();
		int d = 1;
		for(Item item : items) {
			Progress.show(items.size(), d++, item.getCode());
			
			kdata = kdataService.getKdata(item.getItemID(), byCache);

			dates = new ArrayList<LocalDate>(kdata.getDates());
			for(LocalDate date : dates) {
				amount = new DailyAmount(date,item.getItemID(),kdata.getBar(date).getAmount());
				if(tops.containsKey(date)) {
					amounts = tops.get(date);
				}else {
					amounts = new TreeSet<DailyAmount>();
					tops.put(date, amounts);
				}
				amounts.add(amount);
				
				if(amounts.size()>top) {
					amounts.pollLast();
				}
			}
		}
		
		StringBuffer sb = new StringBuffer();
		dates = new ArrayList<LocalDate>(tops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			amounts = tops.get(theDate);
			if(amounts.size()==top) {
				sb.append(theDate);
				sb.append(",");
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<DailyAmount> i = amounts.iterator() ; i.hasNext();) {
					amount = i.next();
					sb.append(amount.getItemID());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		FileTools.writeTextFile(dailyAmountTopsFile, sb.toString(), false);
	
		System.out.println("generate daily amount tops done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}


	@Override
	public TreeMap<LocalDate, List<String>> getDailyAmountTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids.put(date, this.getDailyAmountTops(top,date));
		}
		return ids;
	}


	@Override
	public List<String> sort(List<String> itemIDs, LocalDate date, Integer duration, boolean byCache) {
		List<String> ids = new ArrayList<String>();
		
		Kdata kdata;
		Kbar kbar;
		DailyAmount gap = null;
		TreeSet<DailyAmount> das = new TreeSet<DailyAmount>();
		
		int d=1;
		for(String id : itemIDs) {
			
			kbar = kdataService.getKbar(id, date, byCache);
			if(kbar != null) {
				gap = new DailyAmount(date,id,kbar.getAmount());
				das.add(gap);				
			}
			Progress.show(itemIDs.size(), d++, id);
		}
		
		for(DailyAmount da : das) {
			ids.add(da.getItemID());
		}
		
		return ids;
	}

}
