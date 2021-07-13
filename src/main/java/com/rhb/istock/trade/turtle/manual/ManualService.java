package com.rhb.istock.trade.turtle.manual;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.account.Account;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.operation.Operation;
import com.rhb.istock.trade.turtle.simulation.six.TurtleMusterSimulation;
import com.rhb.istock.trade.turtle.simulation.six.repository.AmountEntity;
import com.rhb.istock.trade.turtle.simulation.six.repository.TurtleSimulationRepository;

@Service("manualService")
public class ManualService {
	protected static final Logger logger = LoggerFactory.getLogger(TurtleMusterSimulation.class);

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("turtleSimulationRepository")
	TurtleSimulationRepository turtleSimulationRepository;

	@Autowired
	@Qualifier("indexServiceTushare")
	IndexServiceTushare indexServiceTushare;

	@Autowired
	@Qualifier("aggressiveOperation")
	Operation aggressiveOperation;
	
	@Autowired
	@Qualifier("conservativeOperation")
	Operation conservativeOperation;

	@Autowired
	@Qualifier("optimizeOperation")
	Operation optimizeOperation;

	@Autowired
	@Qualifier("optimizeOperation2")
	Operation optimizeOperation2;
	
	@Autowired
	@Qualifier("favorOperation")
	Operation favorOperation;

	@Autowired
	@Qualifier("newbOperation")
	Operation newbOperation;
	
	@Autowired
	@Qualifier("commOperation")
	Operation commOperation;

	@Autowired
	@Qualifier("huntingOperation")
	Operation huntingOperation;
	
	@Autowired
	@Qualifier("favorOperation3")
	Operation favorOperation3;
	
	@Autowired
	@Qualifier("commOperation3")
	Operation commOperation3;

	@Autowired
	@Qualifier("commOperation2")
	Operation commOperation2;
	
	@Autowired
	@Qualifier("manualOperation")
	Operation manualOperation;
	
	private Map<LocalDate, List<String>> selects = new TreeMap<LocalDate, List<String>>();
	private Map<LocalDate, List<String>> sells = new TreeMap<LocalDate, List<String>>();
	private Map<LocalDate, List<String>> buys = new TreeMap<LocalDate, List<String>>();

	public void addBuys(LocalDate date, String itemID) {
		List<String> ids = this.buys.get(date);
		if(ids==null) {
			ids = new ArrayList<String>();
			ids.add(itemID);
			this.buys.put(date, ids);
		}else if(ids!=null && !ids.contains(itemID)) {
			ids .add(itemID);
		}
		//System.out.println(" buys: " + this.buys);
	}
	
	public void deleteBuys(LocalDate date, String itemID) {
		List<String> ids = buys.get(date);
		if(ids!=null && ids.contains(itemID)) {
			ids.remove(itemID);
		}
	}


	public Map<LocalDate, List<String>> getBuys(LocalDate date){
		Map<LocalDate, List<String>> ss = new TreeMap<LocalDate, List<String>>();
		List<String> ids;
		for(Map.Entry<LocalDate, List<String>> entry : this.buys.entrySet()) {
			if(entry.getKey().isBefore(date) || entry.getKey().isEqual(date)) {
				ids = entry.getValue();
				if(ids!=null && ids.size()>0) {
					ss.put(entry.getKey(), ids);
				}
			}
		}
		
		return ss;
	}
	
	public void addSells(LocalDate date, String itemID) {
		List<String> ids = this.sells.get(date);
		if(ids==null) {
			ids = new ArrayList<String>();
			ids.add(itemID);
			this.sells.put(date, ids);
		}else if(ids!=null && !ids.contains(itemID)) {
			ids .add(itemID);
		}
		//System.out.println(" sells: " + this.sells);
	}
	
	public void deleteSells(LocalDate date, String itemID) {
		List<String> ids = sells.get(date);
		if(ids!=null && ids.contains(itemID)) {
			ids.remove(itemID);
		}
	}


	public Map<LocalDate, List<String>> getSells(LocalDate date){
		Map<LocalDate, List<String>> ss = new TreeMap<LocalDate, List<String>>();
		List<String> ids;
		for(Map.Entry<LocalDate, List<String>> entry : this.sells.entrySet()) {
			if(entry.getKey().isBefore(date) || entry.getKey().isEqual(date)) {
				ids = entry.getValue();
				if(ids!=null && ids.size()>0) {
					ss.put(entry.getKey(), ids);
				}
			}
		}
		
		return ss;
	}
	
	public void addSelects(LocalDate date, String itemID) {
		List<String> ids = this.selects.get(date);
		if(ids==null) {
			ids = new ArrayList<String>();
			ids.add(itemID);
			this.selects.put(date, ids);
		}else if(ids!=null && !ids.contains(itemID)) {
			ids .add(itemID);
		}
		//System.out.println(selects);
	}
	
	public void deleteSelects(LocalDate date, String itemID) {
		List<String> ids = selects.get(date);
		if(ids!=null && ids.contains(itemID)) {
			ids.remove(itemID);
		}
	}
	
	public Map<LocalDate, List<String>> getSelects(LocalDate date){
		Map<LocalDate, List<String>> ss = new TreeMap<LocalDate, List<String>>();
		List<String> ids;
		for(Map.Entry<LocalDate, List<String>> entry : this.selects.entrySet()) {
			if(entry.getKey().isBefore(date) || entry.getKey().isEqual(date)) {
				ids = entry.getValue();
				if(ids!=null && ids.size()>0) {
					ss.put(entry.getKey(), ids);
				}
			}
		}
		
		return ss;
	}
	
	public void saveReselect(Map<LocalDate, List<String>> selects) {
		this.selects = selects;
	}
	
	public Map<LocalDate, List<String>> getReselect(){
		this.selects  = turtleSimulationRepository.getBreakers("manual");
		return this.selects;
	}
	
	public void simulate(String simulateType, LocalDate date) {
		long beginTime=System.currentTimeMillis(); 
		String label = "manual " + simulateType;
		BigDecimal initCash = new BigDecimal(1000000);
		Integer top = 1000;
		Account account = new Account(initCash);
		boolean isEvaluation = false;
		Map<String, String> operateResult;
		if("comm".equals(simulateType)) {  //买2+3
			operateResult = commOperation3.run(account, this.selects,null, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}else if("newb".equals(simulateType))  { //买一
			operateResult = newbOperation.run(account, this.selects,null, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}else if("optimize".equals(simulateType)) { //买二
			operateResult = optimizeOperation2.run(account, this.selects,null, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}else if("favor".equals(simulateType))  {  //买三
			operateResult = favorOperation3.run(account, this.selects,null, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}else if("sab".equals(simulateType)) {  //高价
			//operateResult = huntingOperation.run(account, this.selects, this.getBeginDate(), this.getEndDate(), label, top, true,0);
			operateResult = commOperation2.run(account, this.selects,null, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}else  if("conservative".equals(simulateType)){   //低价
			operateResult = conservativeOperation.run(account, this.selects,null, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}else {
			//System.out.println(" buys: " + this.buys);
			//System.out.println(" sells: " + this.sells);
			operateResult = manualOperation.run(account, this.buys,this.sells, this.getBeginDate(), this.getEndDate(), label, top, true,0);
		}
		turtleSimulationRepository.save("manual", operateResult.get("breakers"), operateResult.get("CSV"), operateResult.get("dailyAmount"), operateResult.get("dailyHolds"), isEvaluation);
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("manual simulate 用时：" + used + "秒");          
	}
	
	private LocalDate getBeginDate() {
		TreeMap<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("avb");
		return amounts.firstKey();
	}
	private LocalDate getEndDate() {
		TreeMap<LocalDate, AmountEntity> amounts = turtleSimulationRepository.getAmounts("avb");
		return amounts.lastKey();
	}
}
