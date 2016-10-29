package com.bk.lockscreen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;

import com.bk.lockscreen.utils.LockscreenService;
import com.bk.setting.BasicSetting;

public class MainActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getFragmentManager().beginTransaction()
				.replace(android.R.id.content, new BasicSetting()).commit();
		startService(new Intent(this, LockscreenService.class));
		SharedPreferences preferences = getSharedPreferences("SIZE",
				Context.MODE_PRIVATE);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		Log.e("tuyen.px", "width = " + width);
		Editor editor = preferences.edit();
		editor.putInt("height", height);
		editor.putInt("width", width);
		editor.commit();
	}
}