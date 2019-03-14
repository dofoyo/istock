package com.rhb.istock.comm.util;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ClipboardPicker {

	public static String getSystemClipboard(){//获取系统剪切板的文本内容[如果系统剪切板复制的内容是文本]
		Clipboard sysClb=null;
		sysClb = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable t = sysClb.getContents(null);
		//Transferable t = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(null);  //跟上面三行代码一样
		try { 
			if (null != t && t.isDataFlavorSupported(DataFlavor.stringFlavor)) { 
			String text = (String)t.getTransferData(DataFlavor.stringFlavor); 
			return text; 
			} 
		} catch (UnsupportedFlavorException e) {
			//System.out.println("Error tip: "+e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} 	//System.out.println("Error tip: "+e.getMessage());
		
		return null; 
	}
	
	public static void clear(){
		Clipboard sysClb = Toolkit.getDefaultToolkit().getSystemClipboard();
		sysClb.setContents(null, null);
		
	}
}
