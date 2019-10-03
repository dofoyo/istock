package com.rhb.istock.trade.balloon.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rhb.istock.fund.Account;

/*
 * 气球模型（均线上堆积成交越多，股价上涨的概率越大）
 * 	 *上涨概率
	 *最近100个交易日，股价大于60日均线的天数，为上涨概率，
	 *upDuration 默认为100
	 *upLine = 默认60日均线
	 *upProbability 上涨概率
	 *
	 * 不追涨
	 *股价偏离120日均线的百分比
	 *baseLine 默认为120日均线
	 *biasBaseLIne = ((当前股价-baseLine)/baseLine * 100) 取绝对值
	 *
	 *
	 *不最高
	 *股价偏离近750个交易日的中位数的百分比
	 *midDuration 默认为750
	 *midPrice 股价中位数
	 *biasMidPrice = ((股价-midPrce)/mdiPrice *100) 取绝对值
	 *(未来改进，中位数其实就是750日均线位置）
	 *
	 *
	 *买入时机，同时满足以下条件：
	 *1、upProbability - biasBaseLIne - biasMidPrice/2 > buyValue （默认45）
	 *2、近10个交易日低于60日均线的次数  < 5
	 *3、股价高于baseLine (即：低于120均线（即低于baseLine）的不能买)
	 *4、新股不能买（ipo日期起算，300天后才能买）
	 *5、当天股价没有一字板
	 *
	 *
	 *买入仓位
	 *当前市值的30%
	 *可以透支（融资）
	 *
	 *加仓
	 *最近一次买入上涨或下跌10%以上，再次出现买点，可以加仓
	 *
	 *卖出
	 *近10个交易日股价低于upline均线
	 *
	 *买卖禁止
	 *停牌期间不能买卖
	 *当天股价为一字板，不能买卖
	 *
	 *1 1 2 3 5 8 13 21 34 55 89 144 193 337 530
	 *
 */
public class Balloon {
	private Integer tradeDuration = 337; 		//股票上市多少天后才能买， 默认值337天
	private Integer upDuration = 55;			//用于计算最近多少天股票在upLine之上的天数，默认与upLine一样
	private Integer upLine = 55;				//用于定义upLine，即几日均线。一般用60日均线作为upline
	private Integer baseLine = 144;				//用于定义baseLine,也是几日均线。一般用120均线作为baseLine。股价在baseLine之上才能买入
	private Integer midDuration = 530; 			//用于计算最近多少日的股价中位数。
	private Integer buyValue = 34;   			//买入阀值，
	
	private Integer latestDuration = 13; 	//用于计算最近多少个交易日股价在upline之下的天数。
	private Integer minSlip = 5; 			//在latestDuration期间低于upline均线的次数最多为5次， 超出5次，不得买入
	private Integer maxSlip = 13; 			//在latestDuration期间低于upline均线的次数最多为8次， 超出8次，卖出
	
	private Integer buyPosition = 13; 		//买入仓位，市值的13%
	private Integer reopenRatio = 5; 		//最近一次买入上涨5%以上，再次出现买点，可以加仓; 
	private Integer stopLossRatio = -13;		//最近一次买入下跌5%以上，止损;
	private Integer stopWinRatio = -21;		//买入后，股价从最高点下跌超过13%，止盈;
	
	private BigDecimal initCash = new BigDecimal(1000000);
	private Account account = new Account(initCash);
	
	private Map<String, Bdata> bdatas = new HashMap<String,Bdata>();

	public void clearDatas() {
		bdatas = new HashMap<String,Bdata>();
	}
	
	public void addDailyData(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Bdata bdata = bdatas.get(itemID);
		if(bdata == null) {
			bdata = new Bdata(itemID, tradeDuration, upDuration, midDuration,latestDuration,upLine,baseLine,buyValue,minSlip,maxSlip,reopenRatio,stopLossRatio,stopWinRatio);
			bdatas.put(itemID, bdata);
		}
		bdata.addBar(date, open, high, low, close);
		
		account.refreshHoldsPrice(itemID, close);
		account.setLatestDate(date);		
	}
	
	public Integer getMidDuration() {
		return midDuration;
	}
	
	public boolean noData(String itemID) {
		Bdata bdata = bdatas.get(itemID);
		return bdata==null || bdata.isEmpty();
	}
	
	public Bfeature getFeature(String itemID) {
		Bdata bdata = bdatas.get(itemID);
		if(bdata==null) {
			return null;
		}else {
			return bdata.getFeature();
		}
	}

	public void doIt() {
		Bfeature f;
		List<Bfeature> features = new ArrayList<Bfeature>();
		for(Bdata data : bdatas.values()) {
			f = data.getFeature();
			if(f!=null) {
				features.add(f);
			}
		}
		
		if(features.size()==0) return;
		
		for(Bfeature feature : features) {
			doIt(feature);
		}
	}
	
	public void doIt(Bfeature feature) {
		Integer lots = account.getLots(feature.getItemID());
		if(lots>0) {
			feature.setLatestOpenPrice(account.getLatestOpenPrice(feature.getItemID()));
			feature.setHighestPriceOfHold(account.getHighestPriceOfHold(feature.getItemID()));
		}
		if(feature.getItemID().equals("sz002223")) {
			System.out.println(feature);
		}
		
		//止损或止盈
		if(feature.getStatus() == -2) {
			account.stopByItemID(feature.getItemID(),"");
		}

/*		//平仓
		if(feature.getStatus()==-1) {
			account.drop(feature.getItemID());
		}*/
		
		//开新仓
		if(feature.getStatus()==1) {
			account.open(feature.getItemID(),"","", getQuantity(feature.getItemID(),feature.getNow()),"");
		}
		
		//加仓
		if(feature.getStatus()==2) {
			account.reopen(feature.getItemID(),"","", getQuantity(feature.getItemID(),feature.getNow()), "");
		}	
		
	}
	
	/*
	 *
	 * 每一手的数量，股票是100股，螺纹钢是10吨，...
	 * 目前只针对股票
	 */
	private BigDecimal getQuantityPerHand(String itemID) {
		return new BigDecimal(100);
	}
	
	private Integer getQuantity(String itemID,BigDecimal price) {
		BigDecimal amount = account.getTotal().multiply(new BigDecimal(buyPosition)).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN);
		Integer quantity = amount.divide(price,BigDecimal.ROUND_DOWN).divide(getQuantityPerHand(itemID),BigDecimal.ROUND_DOWN).intValue() * getQuantityPerHand(itemID).intValue();
		
		amount = price.multiply(new BigDecimal(quantity));
		BigDecimal cash = account.getCash();
		//System.out.println("cash=" + cash + ", need " + amount);
		if(amount.compareTo(cash)==1) {
			//System.out.print("not enough cash, change quantity from " + quantity);
			quantity = cash.divide(price,BigDecimal.ROUND_DOWN).divide(getQuantityPerHand(itemID),BigDecimal.ROUND_DOWN).intValue() * getQuantityPerHand(itemID).intValue();
			//System.out.println(" to " + quantity);

		}
		return quantity;
	}
	
	public Set<String> getItemIDsOfHolds(){
		return account.getItemIDsOfHolds();
	}
	
	public Map<String,String> result() {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		result.put("CSV", account.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
		return result;
	}
}
