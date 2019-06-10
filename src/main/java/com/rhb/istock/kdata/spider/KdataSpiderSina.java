package com.rhb.istock.kdata.spider;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.rhb.istock.comm.util.FileTools;
import com.rhb.istock.comm.util.Progress;

/*
* 注意季度交接日
* 最好每天23点前完成下载
*/

@Service("kdataSpiderSina")
public class KdataSpiderSina implements KdataSpider {
	@Value("${sinaKdataPath}")
	private String kdataPath;
	
	private void downKdatas(String id, String year, String jidu) throws Exception {
		String code = id.substring(2);
		
		String strUrl = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/CODE.phtml?year=YEAR&jidu=JIDU";
		strUrl = strUrl.replace("CODE", code);
		strUrl = strUrl.replace("YEAR", year);
		strUrl = strUrl.replace("JIDU", jidu);
		String file = kdataPath + "/" + id + "_" + year + "_" + jidu + ".txt";
		System.out.println("download " + file);
		
		//String str = HttpClient.doGet(strUrl);
		//System.out.println(str);
		
        Connection connect = Jsoup.connect(strUrl);
        Map<String, String> header = new HashMap<String, String>();
        header.put("Host", "http://www.sina.com.cn");
        header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko");
        //Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; Touch; rv:11.0) like Gecko
        header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
        header.put("Accept-Language", "zh-cn,zh;q=0.5");
        header.put("Accept-Charset", "GB2312,utf-8;q=0.7,*;q=0.7");
        header.put("Connection", "keep-alive");
        Connection data = connect.headers(header);
		
		Document doc = data.get();
		Element table = doc.getElementById("FundHoldSharesTable");
		Elements trs = table.select("tr");
		Elements tds;
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < trs.size(); i++) {
			tds = trs.get(i).select("td");
			for (int j = 0; j < tds.size(); j++) {
				String text = tds.get(j).text();
				sb.append(text);
				if(j<(tds.size()-1)) sb.append(",");
			}
			if(sb.length()>0)	sb.append("\n");
		}
		//System.out.println(sb.toString());
		
		FileTools.writeTextFile(file, sb.toString(), false);
	}

	@Override
	public void downKdata(String id)  throws Exception {
		String[] yjs = getYearAndJidu(5);
		String year;
		String jidu;
		String file;
		long times;
		for(int i=0; i<yjs.length; i++) {
			year = yjs[i].substring(0,4);
			jidu = yjs[i].substring(5,6);
			file = kdataPath + "/" + id + "_" + year + "_" + jidu + ".txt";
			if(i==0 || !FileTools.isExists(file)) {
				try {
					downKdatas(id,year,jidu);
					times = (long) (Math.random() * 10) * 1000;
					System.out.println("wait " + times/1000 + " seconds.");
					Thread.sleep(times);
				} catch (Exception e) {
					e.printStackTrace();
					try {
						System.out.println("ERROR, maybe stop by sina. wait 20 minutes");
						Thread.sleep(20*60*1000);  //
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}else {
				System.out.format("%s %s have downloaded!\n", id, yjs[i]);
			}
		}
	}

	private String[] getYearAndJidu(Integer num) {
		LocalDate now = LocalDate.now();
		int year = now.getYear();
		int m = now.getMonthValue();
		int jidu = m<=3 ? 1 : (m<=6 ? 2 : (m<=9 ? 3 :4));
		String[] ss = new String[num];
		ss[0] = String.valueOf(year) + "." + String.valueOf(jidu);
		for(int i=1; i<num; i++) {
			if(jidu-1<=0) {
				year--;
				jidu=4;
			}else {
				jidu--;
			}
			ss[i]=String.valueOf(year) + "." + String.valueOf(jidu);
		}
		return ss;
	}

	@Override
	public void downKdatasAndFactors(LocalDate date) throws Exception {
		// TODO Auto-generated method stub
		throw new Exception("sina do not supply down kdata by date!");
	}

	@Override
	public void downKdata(List<String> ids) throws Exception {
		long beginTime=System.currentTimeMillis(); 
		System.out.println("KdataSpiderSina downKdata...");

		int i=0;
		for(String id : ids) {
			Progress.show(ids.size(),i++,id);
			this.downKdata(id);
		}
		
		System.out.println("KdataSpiderSina downKdata done!");
		
		long used = (System.currentTimeMillis() - beginTime)/1000; 
		System.out.println("用时：" + used + "秒");          

	}

	@Override
	public void downKdatas(LocalDate date) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void downFactors(LocalDate date) throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String downLatestFactors(LocalDate date) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}


}
