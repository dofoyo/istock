package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	@Qualifier("musterRepositoryImp")
	MusterRepository musterRepositoryImp;
	
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
		System.out.println("latest trade date " + latestDate);

		LocalDate lastDownDate = kdataRepository.getLastDate();
		System.out.println("last down date " + lastDownDate);

		List<LocalDate> dates = kdataRealtimeSpider.getCalendar(lastDownDate,latestDate);
		System.out.println("dates bewteen latest trade date and down date " + dates);
		
		LocalDate date;
		for(int i=0; i<dates.size(); i++) {
			date = dates.get(i);
			System.out.println("download "+ date +" kdatas from tushare, please wait....... ");
			kdataSpider.downKdatas(date);
			System.out.println("downloaded "+ date +" kdatas from tushare.");
		}
		
		for(int i=1; i<dates.size(); i++) { //因为factor比收盘数据早得到（开盘前就得到factor，收盘后才得到kdata）,
			date = dates.get(i);
			System.out.println("download "+ date +" factors from tushare, please wait....... ");
			//kdataSpider.downFactors(date);
			System.out.println("downloaded "+ date +" factors from tushare.");
		}
		
		if(dates.size()>0) {
			kdataSpider.downFactors(latestDate);  //执行了这一步后，不需要再对收盘数据额外的复权处理了
			
			kdataSpider163.downKdata(szzs);
			evictKDataCache();
			
			this.generateLatestMusters(); //此方法每天开盘时执行一次
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
		
		musterRepositoryImp.cleanTmpMusters();
		
		Kdata kdata;
		List<LocalDate> dates;
		MusterEntity entity;

		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());//进度条
			
			kdata = this.getKdata(item.getItemID(), true);
			dates = kdata.getDates();
			
			//int j=1;
			for(LocalDate date : dates) {
				//Progress.show(dates.size(),j++, date.toString());//进度条
				entity = this.getMusterEntity(item.getItemID(), date, true);
				if(entity!=null) musterRepositoryImp.saveTmpMuster(date, entity);

				//if(j++>2) return;
			}
			
			this.evictKDataCache();
		}
		
		musterRepositoryImp.copyTmpMusters();
		
		System.out.println("generateMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	
	private MusterEntity getMusterEntity(String itemID,LocalDate endDate, boolean cache) {
		Kdata kdata = this.getKdata(itemID,endDate,openPeriod,cache);
		if(kdata==null || kdata.getSize()<openPeriod) return null;  //此行很重要，涉及averageAmount和dropPrice的准确性
		
		BigDecimal close = null;
		BigDecimal highest = null;
		BigDecimal lowest = null;
		BigDecimal totalPrice = new BigDecimal(0);
		BigDecimal totalAmount = new BigDecimal(0);
		BigDecimal amount = null;
		BigDecimal latestPrice = null;
		
		List<LocalDate> dates = kdata.getDates();

		int dropStart = openPeriod-dropPeriod+1;
		int i=1;
		for(LocalDate date : dates) {
			if(highest==null) {
				highest = kdata.getBar(date).getHigh();
			}else {
				highest = highest.compareTo(kdata.getBar(date).getHigh())==-1 ? kdata.getBar(date).getHigh() : highest;
			}
			if(lowest==null) {
				lowest = kdata.getBar(date).getLow();
			}else {
				lowest = lowest.compareTo(kdata.getBar(date).getLow())==1 ? kdata.getBar(date).getLow() : lowest;
			}
			close = kdata.getBar(date).getClose();
			amount = kdata.getBar(date).getAmount();
			totalAmount = totalAmount.add(amount);
			
			if(i>=dropStart) {
				totalPrice = totalPrice.add(kdata.getBar(date).getClose());
			}
			
			i++;
		}
		
		BigDecimal averageAmount = totalAmount.divide(new BigDecimal(openPeriod),BigDecimal.ROUND_HALF_UP);
		BigDecimal dropPrice = totalPrice.divide(new BigDecimal(dropPeriod),BigDecimal.ROUND_HALF_UP);
		
		latestPrice = close;
		Kbar bar =this.getKbar(itemID, endDate, cache);
		if(bar!=null) latestPrice = bar.getClose();
		
		return new MusterEntity(itemID,amount,averageAmount,highest,lowest,close,dropPrice,latestPrice);
		
	}

	@Override
	public Map<String,Muster> getLatestMusters() {
		LocalDate date = kdataRealtimeSpider.getLatestMarketDate();
		return this.getMusters(date);
	}


	@Override
	public void generateLatestMusters() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generateLastMusters ......");
		
		LocalDate date = kdataRealtimeSpider.getLatestMarketDate();

		//LocalDate date = kdataRepository.getLastDate();
		if(musterRepositoryImp.isMustersExist(date)) {
			System.out.println("The last musters of " + date + " has already exists! pass!");
		}else {
			MusterEntity entity;

			List<Item> items = itemService.getItems();
			int i=1;
			for(Item item : items) {
				Progress.show(items.size(),i++, item.getItemID());//进度条
				
				entity = this.getMusterEntity(item.getItemID(), date, false);
				if(entity!=null) musterRepositoryImp.saveMuster(date, entity);

			}
		}
		
		System.out.println("generateLastMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}

	@Override
	public Map<String,Muster> getMusters(LocalDate date) {
		Map<String,Muster> musters = new HashMap<String,Muster>();
		Muster muster;
		
		Map<String, MusterEntity> entities = musterRepositoryImp.getMusters(date);
		
		for(MusterEntity entity : entities.values()) {
			muster = new Muster();
			muster.setItemID(entity.getItemID());
			muster.setAmount(entity.getAmount());
			muster.setAverageAmount(entity.getAverageAmount());
			muster.setHighest(entity.getHighest());
			muster.setLowest(entity.getLowest());
			muster.setClose(entity.getClose());
			muster.setDropPrice(entity.getDropPrice());
			muster.setLatestPrice(entity.getLatestPrice());
			
			musters.put(muster.getItemID(),muster);
		}
		
		return musters;
	}

	@Override
	public void updateLatestMusters() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("updateLatestMusters ......");

		Kbar kbar;
		LocalDate date = this.getLatestMarketDate();
		Map<String,Muster> musters = this.getLatestMusters();
		List<MusterEntity> entities = new ArrayList<MusterEntity>();
		int i=1;
		for(Muster muster : musters.values()) {
			Progress.show(musters.size(),i++, muster.getItemID());

			kbar = this.getLatestMarketData(muster.getItemID());
			if(kbar!=null) {
				entities.add(new MusterEntity(muster.getItemID(), 
						kbar.getAmount(), 
						muster.getAverageAmount(), 
						muster.getHighest(), 
						muster.getLowest(), 
						muster.getClose(), 
						muster.getDropPrice(), 
						kbar.getClose()));
			}
		}
		musterRepositoryImp.saveMusters(date,entities);

		System.out.println("updateLatestMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     

	}

	@Override
	public List<LocalDate> getMusterDates(LocalDate beginDate, LocalDate endDate) {
		return musterRepositoryImp.getMusterDates(beginDate, endDate);
	}

}
