package com.rhb.istock.trade.turtle.operation.service;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.spider.KdataRealtimeSpider;
import com.rhb.istock.trade.turtle.domain.Tfeature;
import com.rhb.istock.trade.balloon.operation.repository.BalloonOperationRepository;
import com.rhb.istock.trade.turtle.domain.Tbar;
import com.rhb.istock.trade.turtle.domain.Turtle;
import com.rhb.istock.trade.turtle.operation.api.HoldView;
import com.rhb.istock.trade.turtle.operation.api.KdatasView;
import com.rhb.istock.trade.turtle.operation.api.TurtleView;
import com.rhb.istock.trade.turtle.operation.repository.HoldEntity;
import com.rhb.istock.trade.turtle.operation.repository.TurtleRepository;

@Service("turtleOperationServiceImp")
public class TurtleOperationServiceImp implements TurtleOperationService {
	@Value("${preysFile}")
	private String preysFile;
	
	@Autowired
	@Qualifier("turtleRepositoryImp")
	TurtleRepository turtleRepository;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("kdataRealtimeSpiderImp")
	KdataRealtimeSpider kdataRealtimeSpider;

	@Autowired
	@Qualifier("balloonOperationRepositoryImp")
	BalloonOperationRepository balloonOperationRepository;
	
	Turtle turtle = null;
	
	@Override
	public void init() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("TurtleOperationService init...");
		turtle = new Turtle();
		//启动时把数据都加载，太耗时。大约6至7分钟
/*		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++,item.getItemID());
			this.setDailyKdata(item.getItemID());
			this.setLatestKdata(item.getItemID());
		}*/		
		System.out.println("TurtleOperationService init done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	@Override
	public List<HoldView> getHolds() {
		List<HoldView> holds = new ArrayList<HoldView>();
		
		Tfeature feature;
		BigDecimal stopPrice, reopenPrice;
		Item item;
		
		HoldView hold;
		List<HoldEntity> entities = turtleRepository.getHolds();
		//System.out.println(entities);
		for(HoldEntity entity : entities) {
			this.setLatestKdata(entity.getItemID(),true);
			
			feature = turtle.getFeature(entity.getItemID());
			stopPrice = entity.getPrice().subtract(feature.getAtr());
			reopenPrice = entity.getPrice().add(feature.getAtr().divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
			
			item = itemService.getItem(entity.getItemID());
			
			hold = new HoldView(entity.getItemID(),item.getCode(),item.getName(),feature.getAtr().toString());
			hold.setNowPrice(feature.getNow());
			hold.setHighPrice(feature.getOpenHigh());
			hold.setLowPrice(feature.getOpenLow());
			hold.setBuyPrice(entity.getPrice());
			hold.setStopPrice(stopPrice);
			hold.setDropPrice(feature.getDropLow());
			hold.setReopenPrice(reopenPrice);
			
			holds.add(hold);
			
			//System.out.println(hold);
		}
		return holds;
	}

	@Override
	public List<TurtleView> getPreys() {
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		Map<String,String> preyMap;
		String[] columns;
		String[] lines = FileUtil.readTextFile(preysFile).split("\n");
		for(int i=1; i<lines.length; i++) {
			columns = lines[i].split(",");
			preyMap = new HashMap<String,String>();
			preyMap.put("itemID", columns[0]);
			preyMap.put("code", columns[1]);
			preyMap.put("name", columns[2]);
			preyMap.put("low", columns[3]);
			preyMap.put("high", columns[4]);
			preyMap.put("now", columns[5]);
			preyMap.put("drop", columns[6]);
			preyMap.put("hlgap", columns[7]);
			preyMap.put("nhgap", columns[8]);
			preyMap.put("atr", columns[9]);	
			preyMap.put("status", columns[10]);	
			
			views.add(new TurtleView(preyMap));
		}
		return views;
	}

	@Override
	public void generatePreys() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate preys ......");
		DecimalFormat df = new DecimalFormat("0.00"); 

		StringBuffer sb = new StringBuffer();
		sb.append("itemID,code,name,openLow,openHigh,now,dropLow,hlgap,nhgap,atr,status\n");
		
		List<Item> items = itemService.getItems();
		
		Tfeature feature;
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			
			if(this.setLatestKdata(item.getItemID(),false)) {
				feature = turtle.getFeature(item.getItemID());
				if(feature!=null) {
					sb.append(item.getItemID());
					sb.append(",");
					sb.append(item.getCode());
					sb.append(",");
					sb.append(item.getName());
					sb.append(",");
					sb.append(feature.getOpenLow());
					sb.append(",");
					sb.append(feature.getOpenHigh());
					sb.append(",");
					sb.append(feature.getNow());
					sb.append(",");
					sb.append(feature.getDropLow());
					sb.append(",");
					sb.append(feature.getHlgap());
					sb.append(",");
					sb.append(feature.getNhgap());
					sb.append(",");
					sb.append(df.format(feature.getAtr()));
					sb.append(",");
					sb.append(feature.getStatus());
					sb.append("\n");
				}
			}
		}
		FileUtil.writeTextFile(preysFile, sb.toString(), false);
		System.out.println("generate preys done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	private void setDailyKdata(String itemID, boolean byCache) {
		LocalDate endDate = kdataService.getLatestMarketDate();
		
		Kbar kbar;
		Kdata kdata = kdataService.getDailyKdata(itemID, endDate, turtle.getOpenDuration(), byCache);
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			turtle.addDailyData(itemID,date,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
	}
	
	private boolean setLatestKdata(String itemID, boolean byCache) {
		if(turtle==null) {
			this.init();
		}
		
		if(turtle.getDailyDatas(itemID)==null) {
			setDailyKdata(itemID, byCache);
		}
		
		boolean flag = false;
		
		LocalDate endDate = kdataService.getLatestMarketDate();
		
		Kbar kbar = kdataService.getLatestMarketData(itemID);
		if(kbar!=null) {
			turtle.addLatestData(itemID,endDate ,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
			flag = true;
		}
		
		return flag;
	}

	@Override
	public KdatasView getKdatas(String itemID) {
		setLatestKdata(itemID,true);
		
		KdatasView kdata = new KdatasView();
		
		Item item = itemService.getItem(itemID);
		kdata.setItemID(itemID);
		kdata.setCode(item.getCode());
		kdata.setName(item.getName());
		
		List<Tbar> tbars = turtle.getDailyDatas(itemID);
		for(Tbar tbar : tbars) {
			kdata.addKdata(tbar.getDate(), tbar.getOpen(), tbar.getHigh(), tbar.getLow(), tbar.getClose());
		}
		
		Tbar tbar = turtle.getLatestData(itemID);
		kdata.addKdata(tbar.getDate(), tbar.getOpen(), tbar.getHigh(), tbar.getLow(), tbar.getClose());
		
		return kdata;
	}

	@Override
	public List<TurtleView> getFavors() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("getting favors ......");
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Tfeature feature;
		Map<String, String> favors = turtleRepository.getFavors();
		
		int i=1;
		for(String id : favors.keySet()) {
			Progress.show(favors.size(),i++,id);
			
			if(this.setLatestKdata(id,true)) {
				feature = turtle.getFeature(id);
				if(feature!=null) {
					preyMap = new HashMap<String,String>();
					preyMap.put("itemID", id);
					preyMap.put("code", id.substring(2));
					preyMap.put("name", favors.get(id));
					preyMap.put("low", df.format(feature.getOpenLow()));
					preyMap.put("high", df.format(feature.getOpenHigh()));
					preyMap.put("now", df.format(feature.getNow()));
					preyMap.put("drop", df.format(feature.getDropLow()));
					preyMap.put("hlgap", feature.getHlgap().toString());
					preyMap.put("nhgap", feature.getNhgap().toString());
					preyMap.put("atr", df.format(feature.getAtr()));	
					preyMap.put("status",feature.getStatus().toString());	
					
					views.add(new TurtleView(preyMap));

				}
			}
		}
		System.out.println("get favors done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
		
	}

	@Override
	public List<TurtleView> getDailyTops(Integer top) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("getting daily amount tops " + top + " ......");
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Tfeature feature;
		Item item;
		
		List<String> ids = kdataRealtimeSpider.getLatestDailyTop(top);
		
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			
			if(this.setLatestKdata(id, true)) {
				feature = turtle.getFeature(id);
				if(feature!=null) {
					item = itemService.getItem(id);

					preyMap = new HashMap<String,String>();
					preyMap.put("itemID", id);
					preyMap.put("code", item.getCode());
					preyMap.put("name", item.getName());
					preyMap.put("low", df.format(feature.getOpenLow()));
					preyMap.put("high", df.format(feature.getOpenHigh()));
					preyMap.put("now", df.format(feature.getNow()));
					preyMap.put("drop", df.format(feature.getDropLow()));
					preyMap.put("hlgap", feature.getHlgap().toString());
					preyMap.put("nhgap", feature.getNhgap().toString());
					preyMap.put("atr", df.format(feature.getAtr()));	
					preyMap.put("status", feature.getStatus().toString());	
					
					views.add(new TurtleView(preyMap));

				}
			}
		}
		System.out.println("getting daily amount tops "+top+" done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
	}

	@Override
	public List<TurtleView> getAvTops(Integer top) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("getting average amount tops " + top + " ......");
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Tfeature feature;
		Item item;
		
		List<String> ids = kdataService.getDailyAverageAmountTops(top);
		
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			
			if(this.setLatestKdata(id, true)) {
				feature = turtle.getFeature(id);
				if(feature!=null) {
					item = itemService.getItem(id);

					preyMap = new HashMap<String,String>();
					preyMap.put("itemID", id);
					preyMap.put("code", item.getCode());
					preyMap.put("name", item.getName());
					preyMap.put("low", df.format(feature.getOpenLow()));
					preyMap.put("high", df.format(feature.getOpenHigh()));
					preyMap.put("now", df.format(feature.getNow()));
					preyMap.put("drop", df.format(feature.getDropLow()));
					preyMap.put("hlgap", feature.getHlgap().toString());
					preyMap.put("nhgap", feature.getNhgap().toString());
					preyMap.put("atr", df.format(feature.getAtr()));	
					preyMap.put("status", feature.getStatus().toString());	
					
					views.add(new TurtleView(preyMap));

				}
			}
		}
		System.out.println("getting average amount tops  "+top+" done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
		
	}

	@Override
	public void generateAvTops() {
		Integer duration = turtle==null ? 89 : turtle.getOpenDuration();
		
		List<String> ids = kdataRealtimeSpider.getLatestDailyTop(100);
		//System.out.println(ids);
		
		kdataService.generateDailyAverageAmountTops(ids, duration);
		
	}

	@Override
	public List<TurtleView> getBluechips() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("getting bluechips ......");
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;
		
		Item item;
		
		Tfeature feature;
		List<String> ids = balloonOperationRepository.getBluechipIDs();
		
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			
			if(this.setLatestKdata(id,true)) {
				feature = turtle.getFeature(id);
				if(feature!=null) {
					item = itemService.getItem(id);

					preyMap = new HashMap<String,String>();
					preyMap.put("itemID", id);
					preyMap.put("code", item.getCode());
					preyMap.put("name", item.getName());
					preyMap.put("low", df.format(feature.getOpenLow()));
					preyMap.put("high", df.format(feature.getOpenHigh()));
					preyMap.put("now", df.format(feature.getNow()));
					preyMap.put("drop", df.format(feature.getDropLow()));
					preyMap.put("hlgap", feature.getHlgap().toString());
					preyMap.put("nhgap", feature.getNhgap().toString());
					preyMap.put("atr", df.format(feature.getAtr()));	
					preyMap.put("status",feature.getStatus().toString());	
					
					views.add(new TurtleView(preyMap));

				}
			}
		}
		System.out.println("get bluechips done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
	}

	
}
