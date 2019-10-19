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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("turtleMusterSimulationByBlueChips")
public class TurtleMusterSimulationByBlueChips {
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
	
	Integer pool = 21;
	Integer top = 5;
	BigDecimal initCash = new BigDecimal(100000);
	
	
	/*
	 * 根据输入起止日期，系统模拟买入和卖出
	 */
	public void simulate(LocalDate beginDate, LocalDate endDate) {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("simulate by compass from " + beginDate + " to " + endDate +" ......");
		
		XxB bavPaul = new XxB(initCash);
		XxB bhlPaul = new XxB(initCash);
		XxB bdtPaul = new XxB(initCash);
		XxB avbPaul = new XxB(initCash);
		XxB dtbPaul = new XxB(initCash);
		XxB hlbPaul = new XxB(initCash);
		
		Map<String,Muster> musters = null;
		Map<String,Muster> ms = null;
		List<String> bluechips = new ArrayList<String>();
		
		long days = endDate.toEpochDay()- beginDate.toEpochDay();
		int i=1;
		int flag = 1;
		for(LocalDate date = beginDate; (date.isBefore(endDate) || date.equals(endDate)); date = date.plusDays(1)) {
			Progress.show((int)days, i++, date.toString());
			
			musters = kdataService.getMusters(date);

			if(musters!=null && musters.size()>0) {
				bluechips = bluechipService.getBluechipIDs(date);

				ms = new HashMap<String,Muster>();
				for(Map.Entry<String, Muster> entry : musters.entrySet()) {
					if(bluechips.contains(entry.getKey())) {
						ms.put(entry.getKey(), entry.getValue());
					}
				}			
				bavPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), "bav"), date);
				bhlPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), "bhl"), date);
				bdtPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), "bdt"), date);
				
				avbPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), "avb"), date);
				hlbPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), "hlb"), date);
				dtbPaul.doIt_plus(musters, this.getTops(new ArrayList<Muster>(ms.values()), "dtb"), date);					
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

		//System.out.println("simulate by compass done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          
		
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
	
	private List<String> getWinIndustrys(){
		String str = "保险,船舶,电器仪表,多元金融,航空,红黄酒,化学制药,家居用品,汽车配件,软饮料,石油开采,水力发电,塑料,通信设备,文教休闲,医疗保健,造纸";
		String[] ss = str.split(",");
		List<String> industrys = new ArrayList<String>(ss.length);
		Collections.addAll(industrys, ss);
		return industrys;
	}
	
	private List<String> getLostIndustrys(){
		String str = "纺织,综合类,火力发电,酒店餐饮,公共交通,汽车整车,煤炭开采,旅游景点,银行,石油加工,农业综合,铅锌,影视音像,电信运营,建筑工程,水务,化纤,机械基件,园区开发,机床制造,铜,装修装饰,空运,水运,新型电力,软件服务,互联网,港口,普钢,化工机械,其他建材,白酒,出版业,农用机械,水泥,食品,啤酒,黄金,石油开采,铁路,路桥,区域地产";
		String[] ss = str.split(",");
		List<String> industrys = new ArrayList<String>(ss.length);
		Collections.addAll(industrys, ss);
		return industrys;
	}
}
