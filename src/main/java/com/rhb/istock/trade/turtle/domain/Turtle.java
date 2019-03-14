package com.rhb.istock.trade.turtle.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	 * 是否止损
	 */
	private boolean isStop;  
	
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
		initCash = new BigDecimal(100000);
		isStop  = false;
		gap = 60;
		account = new Account(initCash);
		tdatas = new HashMap<String,Tdata>();		
	}
	
	public Turtle(BigDecimal deficitFactor, 
				Integer openDuration, 
				Integer dropDuration, 
				Integer maxOfLot, 
				BigDecimal initCash, 
				boolean isStop,
				Integer gap
				) {
		
		this.deficitFactor = deficitFactor;
		this.openDuration = openDuration;
		this.dropDuration = dropDuration;
		this.maxOfLot = maxOfLot;
		this.initCash = initCash;
		this.isStop = isStop;
		this.gap = gap;
		account = new Account(initCash);
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
	
	public boolean setLatestBar(String itemID,LocalDate date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close) {
		Tdata tdata = tdatas.get(itemID);
		if(tdata == null) {
			System.out.println("ERROR: item is null!");
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
			//System.out.println(tdatas);
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
	
	//--------------

	
	public void doit(Map<String,String> kData) {
		Tdata tdata = tdatas.get(kData.get("itemID"));
		if(tdata == null) {
			System.out.format("ERROR: item is null of %s.\n", kData.get("itemID") );
			return;
		}		
		if(tdata.getTbars().size()<openDuration) {
			//System.out.format("INF: %s history bars is %d, below open duration %d, skip.\n", kData.get("itemID"),item.getBars().size(),openDuration );
			return;
		}
		
		//item.setLatestBar(kData);
		
/*		Map<String,String> features = tdata.getFeatures();
		Integer hlgap = new BigDecimal(features.get("hlgap")).multiply(new BigDecimal(100)).intValue();

		account.updatePrice(tdata.getItemID(), new BigDecimal(kData.get("close")));
		Integer lots = account.getLots(tdata.getItemID());
		
		//止损
		if(lots>0 && isStop) {
			//account.stop(kData);
		}
		
		//平仓
		if(lots>0 && (features.get("status").equals("-1") || features.get("status").equals("-2"))) {
			//account.drop(kData);
		}
		
		//开新仓、加仓
		if(features.get("status").equals("2")) {
			if(lots>0 && lots<maxOfLot) {
				doReopen(kData);
			}else if(lots==0 && hlgap<=gap){
				//System.out.println("hlgap: " + hlgap);
				doOpen(kData);
			}
		}*/
	}
	

	
	public boolean isExist(String itemID) {
		return tdatas.containsKey(itemID);
	}
	
	public void clearBars(String itemID) {
		Tdata item = tdatas.get(itemID);
		if(item!=null) {
			item.clearBars();
		}
	}
	
	public void addBars(List<Map<String,String>> kDatas) {
		for(Map<String,String> kData : kDatas) {
			//this.addBar(kData);
		}
	}
	

	
	//开新仓
	private void doOpen(Map<String,String> kData) {
/*		Kline item = klines.get(kData.get("itemID"));
		BigDecimal atr = item.getATR();
		BigDecimal price = new BigDecimal(kData.get("close"));
		LocalDate date = LocalDate.parse(kData.get("dateTime"));
		
		BigDecimal stopPrice = price.subtract(atr);
		BigDecimal reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
		
		Order openOrder = new Order(UUID.randomUUID().toString(),item.getItemID(),date, 1, price,stopPrice,reopenPrice	);
		openOrder.setNote("open，stop=" + stopPrice + "，reOpen="+reopenPrice);
		
		account.open(openOrder, deficitFactor, atr, getLot(item.getItemID()));
*/	
	}
	
	//加仓
	private void doReopen(Map<String,String> kData) {
/*		Kline item = klines.get(kData.get("itemID"));
		BigDecimal atr = item.getATR();
		BigDecimal reopenPrice = account.getReopenPrice(item.getItemID());
		BigDecimal price = new BigDecimal(kData.get("close"));
		LocalDate date = LocalDate.parse(kData.get("dateTime"));
		
		BigDecimal high = new BigDecimal(kData.get("high"));
		if(high.compareTo(reopenPrice)==1) {
			BigDecimal stopPrice = price.subtract(atr);
			reopenPrice = price.add(atr.divide(new BigDecimal(2),BigDecimal.ROUND_HALF_UP));
			
			Order openOrder = new Order(UUID.randomUUID().toString(),item.getItemID(),date, 1, price,stopPrice,	reopenPrice	);
			openOrder.setNote("reOpen，stop=" + stopPrice + "，reOpen="+reopenPrice);
			account.open(openOrder, deficitFactor, atr, getLot(item.getItemID()));
		}*/
	}
		
	
	public Map<String,String> result() {
		if(account == null) return null;
		
		Map<String,String> result = new HashMap<String,String>();
		//result.put("CSV", account.getCSV());
		result.put("initCash", this.initCash.toString());
		result.put("cash", account.getCash().toString());
		result.put("value", account.getValue().toString());
		result.put("total", account.getTotal().toString());
		result.put("winRatio", account.getWinRatio().toString()); //赢率
		//result.put("cagr", account.getCAGR().toString());  //复合增长率的英文缩写为：CAGR（Compound Annual Growth Rate）
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
