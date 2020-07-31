package com.rhb.istock.trade.kelly.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.rhb.istock.fund.Account;

/*
 * 
 * 按照海龟的标准版本，突破上轨买入，跌破下轨卖出
 * 
 * 调试时，通过构造器修改参数，确定参数后，将参数设为默认值。
 */
public class Kelly {
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
	 * 止损策略：0--不止损； 1--标准止损； 2--双重止损; 3--三天不涨，止损
	 */
	private Integer stopStrategy;  

	/*
	 * 止赢策略：0--不止赢； 1--标准止赢
	 */
	private Integer winStrategy; 
	
	private Integer gap;  //30在2013-2015年化为35%
	
	private Integer cancels;
	
	/*
	 * 初始值现金。无所谓，一般不低于10万
	 */
	private BigDecimal initCash;
	private Account account;
	
	private Map<String, Kedata> tdatas = new HashMap<String,Kedata>();
	private StringBuffer dailyAmount = new StringBuffer("date,cash,value,total\n");
	private StringBuffer breakers = new StringBuffer();

	public Kelly() {
		deficitFactor  = new BigDecimal(0.008); 
		openDuration = 55; 
		dropDuration = 21; 
		maxOfLot = 1; 
		initCash = new BigDecimal(5000000);
		stopStrategy  = 0;
		gap = 60;
		cancels = 2;
		account = new Account(initCash);
	}
	
	public Kelly(BigDecimal deficitFactor, 
				Integer openDuration, 
				Integer dropDuration, 
				Integer maxOfLot, 
				BigDecimal initCash, 
				Integer stopStrategy,
				Integer gap,
				Integer cancels
				) {
		
		this.deficitFactor = deficitFactor;
		this.openDuration = openDuration;
		this.dropDuration = dropDuration;
		this.maxOfLot = maxOfLot;
		this.initCash = initCash;
		this.stopStrategy = stopStrategy;
		this.gap = gap;
		this.cancels = cancels;
		account = new Account(initCash);
	}
	
	public Set<String> getItemIDsOfHolds(){
		return account.getItemIDsOfHolds();
	}
	
	public void clearDatas() {
		tdatas = new HashMap<String,Kedata>();
	}
	
	public void addDailyData(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Kedata tdata = tdatas.get(itemID);
		if(tdata == null) {
			tdata = new Kedata(itemID,openDuration,dropDuration);
			tdatas.put(itemID, tdata);
		}
		tdata.addBar(date,open,high,low,close);
	}
	
	/*
	 * 此方法很重要
	 * 其修改了account的endDate和price，为doit中的stop、drop、open以及getValue等提供了date和price
	 */
	public boolean addLatestData(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Kedata tdata = tdatas.get(itemID);
		if(tdata == null) {
			System.out.println(" ERROR: tdata is null!");
			return false;
		}
		tdata.setLatestBar(date,open,high,low,close);
		account.refreshHoldsPrice(itemID, close, high);
		account.setLatestDate(date);
		return true;
	}

	public Kefeature getFeature(String itemID){
		Kedata tdata = tdatas.get(itemID);
		if(tdata==null) {
			System.out.println("can Not get feature, for " + itemID + " do not in tdatas.");
			return null;
		}
		return tdata.getFeature();
	}
	

	public List<Kebar> getDailyDatas(String itemID){
		Kedata tdata = tdatas.get(itemID);
		if(tdata==null) {
			return null;
		}else {
			return tdata.getTbars();
		}
	}
	
	public Kebar getLatestData(String itemID) {
		Kedata tdata = tdatas.get(itemID);
		return tdata.getLatestBar();
	}
	
	public void dailyReport() {
		dailyAmount.append(account.getDailyAmount() + "\n");
		System.out.println("\n********* " + account.getDailyAmount());
	}
	
	public void doOpenOrReopen(List<String> itemIDs) {
		breakers.append(account.getEndDate().toString() + ",");
		for(String itemID : itemIDs) {
			if((account.getCash().compareTo(new BigDecimal(10000))==1)) {
				this.doOpenOrReopen(itemID);
			}				
			breakers.append(itemID);
			breakers.append(",");
		}

		breakers.deleteCharAt(breakers.length()-1);
		breakers.append("\n");
	}
	
	public void doStopOrDrop(Set<String> itemIDs) {
		for(String itemID : itemIDs) {
			this.doStopOrDrop(itemID);
		}
	}
	
	private void doStopOrDrop(String itemID) {
		//几天后不涨，取消持仓
		//cancel(itemID);
		
		//止损
		if(stopStrategy==1) {
			doStop(itemID);
		}else if(stopStrategy==2) {
			doDoubleStop(itemID);
		}
		
		//平仓
		doDrop(itemID);
		
	}

	private void doOpenOrReopen(String itemID) {
		Kedata tdata = tdatas.get(itemID);
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
		
		//加仓
		doReopen(itemID);
		
		//开新仓
		doOpen(itemID);
	}
	
	private void cancel(String itemID) {
		Set<Integer> orderIDs = account.getHoldOrderIDs(itemID);
		for(Integer orderID : orderIDs) {
			if(account.isStupid(itemID, orderID, cancels)) {
				account.cancelByOrderID(orderID);
			}
		}
	}
	
	//双重止损
	private void doDoubleStop(String itemID) {
		Kedata tdata = tdatas.get(itemID);
		
		Kefeature feature = tdata.getFeature();
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
		Kedata tdata = tdatas.get(itemID);
		
		Kefeature feature = tdata.getFeature();
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
				account.stopByItemID(itemID, "");
				System.out.println("cash=" + account.getCash());
			}
		}		
	}
	
	private void doDrop(String itemID) {
		Kedata tdata = tdatas.get(itemID);
		Kefeature feature = tdata.getFeature();
		Integer lots = account.getLots(tdata.getItemID());
		if((lots>0 && feature!=null && feature.getStatus()<0)) {
			System.out.println(feature);
			System.out.println("the lots " + lots + ">0, and status is "+feature.getStatus() + ", do drop!!");//--------------
			account.drop(itemID,"doDrop");
			System.out.println("cash=" + account.getCash());
		}
		
	}
	
	private void doReopen(String itemID) {
		Kedata tdata = tdatas.get(itemID);
		Kefeature feature = tdata.getFeature();
		Integer lots = account.getLots(tdata.getItemID());
		if(feature.getStatus()==2 && lots>0 && lots<maxOfLot) {
			System.out.println("the lots " + lots + ">0, and < "+ maxOfLot +", should do reopen?");//--------------
			
			BigDecimal half_atr = feature.getAtr().divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP);
			BigDecimal now = feature.getNow();
			
			BigDecimal reopenPrice = account.getLatestOpenPrice(itemID).add(half_atr);
			Integer ratio = now.subtract(reopenPrice).divide(reopenPrice,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).intValue();
			if(reopenPrice.compareTo(now)==-1) {
			//if(reopenPrice.compareTo(now)==-1 && ratio<=10) {
				System.out.println("reopenPrice "+ reopenPrice +" below now price "+now+" "+ratio+"%, do reopen!!");
				//Integer unit = getPositionUnit(feature.getAtr(),getQuantityPerHand(itemID),deficitFactor);
				//Integer quantity = getLot(itemID).multiply(new BigDecimal(unit)).intValue();
				Integer quantity = getQuantity(feature.getAtr(),getQuantityPerHand(itemID),deficitFactor,feature.getNow());
				if(quantity>0) {
					account.reopen(itemID,"","", quantity, reopenPrice.toString());
				}
				System.out.println("cash=" + account.getCash());
			}else {
				System.out.println("reopenPrice "+ reopenPrice +" above now price "+now+ " "+ratio+"%, do NOT reopen!!");
			}
		}
		
	}
	
	private void doOpen(String itemID) {
		Kedata tdata = tdatas.get(itemID);
		Kefeature feature = tdata.getFeature();
		Integer lots = account.getLots(tdata.getItemID());
		//if(feature.getStatus()==2 && lots<maxOfLot && feature.getHlgap()<=gap && isGoodTime) {
		if(feature.getStatus()==2 && lots==0 ) {
			System.out.println("\n do open");//--------------
			Integer quantity = getQuantity(feature.getAtr(),getQuantityPerHand(itemID),deficitFactor,feature.getNow());
			
			if(quantity>0) {
				account.open(itemID,"","", quantity,lots.toString());
			}
			
			//account.open(itemID, 0, ""); //第一次突破，买入为0，如果后面再突破，才真正买入
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
		result.put("dailyAmount", dailyAmount.toString());
		result.put("breakers", breakers.toString());
		return result;
	}
	
	/*
	 *
	 * 每一手的数量，股票是100股，螺纹钢是10吨，...
	 * 目前只针对股票
	 */
	private BigDecimal getQuantityPerHand(String itemID) {
		return new BigDecimal(100);
	}

	public Integer getOpenDuration() {
		return openDuration;
	}

	public Integer getDropDuration() {
		return dropDuration;
	}
	
	
}
