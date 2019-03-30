package com.rhb.istock.trade.turtle.operation;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.SelectorService;
import com.rhb.istock.selector.hold.HoldEntity;
import com.rhb.istock.trade.turtle.domain.Tfeature;
import com.rhb.istock.trade.turtle.domain.Tbar;
import com.rhb.istock.trade.turtle.domain.Turtle;
import com.rhb.istock.trade.turtle.operation.api.HoldView;
import com.rhb.istock.trade.turtle.operation.api.KdatasView;
import com.rhb.istock.trade.turtle.operation.api.TurtleView;

@Service("turtleOperationServiceImp")
public class TurtleOperationServiceImp implements TurtleOperationService {
	@Value("${preysFile}")
	private String preysFile;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;
	
	Turtle turtle = null;
	
	@Override
	public void init() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("TurtleOperationService init...");
		turtle = new Turtle();
		kdataService.evictDailyKDataCache();
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
		List<HoldEntity> entities = selectorService.getHolds();
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
			
		}
		return holds;
	}

	@Override
	public List<TurtleView> getHighLowTops(Integer top) {
		return getTurtleViews(selectorService.getLatestHighLowTops(top),"high low tops");
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
		Map<String, String> favors = selectorService.getFavors();

		List<TurtleView> views = getTurtleViews(new ArrayList<String>(favors.keySet()),"favors");
		
		for(TurtleView view : views) {
			view.setName(favors.get(view.getItemID()));
		}
		
		return views;
	}

	@Override
	public List<TurtleView> getDailyTops(Integer top) {
		return getTurtleViews(selectorService.getLatestDailyAmountTops(top),"daily tops");
	}

	@Override
	public List<TurtleView> getAvTops(Integer top) {
		return getTurtleViews(selectorService.getLatestAverageAmountTops(top),"av tops");
		
	}
	
	private List<TurtleView> getTurtleViews(List<String> itemIDs, String name) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("getting " + name + " views ......");
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Tfeature feature;
		Item item;
		
		int i=1;
		for(String id : itemIDs) {
			Progress.show(itemIDs.size(),i++,id);
			
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
		
		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal hl1 = new BigDecimal(o1.getHlgap());
				BigDecimal hl2 = new BigDecimal(o2.getHlgap());
				
				if(o1.getStatus().equals(o2.getStatus())) {
					return (hl1).compareTo(hl2);
				}else {
					return (o2.getStatus()).compareTo(o1.getStatus());
				}
			}
		});	
		
		System.out.println("getting "+name+" views done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");     
		
		return views;
		
		
	}

	@Override
	public List<TurtleView> getBluechips() {
		return getTurtleViews(selectorService.getBluechipIDs(LocalDate.now()),"bluechips");
	}

	
}
