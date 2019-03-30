package com.rhb.istock.trade.twin;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;
import java.util.TreeSet;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.twin.repository.TwinRepository;

@Service("twinServiceImp")
public class TwinServiceImp implements TwinService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("twinRepositoryImp")
	TwinRepository twinRepository;
	
	boolean byCache = true;
	
	@Override
	public void generateTradeList() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate twin trade list ......");

		Twin twin = new Twin();
		Kdata kdata;
		List<LocalDate> dates;
		Wfeature feature;
		Kbar bar;
		
		TreeMapSet<LocalDate,Wfeature> opens = new TreeMapSet<LocalDate,Wfeature>();
		//TreeMapSet<LocalDate,Wfeature> drops = new TreeMapSet<LocalDate,Wfeature>();
		
		List<Item> items = itemService.getItems();
		//List<Item> items = this.getItems();
		
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			kdata = kdataService.getDailyKdata(item.getItemID(),byCache);
			dates = kdata.getDates();
			for(LocalDate date : dates) {
				bar = kdata.getBar(date);
				if(bar!=null) {
					twin.addDailyData(item.getItemID(), date, bar.getClose());
					feature = twin.getFeature(item.getItemID());
					if(feature!=null && feature.getStatus()==2 && feature.getBiasOfNowPriceAndShortLine()<5 && feature.getBiasOfShortAndLong()>5) {
						opens.put(date, feature);
					}
/*					
					if(feature!=null && feature.getStatus()==-1) {
						drops.put(date, feature);					
					}*/
					//System.out.println(feature);
				}
			}
			kdataService.evictDailyKDataCache();
		}
		
		twinRepository.saveOpens(opens.getResults());
		//twinRepository.saveDrops(drops.getResults());
	
		System.out.println("generate twin trade list done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	
	private List<Item> getItems(){
		List<Item> items = new ArrayList<Item>();
		Item item = new Item();
		item.setItemID("sz000897");
		items.add(item);
		return items;
	}

	@Override
	public List<String> getOpenList(LocalDate theDate) {
		return twinRepository.getOpens().get(theDate);
	}

	//@Override
	public List<String> getDropList(LocalDate theDate) {
		return twinRepository.getDrops().get(theDate);
	}
	
	class TreeMapSet<K,T>{
		TreeMap<K,TreeSet<T>> results = new TreeMap<K,TreeSet<T>>();
		TreeSet<T> values;
		public void put(K key,T value) {
			if(results.containsKey(key)) {
				values = results.get(key);
			}else {
				values = new TreeSet<T>();
				results.put(key, values);
			}
			values.add(value);		
		}
		
		public TreeMap<K,TreeSet<T>> getResults(){
			return results;
		}
		
	}
}
