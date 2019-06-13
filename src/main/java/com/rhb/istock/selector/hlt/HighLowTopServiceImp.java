package com.rhb.istock.selector.hlt;

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
import com.rhb.istock.selector.hlt.repository.HighLowTopRepository;

@Service("highLowTopServiceImp")
public class HighLowTopServiceImp implements HighLowTopService {
	@Value("${highLowTopsFile}")
	private String highLowTopsFile;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("highLowTopRepositoryImp")
	HighLowTopRepository highLowTopRepository;
	
	@Override
	public List<String> getLatestHighLowTops(Integer top) {
		List<String> tops = new ArrayList<String>();

		List<Muster> musters = kdataService.getLatestMusters();
		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}
		});
		
		for(int i=0; i<top; i++) {
			//System.out.println(musters.get(i).getItemID() + ": " + musters.get(i).getHLGap());
			tops.add(musters.get(i).getItemID());
		}
		
		return tops;
	}

	class HighLowGap implements Comparable<HighLowGap>{
		String itemID;
		Integer highLowGap;
		boolean ascend = true;

		public HighLowGap(String itemID, Integer highLowGap) {
			this.itemID = itemID;
			this.highLowGap = highLowGap;
		}
		
		public String getItemID() {
			return itemID;
		}

		public void setItemID(String itemID) {
			this.itemID = itemID;
		}

		public Integer getHighLowGap() {
			return highLowGap;
		}

		public void setHighLowGap(Integer highLowGap) {
			this.highLowGap = highLowGap;
		}

		@Override
		public int compareTo(HighLowGap o) {
			if(ascend) {
				return this.highLowGap.compareTo(o.getHighLowGap());
			}else {
				return o.getHighLowGap().compareTo(this.highLowGap);
			}
		}

		@Override
		public String toString() {
			return "HighLowGap [itemID=" + itemID + ", highLowGap=" + highLowGap + "]";
		}
		
	}

	@Override
	public List<String> getHighLowTops(Integer top, LocalDate date) {
		List<String> tops = new ArrayList<String>();
		List<String> all = highLowTopRepository.getHighLowTops().get(date);
		if(all != null && all.size()>0) {
			for(int i=0; i<top && i<all.size(); i++) {
				tops.add(all.get(i));
			}
		}
		return tops;
	}

	@Override
	public TreeMap<LocalDate, List<String>> getHighLowTops(Integer top, LocalDate beginDate, LocalDate endDate) {
		TreeMap<LocalDate, List<String>> ids = new TreeMap<LocalDate, List<String>>();
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			ids.put(date, this.getHighLowTops(top,date));
		}
		return ids;
	}

	@Override
	public void generateHighLowTops() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate high low tops ......");

		Integer top = 100;
		Integer duration = 55; //与openduration一致
		Map<LocalDate,TreeSet<HighLowGap>> tops = new HashMap<LocalDate,TreeSet<HighLowGap>>();
		HighLowGap gap = null;
		TreeSet<HighLowGap> gaps;

		boolean byCache = true;
		Kdata kdata;
		List<LocalDate> dates;
		
		List<Item> items = itemService.getItems();
		int d = 1;
		for(Item item : items) {
			Progress.show(items.size(), d++, item.getCode());
			
			kdata = kdataService.getKdata(item.getItemID(), byCache);
			dates = new ArrayList<LocalDate>(kdata.getDates());
			for(LocalDate date : dates) {
				kdata = kdataService.getKdata(item.getItemID(), date, duration, byCache);
				gap = new HighLowGap(item.getItemID(),kdata.getHighLowGap());
				
				if(tops.containsKey(date)) {
					gaps = tops.get(date);
				}else {
					gaps = new TreeSet<HighLowGap>();
					tops.put(date, gaps);
				}
				gaps.add(gap);
				
				if(gaps.size()>top) {
					gaps.pollLast();
				}
			}
			kdataService.evictKDataCache();
		}
		
		StringBuffer sb = new StringBuffer();
		dates = new ArrayList<LocalDate>(tops.keySet());
		Collections.sort(dates);
		for(LocalDate theDate : dates) {
			gaps = tops.get(theDate);
			if(gaps.size()==top) {
				sb.append(theDate);
				sb.append(",");
				//System.out.print(theDate + "(" + ts.size() + "/" + top + "):");
				for(Iterator<HighLowGap> i = gaps.iterator() ; i.hasNext();) {
					gap = i.next();
					sb.append(gap.getItemID());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		FileTools.writeTextFile(highLowTopsFile, sb.toString(), false);

		
		
		System.out.println("generate high low tops done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}

	@Override
	public List<String> sort(List<String> itemIDs,LocalDate date, Integer duration, boolean byCache) {
		List<String> ids = new ArrayList<String>();
		
		Kdata kdata;
		HighLowGap gap = null;
		TreeSet<HighLowGap> gaps = new TreeSet<HighLowGap>();
		
		int d=1;
		for(String id : itemIDs) {
			kdata = kdataService.getKdata(id, date, duration, byCache);
			gap = new HighLowGap(id,kdata.getHighLowGap());
			gaps.add(gap);

			Progress.show(itemIDs.size(), d++, id+","+kdata.getHighLowGap().toString());
		}
		
		for(HighLowGap hlg : gaps) {
			ids.add(hlg.getItemID());
		}
		
		return ids;
	}

}
