package org.hackedio.wristio.notification;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import org.hackedio.wristio.bluetooth.BluetoothManager;
import org.hackedio.wristio.util.AlertUtil;
import org.hackedio.wristio.util.BluetoothUtil;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;

public class NotificationService extends AccessibilityService {

	private static final String NOTIFCATION_DEVICE_ADDRESS = "00:13:04:10:10:76";
	
	private boolean init = false;
	
	private BluetoothManager manager;
	
	private BluetoothAdapter bluetoothAdapter;
	
	private BluetoothDevice notificationDevice;
	
	private TelephonyManager telephonyManager;
	
	private int lastState = -1;
	
	@Override
	public void onCreate() {
		if(!init){
			//Init bluetooth if required
			bluetoothAdapter = initDeviceBluetooth();
			
			//Obtain bonded device
			//TODO: handle more than one bonded device
			notificationDevice = getPairedDevice(bluetoothAdapter, NOTIFCATION_DEVICE_ADDRESS);
			telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			
			init = true;
		}
		
		if(notificationDevice != null){
			try {
				if(manager == null || !manager.isAlive() || !manager.isEnabled()){
					bluetoothAdapter.cancelDiscovery();
					BluetoothSocket socket = BluetoothUtil.createBluetoothSocket(notificationDevice);
					socket.connect();
					manager = new BluetoothManager(socket);
					manager.start();
					
				}
				BluetoothUtil.sendMessage(this, manager, 3, 250, "Init complete on "+new Date());
			} catch (IOException e) {
				AlertUtil.alertMessage(this, "An error ocurred attempting to communicate with the Notification device");
				Log.i("error", "An error ocurred attempting to communicate with the Notification device", e);
				e.printStackTrace();
			}
		}
		else{
			Log.i("info", "Notification device could not be found");
			AlertUtil.alertMessage(this, "Notification device could not be found");
		}
		System.out.println("Before register listenter");
		PhoneStateListener listener = new PhoneStateListener(){
			public void onCallStateChanged(int state, String incomingNumber) {
				StringBuffer sb = new StringBuffer();
				int vibratePhase = 0, vibrateCount = 0;
				switch (state) {
	               case TelephonyManager.CALL_STATE_IDLE:
	            	   if(lastState == TelephonyManager.CALL_STATE_OFFHOOK){
	            		   sb.append("Call ended with ");
	            	   } else{
	            		   sb.append("Last call with ");
	            	   }
	                 break;
	               case TelephonyManager.CALL_STATE_OFFHOOK:
	            	   sb.append("In call");
	                 break;
	               case TelephonyManager.CALL_STATE_RINGING:
	            	   vibratePhase = 175;
	            	   vibrateCount = 30;
	                 	sb.append("Incoming call from ");
	                 break;
                 default:
                	 System.out.println("Unknown State: "+state);
	               }
				String contact = incomingNumber;
				Cursor cursor = null;
				try {
					Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(incomingNumber));
					cursor = getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
					while(cursor.moveToNext()){
						contact = cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup.DISPLAY_NAME));
					    //String contactId = cursor.getString(cursor.getColumnIndexOrThrow(PhoneLookup._ID));
					    break;
					}
					} catch(Exception e){
						e.printStackTrace();
					}
				finally {
					if(cursor != null)
					cursor.close();
					}
				sb.append(contact);
				BluetoothUtil.sendMessage(null, manager, vibrateCount, vibratePhase, sb.toString());
				lastState = state;
			};
		};
		System.out.println("Before register listenter1");
		telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		System.out.println("Before register listenter2");
	}
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
//		
//		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
//			//Do something, eg getting packagename
//			final String packagename = String.valueOf(event.getPackageName());
//			Log.d("debug", "Package Name "+packagename);
//			if(packagename.startsWith("com.android.phone")){
//				BluetoothUtil.sendMessage(null, manager, 8, 175, "Incoming call from "+event.getText());
//			}
//			System.out.println(event);
//		}
	}

	@Override
	protected void onServiceConnected() {
		if (init) {
			return;
		}
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		setServiceInfo(info);
		init = true;
	}
	
	private BluetoothDevice getPairedDevice(BluetoothAdapter bluetoothAdapter, String address){
		BluetoothDevice desiredDevice = null;
		Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
		if(pairedDevices.isEmpty()){
			AlertUtil.alertMessage(this, "No devices have been paired");
		}
		else{
			for(BluetoothDevice device : pairedDevices){
				if(device.getName().equals("linvor")){
					desiredDevice = device;
					break;
				}
			}
		}
		return desiredDevice;
	}

	private BluetoothAdapter initDeviceBluetooth() {
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if(bluetoothAdapter==null){
			Log.e("error", "Bluetooth not supported on this device");
			AlertUtil.alertMessage(this, "Bluetooth is not available on this device.");
		}
		else if(!bluetoothAdapter.isEnabled()){
			//This isn't great because we don't ask for any permissions.
			bluetoothAdapter.enable();
		}
		return bluetoothAdapter;
	}
	
	@Override
	public void onDestroy() {
		manager.cancel();
	}
	
	@Override
	public void onInterrupt() {
		manager.cancel();
		init = false;
	}
}