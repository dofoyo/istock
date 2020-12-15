package com.rhb.istock.evaluation;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service("evaluationService")
public class EvaluationService {
	protected static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);
	
	@Autowired
	@Qualifier("evaluationRepository")
	EvaluationRepository evaluationRepository;
	
	@Value("${evaluationPath}")
	private String evaluationPath;

	@Value("${openDuration}")
	private String openDuration;
	
	@Value("${dropDuration}")
	private String dropDuration;
	
	/*
	 * 
凯利公式有几种形式，其中的一种如下：

f=p/a-q/b

其中：f表示分配的资金比例

p表示获胜的概率

q表示失败的概率

a表示失败损失率，指失败后押注的资金从1变成1-a

b表示获胜增长率，指获胜后押注的资金从1变成1+b

如果f算出来是0，表示这是一个期望收益为0的游戏，最优决策是不参加。

如果f算出来是负数，表示这是一个期望收益为负的游戏，更是不能参加了。

如果f算出来是小于1的正数，就应该按照这个比例下注；如果是个大于1的数，最优的决策是需要借钱来参与这个游戏。
	 */
	public KelliesView getKelliesView(Integer period, LocalDate bDate, LocalDate eDate) {
		KelliesView view = new KelliesView();
		Set<LocalDate> dates = evaluationRepository.getDates(bDate, eDate);
		Map<LocalDate, Kelly> avbs = this.getKellies("avb", period);
		Map<LocalDate, Kelly> bavs = this.getKellies("bav", period);
		Map<LocalDate, Kelly> bdts = this.getKellies("bdt", period);
		Map<LocalDate, Kelly> bhls = this.getKellies("bhl", period);
		Map<LocalDate, Kelly> dtbs = this.getKellies("dtb", period);
		Map<LocalDate, Kelly> hlbs = this.getKellies("hlb", period);
		
		Integer bhl=0,bav=0,bdt=0,hlb=0,avb=0,dtb=0;
		for(LocalDate date : dates) {
			if(bhls.get(date)!=null) {
				bhl = bhls.get(date).getScore();
			}
			if(bavs.get(date)!=null) {
				bav = bavs.get(date).getScore();
			}
			if(bdts.get(date)!=null) {
				bdt = bdts.get(date).getScore();
			}
			if(hlbs.get(date)!=null) {
				hlb = hlbs.get(date).getScore();
			}
			if(avbs.get(date)!=null) {
				avb = avbs.get(date).getScore();
			}
			if(dtbs.get(date)!=null) {
				dtb = dtbs.get(date).getScore();
			}

			view.add(date, bhl, bav, bdt, hlb, avb, dtb);
		}
		
		return view;
	}
	
	public Map<LocalDate, Kelly> getKellies(String type, Integer period){
		Map<LocalDate, Kelly> kellies = new TreeMap<LocalDate, Kelly>();
		Map<LocalDate, Kelly> tmps = this.getKellies(type);
		List<Kelly> pres = new ArrayList<Kelly>();
		Kelly tmp, pre=null, kelly;
		Kelly total = new Kelly(null);
		for(Map.Entry<LocalDate, Kelly> entry : tmps.entrySet()) {
			tmp = entry.getValue();
			total.add(tmp.getWin(), tmp.getWinOpen(), tmp.getWinClose());
			total.add(tmp.getLose(), tmp.getLoseOpen(), tmp.getLoseClose());
			
			pres.add(tmp);
			if(pres.size()>period) {
				pre = pres.get(0);
				pres.remove(0);
			}
			
			if(pre != null) {
				total.subtract(pre.getWin(), pre.getWinOpen(), pre.getWinClose());
				total.subtract(pre.getLose(), pre.getLoseOpen(), pre.getLoseClose());
			}
			
			kelly = new Kelly(entry.getKey());
			kelly.add(total.getWin(), total.getWinOpen(), total.getWinClose());
			kelly.add(total.getLose(), total.getLoseOpen(), total.getLoseClose());
			kellies.put(entry.getKey(), kelly);
		}
		return kellies;
	}
	
	public Map<LocalDate, Kelly> getKellies(String type){
		Map<LocalDate, Kelly> kellies = new TreeMap<LocalDate, Kelly>();
		List<Busi> orders = evaluationRepository.getBusis(type);
		Kelly kelly;
		for(Busi order : orders) {
			kelly = kellies.get(order.getOpenDate());
			if(kelly==null) {
				kelly = new Kelly(order.getOpenDate());
				kellies.put(order.getOpenDate(), kelly);
			}
			kelly.add(1, order.getOpenPrice(), order.getClosePrice());
		}
		
		return kellies;
	}
	
	
	public BusiView  getBusiView(String type, LocalDate bDate, LocalDate eDate){
		List<Busi> orders = evaluationRepository.getBusis(type);
		Kelly kelly = new Kelly(eDate);
		for(Busi order : orders) {
			if((order.getOpenDate().isBefore(eDate) || order.getOpenDate().equals(eDate))
					&& (order.getOpenDate().isAfter(bDate) || order.getOpenDate().equals(bDate))
					) {
				kelly.add(1, order.getOpenPrice(), order.getClosePrice());
			}
		}
		
		return new BusiView(eDate, kelly.getWinPR(),kelly.getWinRate(),kelly.getLosePR(),kelly.getLoseRate(),kelly.getScore());
	}
	
	public BusiView  getMaxBusiView(String type, LocalDate bDate, LocalDate eDate){
		List<Busi> orders = evaluationRepository.getBusis(type);
		Kelly kelly = new Kelly(eDate);
		for(Busi order : orders) {
			if((order.getOpenDate().isBefore(eDate) || order.getOpenDate().equals(eDate))
					&& (order.getOpenDate().isAfter(bDate) || order.getOpenDate().equals(bDate))
					) {
				
				kelly.add(1, order.getOpenPrice(), order.getHighestPrice());
			}
		}
		return new BusiView(eDate, kelly.getWinPR(),kelly.getWinRate(),kelly.getLosePR(),kelly.getLoseRate(),kelly.getScore());
	}

}
