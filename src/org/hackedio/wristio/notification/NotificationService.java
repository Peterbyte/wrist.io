package org.hackedio.wristio.notification;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.view.accessibility.AccessibilityEvent;

public class NotificationService extends AccessibilityService {

	private boolean isInit;
	
	
	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			//Do something, eg getting packagename
//			final String packagename = String.valueOf(event.getPackageName());
			System.out.println(event);
		}
	}

	@Override
	protected void onServiceConnected() {
		if (isInit) {
			return;
		}
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN;
		setServiceInfo(info);
		isInit = true;
	}

	@Override
	public void onInterrupt() {
		isInit = false;
	}
}