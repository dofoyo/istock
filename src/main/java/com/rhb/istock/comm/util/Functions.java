package com.rhb.istock.comm.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.List;

public class Functions {
	public static Integer cagr(Integer a, Integer b, Integer n) {
		return cagr(new BigDecimal(a), new BigDecimal(b), new Double(n));
	}
	
	//CAGR是Compound Annual Growth Rate的缩写，意思是复合年均增长率
	public static Integer cagr(BigDecimal a, BigDecimal b, double n) {
		if(b.equals(BigDecimal.ZERO)) return 0;
		
		Double r = a.divide(b,BigDecimal.ROUND_HALF_UP).doubleValue();
		Double pow = Math.pow(r, 1/n);
		Double gr = (pow-1+0.005)*100;
		//Double gr = (pow-1)*100;
		
		//System.out.format("a=%.2f,b=%.2f,r=a/b=%.2f,pow=pow(%.2f,%.2f)=%.3f,gr=%d\n", a,b,r,r,n,pow,gr.intValue());
		return gr.intValue();
		
	}
	
	public static Integer growthRate(BigDecimal a, BigDecimal b) {
		return a.divide(b,BigDecimal.ROUND_HALF_UP).subtract(new BigDecimal(1)).multiply(new BigDecimal(100)).add(new BigDecimal(0.5)).intValue();
	}
	
	// rate = a/b
	public static Integer rate(BigDecimal a, BigDecimal b) {
		return a.divide(b,BigDecimal.ROUND_HALF_UP).multiply(new BigDecimal(100)).add(new BigDecimal(0.5)).intValue();
	}
	
	public static boolean between(BigDecimal value, Integer a, Integer b) {
		if(value.compareTo(new BigDecimal(a))==1 
				&& value.compareTo(new BigDecimal(b))==-1) {
			return true;
		}else {
			return false;
		}
	}

	public static boolean between(Integer value, Integer a, Integer b) {
		if(value.compareTo(a)>=0 
				&& value.compareTo(b)<=0) {
			return true;
		}else {
			return false;
		}
	}
	
	public static BigDecimal EMA(BigDecimal close, BigDecimal pema, Integer period) {
		if(pema==null) pema = close;
		BigDecimal p = pema.multiply(new BigDecimal(period-2)).divide(new BigDecimal(period), BigDecimal.ROUND_HALF_UP);
		BigDecimal c = close.multiply(new BigDecimal(2)).divide(new BigDecimal(period), BigDecimal.ROUND_HALF_UP);
		return p.add(c);
	}
	
	
	/*
	 * BOLL指标的计算过程
		（1）计算MA,
		MA=N日内的收盘价之和÷N
		
		（2）计算标准差MD,
		MD=平方根N日的（C－MA）的两次方之和除以N
		
		（3）计算MB、UP、DN线,
		MB=（N－1）日的MA,
		UP=MB＋2×MD,
		DN=MB－2×MD
	 */
	public static BigDecimal[] BOLL(List<BigDecimal> mas,List<BigDecimal> closes) {
		BigDecimal mb = mas.get(mas.size()-2);
		BigDecimal md = MD(mas,closes).multiply(new BigDecimal(2));
		//System.out.printf("2md = %.2f  ",md);
		BigDecimal up = mb.add(md);
		BigDecimal dn = mb.subtract(md);
		
		return new BigDecimal[]{up,mb,dn};
	}
	
	public static BigDecimal MD(List<BigDecimal> mas,List<BigDecimal> closes) {
		BigDecimal total = BigDecimal.ZERO;
		BigDecimal p;
		for(int i=0; i<mas.size(); i++) {
			p = closes.get(i).subtract(mas.get(i)).pow(2);
			total = total.add(p);
		}
		total = total.divide(new BigDecimal(mas.size()), BigDecimal.ROUND_HALF_UP);
		return sqrt(total);
		
	}
	
	public static BigDecimal sqrt(BigDecimal value){
		Double d = value.doubleValue();
		return new BigDecimal(Math.sqrt(d));

    }
		
	public static BigDecimal MA(List<BigDecimal> datas) {
		BigDecimal total = BigDecimal.ZERO;
		for(BigDecimal data : datas) {
			total = total.add(data);
		}
		return total.divide(new BigDecimal(datas.size()),BigDecimal.ROUND_HALF_UP);
	}
	
	
	
	/*
	 * MCST = DMA(成交额(元)/(100*成交量(手)),成交量(手)/当前流通股本(手))
	 */
	public static BigDecimal MSCT(BigDecimal vol, BigDecimal amount, BigDecimal float_share, BigDecimal previous) {
		if(vol==null || vol.equals(BigDecimal.ZERO) || float_share==null || float_share.equals(BigDecimal.ZERO)) {
			return null;
		}
		
		BigDecimal p1 = amount.divide(vol, BigDecimal.ROUND_HALF_UP);
		BigDecimal p2 = vol.divide(float_share, BigDecimal.ROUND_HALF_UP);
		return DMA(p1,p2,previous);
	}

	/*
	 * 		DMA(P1,P2)
		中文名： 变因子移动平均
		英文名： DMA
		描述：求P1的变因子移动平均, P2为平滑因子
		算法：
		P = DMA(P1,P2) = P2*P1+(1-P2)*P'
		其中：P'=上周期P值，P2<1
	 */
	public static BigDecimal DMA(BigDecimal p1, BigDecimal p2, BigDecimal previous) {
		if(previous==null) return p1;
		
		BigDecimal p3 = p1.multiply(p2);
		BigDecimal p4 = (BigDecimal.ONE).subtract(p2).multiply(previous);
		return p3.add(p4);
	}
}
