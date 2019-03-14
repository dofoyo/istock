package com.rhb.istock.kdata.spider;

import java.util.Timer;
import java.util.TimerTask;

import com.rhb.istock.comm.util.ClipboardPicker;
import com.rhb.istock.comm.util.FileUtil;

public class KdataFromDZHByClipboard   extends TimerTask{
	String page_o = null;
	private static final String path = "D:\\workspace\\gulex-data\\trade\\dzh\\";
	
	@Override
	public void run() {
		String page = ClipboardPicker.getSystemClipboard();
		if(!page.equals(page_o)){
			page_o = page;
			String code = page.substring(1,7);
			String filename = code + ".txt";
			FileUtil.writeTextFile(path+filename, page, false);
			
			System.out.println(path+filename + " saved!");
			
			/*String[] lines = page.split("\n");
			for(String line : lines){
				String[] cells = line.split("\t");
				for(String cell : cells){
					System.out.print(cell + ",");
				}
				System.out.println("");
			}*/
		}
		//ClipboardPicker.clear();
	}
	
	
	public static void main(String[] args){
		Timer timer = new Timer();
		KdataFromDZHByClipboard ck = new KdataFromDZHByClipboard();
		timer.schedule(ck, 0, 1*1000);
	}
}
