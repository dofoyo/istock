package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.muster.MusterEntity;
import com.rhb.istock.kdata.muster.MusterRepository;
import com.rhb.istock.kdata.repository.KbarEntity;
import com.rhb.istock.kdata.repository.KdataEntity;
import com.rhb.istock.kdata.repository.KdataRepository;
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;
import com.rhb.istock.kdata.spider.KdataSpider;

@Service("kdataServiceImp")
public class KdataServiceImp implements KdataService{
	@Autowired
	@Qualifier("kdataRepositoryTushare")
	KdataRepository kdataRepository;
	
	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;

	@Autowired
	@Qualifier("kdataSpiderTushare")
	KdataSpider kdataSpider;

	@Autowired
	@Qualifier("kdataRepository163")
	KdataRepository kdataRepository163;
	
	@Autowired
	@Qualifier("kdataSpider163")
	KdataSpider kdataSpider163;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("musterRepositoryOfSimulation")
	MusterRepository musterRepositoryOfSimulation;

	@Autowired
	@Qualifier("musterRepositoryOfOperation")
	MusterRepository musterRepositoryOfOperation;
	
	private String szzs = "sh000001"; //上证指数
	private Integer openPeriod = 55;
	private Integer dropPeriod = 21;

	
	private KdataEntity getEntity(String itemID, boolean byCache) {
		KdataEntity entity = null;
		if(byCache) {
			entity = szzs.equals(itemID)? kdataRepository163.getKdataByCache(szzs): kdataRepository.getKdataByCache(itemID);
		}else {
			entity = szzs.equals(itemID)? kdataRepository163.getKdata(szzs): kdataRepository.getKdata(itemID);
		}
		return entity;
	}
	
	@Override
	public Kdata getKdata(String itemID, boolean byCache) {
		Kdata kdata = new Kdata(itemID);
		
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		//System.out.println(entity.getBarSize());
		LocalDate date;
		KbarEntity bar;
		for(Map.Entry<LocalDate, KbarEntity> entry : entity.getBars().entrySet()) {
				date = entry.getKey();
				bar = entry.getValue();
				kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity());
		}
		
		return kdata;
	}
	
	@Override
	public Kdata getKdata(String itemID, LocalDate endDate, Integer count, boolean byCache) {
		Kdata kdata = new Kdata(itemID);

		KbarEntity bar;
		LocalDate date = endDate.minusDays(1);
		
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		//System.out.println(entity.getBarSize());
		
		for(int i=0,j=0; i<count && j<entity.getBarSize(); j++) {
			bar = entity.getBar(date);
			if(bar!=null) {
				kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity());
				i++;
			}
			date = date.minusDays(1);
		}
		
		return kdata;
	}
	

	@Override
	public Kbar getLatestMarketData(String itemID) {
		Map<String, String> map = kdataRealtimeSpider.getLatestMarketData(itemID);
		
		if(map==null) return null;
		Kbar bar = new Kbar(map.get("open"), map.get("high"), map.get("low"), map.get("close"), map.get("amount"), map.get("quantity"),map.get("dateTime"));
		
		return bar;
	}

	@Override
	public LocalDate getLatestMarketDate() {
		return kdataRealtimeSpider.getLatestMarketDate();
	}

	@Override
	public void downKdatasAndFactors() throws Exception {
		System.out.println("KdataService.downKdatas..........");
		LocalDate latestDate = kdataRealtimeSpider.getLatestMarketDate();
		LocalDate lastDownDate = kdataRepository.getLastDate();
		List<LocalDate> dates = kdataRealtimeSpider.getCalendar(lastDownDate,latestDate);
		
		System.out.println("latest trade date " + latestDate);
		System.out.println("latest down date " + lastDownDate);
		System.out.println("dates bewteen latest trade date and down date " + dates);
		
		for(LocalDate date : dates) {
			System.out.println("download "+ date +" kdatas from tushare, please wait....... ");
			//kdataSpider.downKdatasAndFactors(date);
			kdataSpider.downKdatas(date);
			kdataSpider.downFactors(date);

			kdataSpider163.downKdata(szzs);
			
			evictKDataCache();
			
			this.generateLastMusters();
			
			System.out.println("downloaded "+ date +" kdatas from tushare.");
		}
		
		if(dates.size()>0) {
			kdataSpider.downFactors(latestDate);  //执行了这一步后，不需要再对收盘数据额外的复权处理了
		}

	}


	@Override
	public Kbar getKbar(String itemID, LocalDate date, boolean byCache) {
		Kbar kbar= null;
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		KbarEntity bar = entity.getBar(date);
		if(bar!=null) {
			kbar = new Kbar(bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity(),date);
		}else {
			//System.out.println(" kbar is null");
			//System.out.println(entity);
		}
		return kbar;
	}

	@Override
	public void evictKDataCache() {
		kdataRepository.evictKDataCache();
		kdataRepository163.evictKDataCache();
	}

	@Override
	public List<String> getLatestDailyTop(Integer top) {
		return kdataRealtimeSpider.getLatestDailyTop(top);
	}

	@Override
	public Kdata getKdata(String itemID, LocalDate endDate, boolean byCache) {
		if(endDate==null) return this.getKdata(itemID, byCache);
		
		Kdata kdata = new Kdata(itemID);
		
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		//System.out.println(entity.getBarSize());
		LocalDate date;
		KbarEntity bar;
		for(Map.Entry<LocalDate, KbarEntity> entry : entity.getBars().entrySet()) {
				date = entry.getKey();
				bar = entry.getValue();
				if(date.isBefore(endDate.plusDays(1))) {
					kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity());
				}
		}
		
		return kdata;

	}

	@Override
	public LocalDate getLastKdataDate() {
		return kdataRepository.getLastDate();
	}

	@Override
	public Kdata getLastKdata(String itemID, Integer count, boolean byCache) {
		Kdata kdata = new Kdata(itemID);

		KbarEntity bar;
		
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		//System.out.println(entity.getBarSize());
		LocalDate date = entity.getLastDate();
		
		for(int i=0,j=0; i<count && j<entity.getBarSize(); j++) {
			bar = entity.getBar(date);
			if(bar!=null) {
				kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity());
				i++;
			}
			date = date.minusDays(1);
		}
		
		return kdata;
	}

	@Override
	public void generateMusters() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateMusters ......");
		
		musterRepositoryOfSimulation.cleanMusters();
		
		Kdata kdata;
		List<LocalDate> dates;
		MusterEntity entity;

		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());//进度条
			
			kdata = this.getKdata(item.getItemID(), true);
			dates = kdata.getDates();
			
			int j=1;
			for(LocalDate date : dates) {
				//Progress.show(dates.size(),j++, date.toString());//进度条
				entity = this.getMusterEntity(item.getItemID(), date.plusDays(1), true);
				if(entity!=null) musterRepositoryOfSimulation.saveMuster(date, entity, openPeriod, dropPeriod);

				//if(j++>2) return;
			}
			
			this.evictKDataCache();
		}
		
		System.out.println("generateMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	
	private MusterEntity getMusterEntity(String itemID,LocalDate endDate, boolean cache) {
		Kdata kdata = this.getKdata(itemID,endDate,openPeriod,cache);
		if(kdata==null || kdata.getSize()<openPeriod) return null;  //此行很重要，涉及averageAmount和dropPrice的准确性
		
		BigDecimal latestPrice = null;
		BigDecimal highest = null;
		BigDecimal lowest = null;
		
		BigDecimal totalPrice = new BigDecimal(0);

		BigDecimal totalAmount = new BigDecimal(0);
		BigDecimal amount = null;
		
		List<LocalDate> dates = kdata.getDates();

		int dropStart = openPeriod-dropPeriod+1;
		int i=1;
		for(LocalDate date : dates) {
			if(latestPrice==null) {
				latestPrice = kdata.getBar(date).getClose();
				highest = kdata.getBar(date).getHigh();
				lowest = kdata.getBar(date).getLow();
				amount = kdata.getBar(date).getAmount();
			}else {
				latestPrice = kdata.getBar(date).getClose();
				highest = highest.compareTo(kdata.getBar(date).getHigh())==-1 ? kdata.getBar(date).getHigh() : highest;
				lowest = lowest.compareTo(kdata.getBar(date).getLow())==1 ? kdata.getBar(date).getLow() : lowest;
				amount = kdata.getBar(date).getAmount();
			}
			totalAmount = totalAmount.add(amount);
			//System.out.println(date + "," + amount + "," + totalAmount);
			
			if(i>=dropStart) {
				totalPrice = totalPrice.add(kdata.getBar(date).getClose());
			}
			
			i++;
		}
		
		BigDecimal averageAmount = totalAmount.divide(new BigDecimal(openPeriod),BigDecimal.ROUND_HALF_UP);
		BigDecimal dropPrice = totalPrice.divide(new BigDecimal(dropPeriod),BigDecimal.ROUND_HALF_UP);
		
		//System.out.println("av=" + averageAmount);
		return new MusterEntity(itemID,amount,averageAmount,highest,lowest,latestPrice,dropPrice);
		
	}

	@Override
	public List<Muster> getLastMusters() {
		List<Muster> musters = new ArrayList<Muster>();
		Muster muster;
		
		LocalDate lastDate = kdataRepository.getLastDate();

		List<MusterEntity> entities = musterRepositoryOfOperation.getMusters(lastDate);
		for(MusterEntity entity : entities) {
			muster = new Muster();
			muster.setItemID(entity.getItemID());
			muster.setAmount(entity.getAmount());
			muster.setAverageAmount(entity.getAverageAmount());
			muster.setHighest(entity.getHighest());
			muster.setLowest(entity.getLowest());
			muster.setPrice(entity.getPrice());
			
			musters.add(muster);
		}
		
		return musters;
	}


	@Override
	public void generateLastMusters() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateLastMusters ......");

		LocalDate date = kdataRepository.getLastDate();
		if(musterRepositoryOfOperation.isMustersExist(date)) {
			System.out.println("The last musters of " + date + " has already exists! pass!");
		}else {
			MusterEntity entity;

			List<Item> items = itemService.getItems();
			int i=1;
			for(Item item : items) {
				Progress.show(items.size(),i++, item.getItemID());//进度条
				
				entity = this.getMusterEntity(item.getItemID(), date.plusDays(1), false);
				if(entity!=null) musterRepositoryOfOperation.saveMuster(date, entity, openPeriod, dropPeriod);

			}
		}
		
		System.out.println("generateLastMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}

}
