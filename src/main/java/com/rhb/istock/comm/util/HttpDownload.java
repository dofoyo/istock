package com.rhb.istock.comm.util;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HttpDownload {
	private static int BUFFER_SIZE = 8096;

	public static void saveToFile(String destUrl, String file){
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
		
			URL url = new URL(destUrl);
			HttpURLConnection httpUrl = (HttpURLConnection)url.openConnection();
			httpUrl.setConnectTimeout(10000);
			httpUrl.setReadTimeout(10000);
			httpUrl.connect();
			BufferedInputStream bis = new BufferedInputStream(httpUrl.getInputStream());
			
			
			int size = 0;
			byte[] buf = new byte[BUFFER_SIZE];
			while((size=bis.read(buf)) != -1){
				fos.write(buf, 0, size);
			}
			
			fos.close();
			bis.close();
			httpUrl.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static String getResult(String destUrl){
		StringBuffer sb = new StringBuffer(); 
		
		try {
			URL url = new URL(destUrl);
			HttpURLConnection httpUrl = (HttpURLConnection)url.openConnection();
			httpUrl.setConnectTimeout(10000);
			httpUrl.setReadTimeout(10000);
			httpUrl.connect();
			InputStream in = httpUrl.getInputStream();
			
	        byte[] b = new byte[BUFFER_SIZE]; 
	        for(int n; (n = in.read(b))!= -1;){ 
	             sb.append(new String(b,0,n)); 
	        } 

	        httpUrl.disconnect();
		} catch (Exception e) {
			System.out.println("  ERROR: " + destUrl);
			e.printStackTrace();
		}
        return sb.toString(); 
	}
	
}
