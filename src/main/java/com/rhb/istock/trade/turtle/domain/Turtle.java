package com.rhb.istock.trade.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.rhb.istock.fund.Account;
import com.rhb.istock.kdata.Kdata;

/*
 * 按照海龟的标准版本，突破上轨买入，跌破下轨卖出
 * 最高的年化收益率16%
 * 
 * 调试时，通过构造器修改参数，确定参数后，将参数设为默认值。
 * 
 */
public class Turtle {
	/*
	 * 亏损因子，即买了一个品种后 ，该品种价格下跌一个atr，总资金将下跌百分之几
	 */
	private BigDecimal deficitFactor; 
	
	/*
	 * 开仓判定通道区间
	 */
	private Integer openDuration; 

	/*
	 * 平仓判定通道区间
	 */
	private Integer dropDuration; 
	
	/*
	 * 一只股票最大持仓单位
	 */
	private Integer maxOfLot; 

	/*
	 * 止损策略：0--不止损； 1--标准止损； 2--双重止损
	 */
	private Integer stopStrategy;  
	
	private Integer gap;  //30在2013-2015年化为35%
	
	/*
	 * 初始值现金。无所谓，一般不低于10万
	 */
	private BigDecimal initCash;
	
	private Map<String, Tdata> tdatas;
	private Account account;

	public Turtle() {
		deficitFactor  = new BigDecimal(0.01); 
		openDuration = 89; 
		dropDuration = 34; 
		maxOfLot = 3; 
		initCash = new BigDecimal(500000);
		stopStrategy  = 1;
		gap = 60;
		account = new Account(initCash);
		tdatas = new HashMap<String,Tdata>();		
	}
	
	public Turtle(BigDecimal deficitFactor, 
				Integer openDuration, 
				Integer dropDuration, 
				Integer maxOfLot, 
				BigDecimal initCash, 
				Integer stopStrategy,
				Integer gap
				) {
		
		this.deficitFactor = deficitFactor;
		this.openDuration = openDuration;
		this.dropDuration = dropDuration;
		this.maxOfLot = maxOfLot;
		this.initCash = initCash;
		this.stopStrategy = stopStrategy;
		this.gap = gap;
		account = new Account(initCash);
		tdatas = new HashMap<String,Tdata>();
	}
	
	public Set<String> getItemIDsOfHolds(){
		return account.getItemIDsOfHolds();
	}
	
	public void clearDatas() {
		tdatas = new HashMap<String,Tdata>();
	}
	
	public void addBar(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Tdata tdata = tdatas.get(itemID);
		if(tdata == null) {
			tdata = new Tdata(itemID,openDuration,dropDuration);
			tdatas.put(itemID, tdata);
		}
		tdata.addBar(date,open,high,low,close);
		
	}
	
	/*
	 * setLatestBar很重要
	 * 其修改了account的endDate和price，为doit中的stop、drop、open以及getValue等提供了date和price
	 */
	public boolean setLatestBar(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Tdata tdata = tdatas.get(itemID);
		if(tdata == null) {
			System.out.println(" ERROR: tdata is null!");
			return false;
		}
		tdata.setLatestBar(date,open,high,low,close);
		account.refreshHoldsPrice(itemID, close);
		account.setLatestDate(date);
		return true;
	}

	public Feature getFeature(String itemID){
		Tdata tdata = tdatas.get(itemID);
		if(tdata==null) {
			System.out.println("can Not get feature, for " + itemID + " do not in tdatas.");
			return null;
		}
		return tdatas.get(itemID).getFeature();
	}

	public List<Tbar> getTbars(String itemID){
		Tdata tdata = tdatas.get(itemID);
		return tdata.getTbars();
	}
	
	public Tbar getLatestBar(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		return tdata.getLatestBar();
	}
	
	/*
	 * 
	 */
	public void doIt() {
		Feature f;
		List<Feature> features = new ArrayList<Feature>();
		for(Tdata tdata : tdatas.values()) {
			f = tdata.getFeature();
			if(f != null) {
				features.add(tdata.getFeature());
			}
		}
		
		if(features.size()==0) return;
		
		Collections.sort(features, new Comparator<Feature>() {
			@Override
			public int compare(Feature o1, Feature o2) {
				if(o1.getHlgap().equals(o2.getHlgap())) {
					return o2.getNhgap().compareTo(o1.getNhgap());
				}else {
					return o1.getHlgap().compareTo(o2.getHlgap());
				}
			}
		});
		
		for(Feature feature : features) {
			doIt(feature.getItemID());
		}
	}

	private void doIt(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		if(tdata == null) {
			System.out.format("ERROR: tdata is null of %s.\n", itemID);
			return;
		}		
		if(tdata.getTbars().size()<openDuration) {
			System.out.format("INF: %s history bars is %d, below open duration %d, skip.\n", itemID,tdata.getTbars().size(),openDuration );
			return;
		}
		if(tdata.yzb()) {
			System.out.println("INF: " + itemID + " 一字板，无法成交");
			return;
		}
		
		//止损
		if(stopStrategy==1) {
			doStop(itemID);
		}else if(stopStrategy==2) {
			doDoubleStop(itemID);
		}
		
		//平仓
		doDrop(itemID);
		
		//加仓
		doReopen(itemID);
		
		//开新仓
		doOpen(itemID);
	}
	
	//双重止损
	private void doDoubleStop(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		
		Feature feature = tdata.getFeature();
		System.out.println(feature); //--------------

		Integer lots = account.getLots(tdata.getItemID());
		System.out.println("lots=" + lots);//--------------
		
		if(lots>0) {
			System.out.println("the lots " + lots + ">0, should do Double stop?");//--------------
			BigDecimal hlaf_atr = feature.getAtr().divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP);
			BigDecimal now = feature.getNow();
			Map<String,BigDecimal> openPrices = account.getOpenPrices(itemID);
			BigDecimal stopPrice;
			for(Map.Entry<String, BigDecimal> entry : openPrices.entrySet()) {
				stopPrice = entry.getValue().subtract(hlaf_atr);
				System.out.println("stopPrice=" + stopPrice + ",now=" + now);
				if(stopPrice.compareTo(now)==1) {
					System.out.println("do stop!!");
					account.stopByOrderID(entry.getKey());
					System.out.println("cash=" + account.getCash());

				}
			}
		}		
	}
	
	//标准止损
	private void doStop(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		
		Feature feature = tdata.getFeature();
		System.out.println(feature); //--------------

		Integer lots = account.getLots(tdata.getItemID());
		System.out.println("lots=" + lots);//--------------
		
		if(lots>0) {
			System.out.println("the lots " + lots + ">0, should do stop?");//--------------
			BigDecimal doubleAtr = feature.getAtr().multiply(new BigDecimal(2));
			BigDecimal stopPrice = account.getLatestOpenPrice(itemID).subtract(doubleAtr);
			BigDecimal now = feature.getNow();
			System.out.println("stopPrice=" + stopPrice + ",now=" + now);
			if(stopPrice.compareTo(now)==1) {
				System.out.println("do stop!!");
				account.stopByItemID(itemID);
				System.out.println("cash=" + account.getCash());

			}
		}		
	}
	
	private void doDrop(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		Feature feature = tdata.getFeature();
		Integer lots = account.getLots(tdata.getItemID());
		if(lots>0 && feature.getStatus()<0) {
			System.out.println("the lots " + lots + ">0, and status is "+feature.getStatus()+", do drop!!");//--------------
			account.drop(itemID);
			System.out.println("cash=" + account.getCash());
		}
		
	}
	
	private void doReopen(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		Feature feature = tdata.getFeature();
		Integer lots = account.getLots(tdata.getItemID());
		if(feature.getStatus()==2 && lots>0 && lots<maxOfLot) {
			System.out.println("the lots " + lots + ">0, and < "+ maxOfLot +", should do reopen?");//--------------
			
			BigDecimal half_atr = feature.getAtr().divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP);
			BigDecimal now = feature.getNow();
			
			BigDecimal reopenPrice = account.getLatestOpenPrice(itemID).add(half_atr);
			if(reopenPrice.compareTo(now)==-1) {
				System.out.println("reopenPrice "+ reopenPrice +" below now price "+now+", do reopen!!");
				Integer unit = getPositionUnit(feature.getAtr(),getLot(itemID),deficitFactor);
				//Integer quantity = getLot(itemID).multiply(new BigDecimal(unit)).intValue();
				Integer quantity = getQuantity(feature.getAtr(),getLot(itemID),deficitFactor,feature.getNow());
				account.reopen(itemID, quantity);
				System.out.println("cash=" + account.getCash());
			}else {
				System.out.println("reopenPrice "+ reopenPrice +" above now price "+now+", do NOT reopen!!");
			}
		}
		
	}
	
	private void doOpen(String itemID) {
		Tdata tdata = tdatas.get(itemID);
		Feature feature = tdata.getFeature();
		Integer lots = account.getLots(tdata.getItemID());
		if(feature.getStatus()==2 && lots==0 && feature.getHlgap()<=gap) {
			System.out.println("do open");//--------------
			Integer quantity = getQuantity(feature.getAtr(),getLot(itemID),deficitFactor,feature.getNow());
			account.open(itemID, quantity);
			System.out.println("cash=" + account.getCash());
		}
		
	}
	
	
	private Integer getQuantity(BigDecimal atr, BigDecimal lot, BigDecimal deficitFactor, BigDecimal price) {
		Integer unit = getPositionUnit(atr,lot,deficitFactor);
		Integer quantity = lot.multiply(new BigDecimal(unit)).intValue();
		BigDecimal amount = price.multiply(new BigDecimal(quantity));
		BigDecimal cash = account.getCash();
		System.out.println("cash=" + cash + ", need " + amount);
		if(amount.compareTo(cash)==1) {
			System.out.print("not enough cash, change quantity from " + quantity);
			quantity = cash.divide(price,BigDecimal.ROUND_DOWN).divide(lot,BigDecimal.ROUND_DOWN).intValue() * lot.intValue();
			System.out.println(" to " + quantity);

		}
		return quantity;
	}
	
	/*
	 * 根据账户总市值和在手现金获得头寸规模单位，即一次可买入多少手
	 */
	private Integer getPositionUnit(BigDecimal atr, BigDecimal lot, BigDecimal deficitFactor) {
		return account.getTotal().multiply(deficitFactor).divide(atr,BigDecimal.ROUND_DOWN).divide(lot,BigDecimal.ROUND_DOWN).intValue();
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
	
	/*
	 *
	 * 一手的数量，股票是100股，螺纹钢是10吨，...
	 * 目前只针对股票
	 */
	private BigDecimal getLot(String articleID) {
		return new BigDecimal(100);
	}

	public Integer getOpenDuration() {
		return openDuration;
	}

	public Integer getDropDuration() {
		return dropDuration;
	}
	
	
}
