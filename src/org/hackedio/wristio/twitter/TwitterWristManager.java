package org.hackedio.wristio.twitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;

import org.hackedio.wristio.bluetooth.BluetoothManager;
import org.hackedio.wristio.util.BluetoothUtil;

public class TwitterWristManager extends Thread {
	
	private BluetoothManager manager;
	private boolean enable = true;
	
	public TwitterWristManager(BluetoothManager manager) {
		this.manager = manager;
	}
	
	@Override
	public void run() {
		Date previousTime = new Date();
		try {
			while(enable){
				Thread.sleep(1000);
				
				Date currentTime = new Date();
				if(currentTime.getTime()-previousTime.getTime() > 60000){
					previousTime = currentTime;
					
					URL url = new URL("https://github.com/Peterbyte/wrist.io");
					URLConnection yc = url.openConnection();
					BufferedReader in = new BufferedReader(new InputStreamReader(
                            yc.getInputStream()));
					String inputLine;
					StringBuffer sb = new StringBuffer();
					while ((inputLine = in.readLine()) != null){
						sb.append(inputLine);
						if(sb.length()>140){
							break;
						}
					}
					in.close();
					
					BluetoothUtil.sendMessage(null, manager, 2, 600, sb.subSequence(0, 140).toString().replaceAll("\n", ""));
				}
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void interrupt(){
		this.enable = false;
	}
}
