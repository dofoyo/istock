package com.rhb.istock.comm.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class Line {
	public static String draw(Map<String,BigDecimal> pots) {
		if(pots==null || pots.size()==0) return "";
		
		DecimalFormat df = new DecimalFormat("0.00"); 
		
		List<String> list = new ArrayList<String>();
		for(Map.Entry<String, BigDecimal> entry : pots.entrySet()) {
			list.add(entry.getKey() + "(" + df.format(entry.getValue()) + ")");
		}
		
		Collections.sort(list,new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				return getBigDecimal(o1).compareTo(getBigDecimal(o2));
			}
		});
		
		StringBuffer sb = new StringBuffer();
		for(int i=0; i<list.size(); i++) {
			if(i==0) {
				sb.append(list.get(i));
			}else {
				sb.append(getDots(list.get(i-1),list.get(i)));
			}
		}
		
		return sb.toString();
	}
	
	private static BigDecimal getBigDecimal(String str) {
		return new BigDecimal(str.substring(str.indexOf("(")+1, str.indexOf(")")));
	}
	
	private static String getDots(String low, String high) {
		StringBuffer sb = new StringBuffer();
		BigDecimal h = getBigDecimal(high);
		BigDecimal l = getBigDecimal(low);
		BigDecimal l1 = getBigDecimal(low).multiply(new BigDecimal(2));

		Integer ratio = h.subtract(l).divide(l1,BigDecimal.ROUND_DOWN).multiply(new BigDecimal(100)).intValue();
		//System.out.println(ratio);
		for(int i=0; i<ratio; i++) {
			sb.append("-");
		}
		sb.append(high);
		return sb.toString();
	}

}
