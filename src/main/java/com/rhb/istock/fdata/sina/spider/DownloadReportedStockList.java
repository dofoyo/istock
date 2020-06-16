package com.rhb.istock.fdata.sina.spider;

import java.util.Map;

public interface DownloadReportedStockList {

	public Map<String,String> go(String year);
}
