package com.rhb.istock.kdata;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

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

	@Override
	public Kdata getDailyKdata(String itemID, boolean byCache) {
		Kdata kdata = new Kdata(itemID);
		
		KdataEntity entity = null;
		if(byCache) {
			entity = kdataRepository.getDailyKdataByCache(itemID);
		}else {
			entity = kdataRepository.getDailyKdata(itemID);
		}
		
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
		KdataEntity entity = null;
		
		if(byCache) {
			entity = kdataRepository.getDailyKdataByCache(itemID);
		}else {
			entity = kdataRepository.getDailyKdata(itemID);
		}
		
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
		
		return new Kbar(map.get("open"), map.get("high"), map.get("low"), map.get("close"), map.get("amount"), map.get("quantity"));
	}

	@Override
	public LocalDate getLatestMarketDate() {
		return kdataRealtimeSpider.getLatestMarketDate();
	}


	@Override
	public void downKdatas() throws Exception {
		System.out.println("KdataService.downKdatas..........");
		LocalDate latestDate = kdataRealtimeSpider.getLatestMarketDate();
		LocalDate latestDownDate = kdataRepository.getLatestDate();
		List<LocalDate> dates = kdataRealtimeSpider.getCalendar(latestDownDate,latestDate);
		
		System.out.println("latest trade date " + latestDate);
		System.out.println("latest down date " + latestDownDate);
		System.out.println("dates bewteen latest trade date and down date " + dates);
		
		for(LocalDate date : dates) {
			System.out.println("download "+ date +" kdatas from tushare, please wait....... ");
			kdataSpider.downKdata(date);
			System.out.println("downloaded "+ date +" kdatas from tushare.");
		}
	}


	@Override
	public Kbar getKbar(String itemID, LocalDate date, boolean byCache) {
		Kbar kbar= null;
		KdataEntity entity = null;
		
		if(byCache) {
			entity = kdataRepository.getDailyKdataByCache(itemID);
		}else {
			entity = kdataRepository.getDailyKdata(itemID);
		}
		
		KbarEntity bar = entity.getBar(date);
		if(bar!=null) {
			kbar = new Kbar(bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity());
		}else {
			//System.out.println(" kbar is null");
			//System.out.println(entity);
		}
		return kbar;
	}

	@Override
	public void evictDailyKDataCache() {
		kdataRepository.evictDailyKDataCache();
	}

	@Override
	public List<String> getDailyAverageAmountTops(Integer top) {
		List<String> ids = kdataRepository.getDailyAverageAmountTops();
		return ids.subList(0, Math.min(top, ids.size()));
	}

	@Override
	public void generateDailyAverageAmountTops(List<String> itemIDs, Integer duration) {
		kdataRepository.generateDailyAverageAmountTops(itemIDs, duration);
	}


}