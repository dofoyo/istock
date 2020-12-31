package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.HttpClient;
import com.rhb.istock.comm.util.HttpDownload;
import com.rhb.istock.comm.util.ParseString;
import com.rhb.istock.comm.util.Progress;
import com.rhb.istock.item.ItemService;
import com.rhb.istock.kdata.Kbar;


@Service("kdataRealtimeSpiderImp")
public class KdataRealtimeSpiderImp implements KdataRealtimeSpider{
	@Autowired
	@Qualifier("itemServiceImp")
	ItemService itemService;
	
	@Value("${tushareUrl}")
	private String url;
	
	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

	@Override
	public LocalDate getLatestMarketDate(String itemID) {
		String url = "https://qt.gtimg.cn/q=" + itemID;
		
		String result = HttpDownload.getResult(url);
		//String result = HttpClient.doGet(url);
		
		String[] ss = result.split("~");
		
		if(ss==null || ss.length<30) {
			return null;
		}
		
		return LocalDate.parse(ss[30].substring(0, 8),formatter);
	}

	
	@Override
	public Map<String, String> getLatestMarketData(String id) {
		Map<String,String> map = null;
		
		String url = "https://qt.gtimg.cn/q=" + id;
		//System.out.println(url);
		String result = HttpDownload.getResult(url);

		//String result = HttpClient.doGet(url);
		
		String[] ss = result.split("~");
		if(ss.length>4 && !ss[3].equals("0.00")) {
			map = new HashMap<String,String>();
			map.put("dateTime", LocalDate.parse(ss[30].substring(0, 8),DateTimeFormatter.ofPattern("yyyyMMdd")).toString());
			map.put("itemID", id);
			map.put("code", ss[2]);
			map.put("name", ss[1]);
			map.put("preClose", ss[4]);
			map.put("open", ss[5]);
			map.put("high", ss[33]);
			map.put("low", ss[34]);
			map.put("close", ss[3]);
			map.put("quantity", ss[6]);
			map.put("amount", ss[37]);
		}

		return map;
	}


	@Override
	public List<String> getLatestDailyTop(Integer top) {
		List<String> ids = new ArrayList<String>();
		String strUrl = "http://nufm.dfcfw.com/EM_Finance2014NumericApplication/JS.aspx?cb=jQuery112403517194352564321_1550228468554&type=CT&token=4f1862fc3b5e77c150a2b985b12db0fd&sty=FCOIATC&js=(%7Bdata%3A%5B(x)%5D%2CrecordsFiltered%3A(tot)%7D)&cmd=C._A&st=(Amount)&sr=-1&p=1&ps="+top.toString()+"&_=1550228468980";
		String result = HttpClient.doGet(strUrl);
		List<String> strs = ParseString.subStrings(result, "\"|\"");
		String ss;
		String code;
		StringBuffer sb = new StringBuffer();
		for(String str : strs) {
			ss = str.substring(0,1).equals("1") ? "sh" : "sz";
			code = str.substring(2,8);
			sb.append(ss + code);
			sb.append(",");
			ids.add(ss + code);
		}
		if(strs.size()>0) sb.deleteCharAt(sb.length()-1);
		
		return ids;
	}


	@Override
	public List<LocalDate> getCalendar(LocalDate startDate, LocalDate endDate) throws Exception {
		List<LocalDate> dates = new ArrayList<LocalDate>();
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "trade_cal");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("start_date", startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONArray items = (new JSONObject(str)).getJSONObject("data").getJSONArray("items");
		Integer isOpen;
		LocalDate date;
		if(items.length()>0) {
			JSONArray item;
			for(int i=1; i<items.length(); i++) {
				item = items.getJSONArray(i);
				isOpen = item.getInt(2);
				if(isOpen==1) {
					date = LocalDate.parse(item.getString(1),DateTimeFormatter.ofPattern("yyyyMMdd"));
					if(date.isBefore(endDate)) {
						dates.add(date);
					}else {
						break;
					}
				}
			}
		}
		
		return dates;
	}


	@Override
	public boolean isTradeDate1(LocalDate date) {
		//String url = "http://api.tushare.pro";
		JSONObject args = new JSONObject();
		args.put("api_name", "trade_cal");
		args.put("token", "175936caa4637bc9ac8e5e75ac92eff6887739ca6be771b81653f278");
		
		JSONObject params = new JSONObject();
		params.put("start_date", date.format(DateTimeFormatter.ofPattern("yyyyMMdd")));
		
		args.put("params", params);
		
		String str = HttpClient.doPostJson(url, args.toString());
		JSONObject tmp = new JSONObject(str);
		
		JSONArray items = null;
		try {
			items = tmp.getJSONObject("data").getJSONArray("items");
		}catch(Exception e) {
			System.out.println(date.toString());
			System.out.println(str);
			e.printStackTrace();
		}
		
		Integer isOpen;
		LocalDate theDate;
		if(items!=null && items.length()>0) {
			JSONArray item = items.getJSONArray(0);
			System.out.println(item);
			isOpen = item.getInt(2);
			theDate = LocalDate.parse(item.getString(1),DateTimeFormatter.ofPattern("yyyyMMdd"));
			if(isOpen==1 && theDate.equals(date)) {
				return true;
			}
		}
		
		return false;
	}


	@Override
	public Set<Kbar> getLatestMarketData() {
		Set<Kbar> bars = new HashSet<Kbar>();
		StringBuffer sb = new StringBuffer();
		List<String> ids = itemService.getItemIDs();
		for(int i=0, j=200; i<ids.size(); i=i+200) {
			Progress.show(ids.size(),i, " getLatestMarketData ");
			j = i + 200;
			if(j>ids.size()) {
				j = ids.size();
			}
			
			//System.out.println(i + " - " + j);
			bars.addAll(this.getLatestMarketDateKbars(ids.subList(i, j)));
			HttpClient.sleep(5);
		}

		return bars;
	}
	
	private Set<Kbar> getLatestMarketDateKbars(List<String> ids) {		
		Set<Kbar> bars = new HashSet<Kbar>();
		StringBuffer sb = new StringBuffer();
		for(String id : ids) {
			sb.append(id);
			sb.append(",");
		}
		String url = "https://qt.gtimg.cn/q=" + sb.toString();
		String result = HttpDownload.getResult(url);
		//System.out.println(result);
		String[] ss = result.split(";\n");
		Kbar bar;
		for(String s : ss) {
			//System.out.println(s);
			bar = this.getKbar(s);
			if(bar!=null) {
				bars.add(bar);
			}
		}
		return bars;
	}
	
	private Kbar getKbar(String str) {
		Kbar bar = null;
		String[] ss = str.split("~");
		if(ss.length>4 && !ss[3].equals("0.00") && ss[30].length()>8) {
			bar = new Kbar(ss[5],
					ss[33], 
					ss[34], 
					ss[3], 
					ss[37],
					ss[6],
					LocalDate.parse(ss[30].substring(0, 8),DateTimeFormatter.ofPattern("yyyyMMdd")).toString(),
					"0","0","0","0","0","0","0","0");
			bar.setId(ss[0].substring(2,10));
			//System.out.print(ss[0]);
		}
		//System.out.println(bar);
		return bar;
	}
	
}






/*
 * 
接口： 
http://qt.gtimg.cn/q=sh600519

返回： 
v_sh600519="1~贵州茅台~600519~358.74~361.29~361.88~27705~12252~15453~358.75~8~358.74~4~358.72~7~358.71~6~358.70~5~358.77~3~358.78~2~358.79~16~358.80~4~358.86~1~14:59:59/358.75/5/S/179381/28600|14:59:56/358.75/1/S/35875/28594|14:59:53/358.75/1/S/35875/28588|14:59:50/358.75/1/S/35875/28579|14:59:47/358.75/4/B/143499/28574|14:59:41/358.72/4/S/143501/28562~20170221150553~-2.55~-0.71~362.43~357.18~358.75/27705/994112865~27705~99411~0.22~27.24~~362.43~357.18~1.45~4506.49~4506.49~6.57~397.42~325.16~0.86";

解释： 
0: 未知
1: 股票名字
2: 股票代码
3: 当前价格
4: 昨收
5: 今开
6: 成交量（手）
7: 外盘
8: 内盘
9: 买一
10: 买一量（手）
11-18: 买二 买五
19: 卖一
20: 卖一量
21-28: 卖二 卖五
29: 最近逐笔成交
30: 时间
31: 涨跌
32: 涨跌%
33: 最高
34: 最低
35: 价格/成交量（手）/成交额
36: 成交量（手）
37: 成交额（万）
38: 换手率
39: 市盈率
40: 
41: 最高
42: 最低
43: 振幅
44: 流通市值
45: 总市值
46: 市净率
47: 涨停价
48: 跌停价
 */
