package com.rhb.istock.selector.bt;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;

@Service("breakThroughService")
public class BreakThroughService {
	@Value("${breakersFile}")
	private String breakersFile;

	@Value("${latestBreakersFile}")
	private String latestBreakersFile;
	
	@Value("${tmpLatestBreakersFile}")
	private String tmpLatestBreakersFile;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	public List<String> getLatestBreakers(){
		return Arrays.asList(FileUtil.readTextFile(latestBreakersFile).split(","));
	}

	public void generateTmpLatestBreakers() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate latest breakers with latest kdata......");
		
		List<String> news = new ArrayList<String>();
		List<String> olds = new ArrayList<String>();
		List<String> outs = this.getTmpLatestBreakers();
		
		LocalDate date;
		Kdata kdata;
		Integer count = 89;
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(),false);
			date = kdataService.getLatestMarketDate();
			if(kdata.getBar(date)==null) {
				kdata.addBar(date, kdataService.getLatestMarketData(item.getItemID()));
			}
			
			if(kdata.isBreaker(count)) {
				if(outs.contains(item.getItemID())) {
					olds.add(item.getItemID());
				}else {
					news.add(item.getItemID());
				}
				outs.remove(item.getItemID());
			}
		}
		
		StringBuffer sb = new StringBuffer();
		sb.append(getLine("news",news));
		sb.append(getLine("olds",olds));
		sb.append(getLine("outs",outs));
		
		//System.out.println(sb.toString());
		FileUtil.writeTextFile(tmpLatestBreakersFile, sb.toString(), false);
		
		System.out.println("generate latest breakers with latest kdata done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	private String getLine(String str, List<String> ids) {
		StringBuffer sb = new StringBuffer();
		sb.append(str + ",");
		for(String id : ids) {
			sb.append(id);
			sb.append(",");				
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("\n");
		return sb.toString();
	}
	
	public void generateLatestBreakers() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate latest breakers ......");

		StringBuffer sb = new StringBuffer();

		Kdata kdata;
		Integer count = 89;
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(),false);
			if(kdata.isBreaker(count)) {
				sb.append(item.getItemID());
				sb.append(",");				
			}
		}
		sb.deleteCharAt(sb.length()-1);
		
		FileUtil.writeTextFile(latestBreakersFile, sb.toString(), false);
		
		System.out.println("generate latest breakers done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	public void generateBreakers() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate breakers ......");

		boolean byCache = false;
		List<LocalDate> dates;
		Kdata kdata;
		Box box;
		TreeMap<LocalDate, List<String>> breakIDs = new TreeMap<LocalDate,List<String>>();
		
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(), byCache);
			dates = kdata.getDates();
			box = new Box();
			for(LocalDate date : dates) {
				if(box.addBar(kdata.getBar(date))) {
					if(!breakIDs.containsKey(date)) {
						breakIDs.put(date, new ArrayList<String>());
					}
					breakIDs.get(date).add(item.getItemID());
				}
			}
		}
		
		StringBuffer sb = new StringBuffer();
		for(LocalDate date : breakIDs.keySet()) {
			sb.append(date);
			sb.append(",");
			for(String id : breakIDs.get(date)) {
				sb.append(id);
				sb.append(",");
			}
			sb.deleteCharAt(sb.length()-1);
			sb.append("\n");
		}
		FileUtil.writeTextFile(breakersFile, sb.toString(), false);
		
		System.out.println("generate breakers done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	class Box {
		boolean redo = false;
		Integer duration = 89;
		BigDecimal highest = new BigDecimal(0);
		List<Kbar> bars = new ArrayList<Kbar>();
		public boolean addBar(Kbar bar) {
			boolean bk = isBreak(bar.getClose());
			
			bars.add(bar);
			
			if(highest.compareTo(bar.getHigh())==-1) {
				highest = bar.getHigh();
			}
			
			if(bars.size()>duration) {
				if(highest.compareTo(bars.get(0).getHigh())==0) {
					redo = true;
				}
				bars.remove(0);
			}
			
			if(redo) {
				redo = false;
				highest = new BigDecimal(0);
				for(Kbar b : bars) {
					if(highest.compareTo(b.getHigh())==-1) {
						highest = b.getHigh();
					}
				}
			}
			
			return bk;
		}
		
		private boolean isBreak(BigDecimal price) {
			if(bars.size() < duration) {
				return false;
			}
			return price.compareTo(highest)==1 ? true : false;
		}
		
	}
	
	//@Cacheable("breaks")
	public Map<LocalDate, List<String>> getBreakers() {
		Map<LocalDate, List<String>> breaks = new TreeMap<LocalDate, List<String>>();
		
		String[] lines = FileUtil.readTextFile(breakersFile).split("\n");
		String[] columns;
		LocalDate date;
		List<String> ids;
		for(String line : lines) {
			columns = line.split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			ids = new ArrayList<String>();
			for(int i=1; i<columns.length; i++) {
				ids.add(columns[i]);
			}
			breaks.put(date, ids);
		}		
		
		return breaks;
	}
	
	public List<String> getTmpLatestBreakers() {
		List<String> breaks = new ArrayList<String>();
		
		String[] lines = FileUtil.readTextFile(tmpLatestBreakersFile).split("\n");
		String[] columns;
		for(String line : lines) {
			columns = line.split(",");
			if((columns[0].equals("news") || columns[0].equals("olds")) && columns.length>1) {
				for(int i=1; i<columns.length; i++) {
					breaks.add(columns[i]);
				}
			}
		}		
		return breaks;
	}

}
