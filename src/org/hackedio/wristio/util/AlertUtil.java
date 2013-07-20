package org.hackedio.wristio.util;

import android.app.AlertDialog;
import android.content.Context;

public class AlertUtil {

	public static void alertMessage(Context context, String msg){
		AlertDialog alertBuilder = new AlertDialog.Builder(context).create();
		alertBuilder.setMessage(msg);
		alertBuilder.show();
	}
	
}
