package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;

@Service("evaluationService")
public class EvaluationService {
	protected static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("evaluationRepository")
	EvaluationRepository evaluationRepository;
	
	@Value("${evaluationPath}")
	private String evaluationPath;

	@Value("${openDuration}")
	private String openDuration;
	
	@Value("${dropDuration}")
	private String dropDuration;
	

	public KelliesView getKelliesView(Integer period, LocalDate eDate) {
		KelliesView view = new KelliesView();
		
		List<LocalDate> dates = this.getDates(89, eDate);
		Map<String,Muster> musters = kdataService.getMusters(dates.get(dates.size()-1));
		
		Map<LocalDate, Kelly> avbs = this.getKellies("avb", period, dates, musters);
		Map<LocalDate, Kelly> bavs = this.getKellies("bav", period, dates, musters);
		Map<LocalDate, Kelly> bdts = this.getKellies("bdt", period, dates, musters);
		Map<LocalDate, Kelly> bhls = this.getKellies("bhl", period, dates, musters);
		Map<LocalDate, Kelly> dtbs = this.getKellies("dtb", period, dates, musters);
		Map<LocalDate, Kelly> hlbs = this.getKellies("hlb", period, dates, musters);
		
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
	
	//
	private Map<LocalDate, Kelly> getKellies(String type, Integer period, List<LocalDate> dates, Map<String,Muster> musters){
		Map<LocalDate, Kelly> kellies = new TreeMap<LocalDate, Kelly>();

		Map<LocalDate,List<Busi>> busis = evaluationRepository.getBusis(type); //每一单
		Kelly kelly;
		List<LocalDate> nextDates;
		for(LocalDate eDate : dates) {
			nextDates = this.getDates(period, eDate);
			kelly = this.getKelly(nextDates, busis, musters);
			kellies.put(eDate, kelly);
		}
		return kellies;
	}
	
	//按period合并将busi合并，并计算出kelly
	private Kelly getKelly(List<LocalDate> dates, Map<LocalDate,List<Busi>> busis, Map<String,Muster> musters){
		LocalDate eDate = dates.get(dates.size()-1);
		Kelly kelly = new Kelly(eDate);
		
		List<Busi> tmps;
		BigDecimal closePrice;
		Muster muster;
		for(LocalDate date : dates) {
			tmps = busis.get(date);
			if(tmps!=null) {
				for(Busi busi : tmps) {
					closePrice = busi.getClosePrice();
					//根据eDate对closePrice进行校正
					if(busi.getCloseDate().isAfter(eDate)) {
						muster = musters.get(busi.getItemID());
						if(muster!=null) {
							closePrice = muster.getLatestPrice();
						}else {
							closePrice = busi.getOpenPrice();
						}
					}
					
					kelly.add(1, busi.getOpenAmount(), busi.getQuantity().multiply(closePrice));
				}			
			}
		}
		
		return kelly;
	}
	
	public List<BusiView> getBusiViews(String type, Integer period, LocalDate eDate){
		List<BusiView> result = new ArrayList<BusiView>();

		List<Busi> tmps;
		BigDecimal closePrice;
		LocalDate closeDate;
		Muster muster;

		Map<LocalDate,List<Busi>> busis = evaluationRepository.getBusis(type); //每一单
		List<LocalDate> dates = this.getDates(period, eDate);
		Map<String,Muster> musters = kdataService.getMusters(dates.get(dates.size()-1));
		for(LocalDate date : dates) {
			tmps = busis.get(date);
			if(tmps!=null) {
				for(Busi busi : tmps) {
					closeDate = busi.getCloseDate();
					closePrice = busi.getClosePrice();
					//根据eDate对closePrice进行校正
					if(busi.getCloseDate().isAfter(eDate)) {
						closeDate = eDate;
						muster = musters.get(busi.getItemID());
						if(muster!=null) {
							closePrice = muster.getLatestPrice();
						}else {
							closePrice = busi.getOpenPrice();
						}
					}
					
					result.add(new BusiView(busi.getItemID(),busi.getItemName(),busi.getOpenDate(), busi.getOpenPrice(),busi.getQuantity(), closeDate, closePrice, busi.getHighestPrice()));
				}			
			}
		}		
		
		return result;
	}
	
	public KellyView getKellyView(String type, Integer period, LocalDate eDate){
		List<LocalDate> dates = this.getDates(period, eDate);
		Map<LocalDate,List<Busi>> busis = evaluationRepository.getBusis(type); //每一单
		Map<String,Muster> musters = kdataService.getMusters(dates.get(dates.size()-1));
		
		Kelly kelly = this.getKelly(dates, busis, musters);
		
		return new KellyView(eDate, kelly.getWin(), kelly.getWinPR(),kelly.getWinRate(),kelly.getLose(),kelly.getLosePR(),kelly.getLoseRate(),kelly.getScore());
	}
	
	//将busi合并，并计算出kelly
	private Kelly getMaxKelly(List<LocalDate> dates, Map<LocalDate,List<Busi>> busis){
		LocalDate eDate = dates.get(dates.size()-1);
		Kelly kelly = new Kelly(eDate);
		
		List<Busi> tmps;
		for(LocalDate date : dates) {
			tmps = busis.get(date);
			if(tmps!=null) {
				for(Busi busi : tmps) {
					kelly.add(1, busi.getOpenAmount(), busi.getHighestAmount());
				}			
			}
		}
		
		return kelly;
	}
	
	public KellyView  getMaxKellyView(String type, Integer period, LocalDate eDate){
		List<LocalDate> dates = this.getDates(period, eDate);
		Map<LocalDate,List<Busi>> busis = evaluationRepository.getBusis(type); //每一单
		
		Kelly kelly = this.getMaxKelly(dates, busis);
		
		return new KellyView(eDate, kelly.getWin(), kelly.getWinPR(),kelly.getWinRate(), kelly.getLose(),kelly.getLosePR(),kelly.getLoseRate(),kelly.getScore());
	}
	
	
	/*
	 * 采用排序和循环的目的是能满足即使eDate不是交易日的情况，也能正确返回
	 */
	private List<LocalDate> getDates(Integer period, LocalDate eDate){
		List<LocalDate> dates = new ArrayList<LocalDate>();
		
		List<LocalDate> tmps = evaluationRepository.getDates(true);
		for(LocalDate date : tmps) {
			if((date.isBefore(eDate) || date.equals(eDate)) && dates.size()<=period) {
				dates.add(date);
			}
			if(dates.size()>=period) {
				break;
			}
		}
		
		Collections.sort(dates, new Comparator<LocalDate>() {
			@Override
			public int compare(LocalDate o1, LocalDate o2) {
				return o1.compareTo(o2);
			}
			
		});
		
		return dates;
	}

}
