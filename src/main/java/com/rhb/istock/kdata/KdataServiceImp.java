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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.repository.KbarEntity;
import com.rhb.istock.kdata.repository.KdataEntity;
import com.rhb.istock.kdata.repository.KdataMusterEntity;
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
	
	private String szzs = "sh000001"; //上证指数
	private Integer period = 55;

	
	private KdataEntity getEntity(String itemID, boolean byCache) {
		KdataEntity entity = null;
		if(byCache) {
			entity = szzs.equals(itemID)? kdataRepository163.getDailyKdataByCache(szzs): kdataRepository.getDailyKdataByCache(itemID);
		}else {
			entity = szzs.equals(itemID)? kdataRepository163.getDailyKdata(szzs): kdataRepository.getDailyKdata(itemID);
		}
		return entity;
	}
	
	@Override
	public Kdata getDailyKdata(String itemID, boolean byCache) {
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
	public Kdata getDailyKdata(String itemID, LocalDate endDate, Integer count, boolean byCache) {
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
		
		BigDecimal factor = this.getLatestFactors(itemID);
		if(factor!=null) {
			bar.setFactor(factor);
		}
			
		
		return bar;
	}

	@Override
	public LocalDate getLatestMarketDate() {
		return kdataRealtimeSpider.getLatestMarketDate();
	}

	@Override
	public void downKdatas() throws Exception {
		System.out.println("KdataService.downKdatas..........");
		LocalDate latestDate = kdataRealtimeSpider.getLatestMarketDate();
		LocalDate latestDownDate = kdataRepository.getLastDate();
		List<LocalDate> dates = kdataRealtimeSpider.getCalendar(latestDownDate,latestDate);
		
		System.out.println("latest trade date " + latestDate);
		System.out.println("latest down date " + latestDownDate);
		System.out.println("dates bewteen latest trade date and down date " + dates);
		
		for(LocalDate date : dates) {
			System.out.println("download "+ date +" kdatas from tushare, please wait....... ");
			kdataSpider.downKdata(date);

			kdataSpider163.downKdata(szzs);
			
			evictDailyKDataCache();
			
			this.generateMusters();
			
			System.out.println("downloaded "+ date +" kdatas from tushare.");
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
	public void evictDailyKDataCache() {
		kdataRepository.evictDailyKDataCache();
		kdataRepository163.evictDailyKDataCache();
	}

	@Override
	public List<String> getLatestDailyTop(Integer top) {
		return kdataRealtimeSpider.getLatestDailyTop(top);
	}

	@Override
	public Kdata getDailyKdata(String itemID, LocalDate endDate, boolean byCache) {
		if(endDate==null) return this.getDailyKdata(itemID, byCache);
		
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
	public LocalDate getLastDownDate() {
		return kdataRepository.getLastDate();
	}

	@Override
	public Kdata getLatestDailyKdata(String itemID, Integer count, boolean byCache) {
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
		
		LocalDate latestKdataDate = this.getLastDownDate();
		LocalDate theDate = kdataRepository.getLatestMusterDate();

		if(theDate==null || theDate.isBefore(latestKdataDate)) {
			List<KdataMusterEntity> entities = new ArrayList<KdataMusterEntity>();
			KdataMusterEntity entity;
			List<Item> items = itemService.getItems();
			int i=1;
			for(Item item : items) {
				Progress.show(items.size(),i++, item.getItemID());//进度条
				
				entity = this.getKdataMusterEntity(item.getItemID());
				if(entity!=null) {
					entities.add(entity);	
				}
			}
			
			kdataRepository.saveLatestMusters(latestKdataDate, entities, period);
			kdataRepository.evictKdataMustersCache();
			
		}else {
			System.out.println("it has been generated! pass!");
		}
		
		System.out.println("generateMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	
	private KdataMusterEntity getKdataMusterEntity(String itemID) {
		Kdata kdata = this.getLatestDailyKdata(itemID,period,false);
		
		BigDecimal latestPrice = null;
		BigDecimal highest = null;
		BigDecimal lowest = null;

		BigDecimal totalAmount = new BigDecimal(0);
		BigDecimal amount = null;
		
		List<LocalDate> dates = kdata.getDates();

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
			totalAmount = totalAmount.add(kdata.getBar(date).getAmount());
		}
		
		BigDecimal averageAmount = totalAmount.divide(new BigDecimal(kdata.getSize()),BigDecimal.ROUND_HALF_UP);

		return new KdataMusterEntity(itemID,amount,averageAmount,highest,lowest,latestPrice,period,kdata.getSize());
		
	}

	@Override
	public List<KdataMuster> getKdataMusters() {
		List<KdataMuster> musters = new ArrayList<KdataMuster>();
		KdataMuster muster;
		
		List<KdataMusterEntity> entities = kdataRepository.getKdataMusters();
		for(KdataMusterEntity entity : entities) {
			muster = new KdataMuster();
			muster.setItemID(entity.getItemID());
			muster.setAmount(entity.getAmount());
			muster.setAverageAmount(entity.getAverageAmount());
			muster.setHighest(entity.getHighest());
			muster.setLowest(entity.getLowest());
			muster.setPrice(entity.getPrice());
			muster.setPeriod(entity.getPeriod());
			muster.setCount(entity.getCount());
			
			musters.add(muster);
		}
		
		return musters;
	}

	@Override
	public void generateLatestFactors() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateLatestFactors ......");
		//TO DO 通过加标签的方式显示执行某方法耗时多少秒

		Map<String,BigDecimal> latestFactors = new HashMap<String, BigDecimal>();
		
		TreeMap<LocalDate,BigDecimal> factors;
		LocalDate latestDate, previousDate;
		BigDecimal latestFactor, previousFactor;
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());//进度条

			factors = kdataRepository.getFactors(item.getItemID());
			latestDate = factors.lastKey();
			latestFactor = factors.get(latestDate);
			previousDate = factors.lowerKey(latestDate);
			previousFactor = factors.get(previousDate);
			
			if(!latestFactor.equals(previousFactor)) {
				latestFactors.put(item.getItemID(), latestFactor.divide(previousFactor,BigDecimal.ROUND_HALF_UP));
			}
		}
		
		kdataRepository.saveLatestFactors(latestFactors);
		kdataRepository.evictLatestFactorsCache();
		
		System.out.println("generateLatestFactors done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	@Override
	public BigDecimal getLatestFactors(String itemID) {
		return kdataRepository.getLatestFactors().get(itemID);
	}

	@Override
	public void downLatestFactors() {
		LocalDate latestDate = kdataRealtimeSpider.getLatestMarketDate();
		try {
			String result = kdataSpider.downloadLatestFactors(latestDate);
			if(result!=null) this.generateLatestFactors();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

}
