package com.rhb.istock.trade.kelly;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.kdata.Kbar;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.selector.breaker.BreakerService;
import com.rhb.istock.trade.kelly.repository.KellyRepository;

@Service("kellyService")
public class KellyService {
	@Autowired
	@Qualifier("breakerService")
	BreakerService breakerService;
	
	@Autowired
	@Qualifier("kdataServiceImp")
	KdataService kdataService;

	@Autowired
	@Qualifier("kellyRepository")
	KellyRepository kellyRepository;
	
	private Integer top = 3;
	private Integer maxOfOrders = 9;
	
	public void caculateFValuesByHL() {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("caculateFValuesByHL ......");

		TreeMap<LocalDate,BigDecimal> fs = new TreeMap<LocalDate,BigDecimal>();
		
		Map<LocalDate,List<String>> breakerIDs = breakerService.getBreakersSortByHL();
		List<String> newIDs;
		Set<String> holdIDs;
		Kbar bar;
		Formula formula = new Formula();
		BigDecimal fvalue = null;
		DecimalFormat df = new DecimalFormat("0.00");
		int i=1;
		for(Map.Entry<LocalDate, List<String>> entry : breakerIDs.entrySet()) {
			if(entry.getKey().isAfter(LocalDate.parse("2018-01-01"))) {
				//放入新价格
				holdIDs = formula.getHoldIDs();
				for(String id : holdIDs) {
					bar = kdataService.getKbar(id, entry.getKey(), false);
					if(bar != null) {
						formula.setLatestPrice(id, bar.getClose());
					}
				}
				
				//获得f值
				fvalue = formula.getF();
				if(fvalue != null) {
					fs.put(entry.getKey(), fvalue);
				}
				
				//用最新价格替换原价
				formula.refresh();
				
				
				//放入新记录
				newIDs = entry.getValue();
				int count = 0;
				for(String id : newIDs) {
					bar = kdataService.getKbar(id, entry.getKey(), false);
					if(bar != null && formula.add(id, entry.getKey(), bar.getClose())) {
						count++;
						if(count > top) {
							break;
						}
					}
				}
				
			}
			Progress.show(breakerIDs.size(),i++, entry.getKey().toString() + ", " + (fvalue==null? "" : df.format(fvalue)));
		}
		
		kellyRepository.saveFvaluesOfHL(fs);
		
		System.out.println("\ncaculateFValuesByHL done!");
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}
	
	class Formula{
		List<Order> orders = new ArrayList<Order>();
		public void refresh() {
			for(Order order : orders) {
				order.refresh();
			}			
		}
		
		public boolean add(String itemID, LocalDate date, BigDecimal price) {
			for(Order order : orders) {
				if(order.getItemID().equals(itemID)) {
					return false;  //不能重复
				}
			}
			orders.add(new Order(itemID,date,price));
			if(orders.size()>maxOfOrders) {
				orders.remove(0);
			}
			
			return true;
		}
		
		public Set<String> getHoldIDs(){
			Set<String> ids = new HashSet<String>();
			for(Order order : orders) {
				ids.add(order.getItemID());
			}
			return ids;
		}
		
		public void setLatestPrice(String itemID, BigDecimal price) {
			for(Order order : orders) {
				if(order.getItemID().equals(itemID)) {
					order.setLatestPrice(price);
				}
			}
		}
		
		/*
		 * f = p/a - q/b
		 */
		public BigDecimal getF() {
			int win=0, lose=0;
			BigDecimal winCost=new BigDecimal(0),
					winAmount = new BigDecimal(0),
					loseCost = new BigDecimal(0), 
					loseAmount = new BigDecimal(0);
			for(Order order : orders) {
				if(order.getSign()==1) { 
					win++;
					winCost = winCost.add(order.getCost());
					winAmount = winAmount.add(order.getAmount());
				}else if(order.getSign()==-1) {
					lose++;
					loseCost = loseCost.add(order.getCost());
					loseAmount = loseAmount.add(order.getAmount());
				}
				//System.out.println(order);
			}
			
/*			System.out.println("win = " + win);
			System.out.println("winCost = " + winCost);
			System.out.println("winAmount = " + winAmount);
			System.out.println("lose = " + lose);
			System.out.println("loseCost = " + loseCost);
			System.out.println("loseAmount = " + loseAmount);*/
			
			if(win+lose == 0) {
				return null;
			}
			
			return new BigDecimal(1.0*win/(win+lose));
			/*
			if(win!=0 && lose==0) {
				return new BigDecimal(100);  //买入的股票都盈利，没有亏损
			}
			
			if(win==0 && lose!=0) {
				return new BigDecimal(-100);  //买入的股票都亏损
			}
			
			BigDecimal p = new BigDecimal(1.0 * win/(win+lose));
			BigDecimal q = new BigDecimal(1.0 * lose/(win+lose));
			BigDecimal a = loseCost.subtract(loseAmount).divide(loseCost,BigDecimal.ROUND_HALF_UP);
			BigDecimal b = winAmount.subtract(winCost).divide(winCost,BigDecimal.ROUND_HALF_UP);
			
			if(a.compareTo(new BigDecimal(0))==0 || b.compareTo(new BigDecimal(0))==0) {
				return new BigDecimal(0);
			}
			
			BigDecimal pa = p.divide(a,BigDecimal.ROUND_HALF_UP);
			BigDecimal qb = q.divide(b,BigDecimal.ROUND_HALF_UP);
			
			BigDecimal f = pa.subtract(qb);
			
			System.out.println("p = " + p);
			System.out.println("q = " + q);
			System.out.println("a = " + a);
			System.out.println("b = " + b);
			System.out.println("p/a = " + pa);
			System.out.println("q/b = " + qb);
			System.out.println("f = " + f);
			
			return f;*/
			
		}
	}
	
	class Order{
		private BigDecimal cash = new BigDecimal(100000);
		private String itemID;
		private LocalDate date;
		private BigDecimal price;
		private Integer quantity;
		private BigDecimal latestPrice;
		
		public Order(String itemID, LocalDate date, BigDecimal price) {
			this.itemID = itemID;
			this.date = date;
			this.price = price;
			this.quantity = cash.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue() * 100;
			this.latestPrice = price;
		}
		
		public void refresh() {
			this.price = this.latestPrice;
		}
		
		public BigDecimal getCost() {
			return price.multiply(new BigDecimal(quantity));
		}
		
		public BigDecimal getAmount() {
			return latestPrice.multiply(new BigDecimal(quantity));
		}
		
		public int getSign() {
			return latestPrice.compareTo(price);
		}
		
		public String getItemID() {
			return itemID;
		}
		public void setItemID(String itemID) {
			this.itemID = itemID;
		}
		public LocalDate getDate() {
			return date;
		}
		public void setDate(LocalDate date) {
			this.date = date;
		}
		public BigDecimal getPrice() {
			return price;
		}
		public void setPrice(BigDecimal price) {
			this.price = price;
		}
		public Integer getQuantity() {
			return quantity;
		}
		public void setQuantity(Integer quantity) {
			this.quantity = quantity;
		}

		public BigDecimal getLatestPrice() {
			return latestPrice;
		}

		public void setLatestPrice(BigDecimal latestPrice) {
			this.latestPrice = latestPrice;
		}

		@Override
		public String toString() {
			return "Order [cash=" + cash + ", itemID=" + itemID + ", date=" + date + ", price=" + price + ", quantity="
					+ quantity + ", latestPrice=" + latestPrice + "]";
		}
		
		
	}
	
}
