package com.rhb.istock.trade.turtle.simulation.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.turtle.simulation.DailyItem;

@Service("simulationRepositoryImp")
public class SimulationRepositoryImp implements SimulationRepository {
	@Value("${dailyAmountTopsFile}")
	private String dailyAmountTopsFile;

	@Value("${avarageAmountTopsFile}")
	private String avarageAmountTopsFile;

	@Value("${bluechipsFile}")
	private String bluechipsFile;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Override
	public DailyItem getBluechips(Integer top, LocalDate beginDate, LocalDate endDate) {
		return getDailyItem(top,beginDate,endDate,bluechipsFile);
	}
	
	@Override
	public DailyItem getAvarageAmountTops(Integer top,LocalDate beginDate, LocalDate endDate) {
		return getDailyItem(top,beginDate,endDate,avarageAmountTopsFile);
	}
	
	@Override
	public DailyItem getDailyAmountTops(Integer top,LocalDate beginDate, LocalDate endDate) {
		return getDailyItem(top,beginDate,endDate,dailyAmountTopsFile);
	}
	
	private DailyItem getDailyItem(Integer top,LocalDate beginDate, LocalDate endDate,String file) {
		DailyItem dailyItem = new DailyItem();
		String[] lines = FileUtil.readTextFile(file).split("\n");
		String[] columns;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			date = LocalDate.parse(columns[0],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			for(int i=1; i<columns.length && i<=top; i++) {
				if(((date.isAfter(beginDate) || date.isEqual(beginDate)) 
						&& (date.isBefore(endDate)) || date.isEqual(endDate))){

							dailyItem.putItemID(date, columns[i]);
							
						}
			}
		}
		return dailyItem;
	}

	@Override
	public void generateDailyAmountTops(Integer top) {
		Map<LocalDate,TreeSet<Amount>> tops = new HashMap<LocalDate,TreeSet<Amount>>();
		Amount amount = null;
		TreeSet<Amount> amounts;

		boolean byCache = false;
		Kdata kdata;
		List<LocalDate> dates;
		
		List<Item> items = itemService.getItems();
		int d = 1;
		for(Item item : items) {
			Progress.show(items.size(), d++, item.getCode());
			
			kdata = kdataService.getDailyKdata(item.getItemID(), byCache);

			dates = new ArrayList<LocalDate>(kdata.getDates());
			for(LocalDate date : dates) {
					amount = new Amount(date,item.getItemID(),kdata.getBar(date).getAmount());
					if(tops.containsKey(date)) {
						amounts = tops.get(date);
					}else {
						amounts = new TreeSet<Amount>();
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
				for(Iterator<Amount> i = amounts.iterator() ; i.hasNext();) {
					amount = i.next();
					sb.append(amount.getCode());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		FileUtil.writeTextFile(dailyAmountTopsFile, sb.toString(), false);
		
	}

	@Override
	public void generateAvarageAmountTops(Integer top) {
		Integer duration = 89; //与openduration一致
		Map<LocalDate,TreeSet<Amount>> tops = new HashMap<LocalDate,TreeSet<Amount>>();
		Amount amount = null;
		TreeSet<Amount> amounts;

		boolean byCache = true;
		Kdata kdata;
		List<LocalDate> dates;
		BigDecimal total;
		
		List<Item> items = itemService.getItems();
		int d = 1;
		for(Item item : items) {
			Progress.show(items.size(), d++, item.getCode());
			
			kdata = kdataService.getDailyKdata(item.getItemID(), byCache);
			dates = new ArrayList<LocalDate>(kdata.getDates());
			for(LocalDate date : dates) {
				total = new BigDecimal(0);
				kdata = kdataService.getDailyKdata(item.getItemID(), date, duration, byCache);
				if(kdata.getDates().size()>0) {
					for(LocalDate thedate : kdata.getDates()) {
						total = total.add(kdata.getBar(thedate).getAmount());
					}
					
					amount = new Amount(date,item.getItemID(),total.divide(new BigDecimal(kdata.getDates().size()),BigDecimal.ROUND_HALF_UP));
					
					if(tops.containsKey(date)) {
						amounts = tops.get(date);
					}else {
						amounts = new TreeSet<Amount>();
						tops.put(date, amounts);
					}
					amounts.add(amount);
					
					if(amounts.size()>top) {
						amounts.pollLast();
					}
				}
			}
			kdataService.evictDailyKDataCache();
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
				for(Iterator<Amount> i = amounts.iterator() ; i.hasNext();) {
					amount = i.next();
					sb.append(amount.getCode());
					sb.append(",");
					//System.out.print(bar.getCode() + "(" + bar.getAva() + "),");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("\n");
				
				//System.out.println("\n");
			}
		}
		
		FileUtil.writeTextFile(avarageAmountTopsFile, sb.toString(), false);

	}


	
}
