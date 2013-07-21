package org.hackedio.wristio.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.hackedio.wristio.twitter.TwitterWristManager;

import android.bluetooth.BluetoothSocket;

//Copied from http://developer.android.com/guide/topics/connectivity/bluetooth.html#Permissions (ConnectedThread)
public class BluetoothManager extends Thread{

	private final BluetoothSocket mmSocket;
//    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    
    private boolean enabled = true;
 
    public BluetoothManager(BluetoothSocket socket) {
        mmSocket = socket;
//        InputStream tmpIn = null;
        OutputStream tmpOut = null;
 
        // Get the input and output streams, using temp objects because
        // member streams are final
        try {
//            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }
 
//        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }
 
    public void run() {
    	
    	TwitterWristManager twm = new TwitterWristManager();
    	twm.start();
    	
        // Keep listening to the InputStream until an exception occurs
        while (enabled) {
        	try {
				Thread.sleep(25);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
    }

    /* Call this from the main activity to send data to the remote device */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }
 
    /* Call this from the main activity to shutdown the connection */
    public void cancel() {
        try {
            mmSocket.close();
            enabled = false;
        } catch (IOException e) { }
    }

	public boolean isEnabled() {
		return enabled;
	}
    
}
