package com.rhb.istock.trade.turtle.operation;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.fdata.tushare.FdataServiceTushare;
import com.rhb.istock.fdata.tushare.GrowModel;
import com.rhb.istock.item.Item;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.kdata.api.KdatasView;
import com.rhb.istock.selector.SelectorService;
import com.rhb.istock.selector.b21.B21Service;
import com.rhb.istock.selector.fina.FinaService;
import com.rhb.istock.selector.fina.QuarterCompare;
import com.rhb.istock.selector.hold.HoldEntity;
import com.rhb.istock.selector.potential.Potential;
import com.rhb.istock.trade.turtle.domain.Tfeature;
import com.rhb.istock.trade.turtle.domain.Tbar;
import com.rhb.istock.trade.turtle.domain.Turtle;
import com.rhb.istock.trade.turtle.operation.api.ItemView;
import com.rhb.istock.trade.turtle.operation.api.HoldView;
import com.rhb.istock.trade.turtle.operation.api.IndustryView;
import com.rhb.istock.trade.turtle.operation.api.PotentialView;
import com.rhb.istock.trade.turtle.operation.api.ForecastView;
import com.rhb.istock.trade.turtle.operation.api.TurtleView;

@Service("turtleOperationServiceImp")
public class TurtleOperationServiceImp implements TurtleOperationService {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;

	@Autowired
	@Qualifier("selectorServiceImp")
	SelectorService selectorService;

	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	@Autowired
	@Qualifier("b21Service")
	B21Service b21Service;
	
	DecimalFormat df = new DecimalFormat("0.00"); 
	DecimalFormat df_integer = new DecimalFormat("000"); 

	Turtle turtle = null;
	
	List<Potential> potentials = null;
	Map<String,Integer> hltops = null;
	Map<String,Integer> avtops = null;
	
	Integer bxxtops = 8;
	Integer tops = 55;
	
	protected static final Logger logger = LoggerFactory.getLogger(TurtleOperationServiceImp.class);
	
	@Override
	public void init() {
		long beginTime=System.currentTimeMillis(); 
		logger.info("TurtleOperationService init...");
		
		this.turtle = new Turtle();
		this.potentials = null;
		this.hltops = selectorService.getHighLowTops(tops);
		this.avtops = selectorService.getLatestAverageAmountTops(tops);
		
		logger.info("\nTurtleOperationService init done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");          
	}
	
	@Override
	public List<HoldView> getHolds() {
		List<HoldView> holds = new ArrayList<HoldView>();
		
		Map<String,Muster> musters = kdataService.getLatestMusters();

		Item item;
		HoldView hold;
		Muster muster;
		
		Map<String, String> favors = selectorService.getFavors();
		List<HoldEntity> entities = selectorService.getHolds();
		Map<String,QuarterCompare> qcs = finaService.getQuarterCompares();
		
		List<String> holdIDs = new ArrayList<String>();
		for(HoldEntity entity : entities) {
			holdIDs.add(entity.getItemID());
		}
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		Map<String,String> b21s = b21Service.isB21(holdIDs, endDate);
		
		for(HoldEntity entity : entities) {
			this.setLatestKdata(entity.getItemID(),true);
			
			item = itemService.getItem(entity.getItemID());
			muster = musters.get(entity.getItemID());
			
			hold = new HoldView();
			hold.setItemID(entity.getItemID());
			hold.setCode(item.getCode());
			hold.setName(item.getName());
			
			hold.setIndustry(item.getIndustry());
			hold.setArea(item.getArea());
			hold.setBuyPrice(entity.getPrice());
			hold.setBuyDate(entity.getDate());;
			hold.setNowPrice(muster==null ? "" : muster.getLatestPrice().toString());
			hold.setTopic(this.getTopic(item.getItemID()));
			hold.setLabel(entity.getLabel());
			hold.setFavor(favors.get(item.getItemID()));
			if(qcs.get(item.getItemID())!=null) {
				hold.addFavor(qcs.get(item.getItemID()).getInfo());
			}
			hold.setStatus(b21s.get(item.getItemID()));
			
			holds.add(hold);
			
		}
		return holds;
	}

	private void setDailyKdata(String itemID, boolean byCache) {
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		
		Kbar kbar;
		Kdata kdata = kdataService.getKdata(itemID, endDate, turtle.getOpenDuration(), byCache);
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
		
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		
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
	public List<ItemView> getFavors() {
		Map<String, String> favors = selectorService.getFavors();
		Map<String,QuarterCompare> qcs = finaService.getQuarterCompares();

		List<ItemView> views = buildItemViews(new ArrayList<String>(favors.keySet()));

		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		Map<String,String> b21s = b21Service.isB21(new ArrayList<String>(favors.keySet()), endDate);
		
		for(ItemView view : views) {
			view.setLabel(favors.get(view.getItemID()));
			if(qcs.get(view.getItemID())!=null) {
				view.addLabel(qcs.get(view.getItemID()).getInfo());
			}
			view.setStatus(b21s.get(view.getItemID()));
		}
		
		return views;
	}
	

	@Override
	public List<PotentialView> getPotentials(String type, LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("getting potentials of "+type+" views of "+date.toString()+" ......");
		List<PotentialView> views = new ArrayList<PotentialView>();
		PotentialView view;
		
		List<Potential> potentials = new ArrayList<Potential>(selectorService.getPotentials(date).values());
		
		//System.out.println(potentials.size());
		
		Collections.sort(potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
				if(type.equals("hlb")) {
					if(o1.getHLGap().compareTo(o2.getHLGap())==0) {
						return o1.getClose().compareTo(o2.getClose());
					}else {
						return o1.getHLGap().compareTo(o2.getHLGap());
					}
				}else if(type.equals("avb")) {
					return o2.getAverageAmount().compareTo(o1.getAverageAmount());
				}else {
					return 0;
				}
			}
		});
		
		int i=0;
		for(Potential potential : potentials) {
			view = new PotentialView(potential.getItemID(),potential.getItemName(),potential.getIndustry(),potential.getHLGap(), potential.getHNGap());
			views.add(view);
			if(i++ > tops) {
				break;
			}
		}
		
		//System.out.println(views.size());
		
		//List<Muster> musters = selectorService.getTops(21,date);
		
		logger.info("\ngetting potentials done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     

		return views;
	}

	private List<TurtleView> getLpb2(){
		Map<String,String> ids = selectorService.getLpb2();
		List<TurtleView> views = this.getTurtleViews(new ArrayList(ids.keySet()), "LPB2",true); 
		for(TurtleView view : views) {
			view.setLabel(ids.get(view.getItemID()));
		}
		
		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				//BigDecimal now1 = new BigDecimal(o1.getNow());
				//BigDecimal now2 = new BigDecimal(o2.getNow());
				
				//return now1.compareTo(now2);
				return o1.getLabel().compareTo(o2.getLabel());
			}
		});
		return views;
	}
	
	private List<TurtleView> getHlb2(){
		Map<String,String> ids = selectorService.getHlb2();
		List<TurtleView> views = this.getTurtleViews(new ArrayList(ids.keySet()), "HLB2",true); 
		for(TurtleView view : views) {
			view.setLabel(ids.get(view.getItemID()));
		}
		
		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal now1 = new BigDecimal(o1.getNow());
				BigDecimal now2 = new BigDecimal(o2.getNow());
				
				return now1.compareTo(now2);
			}
		});
		return views;
	}
	
	private List<TurtleView> getLpb(){
		Map<String,String> ids = selectorService.getLpbs();
		List<TurtleView> views = this.getTurtleViews(new ArrayList(ids.keySet()), "LPB",true); 
		for(TurtleView view : views) {
			view.setLabel(ids.get(view.getItemID()));
		}
		
		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal now1 = new BigDecimal(o1.getNow());
				BigDecimal now2 = new BigDecimal(o2.getNow());
				
				return now1.compareTo(now2);
			}
		});
		return views;
	}
	
	private List<TurtleView> getDrum(){
		Map<String,String> ids = selectorService.getDrums();
		List<TurtleView> views = this.getTurtleViews(new ArrayList(ids.keySet()), "DRUM",true); 
		for(TurtleView view : views) {
			view.setLabel(ids.get(view.getItemID()));
		}
		
		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal now1 = new BigDecimal(o1.getNow());
				BigDecimal now2 = new BigDecimal(o2.getNow());
				
				return now1.compareTo(now2);
			}
		});
		return views;
	}
	
	private List<TurtleView> getBav(){
		Map<String,String> ids = selectorService.getBavs();
		List<TurtleView> views = this.getTurtleViews(new ArrayList<String>(ids.keySet()), "BAV",true); 
		for(TurtleView view : views) {
			view.setLabel(ids.get(view.getItemID()));
		}
		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal now1 = new BigDecimal(o1.getLabel());
				BigDecimal now2 = new BigDecimal(o2.getLabel());
				
				return now1.compareTo(now2);
			}
		});
		return views;
	}
	

	@Override
	public List<TurtleView> getPotentials(String type) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("getting potential views ......");

		if(type.equals("lpb2")) {
			return this.getLpb2();
		}else if(type.equals("hlb2")) {
			return this.getHlb2();
		}else if(type.equals("lpb")) {
			return this.getLpb();
		}else if(type.equals("bav")) {
			return this.getBav();
		}else if(type.equals("drum")) {
			return this.getDrum();
		}
		
		
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		TurtleView view;
		
		Item item;

		List<String> holds = selectorService.getHoldIDs();
		List<String> labels;
		
/*		if(this.potentials == null) {
			this.createPotentialsWithLatestMarketData();			
		}else {
			this.refreshPotentialsWithLatestMarketDate();
		}*/

		this.createPotentialsWithLatestMarketData();			
		
		int i=1;
		for(Potential potential : this.potentials) {
			Progress.show(this.potentials.size(), i++, "assembling view... " + potential.getItemID());
			view = new TurtleView();
			if(potential.getHlb()!=null || potential.getAvb()!=null) {
				view.setItemID(potential.getItemID());
				view.setHigh(df.format(potential.getHighest()));
				view.setLow(df.format(potential.getLowest()));
				view.setPclose(df.format(potential.getClose()));
				view.setNow(df.format(potential.getLatestPrice()));
				view.setHlgap(df_integer.format(potential.getHLGap()));
				view.setNhgap(df_integer.format(potential.getHNGap()));
				view.setStatus(potential.getStatus());
				
				view.setTopic(this.getTopic(potential.getItemID()));
				
				labels = potential.getLabels();
				labels.addAll(this.getLabels(potential.getItemID()));
				view.setLabels(labels);
				
				item = itemService.getItem(potential.getItemID());
				view.setCode(holds!=null && holds.contains(potential.getItemID()) ? "*" + item.getCode() : item.getCode());
				view.setName(item.getName());
				view.setArea(item.getArea());
				view.setIndustry(item.getIndustry());
				
				if(type.equals("hlb") && potential.getHlb()!=null) {
					views.add(view);
				}else if(type.equals("avb") && potential.getAvb()!=null) {
					views.add(view);
				}
			}
		}		

		Collections.sort(views, new Comparator<TurtleView>() {
			@Override
			public int compare(TurtleView o1, TurtleView o2) {
				BigDecimal nh1 = new BigDecimal(o1.getNhgap());
				BigDecimal nh2 = new BigDecimal(o2.getNhgap());
				
				return nh1.compareTo(nh2);
			}
		});	
		
		logger.info("\ngetting potential views done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     
		
		return views;
	}
	
	private List<ForecastView> buildQuarterCompareViews(List<String> itemIDs) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("building QuarterCompareViews ......");
		
		List<ForecastView> views = new ArrayList<ForecastView>();
		
		Map<String,String> preyMap;

		Item item;

		List<String> holds = selectorService.getHoldIDs();
		Map<String,Muster> musters = kdataService.getLatestMusters();
		Muster muster;
		
		int i=1;
		for(String id : itemIDs) {
			Progress.show(itemIDs.size(),i++,id);
			
				item = itemService.getItem(id);
				if(item == null) {
					System.err.println("item of " + id + " is null!!!");
				}else {
					muster = musters.get(id);
					preyMap = new HashMap<String,String>();
					preyMap.put("itemID", id);
					preyMap.put("code", (holds!=null && holds.contains(id)) ? "*" + item.getCode() : item.getCode());
					preyMap.put("name", item.getName());
					preyMap.put("industry", item.getIndustry());
					preyMap.put("area", item.getArea());
					preyMap.put("topic", this.getTopic(id));
					preyMap.put("price", muster==null ? "0" : muster.getLatestPrice().toString());
					views.add(new ForecastView(preyMap));						
				}
		}
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("getting b21 views done! 用时：" + used + "秒");     
		
		return views;
	}

	
	private List<ItemView> buildItemViews(List<String> itemIDs) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("building itemViews ......");
		
		List<ItemView> views = new ArrayList<ItemView>();
		
		Map<String,String> preyMap;

		Item item;

		List<String> holds = selectorService.getHoldIDs();
		Map<String,Muster> musters = kdataService.getLatestMusters();
		Muster muster;
		
		int i=1;
		for(String id : itemIDs) {
			Progress.show(itemIDs.size(),i++,id);
			
				item = itemService.getItem(id);
				if(item == null) {
					System.err.println("item of " + id + " is null!!!");
				}else {
					muster = musters.get(id);
					preyMap = new HashMap<String,String>();
					preyMap.put("itemID", id);
					preyMap.put("code", (holds!=null && holds.contains(id)) ? "*" + item.getCode() : item.getCode());
					preyMap.put("name", item.getName());
					preyMap.put("industry", item.getIndustry());
					preyMap.put("area", item.getArea());
					preyMap.put("topic", this.getTopic(id));
					preyMap.put("price", muster==null ? "0" : muster.getLatestPrice().toString());
					views.add(new ItemView(preyMap));						
				}
		}
		
		Collections.sort(views, new Comparator<ItemView>() {
			@Override
			public int compare(ItemView o1, ItemView o2) {
				BigDecimal now1 = new BigDecimal(o1.getPrice());
				BigDecimal now2 = new BigDecimal(o2.getPrice());
				
				return now1.compareTo(now2);
			}
		});		
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("getting b21 views done! 用时：" + used + "秒");     
		
		return views;
	}

	
	private List<TurtleView> getTurtleViews(List<String> itemIDs, String name, boolean reSort) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("getting " + name + " views ......");
		
		List<TurtleView> views = new ArrayList<TurtleView>();
		
		//DecimalFormat df = new DecimalFormat("0.00"); 
		Map<String,String> preyMap;

		Tfeature feature;
		Item item;

		List<String> holds = selectorService.getHoldIDs();

		int i=1;
		for(String id : itemIDs) {
			Progress.show(itemIDs.size(),i++,id);
			
			if(this.setLatestKdata(id, true)) {   //放入历史K线数据，很耗时
				feature = turtle.getFeature(id);
				if(feature!=null) {
					item = itemService.getItem(id);
					
					if(item == null) {
						System.err.println("item of " + id + " is null!!!");
					}else {
						preyMap = new HashMap<String,String>();
						preyMap.put("itemID", id);
						preyMap.put("code", (holds!=null && holds.contains(id)) ? "*" + item.getCode() : item.getCode());
						preyMap.put("name", item.getName());
						preyMap.put("industry", item.getIndustry());
						preyMap.put("area", item.getArea());
						preyMap.put("low", df.format(feature.getOpenLow()));
						preyMap.put("high", df.format(feature.getOpenHigh()));
						preyMap.put("now", df.format(feature.getNow()));
						preyMap.put("drop", df.format(feature.getDropLow()));
						preyMap.put("hlgap", feature.getHlgap().toString());
						preyMap.put("nhgap", feature.getNhgap().toString());
						preyMap.put("atr", df.format(feature.getAtr()));	
						preyMap.put("status", feature.getStatus().toString());	
						preyMap.put("topic", this.getTopic(id));
						preyMap.put("label", "");
						
						views.add(new TurtleView(preyMap));						
					}
				}
			}
		}
		
		if(reSort) {
			Collections.sort(views, new Comparator<TurtleView>() {
				@Override
				public int compare(TurtleView o1, TurtleView o2) {
					BigDecimal nh1 = new BigDecimal(o1.getNhgap());
					BigDecimal nh2 = new BigDecimal(o2.getNhgap());
					
					return nh1.compareTo(nh2);

				}
			});	
		}
		
		logger.info("\ngetting "+name+" views done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     
		
		return views;
	}
	
	private String getTopic(String itemID) {
		String[] tops = itemService.getTopicTops(5);
		String topic = itemService.getTopic(itemID);
		int i=0;
		String start = "*";
		for(String top : tops) {
			if(topic.indexOf(top)!=-1) {
				if(i==0) start="***";
				if(i==1 || i==2) start="**";
				topic = topic.replaceAll(top, start+top);
			}
			i++;
		}
		return topic;
	}
	
	private List<String> getLabels(String itemID) {
		List<String> labels = new ArrayList<String>();
		
		if(isFavors(itemID)) labels.add("favor");
		if(isBluechip(itemID)) labels.add("bluechip");
		
		return labels;
	}
	

	private boolean isBluechip(String itemID) {
		Set<String> bluechips = new HashSet<String>(selectorService.getLatestBluechipIDs());
		return bluechips.contains(itemID);
	}
	
	private boolean isFavors(String itemID) {
		Map<String, String> favors = selectorService.getFavors();
		return favors.containsKey(itemID);
	}
	
	private void refreshPotentialsWithLatestMarketDate() {
		long beginTime=System.currentTimeMillis(); 
		logger.info("refreshPotentialsWithLatestMarketDate ......");

		Map<String, Potential> ps= selectorService.getLatestPotentials();
		
		//System.out.println("potentials.size() = " + ps.size());
		
		if(ps.size()>0) {
			int i=1;
			for(Potential potential : this.potentials) {
				Progress.show(this.potentials.size(),i++, " refresh latest price and amount ... " + potential.getItemID());
				if(ps.get(potential.getItemID())!=null) {
					potential.setLatestPrice(ps.get(potential.getItemID()).getLatestPrice());
					potential.setAmount(ps.get(potential.getItemID()).getAmount());
				}else {
					System.out.println(" kicked out of potentials!");
					// TODO  落选potential后，应该从collection中remove
				}
			}
			
			this.setBHL();
			this.setBDT();
			this.setBAV();
		}

		logger.info("\nrefreshPotentialsWithLatestMarketDate done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     
	}
	
	private void createPotentialsWithLatestMarketData(){
		long beginTime=System.currentTimeMillis(); 
		logger.info("createPotentialsWithLatestMarketData ......");

		//Map<String,Integer> datops = selectorService.getLatestDailyAmountTops(21);
		
		this.potentials = new ArrayList<Potential>(selectorService.getLatestPotentials().values());

		int i=1;
		for(Potential potential : potentials) {
			Progress.show(potentials.size(),i++, " sorting by hlb/avb/dtb... " + potential.getItemID());
			
			potential.setHlb(this.hltops.get(potential.getItemID()));
			potential.setAvb(this.avtops.get(potential.getItemID()));
			//potential.setDtb(datops.get(potential.getItemID()));
		}
		
		//this.setBHL();
		//this.setBDT();
		//this.setBAV();

		logger.info("\ncreatePotentialsWithLatestMarketData done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     
		
	}
	
	private void setBHL(){
		Collections.sort(this.potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
				return o1.getHLGap().compareTo(o2.getHLGap());
			}
		});
		int i=1;
		for(Potential potential : this.potentials) {
			if(potential.getStatus()=="2") {
				potential.setBhl(i++);
			}
			if(i>=bxxtops) {
				potential.setBhl(null);
			};
		}
	}
	
	private void setBDT(){
		Collections.sort(this.potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
				return o2.getAmount().compareTo(o1.getAmount());
			}
		});
		int i=1;
		for(Potential potential : this.potentials) {
			if(potential.getStatus()=="2") {
				potential.setBdt(i++);
			}
			if(i>=bxxtops) {
				potential.setBdt(null);
			}
		}
	}

	private void setBAV(){
		Collections.sort(this.potentials, new Comparator<Potential>() {
			@Override
			public int compare(Potential o1, Potential o2) {
				return o2.getAverageAmount().compareTo(o1.getAverageAmount());
			}
		});
		int i=1;
		for(Potential potential : this.potentials) {
			if(potential.getStatus()=="2") {
				potential.setBav(i++);
			}
			if(i>=bxxtops) {
				potential.setBav(null);
			};
		}
	}

	@Override
	public String[] getTopics() {
		return itemService.getTopicTops(5);
	}

	@Override
	public void redoPotentials() {
		logger.info("redoPotentials ......");
		this.createPotentialsWithLatestMarketData();
		kdataService.updateLatestMusters();
	}

	@Override
	public List<ItemView> getPowers() {
		//List<String> ps = selectorService.getPowerIDs();
		String b_date = "20161231";
		String e_date = "20191231";
		Integer n = 3;

		List<String> models = fdataServiceTushare.getGrowModels(b_date, e_date, n);
		
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		Map<String,String> b21s = b21Service.isB21(models, endDate);

		List<ItemView> views = buildItemViews(models);
		for(ItemView view : views) {
			view.setStatus(b21s.get(view.getItemID()));
		}
		
		return views;
	}

	@Override
	public Map<String,IndustryView> getPotentialIndustrys(LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		logger.info("getting potentials industrys of "+date.toString()+" ......");
		Map<String,IndustryView> industryViews = new HashMap<String,IndustryView>();
		IndustryView industryView;
		PotentialView potentialView;
		
		List<Potential> potentials = new ArrayList<Potential>(selectorService.getPotentials(date).values());
		
		int i=0;
		for(Potential potential : potentials) {
			potentialView = new PotentialView(potential.getItemID(),potential.getItemName(),potential.getIndustry(),potential.getHLGap(),potential.getHNGap());
			industryView = industryViews.get(potential.getIndustry());
			if(industryView == null) {
				industryView = new IndustryView(potential.getIndustry());
				industryViews.put(potential.getIndustry(), industryView);
			}
			industryView.addPotential(potentialView);
		}

		logger.info(String.format("\ngetting potentials industrys done! there are %d industrys", industryViews.size()));
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		logger.info("用时：" + used + "秒");     
		
		return industryViews;
	}

	@Override
	public List<PotentialView> getPotentials_hlb(LocalDate date) {
		List<PotentialView> views = new ArrayList<PotentialView>();
		List<Potential> potentials = selectorService.getPotentials_hlb(date, tops);
		PotentialView view;
		for(Potential potential : potentials) {
			view = new PotentialView(potential.getItemID(),potential.getItemName(),potential.getIndustry(),potential.getHLGap(), potential.getHNGap());
			view.setIndustryHot(potential.getIndustryHot());
			views.add(view);
		}
		
		return views;
	}

	@Override
	public List<PotentialView> getPotentials_avb(LocalDate date) {
		List<PotentialView> views = new ArrayList<PotentialView>();
		List<Potential> potentials = selectorService.getPotentials_avb(date, tops);
		PotentialView view;
		for(Potential potential : potentials) {
			view = new PotentialView(potential.getItemID(),potential.getItemName(),potential.getIndustry(),potential.getHLGap(), potential.getHNGap());
			view.setIndustryHot(potential.getIndustryHot());
			views.add(view);
		}
		
		return views;
	}

	@Override
	public List<ItemView> getB21s() {
		Map<String, String> favors = selectorService.getFavors();
		Map<String,QuarterCompare> qcs = finaService.getQuarterCompares();
		Map<String,String> ids = selectorService.getB21();
		List<ItemView> views = this.buildItemViews(new ArrayList(ids.keySet())); 
		for(ItemView view : views) {
			view.setLabel(favors.get(view.getItemID()));
			if(qcs.get(view.getItemID())!=null) {
				view.addLabel(qcs.get(view.getItemID()).getInfo());
			}
			view.setStatus(ids.get(view.getItemID()));
		}
		
		return views;
	}

	@Override
	public List<ForecastView> getForecastViews() {
		Map<String,QuarterCompare> qcs = finaService.getForecasts();
		
		List<String> ids = new ArrayList<String>(qcs.keySet());
		
		LocalDate endDate = kdataService.getLatestMarketDate("sh000001");
		Map<String,String> b21s = b21Service.isB21(ids, endDate);
		Map<String, String> favors = selectorService.getFavors();

		List<ForecastView> views = buildQuarterCompareViews(ids);
		for(ForecastView view : views) {
			view.setStatus(b21s.get(view.getItemID()));
			view.setAll(qcs.get(view.getItemID()));
			view.setLabel(favors.get(view.getItemID()));
		}
		
		Collections.sort(views, new Comparator<ForecastView>() {
			@Override
			public int compare(ForecastView o1, ForecastView o2) {
				return new Integer(o2.getPrevious_netprofit_yoy()).compareTo(new Integer(o1.getPrevious_netprofit_yoy()));
			}
			
		});
		
		return views;
	}

	
}
