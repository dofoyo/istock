package com.rhb.istock.comm.util;
public class Progress{
	public static void show(int total, int now, String note){
        System.out.print("\r");
		//double rate = 1.0 * now / total;
		//System.out.print(String.format(" %.2f%%", rate * 100));
		System.out.format(" %.2f%% %d/%d %s", 1.0 * now / total * 100, now, total, note);
		if(now==total) {
			System.out.println("\n");
		}
	}

	
	public static void main(String[] args) throws Exception {
		int total = 100000;
	    for (int i = 1; i <= total; i++) {
	    	Progress.show(total,i,"");
		}
	    System.out.println();
	}
}