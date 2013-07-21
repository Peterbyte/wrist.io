package org.hackedio.wristio.twitter;

import java.util.Calendar;

public class TwitterWristManager extends Thread {
	
	private boolean enable = true;	
	@Override
	public void run() {
		Calendar cal = Calendar.getInstance();

		try {
			while(enable){
				Thread.sleep(60000);
				
				
			}
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void interrupt(){
		this.enable = false;
	}
}
