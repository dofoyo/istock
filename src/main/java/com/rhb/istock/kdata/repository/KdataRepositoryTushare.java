package com.rhb.istock.kdata.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("kdataRepositoryTushare")
public class KdataRepositoryTushare implements KdataRepository{
	@Value("${tushareKdataPath}")
	private String kdataPath;
	
	//private Map<String,KdataEntity> kdatas = new HashMap<String,KdataEntity>();;
	
	protected static final Logger logger = LoggerFactory.getLogger(KdataRepositoryTushare.class);

	@Override
	@CacheEvict(value="tushareDailyKdatas",allEntries=true)
	public void evictKDataCache() {}
	
	@Override
	public KdataEntity getKdata(String itemID) {
		KdataEntity kdata = new KdataEntity(itemID);

		String tushareID = itemID.indexOf("sh")==0 ? itemID.substring(2)+".SH" : itemID.substring(2)+".SZ";
		String kdataFile = kdataPath + "/daily/" + tushareID + "_kdatas.json";
		String factorFile = kdataPath + "/factor/" + tushareID + "_factors.json";
		String basicFile = kdataPath + "/basic/" + tushareID + "_basics.json";

		if(FileTools.isExists(kdataFile) && FileTools.isExists(factorFile) && FileTools.isExists(basicFile)) {
			Map<LocalDate,Basic> basics = new HashMap<LocalDate,Basic>();
			Basic basic;
			JSONObject basicObject = new JSONObject(FileTools.readTextFile(basicFile));
			JSONArray items = basicObject.getJSONArray("items");
			
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					basic = new Basic(item);
					basics.put(basic.getTrade_date(),basic);
				}
			}
			
			BigDecimal roof = null;
			JSONObject factor = new JSONObject(FileTools.readTextFile(factorFile));
			TreeMap<LocalDate,BigDecimal> factors = new TreeMap<LocalDate,BigDecimal>();
			items = factor.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					factors.put(LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd")),item.getBigDecimal(2));
				}
				roof = factors.lastEntry().getValue();
			}
			
			LocalDate date;
			BigDecimal open,high,low,close,amount,quantity;
			BigDecimal nowFactor = null;
			JSONObject data = new JSONObject(FileTools.readTextFile(kdataFile));
			items = data.getJSONArray("items");
			if(items.length()>0) {
				JSONArray item;
				BigDecimal turnover_rate_f,volume_ratio,total_mv,circ_mv,total_share,float_share,free_share,pe;
				for(int i=0; i<items.length(); i++) {
					item = items.getJSONArray(i);
					
					date = LocalDate.parse(item.getString(1),DateTimeFormatter.ofPattern("yyyyMMdd"));
					nowFactor = factors.get(date);
					
					//System.out.println("date=" + date + ", nowFactor=" + nowFactor);
					//if(preFactor==null) preFactor = nowFactor;
					
					if(nowFactor!=null && roof!=null) {
						open = getPrice(item.getBigDecimal(2),nowFactor,roof);
						high = getPrice(item.getBigDecimal(3),nowFactor,roof);
						low = getPrice(item.getBigDecimal(4),nowFactor,roof);
						close = getPrice(item.getBigDecimal(5),nowFactor,roof);
						amount = item.getBigDecimal(10).multiply(new BigDecimal(1000));  //1千元等于1000元
						quantity = item.getBigDecimal(9).multiply(new BigDecimal(100));	// 1手等于100股					
						
						basic = basics.get(date);
						turnover_rate_f = basic==null ? new BigDecimal(0) : basic.getTurnover_rate_f();
						volume_ratio = basic==null ? new BigDecimal(0) : basic.getVolume_ratio();
						total_mv = basic==null ? new BigDecimal(0) : basic.getTotal_mv();
						circ_mv = basic==null ? new BigDecimal(0) : basic.getCirc_mv();
						total_share = basic==null ? new BigDecimal(0) : basic.getTotal_share();
						float_share = basic==null ? new BigDecimal(0) : basic.getFloat_share();
						free_share = basic==null ? new BigDecimal(0) : basic.getFree_share();
						pe = basic==null ? new BigDecimal(0) : basic.getPe();
						
						kdata.addBar(date,open,high,low,close,amount,quantity,turnover_rate_f,volume_ratio,total_mv,circ_mv,total_share,float_share,free_share,pe);
					}
					
/*					if(nowFactor==null){
						logger.error("The nowFactor of " + itemID + " on " + date.toString() + " is NULL.");
					}

					if(roof==null){
						logger.error("The roof of " + itemID + " is NULL.");
					}*/
				}
			}
		}
		
		//kdatas.put(itemID, kdata);
		//System.out.println("kdatas.size()=" + kdatas.size());

		return kdata;
	}
	
	private BigDecimal getPrice(BigDecimal price, BigDecimal nowFactor, BigDecimal preFactor) {
		return price.multiply(nowFactor).divide(preFactor,3,BigDecimal.ROUND_HALF_DOWN);
	}

	@Override
	@Cacheable("tushareDailyKdatas")
	public KdataEntity getKdataByCache(String itemID) {
		return this.getKdata(itemID);
	}

	@Override
	public LocalDate getLastDate(String itemID) {
		KdataEntity kdata = this.getKdata(itemID);
		if(kdata != null) {
			return kdata.getLastDate();
		}else {
			return null;
		}
	}
	
	class Daily{
		/*
		 *  0 ts_code	str	股票代码
			1 trade_date	str	交易日期
			2 open	float	开盘价
			3 high	float	最高价
			4 low	float	最低价
			5 close	float	收盘价
			6 pre_close	float	昨收价
			7 change	float	涨跌额
			8 pct_chg	float	涨跌幅 （未复权，如果是复权请用 通用行情接口 ）
			9 vol	float	成交量 （手）
			10 amount	float	成交额 （千元）
		 */
		private String ts_code;
		private LocalDate trade_date;
		private BigDecimal open;
		private BigDecimal high;
		private BigDecimal low;
		private BigDecimal close;
		private BigDecimal pre_close;
		private BigDecimal change;
		private BigDecimal pct_chg;
		private BigDecimal vol;
		private BigDecimal amount;
		
		public Daily(JSONArray item, BigDecimal nowFactor, BigDecimal roof) {
			this.ts_code = item.getString(0);
			this.trade_date = LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd"));
			this.open = getPrice(item.getBigDecimal(2),nowFactor,roof);
			this.high = getPrice(item.getBigDecimal(3),nowFactor,roof);
			this.low = getPrice(item.getBigDecimal(4),nowFactor,roof);
			this.close = getPrice(item.getBigDecimal(5),nowFactor,roof);
			this.pre_close = getPrice(item.getBigDecimal(6),nowFactor,roof);
			this.change = getPrice(item.getBigDecimal(7),nowFactor,roof);
			this.pct_chg = item.getBigDecimal(8);
			this.vol = item.getBigDecimal(9).multiply(new BigDecimal(100)); //1千元等于1000元
			this.amount = item.getBigDecimal(10).multiply(new BigDecimal(1000)); //1手等于100股
		}
		
		public String getTs_code() {
			return ts_code;
		}
		public void setTs_code(String ts_code) {
			this.ts_code = ts_code;
		}
		public LocalDate getTrade_date() {
			return trade_date;
		}
		public void setTrade_date(LocalDate trade_date) {
			this.trade_date = trade_date;
		}
		public BigDecimal getOpen() {
			return open;
		}
		public void setOpen(BigDecimal open) {
			this.open = open;
		}
		public BigDecimal getHigh() {
			return high;
		}
		public void setHigh(BigDecimal high) {
			this.high = high;
		}
		public BigDecimal getLow() {
			return low;
		}
		public void setLow(BigDecimal low) {
			this.low = low;
		}
		public BigDecimal getClose() {
			return close;
		}
		public void setClose(BigDecimal close) {
			this.close = close;
		}
		public BigDecimal getPre_close() {
			return pre_close;
		}
		public void setPre_close(BigDecimal pre_close) {
			this.pre_close = pre_close;
		}
		public BigDecimal getChange() {
			return change;
		}
		public void setChange(BigDecimal change) {
			this.change = change;
		}
		public BigDecimal getPct_chg() {
			return pct_chg;
		}
		public void setPct_chg(BigDecimal pct_chg) {
			this.pct_chg = pct_chg;
		}
		public BigDecimal getVol() {
			return vol;
		}
		public void setVol(BigDecimal vol) {
			this.vol = vol;
		}
		public BigDecimal getAmount() {
			return amount;
		}
		public void setAmount(BigDecimal amount) {
			this.amount = amount;
		}
		
		
	}
	
	
	class Basic{
		/*
		 * 

0 ts_code	str	TS股票代码
1 trade_date	str	交易日期
2 close	float	当日收盘价
3 turnover_rate	float	换手率（%）
4 turnover_rate_f	float	换手率（自由流通股）
5 volume_ratio	float	量比
6 pe	float	市盈率（总市值/净利润）
7 pe_ttm	float	市盈率（TTM）
8 pb	float	市净率（总市值/净资产）
9 ps	float	市销率
10 ps_ttm	float	市销率（TTM）
11 dv_ratio	float	股息率 （%）
12 dv_ttm	float	股息率（TTM）（%）
13 total_share	float	总股本 （万股）
14 float_share	float	流通股本 （万股）
15 free_share	float	自由流通股本 （万）
16 total_mv	float	总市值 （万元）
17 circ_mv	float	流通市值（万元）
		 */
		private String ts_code;
		private LocalDate trade_date;
		private BigDecimal close;
		private BigDecimal turnover_rate;
		private BigDecimal turnover_rate_f;
		private BigDecimal volume_ratio;
		private BigDecimal pe;
		private BigDecimal pe_ttm;
		private BigDecimal pb;
		private BigDecimal ps;
		private BigDecimal ps_ttm;
		private BigDecimal dv_ratio;
		private BigDecimal dv_ttm;
		private BigDecimal total_share;
		private BigDecimal float_share;
		private BigDecimal free_share;
		private BigDecimal total_mv;
		private BigDecimal circ_mv;
		
		public Basic(JSONArray item) {
			this.ts_code = item.getString(0);
			this.trade_date = LocalDate.parse(item.getString(1), DateTimeFormatter.ofPattern("yyyyMMdd"));
			this.close = item.get(2).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(2);
			this.turnover_rate = item.get(3).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(3);
			this.turnover_rate_f = item.get(4).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(4);
			this.volume_ratio = item.get(5).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(5);
			this.pe = item.get(6).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(6);
			this.pe_ttm = item.get(7).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(7);
			this.pb = item.get(8).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(8);
			this.ps = item.get(9).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(9);
			this.ps_ttm = item.get(10).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(10);
			this.dv_ratio = item.get(11).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(11);
			this.dv_ttm = item.get(12).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(12);
			this.total_share = item.get(13).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(13).multiply(new BigDecimal(10000));
			this.float_share = item.get(14).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(14).multiply(new BigDecimal(10000));
			this.free_share = item.get(15).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(15).multiply(new BigDecimal(10000));
			this.total_mv = item.get(16).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(16).multiply(new BigDecimal(10000));
			this.circ_mv = item.get(17).toString().equals("null") ? BigDecimal.ZERO : item.getBigDecimal(17).multiply(new BigDecimal(10000));
					
		}
		
		public String getTs_code() {
			return ts_code;
		}
		public void setTs_code(String ts_code) {
			this.ts_code = ts_code;
		}
		public LocalDate getTrade_date() {
			return trade_date;
		}
		public void setTrade_date(LocalDate trade_date) {
			this.trade_date = trade_date;
		}
		public BigDecimal getClose() {
			return close;
		}
		public void setClose(BigDecimal close) {
			this.close = close;
		}
		public BigDecimal getTurnover_rate() {
			return turnover_rate;
		}
		public void setTurnover_rate(BigDecimal turnover_rate) {
			this.turnover_rate = turnover_rate;
		}
		public BigDecimal getTurnover_rate_f() {
			return turnover_rate_f;
		}
		public void setTurnover_rate_f(BigDecimal turnover_rate_f) {
			this.turnover_rate_f = turnover_rate_f;
		}
		public BigDecimal getVolume_ratio() {
			return volume_ratio;
		}
		public void setVolume_ratio(BigDecimal volume_ratio) {
			this.volume_ratio = volume_ratio;
		}
		public BigDecimal getPe() {
			return pe;
		}
		public void setPe(BigDecimal pe) {
			this.pe = pe;
		}
		public BigDecimal getPe_ttm() {
			return pe_ttm;
		}
		public void setPe_ttm(BigDecimal pe_ttm) {
			this.pe_ttm = pe_ttm;
		}
		public BigDecimal getPb() {
			return pb;
		}
		public void setPb(BigDecimal pb) {
			this.pb = pb;
		}
		public BigDecimal getPs() {
			return ps;
		}
		public void setPs(BigDecimal ps) {
			this.ps = ps;
		}
		public BigDecimal getPs_ttm() {
			return ps_ttm;
		}
		public void setPs_ttm(BigDecimal ps_ttm) {
			this.ps_ttm = ps_ttm;
		}
		public BigDecimal getDv_ratio() {
			return dv_ratio;
		}
		public void setDv_ratio(BigDecimal dv_ratio) {
			this.dv_ratio = dv_ratio;
		}
		public BigDecimal getDv_ttm() {
			return dv_ttm;
		}
		public void setDv_ttm(BigDecimal dv_ttm) {
			this.dv_ttm = dv_ttm;
		}
		public BigDecimal getTotal_share() {
			return total_share;
		}
		public void setTotal_share(BigDecimal total_share) {
			this.total_share = total_share;
		}
		public BigDecimal getFloat_share() {
			return float_share;
		}
		public void setFloat_share(BigDecimal float_share) {
			this.float_share = float_share;
		}
		public BigDecimal getFree_share() {
			return free_share;
		}
		public void setFree_share(BigDecimal free_share) {
			this.free_share = free_share;
		}
		public BigDecimal getTotal_mv() {
			return total_mv;
		}
		public void setTotal_mv(BigDecimal total_mv) {
			this.total_mv = total_mv;
		}
		public BigDecimal getCirc_mv() {
			return circ_mv;
		}
		public void setCirc_mv(BigDecimal circ_mv) {
			this.circ_mv = circ_mv;
		}
		
		
	}


}
