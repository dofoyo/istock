package com.rhb.istock.trade.turtle.simulation.six;

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
import com.rhb.istock.fdata.tushare.FdataServiceTushare;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.selector.fina.FinaService;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulation")
public class TurtleMusterSimulation {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation.class);

	@Value("${musterPath}")
	private String musterPath;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;

	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;

	@Autowired
	@Qualifier("fdataServiceTushare")
	FdataServiceTushare fdataServiceTushare;

	@Autowired
	@Qualifier("finaService")
	FinaService finaService;
	
	BigDecimal initCash = new BigDecimal(1000000);
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 * 
	 * 2020年6月30日
	 * 经过测算，总结如下：
	 * 1、熊市空仓是最佳策略，目前近20年来，沪深Ａ股经历了四个阶段的熊市：
	 * 2001/6/1	－	2005/6/6
	 * 2007/10/16	－	2008/11/5
	 * 2015/6/15　－		2016/1/28
	 * 2018/1/26	－	2019/1/3
	 * 
	 * 2、牛市和平衡市的最佳策略是B21
	 * 
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		//System.out.println("Functions.ratio(this.averagePrice21, this.averagePrice)<=13");
		System.out.println("simulate from " + beginDate + " to " + endDate +" ......");

		RecoNEWB hlb = new RecoNEWB(initCash,1); //高价创新高
		NEWBplus bdt = new NEWBplus(initCash,0); //低价创新高

		RecoB21 avb = new RecoB21(initCash,1);  //平衡市策略：高价破21日线
		B21plus bhl = new B21plus(initCash,0);  //牛市和平衡市策略：低价破21日线
		

/*		Above hlb = new Above(initCash,21); //连续21天在21日线上
		Above bdt = new Above(initCash,34); //连续34天在21日线上

		Above avb = new Above(initCash,55);  //连续55天在21日线上
		Above bhl = new Above(initCash,89);  //连续89天在21日线上
*/		
		RecoDrum bav = new RecoDrum(initCash,1);  //高价+上升趋势+强于大盘
		DrumPlus dtb = new DrumPlus(initCash,0);  //低价+上升趋势+强于大盘

		Map<String,Muster> musters, tmps;
		Muster muster;
		
		List<Map<String,Muster>> previous = new ArrayList<Map<String,Muster>>();
		Integer previous_period  = 13; //历史纪录区间，主要用于后面判断

		Integer sseiFlag, sseiRatio, sseiTrend;
		
		//Map<Integer,Set<String>> year_oks = fdataServiceTushare.getOks();
		
		Set<String> oks = null;
		List<String> recommendations = null;
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {

			musters = kdataService.getMusters(date);

			Progress.show((int)days, i++, "  simulate: " + date.toString() + ", musters.size()=" + musters.size() + " ");
			
			if(musters!=null && musters.size()>0) {
				previous.add(musters);
				if(previous.size()>previous_period) {
					previous.remove(0);
				}
				
				sseiFlag = kdataService.getSseiFlag(date);
				sseiRatio = indexServiceTushare.getSseiGrowthRate(date, 21);
				sseiTrend = kdataService.getSseiTrend(date, previous_period);
				recommendations = finaService.getHighRecommendations(date, 10000); //推荐买入的顺序是从大到小
				tmps = new HashMap<String,Muster>();
				for(String id : recommendations) {
					muster = musters.get(id);
					if(muster!=null) {
						tmps.put(id, muster);
					}
				}

				hlb.doIt(musters, tmps, previous, date, sseiFlag, sseiRatio, sseiTrend);
				bdt.doIt(musters, previous, date, sseiFlag, sseiRatio, sseiTrend);

				avb.doIt(musters, tmps, previous, date, sseiFlag, sseiRatio, sseiTrend);
				bhl.doIt(musters, previous, date, sseiFlag, sseiRatio, sseiTrend);

				bav.doIt(musters, tmps, previous, date, sseiFlag, sseiRatio, sseiTrend);
				dtb.doIt(musters, previous, date, sseiFlag, sseiRatio, sseiTrend);
			}
		}
		
		Map<String, String> bavResult = bav.result();
		Map<String, String> bhlResult = bhl.result();
		Map<String, String> bdtResult = bdt.result();
		Map<String, String> dtbResult = dtb.result();

		Map<String, String> avbResult = avb.result();
		Map<String, String> hlbResult = hlb.result();
		
		turtleSimulationRepository.save("bav", bavResult.get("breakers"), bavResult.get("CSV"), bavResult.get("dailyAmount"));
		turtleSimulationRepository.save("bhl", bhlResult.get("breakers"), bhlResult.get("CSV"), bhlResult.get("dailyAmount"));
		turtleSimulationRepository.save("bdt", bdtResult.get("breakers"), bdtResult.get("CSV"), bdtResult.get("dailyAmount"));
		
		turtleSimulationRepository.save("dtb", dtbResult.get("breakers"), dtbResult.get("CSV"), dtbResult.get("dailyAmount"));
		turtleSimulationRepository.save("hlb", hlbResult.get("breakers"), hlbResult.get("CSV"), hlbResult.get("dailyAmount"));
		turtleSimulationRepository.save("avb", avbResult.get("breakers"), avbResult.get("CSV"), avbResult.get("dailyAmount"));
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("simulate 用时：" + used + "秒");          
	}
}