package com.rhb.istock.selector.hua;

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
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.selector.SelectorService;

@Service("huaService")
public class HuaService {
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
	@Value("${huaFile}")
	private String huaFile;	
	
	@Value("${huaPotentialFile}")
	private String huaPotentialFile;

	@Value("${mcstFile}")
	private String mcstFile;
	
	private TreeMap<LocalDate,Set<String>> huas = null;
	
	private void init() {
		huas = new TreeMap<LocalDate,Set<String>>();
		String[] lines = FileTools.readTextFile(huaFile).split("\n");
		String[] columns;
		Set<String> ids;
		LocalDate date;
		for(String line : lines) {
			columns = line.split(",");
			date = LocalDate.parse(columns[0]);
			ids = new HashSet<String>();
			for(int i=1; i<columns.length; i++) {
				ids.add(columns[i]);
			}
			huas.put(date, ids);
		}
	}
	
	public void generateHuaPotentials(LocalDate date) {
		Integer period = 21;
		BigDecimal mcst_ratio = new BigDecimal(0.13);
		this.generateHuaPotentials(date,period, mcst_ratio, false);
	}

	
	public void generateHuaPotentials(LocalDate endDate, Integer period, BigDecimal mcst_ratio, boolean append) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate Hua potentials begin......");

		huas = new TreeMap<LocalDate,Set<String>>();
		List<LocalDate> dates;
		Set<String> tmp;
		
		List<String> ids = itemService.getItemIDs();
		Set<String> excludes = new HashSet<String>();
		excludes.add("sh600240");  //退市华业;
		
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			if(!excludes.contains(id)) {
				dates = selectorService.getHuaFirstPotentials(id, endDate, period, mcst_ratio);
				for(LocalDate date : dates) {
					if(huas.containsKey(date)) {
						tmp = huas.get(date);
					}else {
						tmp = new HashSet<String>();
						huas.put(date, tmp);
					}
					tmp.add(id);
				}
			}
		}
		
		FileTools.writeTextFile(huaPotentialFile, huas, append);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("generate Hua potential 用时：" + used + "秒");          
	}
	
	public void generateHuaFirst(LocalDate beginDate, LocalDate endDate, Integer period, BigDecimal mcst_ratio, BigDecimal volume_r, boolean append) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate Hua begin......");

		huas = new TreeMap<LocalDate,Set<String>>();
		List<LocalDate> dates;
		Set<String> tmp;
		
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			dates = selectorService.getHuaFirst(id, beginDate, endDate, period, mcst_ratio,volume_r);
			for(LocalDate date : dates) {
				if(huas.containsKey(date)) {
					tmp = huas.get(date);
				}else {
					tmp = new HashSet<String>();
					huas.put(date, tmp);
				}
				tmp.add(id);
			}
		}
		
		FileTools.writeTextFile(huaFile, huas, append);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("generate Hua 用时：" + used + "秒");          
	}
	
	/*
	 * 收盘后才能筛选出符合花式1号的股票
	 * 因此根据当前日期得到的是上一交易日的符合花式1号的股票
	 */
	public Set<String> getHua(LocalDate date){
		if(huas == null) {
			this.init();
		}
		
		Set<String> ids = null;
		for(int i=0; i<this.huas.size(); i++) {
			date = date.minusDays(1);
			ids = huas.get(date);
			if(ids!=null) {
				break;
			}
		}
		
		return ids;
	}
	
	public void generateMcstBreakers(LocalDate endDate, Integer count, BigDecimal ratio) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate Mcst Breakers begin......");

		List<Breaker> breakers = new ArrayList<Breaker>();
		BigDecimal mcst;
		
		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(), i++, id);
			mcst = selectorService.getMCST(id, endDate, count, ratio);
			if(mcst != null) {
				breakers.add(new Breaker(id,mcst));
			}
		}
		
		Collections.sort(breakers, new Comparator<Breaker>() {
			@Override
			public int compare(Breaker o1, Breaker o2) {
				return o1.getMcst().compareTo(o2.getMcst());
			}
		});
		
		StringBuffer sb = new StringBuffer(endDate.toString());
		sb.append(",");
		for(Breaker b : breakers) {
			sb.append(b.getItemId());
			sb.append(",");
			System.out.println(b.toString());
		}
		sb.replace(sb.length()-1, sb.length(), "\n");
		
		FileTools.writeTextFile(mcstFile, sb.toString(), false);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	class Breaker{
		private String itemId;
		private BigDecimal mcst;
		
		public Breaker(String itemId, BigDecimal mcst) {
			this.itemId = itemId;
			this.mcst = mcst;
		}

		public String getItemId() {
			return itemId;
		}

		public void setItemId(String itemId) {
			this.itemId = itemId;
		}

		public BigDecimal getMcst() {
			return mcst;
		}

		public void setMcst(BigDecimal mcst) {
			this.mcst = mcst;
		}

		@Override
		public String toString() {
			return String.format("Breaker [itemId=%s, mcst=%.2f]",itemId, mcst);
		}
		
	}

}
