package com.rhb.istock.comm.util;
public class ProgressBar {
    private int totalPoint = 100;
    private int barLength = 20;
       
    public ProgressBar(int totalPoint, int barLength){
        this.totalPoint = totalPoint;
        this.barLength = barLength;
    }

	public ProgressBar(int totalPoint){
        this.totalPoint = totalPoint;
    }
    
     public void showBarByPoint(int currentPoint) {
        double rate = 1.0 * currentPoint / this.totalPoint;
        int barSign = (int) (rate * this.barLength);
        System.out.print("\r");
        System.out.print(makeBarBySignAndLength(barSign) + String.format(" %.2f%%", rate * 100));
    }
    
    private String makeBarBySignAndLength(int barSign) {
        StringBuilder bar = new StringBuilder();
        bar.append("[");
        for (int i=1; i<=this.barLength; i++) {
            if (i < barSign) {
                bar.append("-");
            } else if (i == barSign) {
                bar.append("-");
            } else {
                bar.append(".");
            }
        }
        bar.append("]");
        return bar.toString();
    }

	public static void main(String[] args) throws Exception {
		int total = 100;
		ProgressBar bar = new ProgressBar(total);
		
		for(int i=1; i<=total; i++){
				bar.showBarByPoint(i);
				Thread.sleep(50);
		}

	}
}