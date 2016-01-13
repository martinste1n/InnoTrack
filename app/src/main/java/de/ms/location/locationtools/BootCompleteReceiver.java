package de.ms.location.locationtools;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootCompleteReceiver extends BroadcastReceiver {

	
	public void onReceive(Context context, Intent intent) {
		Intent myIntent = new Intent(context, LocationService.class);
	    context.startService(myIntent);

	}

}
