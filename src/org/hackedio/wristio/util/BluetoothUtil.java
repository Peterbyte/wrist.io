package org.hackedio.wristio.util;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import org.hackedio.wristio.bluetooth.BluetoothManager;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class BluetoothUtil {

	private static final String SERIAL_SERVICE_UUID = "00001101-0000-1000-8000-00805f9b34fb";
	
	public static BluetoothSocket createBluetoothSocket(BluetoothDevice device)	throws IOException {
		UUID uuid = UUID.fromString(SERIAL_SERVICE_UUID);
		BluetoothSocket socket = null;
		if (Build.VERSION.SDK_INT >= 10) {
			try {
				final Method m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
				System.out.println("Performing invoke");
				socket = (BluetoothSocket) m.invoke(device, uuid);
				System.out.println("Invoke done");
				System.out.println(socket);
			} catch (Exception e) {
				Log.e("error", "Could not create Insecure RFComm Connection", e);
			}
		}
		else{
			//TODO, support older versions with secure connection
		}
		return socket;
	}
	
	public static void sendMessage(Context context, BluetoothManager mgr, Integer numPulses, Integer pulseDuration, String displayText){
		try{
			StringBuffer sb = new StringBuffer();
			sb.append(numPulses);
			sb.append(",");
			sb.append(pulseDuration);
			sb.append(",");
			sb.append(displayText);
			mgr.write(sb.toString().getBytes("UTF-8"));
		}
		catch(Exception e){
			AlertUtil.alertMessage(context, "An error ocurred attempting to communicate with the Notification device");
			Log.i("error", "An error ocurred attempting to communicate with the Notification device", e);
			e.printStackTrace();
		}
	}

}
