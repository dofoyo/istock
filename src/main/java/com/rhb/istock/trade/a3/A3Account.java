package com.rhb.istock.trade.a3;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.index.tushare.IndexServiceTushare;
import com.rhb.istock.kdata.KdataService;
import com.rhb.istock.kdata.Muster;
import com.rhb.istock.selector.fina.FinaService;

public class A3Account {
	KdataService kdataService;
	FinaService finaService;
	IndexServiceTushare indexServiceTushare;
	LocalDate bDate, eDate;
	Map<LocalDate, Map<String,List<String>>> operations;
	
	private Integer max_holds = 20; //最多持有几只股票，每只股票有三次买入机会：突破21日线，强于大盘，创新高
	private List<Box> boxes = new ArrayList<Box>();  //根据max_holds做好户头，每个户头对应一次买入和卖出操作
	private BigDecimal cash = new BigDecimal(50000); // 每个户头的资金
	private List<Order> orders = new ArrayList<Order>();
	private Integer orderID = 1;

	private 
	Map<String,Muster> musters, tmps;
	Muster muster;
	
	public void setConfig(
			KdataService kdataService,
			FinaService finaService,
			IndexServiceTushare indexServiceTushare,
			LocalDate bDate, 
			LocalDate eDate,
			Map<LocalDate, Map<String,List<String>>> operations
			) {
		this.kdataService = kdataService;
		this.finaService = finaService;
		this.indexServiceTushare = indexServiceTushare;
		this.bDate = bDate;
		this.eDate = eDate;
		this.operations = operations;
		
		for(int i=0; i<max_holds; i++) {
			boxes.add(new Box(cash));
		}
		
	}
	
	public void operate() {
		List<String> buys, sells;
		long days = eDate.toEpochDay()- bDate.toEpochDay();
		int i=1;
		for(LocalDate date=bDate; date.isBefore(eDate) || date.equals(eDate); date = date.plusDays(1)) {
			Progress.show((int)days, i++, "  operate: " + date.toString() + " \n");

			//刷新价格
			musters = kdataService.getMusters(date);
			if(musters!=null && musters.size()>0) {
				Order order;
				for(Box box : boxes) {
					order = box.getOrder();
					if(order!=null) {
						muster = musters.get(order.getItemID());
						if(muster!=null) {
							order.setSellPrice(muster.getLatestPrice());
							//System.out.println("refresh price: " + muster.getItemID() + "," + muster.getLatestPrice());
						}
						
					}
				}
			}			

			//买入卖出
			if(operations.containsKey(date)) {
				sells = operations.get(date).get("sells");
				buys = operations.get(date).get("buys");
				System.out.println("sells: " + sells);
				System.out.println("buys: " + buys);
				
				if(sells.size()>0 || buys.size()>0) {
					for(String id : sells) {
						sell(date,id);
					}
					
					this.averageCash();
					
					for(String id : buys) {
						buy(date,id);
					}
				}
			}
			
			//每日结算
			System.out.println("结算: " + this.getAmount());
		}
	}
	
	private void averageCash() {
		Integer count = 0;
		BigDecimal cash = BigDecimal.ZERO;
		Order order;
		for(Box box : boxes) {
			order = box.getOrder();
			if(order == null) {
				cash = cash.add(box.getCash());
				count++;
			}
		}
		if(count>1) {
			BigDecimal ave = cash.divide(new BigDecimal(count),BigDecimal.ROUND_HALF_DOWN);
			for(Box box : boxes) {
				order = box.getOrder();
				if(order == null) {
					box.setCash(ave);
				}
			}
		}
		
	}
	
	private Map<String, BigDecimal> getAmount() {
		Map<String, BigDecimal> ms = new HashMap<String, BigDecimal>();
		BigDecimal cash = BigDecimal.ZERO;
		BigDecimal value = BigDecimal.ZERO;
		Order order;
		for(Box box : boxes) {
			cash = cash.add(box.getCash());
			order = box.getOrder();
			if(order != null) {
				value = value.add(order.getSellAmount());
			}
			System.out.println(box);
		}
		
		ms.put("cash", cash);
		ms.put("value", value);
		ms.put("total", cash.add(value));
		return ms;

	}
	
	private void buy(LocalDate date, String itemID) {
		Box box = this.getFreeBox();
		if(box!=null) {
			musters = kdataService.getMusters(date);
			if(musters!=null && musters.size()>0) {
				muster = musters.get(itemID);
				if(muster!=null) {
					Order order = new Order(this.getOrderID(), muster.getItemID(), muster.getItemName(),date, muster.getLatestPrice(),box.getQuantity(muster.getLatestPrice()),"");
					order.setSellPrice(muster.getLatestPrice());
					orders.add(order);
					box.occupy(order);
				}
			}			
		}else {
			System.out.println("all boxes occupied!");
		}
	}
	
	private void sell(LocalDate date, String itemID) {
		Order order;
		for(Box box : boxes) {
			order = box.getOrder();
			if(order!=null && order.getItemID().equals(itemID)) {
				order.setSellDate(date);
				box.free();
			}
		}
	}
	
	private Box getFreeBox() {
		Box box = null;
		for(Box b : boxes) {
			if(b.getOrder() == null) {
				box = b;
				break;
			}
		}
		return box;
	}
	
	private Integer getOrderID() {
		return this.orderID++;
	}
}

class Order {
	private Integer orderID;
	private String itemID;
	private String itemName;
	private LocalDate buyDate;  
	private BigDecimal buyPrice;
	private LocalDate sellDate;  
	private BigDecimal sellPrice;
	private Integer quantity;
	private String note;
	
	public Order(Integer orderID,String itemID, String itemName, LocalDate date, BigDecimal price, Integer quantity, String note) {
		this.orderID = orderID;
		this.itemID = itemID;
		this.buyDate = date;
		this.buyPrice = price;
		this.quantity = quantity;
		this.itemName = itemName;
		this.note = note;
		this.sellPrice = price;
	}
	
	public String getItemName() {
		return itemName;
	}

	public BigDecimal getBuyAmount() {
		return buyPrice.multiply(new BigDecimal(quantity));
	}
	
	public BigDecimal getSellAmount() {
		return sellPrice.multiply(new BigDecimal(quantity));
	}
	
	public BigDecimal getSellFee() {
		//return BigDecimal.ZERO;
		return this.getSellAmount().multiply(new BigDecimal(0.002));
	}
	
	public BigDecimal getBuyFee() {
		//return BigDecimal.ZERO;
		return this.getBuyAmount().multiply(new BigDecimal(0.001));
	}
	
	public Integer getOrderID() {
		return orderID;
	}


	public String getItemID() {
		return itemID;
	}


	public Integer getQuantity() {
		return quantity;
	}


	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public LocalDate getBuyDate() {
		return buyDate;
	}

	public void setBuyDate(LocalDate buyDate) {
		this.buyDate = buyDate;
	}

	public BigDecimal getBuyPrice() {
		return buyPrice;
	}

	public void setBuyPrice(BigDecimal buyPrice) {
		this.buyPrice = buyPrice;
	}

	public LocalDate getSellDate() {
		return sellDate;
	}

	public void setSellDate(LocalDate sellDate) {
		this.sellDate = sellDate;
	}

	public BigDecimal getSellPrice() {
		return sellPrice;
	}

	public void setSellPrice(BigDecimal sellPrice) {
		this.sellPrice = sellPrice;
	}

	public void setOrderID(Integer orderID) {
		this.orderID = orderID;
	}

	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public void setItemName(String itemName) {
		this.itemName = itemName;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	@Override
	public String toString() {
		return "Order [orderID=" + orderID + ", itemID=" + itemID + ", itemName=" + itemName + ", buyDate=" + buyDate
				+ ", buyPrice=" + buyPrice + ", sellDate=" + sellDate + ", sellPrice=" + sellPrice + ", quantity="
				+ quantity + ", note=" + note + ", getBuyAmount()=" + getBuyAmount() + ", getSellAmount()="
				+ getSellAmount() + ", getSellFee()=" + getSellFee() + ", getBuyFee()=" + getBuyFee() + "]";
	}
	
}

class Box{
	private BigDecimal cash;
	private Order order;
	
	public Box(BigDecimal cash) {
		this.cash = cash;
	}
	
	public BigDecimal getCash() {
		return cash;
	}
	
	public void setCash(BigDecimal cash) {
		this.cash = cash;
	}
	
	public Order getOrder() {
		return order;
	}

	public void setOrder(Order order) {
		this.order = order;
	}

	public void free() {
		this.cash = this.cash.add(this.order.getSellAmount().subtract(this.order.getSellFee()));
		this.order = null;
	}
	
	public void occupy(Order order) {
		this.order = order;
		this.cash = this.cash.subtract(this.order.getBuyAmount().add(this.order.getBuyFee()));
	}
	
	public Integer getQuantity(BigDecimal price) {
		return this.cash.divide(price,BigDecimal.ROUND_DOWN).divide(new BigDecimal(100),BigDecimal.ROUND_DOWN).intValue()*100;
	}

	@Override
	public String toString() {
		return "Box [cash=" + cash + ", order=" + order + "]";
	}
	
	

}
