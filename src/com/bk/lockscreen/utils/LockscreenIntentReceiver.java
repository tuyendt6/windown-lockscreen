package com.bk.lockscreen.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LockscreenIntentReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
			start_lockscreen(context);
		}
	}

	private void start_lockscreen(Context context) {
		context.startService(new Intent(context, LockscreenService.class));
	}
}
