package com.example.locationsfinalproject.control;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.BatteryManager;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

public class PowerConnectionReceiiver extends BroadcastReceiver {

	private static final String TAG = "PowerConnectionReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {

		// the phone received an update about the power connection
		Log.d(TAG, "POWER CONNECTION RECEIVED! (global receiver)");

		int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
		boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING;

		int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1);
		boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
		boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

		Toast toast = null;
		if (isCharging == true) {
			if (acCharge == true) {
				// make a toast about it
				toast = Toast.makeText(context,"Starting to charge the device using AC adapter...",Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}else if (usbCharge == true) {
				// make a toast about it
				toast = Toast.makeText(context,"Starting to charge the device using USB...",Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();
			}
		}else if(isCharging == false){
			toast = Toast.makeText(context,"The charging of the device has stopped...",Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}



}
}
