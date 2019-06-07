package com.rhb.istock.trade.turtle.simulation;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileUtil;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.trade.turtle.domain.Turtle;
import com.rhb.istock.trade.turtle.simulation.api.AmountView;
import com.rhb.istock.trade.turtle.simulation.api.BreakerView;
import com.rhb.istock.trade.turtle.simulation.api.HoldView;
import com.rhb.istock.trade.turtle.simulation.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.repository.TurtleSimulationRepository;

/*
 * 所谓static，即每天要交易的item是确定的，如上证50、每日交易量top50、日均交易量top50、等
 */
@Service("turtleStaticSimulation")
public class TurtleStaticSimulation implements TurtleSimulation{
	@Value("${reportPath}")
	private String reportPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	boolean cache = true;
	
	Turtle turtle = null;
	
	@Override
	public Map<String, String> simulate(TreeMap<LocalDate,List<String>> dailyItems, Toption option, boolean cache) {
		if(dailyItems==null || dailyItems.size()==0) return null;
		
		this.cache = cache;
		
		if(option==null) {
			turtle = new Turtle();			
		}else {
			turtle = new Turtle(option.getDeficitFactor(),
					option.getOpenDuration(),
					option.getDropDuration(),
					option.getMaxOfLot(),
					option.getInitCash(),
					option.getStopStrategy(),
					option.getCancels(),
					option.getGap());
		}
		
		long days = dailyItems.lastKey().toEpochDay()- dailyItems.firstKey().toEpochDay();
		
		List<String> itemIDs;
		Set<String> tmp ;
		int i = 0;
		for(Map.Entry<LocalDate, List<String>> entry : dailyItems.entrySet()) {
			Progress.show((int)days, i++, entry.getKey().toString());
			
			turtle.clearDatas(); //开始前清除历史记录，当某个item停牌几天，原记录可能会缺失
			
			itemIDs = entry.getValue();
			tmp = new HashSet<String>();
			tmp.addAll(itemIDs);
			//System.out.println(tmp);
			if(itemIDs!=null && !itemIDs.isEmpty()) {
				for(String id :turtle.getItemIDsOfHolds()) {
					//System.out.println("add " + id + "?");
					if(!tmp.contains(id)) {
						//System.out.println(" yes !");
						itemIDs.add(id);   //加入在手的ID
					}
				}
				
				for(String itemID : itemIDs) {
					setDailyKdata(itemID, entry.getKey()); //放入beginDate之前的历史记录
					setLatestKdata(itemID, entry.getKey()); //放入当前记录
				}
				System.out.println("");
				//turtle.doIt(isGoodTime(entry.getKey(),30)); //以上证指数的30日均线为准绳，线上操作，线下休息，个股要止损，可加仓（年复合增长率7%，盈率42%）
				//turtle.doIt(isGoodTime1(entry.getKey()));  //以上证指数的是否突破和30日均线为准绳，突破操作，跌破30日均线休息，个股要止损，可加仓（年复合增长率6%，盈率39%）
				//System.out.println(itemIDs);
				turtle.doIt(itemIDs,true); //不管大盘，个股可加仓、要止损。（年复合增长率10%，盈率37%）
			}
		}
		
		Map<String, String> result = turtle.result();
		System.out.println("initCash: " + result.get("initCash"));
		System.out.println("cash: " + result.get("cash"));
		System.out.println("value: " + result.get("value"));
		System.out.println("total: " + result.get("total"));
		System.out.println("CAGR: " + result.get("cagr"));
		System.out.println("winRatio: " + result.get("winRatio"));
		FileUtil.writeTextFile(reportPath + "/simulation_detail" + System.currentTimeMillis() + ".csv", result.get("CSV"), false);
		FileUtil.writeTextFile(reportPath + "/simulation_dailyAmount" + System.currentTimeMillis() + ".csv", result.get("dailyAmount"), false);
		FileUtil.writeTextFile(reportPath + "/simulation_breakers" + System.currentTimeMillis() + ".csv", result.get("breakers"), false);
		return result;
	}

	private void setDailyKdata(String itemID, LocalDate theDate) {
		Kbar kbar;
		Kdata kdata = kdataService.getDailyKdata(itemID, theDate, turtle.getOpenDuration(), cache);
		
		List<LocalDate> dates = kdata.getDates();
		//System.out.println(dates);
		for(LocalDate date : dates) {
			kbar = kdata.getBar(date);
			//System.out.println(date + "," + kbar);
			turtle.addDailyData(itemID,date,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
	}
	
	private Kbar setLatestKdata(String itemID, LocalDate theDate) {
		Kbar kbar = kdataService.getKbar(itemID, theDate, cache);
		if(kbar!=null) {
			turtle.addLatestData(itemID,theDate ,kbar.getOpen(), kbar.getHigh(), kbar.getLow(), kbar.getClose());
		}
		return kbar;
	}
	
	
	/*
	 * 操作系统：线上操作，向下休息
	 */
	private boolean isGoodTime(LocalDate theDate, Integer duration) {
		Kdata kdata = kdataService.getDailyKdata("sh000001", theDate, duration, true);
		return kdata.isAboveAvaragePrice()==1 ? true : false; 
	}
	
	
	private boolean isGoodTime(LocalDate theDate) {
		boolean flag = false;
		Map<String, String> goodTimes = new HashMap<String,String>();
		goodTimes.put("2006/06/01","2006/06/09");
		goodTimes.put("2006/07/03","2006/07/31");
		goodTimes.put("2006/10/09","2007/02/05");
		goodTimes.put("2007/05/22","2007/06/04");
		goodTimes.put("2007/07/26","2007/11/05");
		goodTimes.put("2009/02/16","2009/02/27");
		goodTimes.put("2009/04/01","2009/08/12");
		goodTimes.put("2010/10/08","2010/11/16");
		goodTimes.put("2011/04/08","2011/04/25");
		goodTimes.put("2012/12/25","2013/02/21");
		goodTimes.put("2014/07/28","2014/10/23");
		goodTimes.put("2014/10/31","2015/02/02");
		goodTimes.put("2015/03/16","2015/06/19");
		goodTimes.put("2016/08/15","2016/09/12");
		goodTimes.put("2016/11/08","2016/12/12");
		goodTimes.put("2017/08/25","2017/11/03");
		goodTimes.put("2017/11/13","2017/11/17");
		goodTimes.put("2018/01/18","2018/02/06");
		goodTimes.put("2019/02/22","2019/04/25");
		
		LocalDate bDate, eDate;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		for(Map.Entry<String, String> entry : goodTimes.entrySet()) {
			bDate = LocalDate.parse(entry.getKey(), dtf).minusDays(1);
			eDate = LocalDate.parse(entry.getValue(), dtf);
			if(theDate.isAfter(bDate) && theDate.isBefore(eDate)) {
				return true;
			}
		}
		
		return flag;
	}

	//牛市
	private boolean isGoodTime1(LocalDate theDate) {
		boolean flag = false;
		Map<String, String> goodTimes = new HashMap<String,String>();
		goodTimes.put("2005/12/09","2007/11/05");
		goodTimes.put("2009/01/22","2009/08/12");
		goodTimes.put("2014/07/22","2015/06/19");
		goodTimes.put("2019/02/11","2019/04/25");
		
		LocalDate bDate, eDate;
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		
		for(Map.Entry<String, String> entry : goodTimes.entrySet()) {
			bDate = LocalDate.parse(entry.getKey(), dtf).minusDays(1);
			eDate = LocalDate.parse(entry.getValue(), dtf);
			if(theDate.isAfter(bDate) && theDate.isBefore(eDate)) {
				return true;
			}
		}
		
		return flag;
	}
	
	@Override
	public List<BreakerView> getBreakers(String type, String year, LocalDate date) {
		List<BreakerView> views = new ArrayList<BreakerView>();
		Map<LocalDate,List<String>> breakers = turtleSimulationRepository.getBreakers(type, year);
		List<String> ids = breakers.get(date);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new BreakerView(id,itemService.getItem(id).getName()));
			}
		}
		return views;
	}

	@Override
	public List<HoldView> getHolds(String type, String year, LocalDate date) {
		List<HoldView> views = new ArrayList<HoldView>();
		
		Map<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys(type, year);
		Map<LocalDate,List<String>> sells = turtleSimulationRepository.getSells(type, year);
		
		List<String> ids = generateHolds(type,year,date);
		//System.out.println("holds: " + ids);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new HoldView(id,itemService.getItem(id).getName(),0));
			}
		}
		
		ids = buys.get(date);
		//System.out.println("buys: " + ids);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				views.add(new HoldView(id,itemService.getItem(id).getName(),1));
			}
		}
		
		ids = sells.get(date);
		if(ids!=null && !ids.isEmpty()) {
			for(String id : ids) {
				for(HoldView view : views) {
					if(view.getItemID().equals(id)) {
						view.setStatus(-1);
					}
				}
			}
		}	
		
		return views;
	}
	
	private List<String> generateHolds(String type, String year, LocalDate date){
		List<String> ids = new ArrayList<String>();
		
		Map<LocalDate,List<String>> buys = turtleSimulationRepository.getBuys(type, year);
		Map<LocalDate,List<String>> sells = turtleSimulationRepository.getSells(type, year);
		
		for(Map.Entry<LocalDate,List<String>> entry : buys.entrySet()) {
			if(entry.getKey().isBefore(date)) {
				ids.addAll(entry.getValue());
			}
		}
		
		for(Map.Entry<LocalDate,List<String>> entry : sells.entrySet()) {
			if(entry.getKey().isBefore(date)) {
				ids.removeAll(entry.getValue());
			}
		}
		
		return ids;
	}

	@Override
	public AmountView getAmount(String type, String year, LocalDate date) {
		AmountView view = new AmountView(0,0);
		
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts(type, year);
		AmountEntity entity = amounts.get(date);
		if(entity!=null) {
			view = new AmountView(entity.getCash(), entity.getValue());
		}
		
		return view;
	}

	@Override
	public List<String> getDates(String type, String year) {
		List<String> dates = new ArrayList<String>();
		Map<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts(type, year);
		for(LocalDate date : amounts.keySet()) {
			dates.add(date.toString());
		}
		return dates;
	}

	@Override
	public void evictCache() {
		System.out.println("evictCache");
		turtleSimulationRepository.evictAmountsCache();
		turtleSimulationRepository.evictBreakersCache();
		turtleSimulationRepository.evictBuysCache();
		turtleSimulationRepository.evictSellsCache();
	}

}
