package org.hackedio.wristio;

import java.io.IOException;
import java.util.Set;

import org.hackedio.wristio.bluetooth.BluetoothManager;
import org.hackedio.wristio.util.AlertUtil;
import org.hackedio.wristio.util.BluetoothUtil;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class ControlActivity extends Activity {

private static final String NOTFICATION_DEVICE_ADDRESS = "00:13:04:10:10:76";
	
	private static final int REQUEST_ENABLE_BLUETOOTH = 1;

	private BluetoothManager manager;
	
	private BluetoothAdapter bluetoothAdapter;
	
	private BluetoothDevice notificationDevice;
	
	private boolean initDone = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_control);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode == REQUEST_ENABLE_BLUETOOTH){
			if(resultCode == RESULT_OK){
				Log.i("info", "Bluetooth enabling");
			} else if (resultCode == RESULT_CANCELED){
				Log.i("info", "Permission for bluetooth not granted");
			}
		}
	}
	
//	@Override
//	public boolean onCreateOptionsMenu(Menu menu) {
//		// Inflate the menu; this adds items to the action bar if it is present.
//		getMenuInflater().inflate(R.menu.main, menu);
//		return true;
//	}

	public void doDisconnect(View view){//View is null when called from doDestroy, fix if required.
		manager.cancel();
	}
	
	public void doVibrate(View view){
		if(!initDone){
			//Init bluetooth if required
			bluetoothAdapter = initDeviceBluetooth();
			
			//Obtain bonded device
			//TODO: handle more than one bonded device
			notificationDevice = getPairedDevice(bluetoothAdapter, NOTFICATION_DEVICE_ADDRESS);
			initDone = true;
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
				BluetoothUtil.sendMessage(this, manager, 3, 250, "TESTING STUFF");
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
	}
	
	@Override
	protected void onDestroy() {
		doDisconnect(null);
		super.onDestroy();
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
			Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
			startActivityForResult(enableBtIntent, REQUEST_ENABLE_BLUETOOTH);
		}
		return bluetoothAdapter;
	}

}
