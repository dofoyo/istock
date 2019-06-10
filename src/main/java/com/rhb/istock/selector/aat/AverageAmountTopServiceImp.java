package com.rhb.istock.selector.aat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.aat.repository.AverageAmountTopRepository;

@Service("averageAmountTopServiceImp")
public class AverageAmountTopServiceImp implements AverageAmountTopService{
	@Value("${averageAmountTopsFile}")
	private String averageAmountTopsFile;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("averageAmountTopRepositoryImp")
	AverageAmountTopRepository averageAmountTopRepository;	
	
	@Override
	public List<String> getLatestAverageAmountTops(Integer top) {
		List<String> tops = new ArrayList<String>();

		List<Muster> musters = kdataService.getLastMusters();
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o2.getAverageAmount().compareTo(o1.getAverageAmount());
			}
		});
		
		for(int i=0; i<top; i++) {
			//System.out.println(musters.get(i).getItemID() + ": " + musters.get(i).getAverageAmount());
			tops.add(musters.get(i).getItemID());
		}
		
		return tops;
	}

	@Override
	public List<String> getAverageAmountTops(Integer top, LocalDate date) {
		List<String> tops = new ArrayList<String>();
		List<String> all = averageAmountTopRepository.getAverageAmountTops().get(date);
		if(all != null && all.size()>0) {
			for(int i=0; i<top && i<all.size(); i++) {
				tops.add(all.get(i));
			}
		}
		return tops;
	}

	@Override
	public void generateAverageAmountTops() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate average amount tops ......");

		Integer top = 100;
		Integer duration = 55; //与openduration一致
		Map<LocalDate,TreeSet<AverageAmount>> tops = new HashMap<LocalDate,TreeSet<AverageAmount>>();
		AverageAmount amount = null;
		TreeSet<AverageAmount> amounts;

		boolean byCache = true;
		Kdata kdata;
		List<LocalDate> dates;
		
		List<Item> items = itemService.getItems();
		int d = 1;
		for(Item item : items) {
			Progress.show(items.size(), d++, item.getCode());
			
			kdata = kdataService.getKdata(item.getItemID(), byCache);
			//System.out.println(kdata.getString());
			dates = new ArrayList<LocalDate>(kdata.getDates());
			for(LocalDate date : dates) {
				kdata = kdataService.getKdata(item.getItemID(), date, duration, byCache);
				//System.out.println(date + "," + duration);
				//System.out.println(kdata.getString());
				amount = new AverageAmount(date,item.getItemID(),kdata.getAverageAmount());
					
				if(tops.containsKey(date)) {
					amounts = tops.get(date);
				}else {
					amounts = new TreeSet<AverageAmount>();
					tops.put(date, amounts);
				}
				amounts.add(amount);
				
				if(amounts.size()>top) {
					amounts.pollLast();
				}
			}
			kdataService.evictKDataCache();
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
				for(Iterator<AverageAmount> i = amounts.iterator() ; i.hasNext();) {
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
		
		FileTools.writeTextFile(averageAmountTopsFile, sb.toString(), false);

		System.out.println("generate average amount tops done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}

	@Override
	public TreeMap<LocalDate, List<String>> getAverageAmountTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids.put(date, this.getAverageAmountTops(top,date));
		}
		return ids;
	}

	@Override
	public List<String> sort(List<String> itemIDs, LocalDate date, Integer duration, boolean byCache) {
		List<String> ids = new ArrayList<String>();
		
		Kdata kdata;
		AverageAmount gap = null;
		TreeSet<AverageAmount> gaps = new TreeSet<AverageAmount>();
		
		int d=1;
		for(String id : itemIDs) {
			kdata = kdataService.getKdata(id, date, duration, byCache);
			gap = new AverageAmount(date,id,kdata.getAverageAmount());
			gaps.add(gap);

			Progress.show(itemIDs.size(), d++, id+","+kdata.getAverageAmount().toString());
		}
		
		for(AverageAmount hlg : gaps) {
			ids.add(hlg.getItemID());
		}
		
		return ids;
	}


}
