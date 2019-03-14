package com.rhb.istock.trade.turtle.service;

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
import com.rhb.istock.trade.turtle.api.HoldView;
import com.rhb.istock.trade.turtle.api.KdatasView;
import com.rhb.istock.trade.turtle.api.PreyView;
import com.rhb.istock.trade.turtle.domain.Feature;
import com.rhb.istock.trade.turtle.domain.Tbar;
import com.rhb.istock.trade.turtle.domain.Turtle;
import com.rhb.istock.trade.turtle.repository.HoldEntity;
import com.rhb.istock.trade.turtle.repository.TurtleRepository;

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
	
	Turtle turtle = null;
	
	@Override
	public void init() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("TurtleOperationService init...");
		turtle = new Turtle();
		List<Item> items = itemService.getItems();
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++,item.getItemID());
			this.setDailyKdata(item.getItemID());
			this.setLatestKdata(item.getItemID());
		}		
		System.out.println("TurtleOperationService init done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	@Override
	public List<HoldView> getHolds() {
		List<HoldView> holds = new ArrayList<HoldView>();
		
		Feature feature;
		BigDecimal stopPrice, reopenPrice;
		Item item;
		
		HoldView hold;
		List<HoldEntity> entities = turtleRepository.getHolds();
		//System.out.println(entities);
		for(HoldEntity entity : entities) {
			this.setLatestKdata(entity.getItemID());
			
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
	public List<PreyView> getPreys() {
		List<PreyView> views = new ArrayList<PreyView>();
		
		Map<String,String> articles = turtleRepository.getFavors();
		
		Map<String,String> preyMap;
		String name,id;
		String[] columns;
		String[] lines = FileUtil.readTextFile(preysFile).split("\n");
		for(int i=1; i<lines.length; i++) {
			columns = lines[i].split(",");
			id = columns[0];
			name = articles.get(id)==null ? columns[2] : articles.get(id);
			preyMap = new HashMap<String,String>();
			preyMap.put("itemID", id);
			preyMap.put("code", columns[1]);
			preyMap.put("name", name);
			preyMap.put("low", columns[3]);
			preyMap.put("high", columns[4]);
			preyMap.put("now", columns[5]);
			preyMap.put("drop", columns[6]);
			preyMap.put("hlgap", columns[7]);
			preyMap.put("nhgap", columns[8]);
			preyMap.put("atr", columns[9]);	
			preyMap.put("status", columns[10]);	
			
			views.add(new PreyView(preyMap));
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
		
		Feature feature;
		int i=1;
		for(Item item : items) {
			Progress.show(items.size(),i++, item.getItemID());
			
			if(this.setLatestKdata(item.getItemID())) {
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
	
	private void setDailyKdata(String itemID) {
		LocalDate endDate = kdataService.getLatestMarketDate();
		
		Kbar kbar;
		Kdata kdata = kdataService.getDailyKdata(itemID, endDate, turtle.getOpenDuration());
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			turtle.addBar(itemID,date,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
	}
	
	private boolean setLatestKdata(String itemID) {
		boolean flag = false;
		
		LocalDate endDate = kdataService.getLatestMarketDate();
		
		Kbar kbar = kdataService.getLatestMarketData(itemID);
		if(kbar!=null) {
			turtle.setLatestBar(itemID,endDate ,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
			flag = true;
		}
		
		return flag;
	}

	@Override
	public KdatasView getKdatas(String itemID) {
		if(turtle == null) {
			this.init();
		}
		
		KdatasView kdata = new KdatasView();
		
		Item item = itemService.getItem(itemID);
		kdata.setItemID(itemID);
		kdata.setCode(item.getCode());
		kdata.setName(item.getName());
		
		List<Tbar> tbars = turtle.getTbars(itemID);
		for(Tbar tbar : tbars) {
			kdata.addKdata(tbar.getDate(), tbar.getOpen(), tbar.getHigh(), tbar.getLow(), tbar.getClose());
		}
		
		Tbar tbar = turtle.getLatestBar(itemID);
		kdata.addKdata(tbar.getDate(), tbar.getOpen(), tbar.getHigh(), tbar.getLow(), tbar.getClose());
		
		return kdata;
	}

	@Override
	public List<PreyView> getFavors() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate favors ......");
		
		List<PreyView> views = new ArrayList<PreyView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Feature feature;
		Map<String, String> favors = turtleRepository.getFavors();
		
		int i=1;
		for(String id : favors.keySet()) {
			Progress.show(favors.size(),i++,id);
			
			if(this.setLatestKdata(id)) {
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
					
					views.add(new PreyView(preyMap));

				}
			}
		}
		System.out.println("generate favors done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
		
	}

	@Override
	public List<PreyView> getTops(Integer top) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("generate tops " + top + " ......");
		
		List<PreyView> views = new ArrayList<PreyView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Feature feature;
		Item item;
		
		List<String> ids = kdataRealtimeSpider.getLatestDailyTop(top);
		
		int i=1;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			
			if(this.setLatestKdata(id)) {
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
					
					views.add(new PreyView(preyMap));

				}
			}
		}
		System.out.println("generate tops "+top+" done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
		
	}

}
