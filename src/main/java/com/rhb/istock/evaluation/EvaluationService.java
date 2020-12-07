package com.rhb.istock.evaluation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Functions;

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
	public BusiView  getBusiView(String type, LocalDate bDate, LocalDate eDate){
		List<Busi> orders = evaluationRepository.getBusis(type);
		Integer win=0, lose=0;
		BigDecimal winOpen=BigDecimal.ZERO, winClose=BigDecimal.ZERO, loseOpen=BigDecimal.ZERO, loseClose=BigDecimal.ZERO; 
		for(Busi order : orders) {
			if((order.getOpenDate().isBefore(eDate) || order.getOpenDate().equals(eDate))
					&& (order.getOpenDate().isAfter(bDate) || order.getOpenDate().equals(bDate))
					) {
				if(order.isWin()) {
					win++;
					winOpen = winOpen.add(order.getOpenPrice());
					winClose = winClose.add(order.getClosePrice());
				}else {
					lose++;
					loseOpen = loseOpen.add(order.getOpenPrice());
					loseClose = loseClose.add(order.getClosePrice());
				}
			}
		}
		
		Integer winPR = (win+lose)!=0 ? win*100/(win+lose) : 0;
		Integer winRate = Functions.growthRate(winClose, winOpen);
		
		Integer losePR = (win+lose)!=0 ? lose*100/(win+lose) : 0;
		Integer loseRate = Functions.growthRate(loseOpen, loseClose);

		Integer kelly = (loseRate!=0 && winRate!=0) ? 100*winPR/loseRate/100 - 100*losePR/winRate/100 : 0;
		if(losePR==0 && winPR!=0) {
			kelly = 100;
		}
		//System.out.format("win=%d, lose=%d,winPR=%d,winOpen=%.2f,winClose=%.2f,winRate=%d,losePR=%d,loseOpen=%.2f,loseClose=%.2f,loseRate=%d\n",win,lose,winPR,winOpen,winClose,winRate,losePR,loseOpen,loseClose,loseRate);
		
		
		return new BusiView(winPR,winRate,losePR,loseRate,kelly);
	}
	
	public BusiView  getMaxBusiView(String type, LocalDate bDate, LocalDate eDate){
		List<Busi> orders = evaluationRepository.getBusis(type);
		Integer win=0, lose=0;
		BigDecimal winOpen=BigDecimal.ZERO, winClose=BigDecimal.ZERO, loseOpen=BigDecimal.ZERO, loseClose=BigDecimal.ZERO; 
		for(Busi order : orders) {
			if((order.getOpenDate().isBefore(eDate) || order.getOpenDate().equals(eDate))
					&& (order.getOpenDate().isAfter(bDate) || order.getOpenDate().equals(bDate))
					) {
				if(order.isGood()) {
					win++;
					winOpen = winOpen.add(order.getOpenPrice());
					winClose = winClose.add(order.getHighestPrice());
				}else {
					lose++;
					loseOpen = loseOpen.add(order.getOpenPrice());
					loseClose = loseClose.add(order.getClosePrice());
				}
			}
		}
		
		Integer winPR = (win+lose)!=0 ? win*100/(win+lose) : 0;
		Integer winRate = Functions.growthRate(winClose, winOpen);
		
		Integer losePR = (win+lose)!=0 ? lose*100/(win+lose) : 0;
		Integer loseRate = Functions.growthRate(loseOpen, loseClose);

		Integer kelly = (loseRate!=0 && winRate!=0) ? 100*winPR/loseRate/100 - 100*losePR/winRate/100 : 0;
		if(losePR==0 && winPR!=0) {
			kelly = 100;
		}
		//System.out.format("win=%d, lose=%d,winPR=%d,winOpen=%.2f,winClose=%.2f,winRate=%d,losePR=%d,loseOpen=%.2f,loseClose=%.2f,loseRate=%d\n",win,lose,winPR,winOpen,winClose,winRate,losePR,loseOpen,loseClose,loseRate);
		
		
		return new BusiView(winPR,winRate,losePR,loseRate,kelly);
	}

}
