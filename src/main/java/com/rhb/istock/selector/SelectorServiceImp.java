package com.rhb.istock.selector;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.IstockScheduledTask;
import com.rhb.istock.comm.util.Functions;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.Kdata;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.aat.AverageAmountTopService;
import com.rhb.istock.selector.b21.B21Service;
import com.rhb.istock.selector.bav.BavService;
import com.rhb.istock.selector.bluechip.BluechipService;
import com.rhb.istock.selector.drum.DrumService;
import com.rhb.istock.selector.favor.FavorService;
import com.rhb.istock.selector.hlb2.Hlb2Service;
import com.rhb.istock.selector.hlt.HighLowTopService;
import com.rhb.istock.selector.hold.HoldEntity;
import com.rhb.istock.selector.hold.HoldService;
import com.rhb.istock.selector.lpb.LpbService;
import com.rhb.istock.selector.lpb2.Lpb2Service;
import com.rhb.istock.selector.potential.Potential;
import com.rhb.istock.selector.potential.PotentialService;

@Service("selectorServiceImp")
public class SelectorServiceImp implements SelectorService{
	protected static final Logger logger = LoggerFactory.getLogger(IstockScheduledTask.class);

	@Autowired
	@Qualifier("bluechipServiceImp")
	BluechipService bluechipService;
	
	@Autowired
	@Qualifier("averageAmountTopServiceImp")	
	AverageAmountTopService aat;
	
	@Autowired
	@Qualifier("highLowTopServiceImp")
	HighLowTopService hlt;

	@Autowired
	@Qualifier("favorServiceImp")
	FavorService favorService;

	@Autowired
	@Qualifier("holdServiceImp")
	HoldService holdService;

	@Autowired
	@Qualifier("potentialService")
	PotentialService potentialService;

	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;
	
	@Autowired
	@Qualifier("lpbService")
	LpbService lpbService;

	@Autowired
	@Qualifier("drumService")
	DrumService drumService;
	
	@Autowired
	@Qualifier("bavService")
	BavService bavService;

	@Autowired
	@Qualifier("b21Service")
	B21Service b21Service;

	@Autowired
	@Qualifier("hlb2Service")
	Hlb2Service hlb2Service;

	@Autowired
	@Qualifier("lpb2Service")
	Lpb2Service lpb2Service;
	
	@Override
	public List<HoldEntity> getHolds() {
		return holdService.getHolds();
	}

	@Override
	public Map<String,Integer> getHighLowTops(Integer top) {
		return hlt.getHighLowTops(top);
	}

	@Override
	public Map<String,Integer>  getLatestAverageAmountTops(Integer top) {
		return aat.getLatestAverageAmountTops(top);
	}

	@Override
	public void generateBluechip() {
		bluechipService.generateBluechip();		
	}

	@Override
	public List<String> getBluechipIDs(LocalDate date) {
		return bluechipService.getBluechipIDs(date);
	}

	@Override
	public Map<String, String> getFavors() {
		return favorService.getFavors();
	}

	@Override
	public List<String> getLatestBluechipIDs() {
		return bluechipService.getLatestBluechipIDs();
	}

	@Override
	public TreeMap<LocalDate,List<String>> getBluechipIDs(LocalDate beginDate, LocalDate endDate) {
		return bluechipService.getBluechipIDs(beginDate, endDate) ;
	}

	@Override
	public List<String> getHoldIDs() {
		List<HoldEntity> holds  = getHolds();
		List<String> ids = new ArrayList<String>();
		for(HoldEntity hold : holds) {
			ids.add(hold.getItemID());
		}
		return ids;
	}


	@Override
	public Map<String,Potential> getLatestPotentials() {
		return potentialService.getLatestPotentials();
	}

	@Override
	public Map<String, Potential> getPotentials(LocalDate date) {
		return potentialService.getPotentials(date);
	}

	@Override
	public List<String> getPowerIDs() {
		return potentialService.getPowerIDs();
	}

	@Override
	public List<Potential> getPotentials_hlb(LocalDate date, Integer tops) {
		return potentialService.getPotentials_hlb(date, tops);
	}

	@Override
	public List<Potential> getPotentials_avb(LocalDate date, Integer tops) {
		return potentialService.getPotentials_avb(date, tops);
	}

	@Override
	public Map<LocalDate, BigDecimal> getMCSTs(String itemID, boolean cache) {
		Map<LocalDate, BigDecimal> mcsts = new TreeMap<LocalDate, BigDecimal>();
		Kdata kdata = kdataService.getKdata(itemID, cache);
		Kbar bar;
		BigDecimal vol=null, amount=null, float_share=null, mcst = null,previous=null;
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			bar = kdata.getBar(date);
			vol = bar.getQuantity();
			amount = bar.getAmount();
			float_share = bar.getFloat_share();
			mcst = Functions.MSCT(vol, amount, float_share, previous);
			//System.out.printf("%tF, %.0f, %.0f, %.0f, %.2f, %.2f\n", date,vol,amount,float_share,mcst,previous);
			if(mcst!=null) {
				mcsts.put(date, mcst);
				previous = mcst;
			}
		}
		return mcsts;
	}

	@Override
	public Map<LocalDate, BigDecimal[]> getBOLLs(String itemID, boolean cache) {
		Integer period = 20;//布林线默认是20日
		Map<LocalDate, BigDecimal[]> bolls = new TreeMap<LocalDate, BigDecimal[]>();
		
		Kdata kdata = kdataService.getKdata(itemID, cache);
		
		Kbar bar;
		BigDecimal[] boll;
		BigDecimal ma;
		
		List<BigDecimal> mas = new ArrayList<BigDecimal>();
		List<BigDecimal> closes = new ArrayList<BigDecimal>();
		
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			
			bar = kdata.getBar(date);

			closes.add(bar.getClose());
			if(closes.size()>period) {
				closes.remove(0);
			}
			
			ma = Functions.MA(closes);
			mas.add(ma);
			if(mas.size()>period) {
				mas.remove(0);
			}
			
			if(mas.size()==period) {
				boll = Functions.BOLL(mas,closes);
				
				//System.out.printf("%tF: closes.size=%d, mas.size=%d, ma=%.2f \n",date,closes.size(),mas.size(),ma);
				
				if(boll!=null) {
					bolls.put(date, boll);
				}
				
			}
			
		}
		return bolls;
	}

	@Override
	public List<LocalDate> getHuaFirst(String itemID, LocalDate beginDate, LocalDate endDate, Integer boll_period, BigDecimal mcst_ratio,BigDecimal volume_r){ 
		List<LocalDate> results = new ArrayList<LocalDate>(); 
		
		//Integer boll_period = 21;  //表示多少日内，布林线突破过下轨
		//BigDecimal mcst_ratio = new BigDecimal(-0.13);
		//BigDecimal volume_r = new BigDecimal(2);
		
		Map<LocalDate, BigDecimal[]> bolls = this.getBOLLs(itemID, true);
		Map<LocalDate, BigDecimal> mcsts = this.getMCSTs(itemID, true);
		Map<LocalDate, BigDecimal[]> macds = this.getMACDs(itemID, true);
		
		Kdata kdata = kdataService.getKdata(itemID, true);
		//Kbar kbar = kdataService.getLatestMarketData(itemID);
		//kdata.addBar(kbar.getDate(), kbar);
		
		Kbar bar;
		BigDecimal mcst, boll_dn, volume_ratio;
		BigDecimal macd;
		
		List<LocalDate> dates = kdata.getDates();
		Integer i = null; 
		for(LocalDate date : dates) {
			if((date.equals(beginDate)||date.isAfter(beginDate)) && date.isBefore(endDate)) {
				bar = kdata.getBar(date);
				mcst = mcsts.get(date);
				boll_dn = bolls.get(date)==null ? null : bolls.get(date)[2];
				volume_ratio = bar.getVolume_ratio();
				if(mcst!=null && this.isDown(bar.getClose(), mcst, mcst_ratio) 
						&& boll_dn!=null && boll_dn.compareTo(bar.getClose()) == 1
						//&& volume_ratio.compareTo(volume_r) == 1
						) {
					i=0;
				}

				//System.out.printf("\n%tF: mcst=%.2f, boll_dn=%.2f, close=%.2f\n", date, mcst, boll_dn, bar.getClose());
				
				macd = macds.get(date)==null ? null : macds.get(date)[2];
				if(i!=null && i<boll_period 
						&& macd!=null && macd.compareTo(BigDecimal.ZERO)==1
						&& volume_ratio.compareTo(volume_r) == 1
						) {
					results.add(date);
					//System.out.printf("\n %tF is OK \n", theDate);
				}
				
				if(i!=null) i++;
				if(i!=null && i>=boll_period) {
					i=null;  //超过boll_period还没有放量拉升，放弃
				}
			}
		}
		
		kdataService.evictKDataCache();
		
		return results;
	}
	
	private boolean isDown(BigDecimal b1, BigDecimal b2, BigDecimal ratio) {
		BigDecimal r = b1.subtract(b2).divide(b2, BigDecimal.ROUND_HALF_UP);
		return r.compareTo(ratio)==-1 ? true : false;
	}

	@Override
	public Map<LocalDate, BigDecimal[]> getMACDs(String itemID, boolean cache) {
		Map<LocalDate, BigDecimal[]> macds = new TreeMap<LocalDate, BigDecimal[]>();
		
		Kdata kdata = kdataService.getKdata(itemID, cache);
		
		Kbar bar;
		BigDecimal ma12=null, ma26=null, dif, dea=null, macd;
		
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			bar = kdata.getBar(date);

			ma12 = Functions.EMA(bar.getClose(), ma12, 12);
			ma26 = Functions.EMA(bar.getClose(), ma26, 26);
			dif = ma12.subtract(ma26);
			dea = Functions.EMA(dif, dea, 9);
			macd = dif.subtract(dea).multiply(new BigDecimal(2));
			
			macds.put(date, new BigDecimal[]{dif,dea,macd});
			
		}
		
		return macds;
	}

	@Override
	public BigDecimal getMCST(String itemID, LocalDate endDate, Integer count, BigDecimal ratio) {
		LocalDate theDate = null; 
		
		Map<LocalDate, BigDecimal> mcsts = this.getMCSTs(itemID, true);
		
		Kdata kdata = kdataService.getKdata(itemID, endDate, count, true);
		
		Kbar bar;
		BigDecimal mcst, r=null, volume_ratio=null;
		McstList mcstList = new McstList(count);
		
		List<LocalDate> dates = kdata.getDates();
		for(LocalDate date : dates) {
			bar = kdata.getBar(date);
			mcst = mcsts.get(date);
			if(bar!=null && mcst!=null) {
				mcstList.add(bar.getClose());
				r = bar.getClose().subtract(mcst).divide(mcst, BigDecimal.ROUND_HALF_UP).abs();
				volume_ratio = bar.getVolume_ratio();
			}

			if(bar!=null && mcst!=null 
					&& mcstList.getHighest().compareTo(mcst)==1
					&& mcstList.getLowestRatio(mcst).compareTo(ratio)==-1
					) {
				theDate = date;

				//System.out.printf("\n%tF,close=%.2f, mcst=%.2f, r=%.2f, highest=%.2f, lowest=%.2f, lowestRatio=%.2f", 
						//date,bar.getClose(),mcst,r,mcstList.getHighest(),mcstList.getLowest(),mcstList.getLowestRatio(mcst));
				
			}
		}
		
		kdataService.evictKDataCache();
		
		if(theDate != null					
				&& r.compareTo(ratio)==-1
				&& volume_ratio.compareTo(new BigDecimal(2))==1
				) {
			System.out.printf("  ok, r=%.2f, volume_ratio=%.2f", r, volume_ratio);
			return r;
		}else {
			//System.out.printf("  X, r=%.2f", r);
			return null;
		}
	}
	
	class McstList{
		BigDecimal highest = null;
		BigDecimal lowest = null;
		
		List<BigDecimal> closes = new ArrayList<BigDecimal>();
		BigDecimal tmp;
		
		Integer count;
		public McstList(Integer count) {
			this.count = count;
		}
		
		public void add(BigDecimal close) {
			highest = (highest==null || highest.compareTo(close)==-1) ? close : highest;
			lowest = (lowest==null || lowest.compareTo(close)==1) ? close : lowest;
			
			closes.add(close);
			if(closes.size()>this.count) {
				tmp = closes.get(0);
				if(tmp.equals(highest) || tmp.equals(lowest)) {
					this.refresh();
				}
				closes.remove(0);
			}
		}
		
		private void refresh() {
			highest = null;
			lowest = null;
			BigDecimal close;
			for(int i=1; i<closes.size(); i++) {
				close = closes.get(i);
				highest = (highest==null || highest.compareTo(close)==-1) ? close : highest;
				lowest = (lowest==null || lowest.compareTo(close)==1) ? close : lowest;
			}
		}
		
		public BigDecimal getHighest() {
			return this.highest;
		}
		public BigDecimal getLowest() {
			return this.lowest;
		}
		public BigDecimal getLowestRatio(BigDecimal mcst) {
			return this.lowest.subtract(mcst).divide(mcst, BigDecimal.ROUND_HALF_UP).abs();
		}
	}

	/*
	 * 截止endDate的一个period内，该股跌破过布林线下轨，并且股价在成本线之下
	 * 
	 */
	@Override
	public List<LocalDate> getHuaFirstPotentials(String itemID, LocalDate endDate, Integer period,BigDecimal mcst_ratio) {
		List<LocalDate> results = new ArrayList<LocalDate>(); 
		
		Map<LocalDate, BigDecimal[]> bolls = this.getBOLLs(itemID, true);
		Map<LocalDate, BigDecimal> mcsts = this.getMCSTs(itemID, true);
		
		Kdata kdata = kdataService.getKdata(itemID, true);
		
		Kbar bar;
		BigDecimal mcst, boll_dn;
		
		List<LocalDate> dates = kdata.getDates();
		
		int toIndex = dates.indexOf(endDate);
		if(toIndex == -1) {
			for(LocalDate date : dates) {
				if(date.isBefore(endDate)) {
					toIndex ++;
				}else {
					break;
				}
			}
		}
		
		if(toIndex>=0) {
			int fromIndex = toIndex>=period ? toIndex-period+1 : 0;
			
			for(LocalDate date : dates.subList(fromIndex, toIndex)) {
				bar = kdata.getBar(date);
				mcst = mcsts.get(date);
				boll_dn = bolls.get(date)==null ? null : bolls.get(date)[2];
				
				if(mcst!=null && this.isDown(bar.getClose(), mcst, mcst_ratio) 
						&& boll_dn!=null && boll_dn.compareTo(bar.getClose()) == 1
						) {
					results.add(date);
				}
			}
			
			kdataService.evictKDataCache();
		}
		
		return results;
	}

	@Override
	public Map<String,String> getDrums() {
		Map<String,String> ids = new HashMap<String,String>();
		List<String> ss = drumService.getDrums();
		if(ss!=null && ss.size()>0) {
			int i=0;
			for(String s : ss) {
				ids.put(s,Integer.toString(i++));
			}
		}
		return ids;
	}
	
	@Override
	public Map<String,String> getLpbs() {
		Map<String,String> ids = new HashMap<String,String>();
		String str = lpbService.getLpb();
		if(str!=null && str.length()>11) {
			String id,order;
			String[] ss = str.substring(11).split(",");
			for(String s : ss) {
				if(s.length()>8) {
					id = s.substring(0, 8);
					order = s.substring(9,s.indexOf(")"));
					ids.put(id,order);
				}
			}
		}
		return ids;
	}

	@Override
	public Map<String,String> getLpb2() {
		Map<String,String> ids = new HashMap<String,String>();
		String str = lpb2Service.getLpb2();
		if(str!=null && str.length()>11) {
			String id,order;
			String[] ss = str.substring(11).split(",");
			for(String s : ss) {
				if(s.length()>8) {
					id = s.substring(0, 8);
					order = s.substring(9,s.indexOf(")"));
					ids.put(id,order);
				}
			}
		}
		return ids;
	}
	
	@Override
	public Map<String,String> getHlb2() {
		Map<String,String> ids = new HashMap<String,String>();
		String str = hlb2Service.getHLB2();
		if(str!=null && str.length()>11) {
			String id,order;
			String[] ss = str.substring(11).split(",");
			for(String s : ss) {
				if(s.length()>8) {
					id = s.substring(0, 8);
					order = s.substring(9,s.indexOf(")"));
					ids.put(id,order);
				}
			}
		}
		return ids;
	}

	@Override
	public Map<String, String> getBavs() {
		Map<String,String> ids = new HashMap<String,String>();
		String str = bavService.getBAV();
		if(str!=null && str.length()>11) {
			String id,order;
			String[] ss = str.substring(11).split(",");
			for(String s : ss) {
				if(s.length()>8) {
					id = s.substring(0, 8);
					order = s.substring(9,s.indexOf(")"));
					ids.put(id,order);
				}
			}
		}
		return ids;
	}

	@Override
	public Map<String, String> getFavorsOfB21() {
		return favorService.getFavorsOfB21();
	}
	
	@Override
	public Map<String, String> getFavorsOfB21up() {
		return favorService.getFavorsOfB21up();
	}

	@Override
	public BigDecimal getMACD(String itemID, LocalDate date, boolean cache) {
		Map<LocalDate, BigDecimal[]> macds = this.getMACDs(itemID, cache);
		if(macds!=null && macds.containsKey(date)) {
			return macds.get(date)[2];
		}else {
			return BigDecimal.ZERO;
		}
	}

}
