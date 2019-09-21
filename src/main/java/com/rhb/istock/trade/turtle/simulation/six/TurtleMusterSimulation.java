package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulation")
public class TurtleMusterSimulation {
	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;
	
	Integer pool = 21;
	Integer top = 5;
	BigDecimal initCash = new BigDecimal(1000000);
	BigDecimal quota = new BigDecimal(20000); //买入每只股票的定额
	
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");

		Paul bavPaul = new Paul(initCash, quota);
		Paul bhlPaul = new Paul(initCash, quota);
		Paul bdtPaul = new Paul(initCash, quota);
		Paul avbPaul = new Paul(initCash, quota);
		Paul dtbPaul = new Paul(initCash, quota);
		Paul hlbPaul = new Paul(initCash, quota);
		
		Map<String,Muster> musters;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			musters = kdataService.getMusters(date);
			if(musters!=null && musters.size()>0) {
				bavPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(musters.values()), "bav"), date);
				bhlPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(musters.values()), "bhl"), date);
				bdtPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(musters.values()), "bdt"), date);
				
				avbPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(musters.values()), "avb"), date);
				hlbPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(musters.values()), "hlb"), date);
				dtbPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(musters.values()), "dtb"), date);
			}
		}
		
		Map<String, String> bavResult = bavPaul.result();
		Map<String, String> bhlResult = bhlPaul.result();
		Map<String, String> bdtResult = bdtPaul.result();

		Map<String, String> avbResult = avbPaul.result();
		Map<String, String> hlbResult = hlbPaul.result();
		Map<String, String> dtbResult = dtbPaul.result();

		
		turtleSimulationRepository.save("bav", bavResult.get("breakers"), bavResult.get("CSV"), bavResult.get("dailyAmount"));
		turtleSimulationRepository.save("bhl", bhlResult.get("breakers"), bhlResult.get("CSV"), bhlResult.get("dailyAmount"));
		turtleSimulationRepository.save("bdt", bdtResult.get("breakers"), bdtResult.get("CSV"), bdtResult.get("dailyAmount"));

		turtleSimulationRepository.save("avb", avbResult.get("breakers"), avbResult.get("CSV"), avbResult.get("dailyAmount"));
		turtleSimulationRepository.save("hlb", hlbResult.get("breakers"), hlbResult.get("CSV"), hlbResult.get("dailyAmount"));
		turtleSimulationRepository.save("dtb", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"));

		System.out.println("generateLastMusters done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
	}
	
	private List<Muster> getTops(List<Muster> musters,String type){
		return type.indexOf("b")==0 ? this.getBxxTops(musters, type)
				: this.getxxBTops(musters, type);
	}
	
	
	//breakers中选av，hl，dt
	private List<Muster> getBxxTops(List<Muster> musters,String type){
		List<Muster> breakers = new ArrayList<Muster>();
		for(Muster m : musters) {
			if(m.isBreaker()) {
				breakers.add(m);
			}
		}
		
		Collections.sort(breakers, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(type.equals("bav")) return o2.getAverageAmount().compareTo(o1.getAverageAmount()); //Z-A
				if(type.equals("bdt")) return o2.getAmount().compareTo(o1.getAmount());//Z-A
				if(type.equals("bhl")) return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				return 0;
			}
		});
		
		if(breakers.size()>top) {
			//System.out.println("breakers.size() = " + breakers.size());
			return breakers.subList(0, top);
		}else {
			return breakers;
		}
	}
	
	//在前21个av，hl，dt中选不超过3个reakers，
	private List<Muster> getxxBTops(List<Muster> musters,String type){
		List<Muster> breakers = new ArrayList<Muster>();

		Collections.sort(musters, new Comparator<Muster>() {
			@Override
			public int compare(Muster o1, Muster o2) {
				if(type.equals("avb")) return o2.getAverageAmount().compareTo(o1.getAverageAmount()); //Z-A
				if(type.equals("dtb")) return o2.getAmount().compareTo(o1.getAmount());//Z-A
				if(type.equals("hlb")) return o1.getHLGap().compareTo(o2.getHLGap());//A-Z
				return 0;
			}
		});

		Muster m;
		for(int i=0; i<musters.size() && i<pool; i++) {
			m = musters.get(i);
			if(m.isBreaker()) {
				breakers.add(m);
			}
		}
		
		if(breakers.size()>top) {
			return breakers.subList(0, top);
		}else {
			return breakers;
		}
	}
	
	/*
	 * 计算每天的盈率
	 * 以买入当天的收盘价和卖出当天的收盘价进行计算。
	 */
	
	public void generateDailyRatios(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("calculateDailyRatio from " + beginDate + " to " + endDate + " ......");
		
		Map<LocalDate,Map<String,Integer>> dailyMeans = new TreeMap<LocalDate,Map<String,Integer>>();
		Integer bhl=0,bav=0,bdt=0,hlb=0,avb=0,dtb=0;
		
		List<LocalDate> dates = kdataService.getMusterDates(beginDate, endDate);
		
		if(dates.size()>2) {
			Map<String,Integer> ratios = new HashMap<String,Integer>();
			ratios.put("bhl", bhl);
			ratios.put("bav", bav);
			ratios.put("bdt", bdt);
			ratios.put("hlb", hlb);
			ratios.put("avb", avb);
			ratios.put("dtb", dtb);
			dailyMeans.put(dates.get(0), ratios);  //第一天的值都为0
			
			int j=1;
			for(int i=1; i<dates.size(); i++) {
				Progress.show(dates.size(), j++, dates.get(i).toString());
				ratios = this.calculateDailyRatio(dates.get(i-1), dates.get(i));
				bhl = bhl + ratios.get("bhl"); ratios.put("bhl", bhl);
				bav = bav + ratios.get("bav"); ratios.put("bav", bav);
				bdt = bdt + ratios.get("bdt"); ratios.put("bdt", bdt);
				hlb = hlb + ratios.get("hlb"); ratios.put("hlb", hlb);
				avb = avb + ratios.get("avb"); ratios.put("avb", avb);
				dtb = dtb + ratios.get("dtb"); ratios.put("dtb", dtb);
				
				dailyMeans.put(dates.get(i), ratios);
			}
		}
		
		turtleSimulationRepository.saveDailyMeans(dailyMeans);
		
		System.out.println("calculateDailyRatio done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
	}
	
	public Map<String,Integer> calculateDailyRatio(LocalDate buyDate, LocalDate sellDate){
		Map<String,Integer> dr = new HashMap<String,Integer>();
		String[] types = {"bhl","bav","bdt","hlb","avb","dtb"};
		
		for(String type : types) {
			dr.put(type, this.calculateRatios(buyDate, sellDate, type).getResult());
		}
		
		return dr;
	}
	
	class Ratios{
		private TreeMap<Integer, Integer> rc = new TreeMap<Integer,Integer>();  // ratio,count
		
		public void put(Integer ratio) {
			if(rc.containsKey(ratio)) {
				rc.put(ratio, rc.get(ratio)+1);
			}else {
				rc.put(ratio, 1);
			}			
		}
		
		public Integer getResult() {
			Integer total = 0;
			for(Map.Entry<Integer, Integer> entry : rc.entrySet()) {
				total = total + entry.getKey()*entry.getValue();
			}
			return total;
		}
	}
	
	/*
	 * 计算买入后第二天的盈率。
	 * 如果连续盈，说明上升势头，可以买入股票
	 * 否则，属于平衡式或下跌市。
	 * 
	 */
	public Ratios calculateRatios(LocalDate buyDate, LocalDate sellDate, String type) {
		Ratios ratios = new Ratios();
		Integer ratio;
		
		Map<String,Muster> buyDateMusters = kdataService.getMusters(buyDate);
		
		List<Muster> buyDateTops = this.getTops(new ArrayList<Muster>(buyDateMusters.values()), type);

		Map<String,Muster> sellDateMusters = kdataService.getMusters(sellDate);
		Muster sell;
		
		for(Muster buy : buyDateTops) {
			if(buy!=null && buy.isBreaker() && !buy.isUpLimited()) {
				sell = sellDateMusters.get(buy.getItemID());
				if(sell!=null) {
					//以买入当天的收盘价和卖出当天的收盘价进行计算
					ratio = sell.getLatestPrice().subtract(buy.getLatestPrice()).divide(buy.getLatestPrice(),BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
					
					//System.out.format("%s %s %s buy price %f, %s day close price %f, ratio is %d.\n", 
							//type,buyDate.toString(), buy.getItemID(), buy.getLatestPrice(), sellDate.toString(), sell.getLatestPrice(), ratio);
					ratios.put(ratio);
				}
			}
		}		
		
		return ratios;
	}
	
}
