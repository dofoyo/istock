package com.rhb.istock.trade.turtle.simulation.muster;

import java.awt.datatransfer.SystemFlavorMap;
import java.io.File;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.trade.turtle.simulation.repository.TurtleSimulationRepository;

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
	Integer top = 3;
	BigDecimal initCash = new BigDecimal(2000000);
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
				bavPaul.doIt(musters, this.getBxxTops(new ArrayList<Muster>(musters.values()), "bav"), date);
				bhlPaul.doIt(musters, this.getBxxTops(new ArrayList<Muster>(musters.values()), "bhl"), date);
				bdtPaul.doIt(musters, this.getBxxTops(new ArrayList<Muster>(musters.values()), "bdt"), date);
				
				avbPaul.doIt(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "avb"), date);
				hlbPaul.doIt(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "hlb"), date);
				dtbPaul.doIt(musters, this.getxxBTops(new ArrayList<Muster>(musters.values()), "dtb"), date);
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
			//System.out.println("breakers.size() = " + breakers.size());
			return breakers.subList(0, top);
		}else {
			return breakers;
		}
	}
	
	public TreeMap<LocalDate, Ratio> calculateSecondDayWinRatio(String type) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("calculateSecondDayWinRatio ......");

		TreeMap<LocalDate, Ratio> ratios = new TreeMap<LocalDate,Ratio>();
		Ratio ratio;
		Integer r;
		
		List<LocalDate> dates = new ArrayList<LocalDate>();
		LocalDate date;
		List<File> files = FileTools.getFiles(musterPath, null, true);
		for(File file : files) {
			date = LocalDate.parse(file.getName().substring(0, 8), DateTimeFormatter.ofPattern("yyyyMMdd"));
			dates.add(date);
			//System.out.println(date);
		}
		
		Collections.sort(dates, new Comparator<LocalDate>() {
			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				return o1.compareTo(o2);
			}
		});
		
		Map<String,Muster> thisMusters;
		List<Muster> thisTops ;
		//Muster pre;
		
		Map<String,Muster> buyDateMusters;
		List<Muster> buyDateTops;
		//Muster buy;
		
		Map<String,Muster> nextMusters;
		List<Muster> nextTops;
		Muster next;
		
		int j=0;
		for(int i=2; i<dates.size(); i++) {
			Progress.show(dates.size(), j++, dates.get(i).toString());
			
			ratio = new Ratio();
			
			nextMusters = kdataService.getMusters(dates.get(i));
			//nextTops = this.getBxxTops(new ArrayList<Muster>(nextMusters.values()), type);
			nextTops = this.getxxBTops(new ArrayList<Muster>(nextMusters.values()), type);

			buyDateMusters = kdataService.getMusters(dates.get(i-1));
			//buyDateTops = this.getBxxTops(new ArrayList<Muster>(buyDateMusters.values()), type);
			buyDateTops = this.getxxBTops(new ArrayList<Muster>(buyDateMusters.values()), type);
			
			thisMusters = kdataService.getMusters(dates.get(i-2));
			//thisTops = this.getBxxTops(new ArrayList<Muster>(thisMusters.values()), type);
			thisTops = this.getxxBTops(new ArrayList<Muster>(thisMusters.values()), type);
			
			for(Muster buy : buyDateTops) {
				if(buy!=null && buy.isBreaker() && !buy.isUpLimited()) {
					next = nextMusters.get(buy.getItemID());
					if(next!=null) {
						r = next.getLatestPrice().subtract(buy.getLatestPrice()).divide(buy.getLatestPrice(),BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
						System.out.format("%s %s buy price %f, next day close price %f, ratio is %d.\n", 
								dates.get(i-1).toString(), buy.getItemID(), buy.getLatestPrice(), next.getLatestPrice(), r);
						ratio.put(r);
					}
				}
			}
			
			ratios.put(dates.get(i), ratio);
		}
		
		System.out.println("calculateSecondDayWinRatio done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
		return ratios;
	}
	
	class Ratio{
		TreeMap<Integer, Integer> rc = new TreeMap<Integer,Integer>();  // ratio,count
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
				//System.out.println(entry.getValue());
				total = total + entry.getKey()*entry.getValue();
			}
			return total;
		}
	}
	
}
