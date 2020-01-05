package com.rhb.istock.kdata;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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
	KdataRepository kdataRepositoryTushare;
	
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
	
	@Value("${openDuration}")
	private Integer openDuration;
	
	@Value("${dropDuration}")
	private Integer dropDuration;
	
	private String sseiID = "sh000001"; //上证指数

	protected static final Logger logger = LoggerFactory.getLogger(KdataServiceImp.class);
			
	private KdataEntity getEntity(String itemID, boolean byCache) {
		KdataEntity entity = null;
		if(byCache) {
			entity = sseiID.equals(itemID)? kdataRepository163.getKdataByCache(sseiID): kdataRepositoryTushare.getKdataByCache(itemID);
		}else {
			entity = sseiID.equals(itemID)? kdataRepository163.getKdata(sseiID): kdataRepositoryTushare.getKdata(itemID);
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
				kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(),
						bar.getClose(), bar.getAmount(), bar.getQuantity(), 
						bar.getTurnover_rate_f(), bar.getVolume_ratio(),
						bar.getTotal_mv(),bar.getCirc_mv(),
						bar.getTotal_share(),bar.getFloat_share(),bar.getFree_share());
		}
		
		return kdata;
	}
	
	@Override
	public Kdata getKdata(String itemID, LocalDate endDate, Integer count, boolean byCache) {
		Kdata kdata = new Kdata(itemID);

		KbarEntity bar;
		LocalDate date = endDate.minusDays(1);
		
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		for(int i=0,j=0; i<count && j<entity.getBarSize(); j++) {
			bar = entity.getBar(date);
			if(bar!=null) {
				//System.out.println(date.toString() + ":" + bar.toString());
				kdata.addBar(date, bar.getOpen(), 
						bar.getHigh(), bar.getLow(), 
						bar.getClose(), bar.getAmount(),
						bar.getQuantity(), bar.getTurnover_rate_f(),
						bar.getVolume_ratio(),
						bar.getTotal_mv(),bar.getCirc_mv(),
						bar.getTotal_share(),bar.getFloat_share(),bar.getFree_share());
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
		Kbar bar = new Kbar(map.get("open"),
				map.get("high"), 
				map.get("low"), 
				map.get("close"), 
				map.get("amount"),
				map.get("quantity"),
				map.get("dateTime"),
				"0","0","0","0","0","0","0");
		
		return bar;
	}

	@Override
	public LocalDate getLatestMarketDate(String itemID) {
		return kdataRealtimeSpider.getLatestMarketDate(itemID);
	}

	/*
	 * 下载前，要先下载上证指数sh000001
	 */
	@Override
	public void downKdatasAndFactors() throws Exception {
		logger.info("KdataService.downKdatas..........");
		
		LocalDate latestDate = kdataRealtimeSpider.getLatestMarketDate("sh000001");
		logger.info("latest trade date " + latestDate);

		LocalDate lastDownDate1 = kdataRepositoryTushare.getLastDate("sh601398");
		LocalDate lastDownDate2 = kdataRepositoryTushare.getLastDate("sh601288");
		LocalDate lastDownDate = lastDownDate1.isBefore(lastDownDate2) ? lastDownDate2 : lastDownDate1;
		logger.info("last down date " + lastDownDate);

		List<LocalDate> dates = this.getKdata("sh000001", lastDownDate.plusDays(1), latestDate, false).getDates();

		//List<LocalDate> dates = kdataRealtimeSpider.getCalendar(lastDownDate,latestDate);
		
		logger.info("dates bewteen latest trade date and down date " + dates);
		
		LocalDate date;
		for(int i=0; i<dates.size(); i++) {
			date = dates.get(i);
			logger.info("download "+ date +" kdatas from tushare, please wait....... ");
			kdataSpider.downKdatas(date);
			logger.info("downloaded "+ date +" kdatas from tushare.");
		}
		
		for(int i=1; i<dates.size(); i++) { //因为factor比收盘数据早得到（开盘前就得到factor，收盘后才得到kdata）,
			date = dates.get(i);
			logger.info("download "+ date +" factors from tushare, please wait....... ");
			kdataSpider.downFactors(date);
			logger.info("downloaded "+ date +" factors from tushare.");
		}
		
		if(dates.size()>0) {
			logger.info("download latest date "+ latestDate +" factors from tushare, please wait....... ");
			kdataSpider.downFactors(latestDate);  //执行了这一步后，不需要再对收盘数据额外的复权处理了
			logger.info("downloaded latest date "+ latestDate +" factors from tushare.");
			
			kdataSpider163.downKdatas(sseiID);
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
			kbar = new Kbar(bar.getOpen(),
					bar.getHigh(), bar.getLow(), 
					bar.getClose(), bar.getAmount(), 
					bar.getQuantity(),date, 
					bar.getTurnover_rate_f(), 
					bar.getVolume_ratio(),
					bar.getTotal_mv(),
					bar.getCirc_mv(),
					bar.getTotal_share(),
					bar.getFloat_share(),
					bar.getFree_share());
		}else {
			//System.out.println(" kbar is null");
			//System.out.println(entity);
		}
		return kbar;
	}

	@Override
	public void evictKDataCache() {
		kdataRepositoryTushare.evictKDataCache();
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
					kdata.addBar(date, bar.getOpen(), bar.getHigh(), 
							bar.getLow(), bar.getClose(), 
							bar.getAmount(), bar.getQuantity(), 
							bar.getTurnover_rate_f(), bar.getVolume_ratio(),
							bar.getTotal_mv(),bar.getCirc_mv(),
							bar.getTotal_share(),bar.getFloat_share(),bar.getFree_share());
				}
		}
		
		return kdata;

	}

	@Override
	public LocalDate getLastKdataDate(String itemID) {
		KdataEntity entity = this.getEntity(itemID, true);
		return entity.getLastDate();
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
				kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity(), bar.getTurnover_rate_f(), bar.getVolume_ratio(),bar.getTotal_mv(),bar.getCirc_mv(),
						bar.getTotal_share(),bar.getFloat_share(),bar.getFree_share());
				i++;
			}
			date = date.minusDays(1);
		}
		
		return kdata;
	}

	@Override
	public void generateMusters(LocalDate beginDate) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("generateMusters ......");
		
		musterRepositoryImp.cleanTmpMusters();
		
		Kdata kdata;
		List<LocalDate> dates;
		MusterEntity entity;

		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, " generateMusters: " + item.getItemID());//进度条
			
			kdata = this.getKdata(item.getItemID(), true);
			dates = kdata.getDates();
			
			for(LocalDate date : dates) {
				if(date.isAfter(beginDate)) {
					entity = this.getMusterEntity(item.getItemID(), date, true);
					if(entity!=null) musterRepositoryImp.saveTmpMuster(date, entity);
				}
			}
			
			this.evictKDataCache();
		}
		
		musterRepositoryImp.copyTmpMusters();
		
		logger.info("\ngenerateMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
	}

	
	private MusterEntity getMusterEntity(String itemID, LocalDate endDate, boolean cache) {
		Kdata kdata = this.getKdata(itemID,endDate,openDuration,cache); //不包含endDate
		
		if(kdata==null || kdata.getSize()<openDuration) return null;  //此行很重要，涉及averageAmount和dropPrice的准确性
		
		Kbar lastBar = kdata.getLastBar(); //是endDate的前一个交易日
		BigDecimal close = lastBar.getClose();
		BigDecimal amount = lastBar.getAmount();
		BigDecimal trunover_rate_f = lastBar.getTurnover_rate_f();
		BigDecimal total_mv = lastBar.getTotal_mv();
		BigDecimal volume_ratio = lastBar.getVolume_ratio();
		BigDecimal circ_mv = lastBar.getCirc_mv();
		BigDecimal total_share = lastBar.getTotal_share();
		BigDecimal float_share = lastBar.getFloat_share();
		BigDecimal free_share = lastBar.getFree_share();
		
		//System.out.println(total_share);
		//System.out.println(volume_ratio);
		
		BigDecimal latestPrice = close;
		Integer limited = 0;
		Kbar kbar = this.getKbar(itemID, endDate, cache);
		if(kbar!=null) {
			latestPrice = kbar.getClose();
			limited = kbar.isLimited();
		}

		Map<String,BigDecimal> features = kdata.getFeatures();
		BigDecimal highest = features.get("highest");
		BigDecimal lowest = features.get("lowest");
		BigDecimal lowest34 = features.get("lowest34");
		BigDecimal lowest21 = features.get("lowest21");
		BigDecimal lowest13 = features.get("lowest13");
		BigDecimal lowest8 = features.get("lowest8");
		BigDecimal lowest5 = features.get("lowest5");
		BigDecimal amount5 = features.get("amount5");
		BigDecimal averageAmount = features.get("averageAmount");
		BigDecimal averagePrice = features.get("averagePrice");
		BigDecimal average_turnover_rate_f = features.get("average_turnover_rate_f");
		BigDecimal average_volume_ratio = features.get("average_volume_ratio");
		
		Map<String,BigDecimal> averagePrices = kdata.getAveragePrices();
		BigDecimal a8 = averagePrices.get("a8");
		BigDecimal a13 = averagePrices.get("a13");
		BigDecimal a21 = averagePrices.get("a21");
		BigDecimal a34 = averagePrices.get("a34");
		
		return new MusterEntity(itemID,close,amount,latestPrice,
				limited,highest,lowest,averageAmount,averagePrice,
				a8,a13,a21,a34,lowest21,lowest34,trunover_rate_f,
				average_turnover_rate_f,volume_ratio,average_volume_ratio,
				total_mv,circ_mv,total_share,float_share,free_share,lowest13,lowest8,lowest5,amount5);
	}
	
	@Override
	public Map<String,Muster> getLatestMusters() {
		LocalDate date = kdataRealtimeSpider.getLatestMarketDate("sh000001");
		return this.getMusters(date);
	}


	@Override
	public void generateLatestMusters() {
		long beginTime=System.currentTimeMillis(); 
		logger.info("generateLatestMusters ......");
		
		LocalDate date = kdataRealtimeSpider.getLatestMarketDate("sh000001");

		//LocalDate date = kdataRepository.getLastDate();
		if(musterRepositoryImp.isMustersExist(date)) {
			System.out.println("The last musters of " + date + " has already exists! pass!");
		}else {
			MusterEntity entity;

			List<Item> items = itemService.getItems();
			int i=1;
			for(Item item : items) {
				Progress.show(items.size(),i++, " generateLatestMusters: " + item.getItemID());//进度条
				
				entity = this.getMusterEntity(item.getItemID(), date, false);
				if(entity!=null) musterRepositoryImp.saveMuster(date, entity);

			}
		}
		
		logger.info("generateLatestMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
		
	}

	@Override
	public Map<String,Muster> getMusters(LocalDate date) {
		Map<String,Muster> musters = new HashMap<String,Muster>();
		if(date==null) return musters;
		
		Muster muster;
		Item item;
		
		Map<String, MusterEntity> entities = musterRepositoryImp.getMusters(date);
		
		for(MusterEntity entity : entities.values()) {
			item = itemService.getItem(entity.getItemID());
			if(item!=null) {
				muster = new Muster();
				muster.setItemID(entity.getItemID());
				muster.setItemName(item.getName());
				muster.setIndustry(item.getIndustry());
				muster.setClose(entity.getClose());
				muster.setAmount(entity.getAmount());
				muster.setLatestPrice(entity.getLatestPrice());
				muster.setLimited(entity.getLimited());
				muster.setHighest(entity.getHighest());
				muster.setLowest(entity.getLowest());
				muster.setLowest21(entity.getLowest21());
				muster.setLowest34(entity.getLowest34());
				muster.setAverageAmount(entity.getAverageAmount());
				muster.setAveragePrice(entity.getAveragePrice());
				muster.setAveragePrice8(entity.getAveragePrice8());
				muster.setAveragePrice13(entity.getAveragePrice13());
				muster.setAveragePrice21(entity.getAveragePrice21());
				muster.setAveragePrice34(entity.getAveragePrice34());
				muster.setTurnover_rate_f(entity.getTurnover_rate_f());
				muster.setAverage_turnover_rate_f(entity.getAverage_turnover_rate_f());
				muster.setVolume_ratio(entity.getVolume_ratio());
				muster.setAverage_volume_ratio(entity.getAverage_volume_ratio());
				muster.setTotal_mv(entity.getTotal_mv());
				muster.setCirc_mv(entity.getCirc_mv());
				muster.setTotal_share(entity.getTotal_share());
				muster.setFloat_share(entity.getFloat_share());
				muster.setFree_share(entity.getFree_share());
				muster.setLowest13(entity.getLowest13());
				muster.setLowest8(entity.getLowest8());
				muster.setLowest5(entity.getLowest5());
				muster.setAmount5(entity.getAmount5());
				
				musters.put(muster.getItemID(),muster);
			}else {
				//logger.info(String.format("item of %s is null", entity.getItemID()));
			}
		}
		
		return musters;
	}
	
	@Override
	public Map<String,Muster> getMusters(LocalDate date, Set<String> includeIndustrys) {
		Map<String,Muster> musters = new HashMap<String,Muster>();
		Muster muster;
		String itemName;
		String industry;
		
		Map<String, MusterEntity> entities = musterRepositoryImp.getMusters(date);
		
		for(MusterEntity entity : entities.values()) {
			itemName = itemService.getItem(entity.getItemID()).getName();
			industry = itemService.getItem(entity.getItemID()).getIndustry();
			if(includeIndustrys==null || includeIndustrys.contains(entity.getItemID())) {
				//System.out.println(entity.getItemID());
				muster = new Muster();
				muster.setItemID(entity.getItemID());
				muster.setItemName(itemName);
				muster.setIndustry(industry);
				muster.setClose(entity.getClose());
				muster.setAmount(entity.getAmount());
				muster.setLatestPrice(entity.getLatestPrice());
				muster.setLimited(entity.getLimited());
				muster.setHighest(entity.getHighest());
				muster.setLowest(entity.getLowest());
				muster.setLowest21(entity.getLowest21());
				muster.setLowest34(entity.getLowest34());
				muster.setAverageAmount(entity.getAverageAmount());
				muster.setAveragePrice(entity.getAveragePrice());
				muster.setAveragePrice8(entity.getAveragePrice8());
				muster.setAveragePrice13(entity.getAveragePrice13());
				muster.setAveragePrice21(entity.getAveragePrice21());
				muster.setAveragePrice34(entity.getAveragePrice34());
				muster.setTurnover_rate_f(entity.getTurnover_rate_f());
				muster.setAverage_turnover_rate_f(entity.getAverage_turnover_rate_f());
				muster.setVolume_ratio(entity.getVolume_ratio());
				muster.setAverage_volume_ratio(entity.getAverage_volume_ratio());
				muster.setTotal_mv(entity.getTotal_mv());
				muster.setCirc_mv(entity.getCirc_mv());
				muster.setTotal_share(entity.getTotal_share());
				muster.setFloat_share(entity.getFloat_share());
				muster.setFree_share(entity.getFree_share());
				muster.setLowest13(entity.getLowest13());
				muster.setLowest8(entity.getLowest8());
				muster.setLowest5(entity.getLowest5());
				muster.setAmount5(entity.getAmount5());

				musters.put(muster.getItemID(),muster);
			}
		}
		
		return musters;
	}

	@Override
	public void updateLatestMusters() {
		long beginTime=System.currentTimeMillis(); 
		logger.info("updateLatestMusters ......");

		Kbar kbar;
		LocalDate date = this.getLatestMarketDate("sh000001");
		Map<String,Muster> musters = this.getMusters(date);
		List<MusterEntity> entities = new ArrayList<MusterEntity>();
		int i=1;
		for(Muster muster : musters.values()) {
			Progress.show(musters.size(),i++, " updateLatestMusters " + muster.getItemID());

			kbar = this.getLatestMarketData(muster.getItemID());
			if(kbar==null) {
				this.generateLatestMusters();
				kbar = this.getLatestMarketData(muster.getItemID());
			}
			if(kbar!=null) {
				entities.add(new MusterEntity(
						muster.getItemID(), 
						muster.getClose(), 
						kbar.getAmount(), 
						kbar.getClose(),
						kbar.isLimited(),
						muster.getHighest(), 
						muster.getLowest(), 
						muster.getAverageAmount(), 
						muster.getAveragePrice(), 
						muster.getAveragePrice8(), 
						muster.getAveragePrice13(), 
						muster.getAveragePrice21(), 
						muster.getAveragePrice34(),
						muster.getLowest21(),
						muster.getLowest34(),
						muster.getTurnover_rate_f(),
						muster.getAverage_turnover_rate_f(),
						muster.getVolume_ratio(),
						muster.getAverage_volume_ratio(),
						muster.getTotal_mv(),
						muster.getCirc_mv(),
						muster.getTotal_share(),
						muster.getFloat_share(),
						muster.getFree_share(),
						muster.getLowest13(),
						muster.getLowest8(),
						muster.getLowest5(),
						muster.getAmount5()
						));
			}
		}
		musterRepositoryImp.saveMusters(date,entities);

		logger.info("\nupdateLatestMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     

	}

	@Override
	public List<LocalDate> getMusterDates(LocalDate beginDate, LocalDate endDate) {
		return musterRepositoryImp.getMusterDates(beginDate, endDate);
	}

	@Override
	public List<LocalDate> getLastMusterDates() {
		return musterRepositoryImp.getLastMusterDates(dropDuration);
	}

	@Override
	public List<LocalDate> getMusterDates() {
		return musterRepositoryImp.getMusterDates();
	}

	@Override
	public Map<String, Muster> getMusters(LocalDate date, String industry) {
		Map<String,Muster> musters = new HashMap<String,Muster>();
		Muster muster;
		
		Map<String, MusterEntity> entities = musterRepositoryImp.getMusters(date);
		
		for(MusterEntity entity : entities.values()) {
			if(itemService.getItem(entity.getItemID()).getIndustry().equals(industry)) {
				//System.out.println(entity.getItemID());
				muster = new Muster();
				muster.setItemID(entity.getItemID());
				muster.setItemName(itemService.getItem(entity.getItemID()).getName());
				muster.setIndustry(itemService.getItem(entity.getItemID()).getIndustry());
				muster.setClose(entity.getClose());
				muster.setAmount(entity.getAmount());
				muster.setLatestPrice(entity.getLatestPrice());
				muster.setLimited(entity.getLimited());
				muster.setHighest(entity.getHighest());
				muster.setLowest(entity.getLowest());
				muster.setLowest21(entity.getLowest21());
				muster.setLowest34(entity.getLowest34());
				muster.setAverageAmount(entity.getAverageAmount());
				muster.setAveragePrice(entity.getAveragePrice());
				muster.setAveragePrice8(entity.getAveragePrice8());
				muster.setAveragePrice13(entity.getAveragePrice13());
				muster.setAveragePrice21(entity.getAveragePrice21());
				muster.setAveragePrice34(entity.getAveragePrice34());
				muster.setTurnover_rate_f(entity.getTurnover_rate_f());
				muster.setAverage_turnover_rate_f(entity.getAverage_turnover_rate_f());
				muster.setVolume_ratio(entity.getVolume_ratio());
				muster.setAverage_volume_ratio(entity.getAverage_volume_ratio());
				muster.setTotal_mv(entity.getTotal_mv());
				muster.setCirc_mv(entity.getCirc_mv());
				muster.setTotal_share(entity.getTotal_share());
				muster.setFloat_share(entity.getFloat_share());
				muster.setFree_share(entity.getFree_share());
				muster.setLowest13(entity.getLowest13());
				muster.setLowest8(entity.getLowest8());
				muster.setLowest5(entity.getLowest5());
				muster.setAmount5(entity.getAmount5());
				
				musters.put(muster.getItemID(),muster);				
			}
		}
		
		return musters;
	}

	@Override
	public void downSSEI() {
		logger.info("KdataService.downSSEI..........");

		try {
			//LocalDate latestMarketDate = this.getLatestMarketDate("sh000001");
			//LocalDate latestKdataDate = this.getLastKdataDate("sh000001");
			//if(!latestMarketDate.equals(latestKdataDate)) {
			//}
			kdataSpider163.downKdatas(sseiID);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public Integer getSseiFlag(LocalDate date) {
		Integer flag = 1;
		Kdata ssei = this.getKdata(sseiID, date.plusDays(1), openDuration, true);
		flag = ssei.isAboveAveragePrice(openDuration) && ssei.isAboveAveragePrice(21) ? 1 : 0;
		return flag;
	}

	@Override
	public Kdata getKdata(String itemID, LocalDate beginDate, LocalDate endDate, boolean byCache) {
		Kdata kdata = new Kdata(itemID);

		KbarEntity bar;
		
		KdataEntity entity = this.getEntity(itemID, byCache);
		
		for(LocalDate date = beginDate; date.isBefore(endDate); date = date.plusDays(1)) {
			bar = entity.getBar(date);
			if(bar!=null) {
				kdata.addBar(date, bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getAmount(), bar.getQuantity(), bar.getTurnover_rate_f(), bar.getVolume_ratio(),bar.getTotal_mv(),bar.getCirc_mv(),
						bar.getTotal_share(),bar.getFloat_share(),bar.getFree_share());
			}
		}
		
		return kdata;
	}

	@Override
	public void downFactors() throws Exception {
		logger.info("KdataService.downFactors..........");
		
		LocalDate latestDate = kdataRealtimeSpider.getLatestMarketDate("sh000001");
		kdataSpider.downFactors(latestDate);
		
	}

	@Override
	public void downClosedDatas() throws Exception {
		long beginTime=System.currentTimeMillis(); 

		List<String> ids = itemService.getItemIDs();
		int i=1;
		for(String id : ids){
			Progress.show(ids.size(),i++, " down closed  datas: " + id);
			try {
				kdataSpider.downKdatas(id);
				kdataSpider.downFactors(id);
				kdataSpider.downBasics(id);
				//Thread.sleep(300); //一分钟200个
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}

		this.downSSEI();
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	@Override
	public void downClosedDatas(LocalDate date) throws Exception {
		long beginTime=System.currentTimeMillis(); 

		this.downSSEI();

		kdataSpider.downKdatas(date);
		kdataSpider.downBasics(date);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}

	@Override
	public void downKdatas() throws Exception {
		// TODO Auto-generated method stub
		
	}

}
