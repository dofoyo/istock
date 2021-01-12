package com.rhb.istock.producer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.fina.FinaService;

/*
 * 新高
 */

@Service("power")
public class Power implements Producer{
	protected static final Logger logger = LoggerFactory.getLogger(Power.class);
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@Value("${operationsPath}")
	private String operationsPath;
	
	private String fileName  = "Power.txt";
	
	@Override
	public Map<LocalDate, List<String>> produce(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		List<String> breakers;
		
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date = bDate; (date.isBefore(eDate) || date.equals(eDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, fileName + ", " + date.toString());
			breakers = this.produce(date,false);
			if(breakers!=null && breakers.size()>0) {
				results.put(date, breakers);
			}
		}
		
		FileTools.writeMapFile(this.getFileName(), results, false);
		
		return results;
	}

	@Override
	public Map<LocalDate, List<String>> getResults(LocalDate bDate, LocalDate eDate) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		
		Map<LocalDate, List<String>> results = new TreeMap<LocalDate, List<String>>();
		LocalDate date;
		for(Map.Entry<LocalDate, List<String>> entry : all.entrySet()) {
			date = entry.getKey();
			if((date.isAfter(bDate) || date.equals(bDate))
					&& (date.isBefore(eDate) || date.equals(eDate))) {
				results.put(date, entry.getValue());
			}
		}
		
		return results;
	}
	
	private String getFileName() {
		return operationsPath + fileName;
	}

	@Override
	public List<String> getResults(LocalDate date) {
		Map<LocalDate, List<String>> all = FileTools.readMapFile(this.getFileName());
		if(all.get(date)!=null) {
			return all.get(date);
		}else {
			return this.produce(date, false);
		}
	}

	@Override
	public List<String> produce(LocalDate endDate, boolean write) {
		List<LocalDate> ds = new ArrayList<LocalDate>();
		List<LocalDate> dates = kdataService.getMusterDates();
		Collections.sort(dates, new Comparator<LocalDate>() {
			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				return o2.compareTo(o1); //倒叙
			}
		});
		
		Integer period = 21;//21天
		LocalDate date;
		for(int i=0; i<dates.size() && ds.size()<=period; i++) {
			date = dates.get(i);
			if(date.isBefore(endDate) || date.isEqual(endDate)) {
				ds.add(date);
			}
		}
		
		Collections.sort(ds, new Comparator<LocalDate>() {
			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				return o1.compareTo(o2); //正叙
			}
		});
		
		HLs hls = new HLs();
		Map<String,Muster> tmps;
		for(LocalDate d : ds) {
			tmps = kdataService.getMusters(d);
			for(Muster m : tmps.values()) {
				hls.put(m.getItemID(), d, m.getLatestPrice());
				if(m.isUpBreaker()) {
					hls.setBreaker(m.getItemID());
				}
			}
		}
		
		Map<String,Muster> endDateMusters = kdataService.getMusters(ds.get(0));
		List<Muster> ms = new ArrayList<Muster>();
		List<String> ids = hls.getResults();
		Muster muster;
		for(String id : ids) {
			muster = endDateMusters.get(id);
			if(muster!=null) {
				ms.add(muster);
			}
		}
		
		Collections.sort(ms, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				Integer rate1 = Functions.growthRate(o1.getLatestPrice(), o1.getHighest());
				Integer rate2 = Functions.growthRate(o2.getLatestPrice(), o2.getHighest());
				return rate1.compareTo(rate2);
			}
		});
		
		List<String> results = new ArrayList<String>();
		for(Muster m : ms) {
				results.add(m.getItemID());
		}

		if(results.size()>0 && write) {
			Map<LocalDate, List<String>> rs = new TreeMap<LocalDate, List<String>>();
			rs.put(endDate, results);
			FileTools.writeMapFile(this.getFileName(), rs, true);
		}

		return results;

	}
	
	class HLs{
		Map<String, HL> hls = new HashMap<String, HL>();
		public void put(String id, LocalDate date, BigDecimal price) {
			HL hl = this.hls.get(id);
			if(hl==null) {
				hl = new HL(id, date, price);
				this.hls.put(id, hl);
			}else {
				hl.setPrice(date, price);
			}
		}
		
		public void setBreaker(String id) {
			HL hl = this.hls.get(id);
			if(hl!=null) {
				hl.setBreaker(true);
			}
		}
		
		public List<String> getResults(){
			List<String> ids = new ArrayList<String>();
			List<HL> tmps = new ArrayList<HL>(this.hls.values());
			for(HL hl : tmps) {
				if(hl.isOK()) {
					ids.add(hl.getId());
				}
			}
			return ids;
		}
		
		class HL{
			String id;
			BigDecimal price;
			BigDecimal highest;
			LocalDate hDate;
			BigDecimal Lowest;
			LocalDate lDate;
			boolean isBreaker;
			
			public boolean isBreaker() {
				return isBreaker;
			}

			public void setBreaker(boolean isBreaker) {
				this.isBreaker = isBreaker;
			}

			public HL(String id,LocalDate date, BigDecimal price) {
				this.id = id;
				this.price = price;
				this.hDate = date;
				this.highest = price;
				this.lDate = date;
				this.Lowest = price;
				this.isBreaker = false;
			}
			
			public void setPrice(LocalDate date, BigDecimal price) {
				this.price = price;
				if(this.highest.compareTo(price)==-1) {
					this.hDate = date;
					this.highest = price;
				}else if(this.Lowest.compareTo(price)==1){
					this.lDate = date;
					this.Lowest = price;
				}
			}
			
			public boolean isOK() {
				return this.hDate.isAfter(this.lDate)
						&& this.isBreaker
						&& this.getUpRate()>=21
						&& this.getDownRate()<=-8;
			}

			public String getId() {
				return id;
			}

			public BigDecimal getHighest() {
				return highest;
			}

			public BigDecimal getLowest() {
				return Lowest;
			}
			
			public Integer getUpRate() {
				return Functions.growthRate(this.highest, this.Lowest);
			}
			
			public Integer getDownRate() {
				return Functions.growthRate(this.price, this.highest);
			}

			@Override
			public String toString() {
				return "HL [id=" + id + ", price=" + price + ", highest=" + highest + ", hDate=" + hDate + ", Lowest="
						+ Lowest + ", lDate=" + lDate + ", isBreaker=" + isBreaker + ", getUpRate()=" + getUpRate()
						+ ", getDownRate()=" + getDownRate() + "]";
			}
		}
	}

}
