package com.rhb.istock.trade.turtle.simulation.six;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;

@Service("turtleMusterSimulationAnalysis")
public class TurtleMusterSimulationAnalysis {
	@Value("${reportPath}")
	private String reportPath;

	@Value("${openDuration}")
	private String openDuration;
	
	@Value("${dropDuration}")
	private String dropDuration;
	
	public void generateRecords(String type){
		String theFile = reportPath + "/" + type + "_simulation_detail_"+openDuration+"_"+dropDuration+".csv"; 

		String[] lines = FileTools.readTextFile(theFile).split("\n");
		String[] columns;
		
		Integer orderID;
		String itemID;
		String itemName;
		BigDecimal profit;
		BigDecimal volumnRatio;
		LocalDate buyDate,sellDate;
		Integer sellType;
		
		List<Record> records = new ArrayList<Record>() ;
		for(int j=1; j<lines.length; j++) {
			//System.out.println(line);
			columns = lines[j].split(",");
			orderID = Integer.parseInt(columns[0]);
			itemID = columns[1];
			itemName = columns[2];
			buyDate = LocalDate.parse(columns[3],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			volumnRatio = new BigDecimal(columns[7]);
			sellDate = LocalDate.parse(columns[8],DateTimeFormatter.ofPattern("yyyy-MM-dd"));
			sellType = columns[11].equals("hold") ? 1 : Integer.parseInt(columns[11]);
			profit = new BigDecimal(columns[12]);
			
			records.add(new Record(orderID,itemID,itemName,buyDate,volumnRatio,sellDate,sellType,profit));
		}
		
		Collections.sort(records, new Comparator<Record>() {
			@Override
			public int compare(Record o1, Record o2) {
				if(o1.getItemID().equals(o2.getItemID())) {
					return o1.getOrderID().compareTo(o2.getOrderID());
				}else {
					return o1.getItemID().compareTo(o2.getItemID());
				}
			}
		});

		List<Record> results = new ArrayList<Record>();
		Record record = records.get(0);
		Record tmp;
		for(int i=1; i<records.size(); i++) {
			tmp = records.get(i);
			if(tmp.getItemID().equals(record.getItemID())) {
				record.setSellDate(tmp.getSellDate());
				record.addProfit(tmp.getProfit());
				if(tmp.getSellType()==1) {
					results.add(record);
					i++;
					if(i>=records.size()) {
						break;
					}else {
						record = records.get(i);
					}
				}
			}else {
				results.add(record);
				record = tmp;
			}
		}
		
		Collections.sort(results, new Comparator<Record>() {
			@Override
			public int compare(Record o1, Record o2) {
				return o1.getOrderID().compareTo(o2.getOrderID());
			}
		});
		
		StringBuffer sb = new StringBuffer("orderID, itemID, itemName, buyDate,volumenRatio,sellDate,profit\n");
		for(Record r : results) {
			sb.append(r.getStr() +"\n");
		}
		
		String file = reportPath + "/" + type + "_simulation_detail_plus"+openDuration+"_"+dropDuration+".csv"; 
		FileTools.writeTextFile(file, sb.toString(), false);

	}
	
	class Record{
		private Integer orderID;
		private String itemID;
		private String itemName;
		private LocalDate buyDate;
		private BigDecimal volumnRatio;
		private LocalDate sellDate;
		private Integer sellType;
		private BigDecimal profit;
		private Integer count;
		
		public String getStr() {
			return orderID + "," + itemID +"," + itemName +"," + buyDate.toString() +"," + volumnRatio.toString() +"," + sellDate.toString() +"," + profit.toString();
		}
		
		public Record(Integer orderID, String itemID, String itemName, LocalDate buyDate, BigDecimal volumnRatio, LocalDate sellDate, Integer sellType, BigDecimal profit) {
			this.orderID = orderID;
			this.itemID = itemID;
			this.itemName = itemName;
			this.buyDate = buyDate;
			this.volumnRatio = volumnRatio;
			this.sellDate = sellDate;
			this.sellType = sellType;
			this.profit = profit;
		}
		
		public LocalDate getSellDate() {
			return sellDate;
		}

		public void setSellDate(LocalDate sellDate) {
			this.sellDate = sellDate;
		}

		public Integer getSellType() {
			return sellType;
		}

		public void setSellType(Integer sellType) {
			this.sellType = sellType;
		}

		public Integer getOrderID() {
			return orderID;
		}

		public void setOrderID(Integer orderID) {
			this.orderID = orderID;
		}

		public BigDecimal getVolumnRatio() {
			return volumnRatio;
		}

		public void setVolumnRatio(BigDecimal volumnRatio) {
			this.volumnRatio = volumnRatio;
		}

		public LocalDate getBuyDate() {
			return buyDate;
		}

		public void setBuyDate(LocalDate date) {
			this.buyDate = date;
		}

		public void addProfit(BigDecimal profit) {
			this.profit = this.profit.add(profit);
		}
		
		public boolean isHold() {
			return count==0 ? false : true;
		}
		
		public String getItemID() {
			return itemID;
		}
		public void setItemID(String itemID) {
			this.itemID = itemID;
		}
		public String getItemName() {
			return itemName;
		}
		public void setItemName(String itemName) {
			this.itemName = itemName;
		}
		public Integer getCount() {
			return count;
		}
		public void deCount() {
			this.count--;
		}
		public BigDecimal getProfit() {
			return profit;
		}
		public void setProfit(BigDecimal profit) {
			this.profit = profit;
		}
	}

}
