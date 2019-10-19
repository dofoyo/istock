package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

/*
 * 只要突破就买入
 * 返回在手股票中，盈利部分的股票所在的行业
 */
@Service("compass")
public class Compass {
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	Integer pool = 21;
	Integer top = 5;
	Integer duration = 21;  // 89, 144, 233, 377, 610, 987
	
	public List<String> generateWinIndustrys(LocalDate date) {
		//System.out.println("\ngenerate lost industrys ...");
		List<String> lostIndustrys = new ArrayList<String>();

		BigDecimal total = new BigDecimal(-100000000);
		
		Map<String, String> result = this.run(date.minusDays(duration), date, lostIndustrys, "hlb");
		
		BigDecimal newTotal = new BigDecimal(result.get("total"));
		String[] ss = result.get("lostIndustrys").split(",");
		List<String> newLostIndustrys = new ArrayList<String>(ss.length);
		Collections.addAll(newLostIndustrys, ss);
		Map<String, String> newResult = null;
		
		Map<String,Muster> ms = null;
		
		int i=0;
		while(newTotal.compareTo(total)==1 && !newLostIndustrys.isEmpty()) {
			//System.out.println("total is " + total +", newTotal is " + newTotal + ", and there are " + newLostIndustrys.size() + " lost industrys, to optimize!");

			i++;
			total = newTotal;
			lostIndustrys.addAll(newLostIndustrys);
			if(newResult!=null) {
				result = newResult;
			}
			
			newResult = this.run(date.minusDays(duration), date, lostIndustrys, "hlb");
			
			newTotal = new BigDecimal(newResult.get("total"));
			
			ss = newResult.get("lostIndustrys").split(",");
			newLostIndustrys = new ArrayList<String>(ss.length);
			Collections.addAll(newLostIndustrys, ss);
		}
		
		//System.out.println("after "+ i +" times optimized , total is " + total + ", and there are "+lostIndustrys.size()+" lost industrys!");

		ss = result.get("winIndustrys").split(",");
		List<String> winIndustrys = new ArrayList<String>(ss.length);
		Collections.addAll(winIndustrys, ss);

		//return winIndustrys.subList(0, winIndustrys.size()>21 ? 21 : winIndustrys.size());
		return winIndustrys;
	}
	
	
	public Map<String, String> run(LocalDate beginDate, LocalDate endDate, List<String> lostIndustrys, String type) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("\nrun with new lost industrys from " + beginDate + " to " + endDate +" ......");
		
		XxB paul = new XxB(new BigDecimal(1000000));
		
		Map<String,Muster> musters = null;
		Map<String,Muster> ms = null;

		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		int flag = 1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			musters = kdataService.getMusters(date);

			if(musters!=null && musters.size()>0) {
				ms = new HashMap<String,Muster>();
				for(Map.Entry<String, Muster> entry : musters.entrySet()) {
					if(!lostIndustrys.contains(entry.getValue().getIndustry())) {
						ms.put(entry.getKey(), entry.getValue());
					}
				}			
				paul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), type), date);
			}
		}
		
		//System.out.println("run compass done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");    
		
		return paul.result();
		
	}

	public List<String> getWinIndustrys(){
		String str = "保险,船舶,电器仪表,多元金融,航空,红黄酒,化学制药,家居用品,汽车配件,软饮料,石油开采,水力发电,塑料,通信设备,文教休闲,医疗保健,造纸";
		
		/*
		 * 保险, 船舶,电器仪表, 多元金融, 航空,化学制药,  家居用品,  汽车配件, 软饮料, 石油开采, 通信设备,塑料, 造纸, 文教休闲,
		 * win industrys: [医疗保健, 路桥, 乳制品, 服饰, 农药化肥, 机场,  化工原料, 铁路, 石油贸易, 半导体,  水运, 旅游服务, 染料涂料, 焦炭加工, 小金属,
银行, 铝, 陶瓷, 林业, 房产服务, 纺织机械, 汽车服务, 矿物制品]
		 */
		
		String[] ss = str.split(",");
		List<String> industrys = new ArrayList<String>(ss.length);
		Collections.addAll(industrys, ss);
		return industrys;
	}
	
	
	private List<Muster> getTops(List<Muster> musters, String type){
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

}
