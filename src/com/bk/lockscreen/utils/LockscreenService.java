package com.bk.lockscreen.utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.bk.lockscreen.R;
import com.bk.setting.BasicSetting;

public class LockscreenService extends Service implements OnClickListener {

	private static final String TAG = "LockscreenService";
	private View mFloatView;
	private LayoutParams mFloatContainerParams;
	private LinearLayout mFloatViewContainer;
	private WindowManager windowManager;

	private TextView mDateTime;
	private TextView mOperator;
	private Handler mTimerUpdate;
	private Calendar c = Calendar.getInstance();
	private SimpleDateFormat sdf;

	private TextView mDate;
	private TextView mTime;
	private TextView mNoon;
	private TextView mBattery;
	private ImageView mBatteryImg;

	private Context mContext;

	private SoundPool mSoundPool = null;
	private int[] sounds = null;
	boolean loaded = false;
	private int dragSoundCount = 0;
	final int DRAG_SOUND_COUNT_INTERVAL = 60;
	final int DRAG_SOUND_COUNT_START_POINT = DRAG_SOUND_COUNT_INTERVAL - 20;
	final int SOUND_ID_TAB = 0;
	final int SOUND_ID_DRAG = 1;
	final int SOUND_ID_UNLOCK = 2;
	private float leftVolumeMax = 0.3f;
	private float rightVolumeMax = 0.3f;
	private Bitmap bitmap;
	private static boolean isShow = false;

	// flipper view .
	private ViewFlipper viewFlipper1;
	private Animation mInfromLeft;
	private Animation mOutToRight;
	private Button btnsave;
	private EditText txtenter;

	private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
			btnclear;
	private String mPasswd = "";

	public void addSymbol(String symbol) {
		mPasswd = mPasswd + symbol;
		txtenter.setText(mPasswd);
	}

	private RelativeLayout mContentLockScreen;

	private BroadcastReceiver batteryStatusReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			int level = intent.getIntExtra("level", 0);
			int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
			boolean batteryCharge = status == BatteryManager.BATTERY_STATUS_CHARGING;
			int chargePlug = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED,
					-1);
			boolean usbCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_USB;
			boolean acCharge = chargePlug == BatteryManager.BATTERY_PLUGGED_AC;

			if ((acCharge) || (usbCharge) || (batteryCharge)) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_charge2_green));
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.charging));
				mBattery.setTextColor(Color.GREEN);
			} else if (60 <= level && level < 80) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_high));
				mBattery.setTextColor(color);
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
			} else if (20 <= level && level < 60) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_half));
				mBattery.setTextColor(color);
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
			} else if (level < 20) {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_low_red));
				mBattery.setTextColor(Color.RED);
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
			} else {
				mBatteryImg.setBackground(getResources().getDrawable(
						R.drawable.ic_battery_full));
				mBattery.setText(level + "% ");
				// + getResources().getString(R.string.discharging));
				mBattery.setTextColor(color);
			}
		}
	};
	private BroadcastReceiver mUpdateTimeDate = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			mTimerUpdate.sendEmptyMessage(0);
		}

	};

	private final PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			super.onCallStateChanged(state, incomingNumber);
			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				if (isShow) {
					removeview();
				}
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:
				break;
			case TelephonyManager.CALL_STATE_IDLE:
				break;
			}
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = getApplicationContext();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Intent.ACTION_SCREEN_OFF);
		filter.addAction(Intent.ACTION_SCREEN_ON);
		registerReceiver(mBroadcastReceiver, filter);
		disableKeyguard();
	}

	private int color = -262401;
	private int count_failed = 3;
	int count = 3;
	ArrayList<LockItem> arrayList = new ArrayList<LockItem>();

	public void setupview(String path_file, int color_change) {
		if (mSoundPool == null) {
			Log.e(TAG, "ACTION_UP, mSoundPool == null");
			makeSound();
		}
		TelephonyManager mTelephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		if (null != mTelephonyManager)
			mTelephonyManager.listen(mPhoneStateListener,
					PhoneStateListener.LISTEN_CALL_STATE);
		isShow = true;
		if (windowManager == null) {
			windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
		}

		if (mFloatView == null) {
			mFloatView = LayoutInflater.from(this).inflate(
					R.layout.activity_lockscreen, null);
		}
		registerReceiver(batteryStatusReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));
		registerReceiver(mUpdateTimeDate, new IntentFilter(
				Intent.ACTION_TIME_TICK));
		mFloatContainerParams = new WindowManager.LayoutParams();
		mFloatContainerParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
		mFloatContainerParams.gravity = Gravity.TOP;
		mFloatContainerParams.flags =

		WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
		// this is to enable the notification to recieve touch events
				WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL |
				// Draws over status bar
				WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		SharedPreferences preferencess = getSharedPreferences("SIZE",
				Context.MODE_PRIVATE);
		int width = preferencess.getInt("width", 1000);
		int height = preferencess.getInt("height", 1000);
		mFloatContainerParams.width = width;
		mFloatContainerParams.height = height;
		mFloatContainerParams.format = PixelFormat.TRANSLUCENT;

		mFloatContainerParams.y = 0;
		mFloatContainerParams.x = 0;

		mFloatViewContainer = new LinearLayout(this);
		mFloatViewContainer.setLayoutParams(new LinearLayout.LayoutParams(
				width, height));
		mContentLockScreen = (RelativeLayout) mFloatView
				.findViewById(R.id.contentlockscreen);
		mContentLockScreen.setDrawingCacheEnabled(false);

		SharedPreferences preferences = getApplicationContext()
				.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
						Context.MODE_PRIVATE);
		String path = "";
		if (path_file == null) {
			path = preferences.getString(Utils.CURRENTBACKGROUND, "");
		} else {
			path = path_file;
			SharedPreferences preferencesss = getApplicationContext()
					.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
							Context.MODE_PRIVATE);
			Editor editor = preferencesss.edit();
			editor.putString(Utils.CURRENTBACKGROUND, path);
			editor.commit();
		}
		Log.e(TAG, "kich thuoc man hinh with = " + width + " height = "
				+ height);
		Log.e(TAG, "tuyenpx path = " + path);
		if (path.trim().equals("")) {
			BitmapDrawable ob = new BitmapDrawable(getResources(),
					decodeSampledBitmapFromResource(R.drawable.face, width,
							height));
			mContentLockScreen.setBackground(ob);
		} else {
			BitmapDrawable ob = new BitmapDrawable(getResources(),
					decodeSampledBitmapFromResource(path, width, height));
			mContentLockScreen.setBackground(ob);
		}
		if (color_change == 0) {
			color = PreferenceManager.getDefaultSharedPreferences(
					getApplicationContext()).getInt(BasicSetting.COLOR_DIALOG,
					-262401);
		} else {
			color = color_change;
			SharedPreferences pfPreference = PreferenceManager
					.getDefaultSharedPreferences(getApplicationContext());
			Editor editor2 = pfPreference.edit();
			editor2.putInt(BasicSetting.COLOR_DIALOG, color);
			editor2.commit();
		}
		mContext = getApplicationContext();
		mDateTime = (TextView) mFloatView.findViewById(R.id.date);
		mDateTime.setTextColor(color);
		mOperator = (TextView) mFloatView.findViewById(R.id.operator);
		mOperator.setPadding(-1, -1, -1, (int) (15 * getResources()
				.getDisplayMetrics().scaledDensity));
		mOperator.setTextColor(color);
		mDate = (TextView) mFloatView.findViewById(R.id.date);
		mDate.setTextColor(color);

		mTime = (TextView) mFloatView.findViewById(R.id.time);
		mTime.setTextColor(color);
		Typeface customFont = Typeface.createFromAsset(getAssets(),
				"Roboto-Thin.ttf");
		mTime.setTypeface(customFont);

		mNoon = (TextView) mFloatView.findViewById(R.id.noon);
		mNoon.setTextColor(color);
		mBattery = (TextView) mFloatView.findViewById(R.id.battery);
		mBattery.setTextColor(color);
		mBatteryImg = (ImageView) mFloatView.findViewById(R.id.battery_img);
		setDateTime();
		settimedate();
		// setup flipview :

		viewFlipper1 = (ViewFlipper) mFloatView
				.findViewById(R.id.view_flipper1);
		viewFlipper1.setDisplayedChild(0);
		initAnimations();

		// setup
		btn0 = (Button) mFloatView.findViewById(R.id.b0);
		btn0.setOnClickListener(this);
		btn1 = (Button) mFloatView.findViewById(R.id.b1);
		btn1.setOnClickListener(this);
		btn2 = (Button) mFloatView.findViewById(R.id.b2);
		btn2.setOnClickListener(this);
		btn3 = (Button) mFloatView.findViewById(R.id.b3);
		btn3.setOnClickListener(this);
		btn4 = (Button) mFloatView.findViewById(R.id.b4);
		btn4.setOnClickListener(this);
		btn5 = (Button) mFloatView.findViewById(R.id.b5);
		btn5.setOnClickListener(this);
		btn6 = (Button) mFloatView.findViewById(R.id.b6);
		btn6.setOnClickListener(this);
		btn7 = (Button) mFloatView.findViewById(R.id.b7);
		btn7.setOnClickListener(this);
		btn8 = (Button) mFloatView.findViewById(R.id.b8);
		btn8.setOnClickListener(this);
		btn9 = (Button) mFloatView.findViewById(R.id.b9);
		btn9.setOnClickListener(this);
		txtenter = (EditText) mFloatView.findViewById(R.id.pin);
		txtenter.setText("");
		btnclear = (Button) mFloatView.findViewById(R.id.back);
		btnclear.setOnClickListener(this);
		btnsave = (Button) mFloatView.findViewById(R.id.button1);
		btnsave.setOnClickListener(this);

		mFloatViewContainer.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_UP:

					if (isShow) {
						if (mSoundPool == null) {
							Log.e(TAG, "ACTION_UP, mSoundPool == null");
							makeSound();
						}
						Log.e(TAG, "ACTION_UP, play sound");

						if (PreferenceManager.getDefaultSharedPreferences(
								getApplicationContext()).getBoolean(
								BasicSetting.KEY_SOUND_UNLOCK, true)) {
							playSound(SOUND_ID_TAB);
						}
						LockItem item = new LockItem();
						item.x = (int) event.getRawX();
						item.y = (int) event.getRawY();

						Log.e("tuyenpx", "item.x = " + item.x + " item.y = "
								+ item.y);

						arrayList.add(item);
						count--;
						if (count == 0) {
							if (checkUnlock(arrayList)) {
								// check to unlock
								removeview();
							} else {
								// rung
								arrayList.removeAll(arrayList);
								count = 3;
								Vibrator vs = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
								vs.vibrate(400);
								count_failed--;
								if (count_failed == 0) {
									count_failed = 3;
									// show password activity
									viewFlipper1.setInAnimation(mInfromLeft);
									viewFlipper1.setOutAnimation(mOutToRight);
									viewFlipper1.showNext();
								}
							}

						}

					}
					return true;
				}
				return false;
			}
		});
		mFloatViewContainer.addView(mFloatView);
		windowManager.addView(mFloatViewContainer, mFloatContainerParams);

	}

	private void initAnimations() {
		AccelerateInterpolator accelerateInterpolator = new AccelerateInterpolator();

		// IN from left :
		mInfromLeft = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
				-1.0f, Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		mInfromLeft.setDuration(500);
		mInfromLeft.setInterpolator(accelerateInterpolator);

		// OUT from right :
		mOutToRight = new TranslateAnimation(Animation.RELATIVE_TO_PARENT,
				0.0f, Animation.RELATIVE_TO_PARENT, +1.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f,
				Animation.RELATIVE_TO_PARENT, 0.0f);
		mOutToRight.setDuration(500);
		mOutToRight.setInterpolator(accelerateInterpolator);
	}

	private boolean checkUnlock(ArrayList<LockItem> arrayList) {

		SharedPreferences preferences = getApplicationContext()
				.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
						Context.MODE_MULTI_PROCESS);
		String s = preferences.getString(Utils.PICTURE_STRING, "");
		Log.e(TAG, "gia tri s = " + s);
		if (s.equals("")) {
			Log.e(TAG, "problem with save database , please check more");
			return true;
		} else {
			String[] s1 = s.split(";");
			for (int i = 0; i < 3; i++) {
				Log.e(TAG, "gia tri s1 = " + s1[i]);
				String[] s2 = s1[i].split(":");
				LockItem item = arrayList.get(i);
				Log.e(TAG, "gia tri item = " + item.toString());
				if (Math.abs(item.x - (int) Double.parseDouble(s2[0])) > 120
						|| (Math.abs(item.y - (int) Double.parseDouble(s2[1])) > 120)) {

					return false;
				}

			}

		}
		return true;
	}

	public void removeview() {
		mPasswd = "";
		txtenter.setText(mPasswd);
		arrayList.removeAll(arrayList);
		count = 3;
		count_failed = 3;
		releaseSound();
		isShow = false;
		mFloatViewContainer.removeAllViews();
		windowManager.removeView(mFloatViewContainer);
		unregisterReceiver(batteryStatusReceiver);
		unregisterReceiver(mUpdateTimeDate);
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
	}

	// Register for Lockscreen event intents
	@SuppressWarnings("deprecation")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (intent.getAction().equals("changebackground")) {
				setupview(intent.getStringExtra(Utils.CURRENTBACKGROUND),
						intent.getIntExtra(BasicSetting.COLOR_DIALOG, 0));
			}
		} catch (Exception e) {
		}
		return START_STICKY;
	}

	// Unregister receiver
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isShow) {
			removeview();
		}

		if (mFloatViewContainer != null) {
			mFloatViewContainer = null;
		}

		if (windowManager != null) {
			windowManager = null;
		}

		if (mFloatView != null) {
			mFloatView = null;
		}
		enableKeyguard();

		unregisterReceiver(mBroadcastReceiver);
	}

	// http://stackoverflow.com/questions/22382148/0-processes-and-1-service-under-settings-apps-and-runnings
	@Override
	public void onTaskRemoved(Intent rootIntent) {
		Intent restartServiceIntent = new Intent(getApplicationContext(),
				this.getClass());
		restartServiceIntent.setPackage(getPackageName());

		PendingIntent restartServicePendingIntent = PendingIntent.getService(
				getApplicationContext(), 1, restartServiceIntent,
				PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmService = (AlarmManager) getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmService.set(AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + 1000,
				restartServicePendingIntent);

		super.onTaskRemoved(rootIntent);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			final boolean flag = PreferenceManager.getDefaultSharedPreferences(
					context).getBoolean(BasicSetting.KEY_USE_SMART_LOCKSCREEN,
					true);

			if (action.equals(Intent.ACTION_SCREEN_ON)) {

				TelephonyManager tm = (TelephonyManager) context
						.getSystemService(Service.TELEPHONY_SERVICE);
				int callState = tm.getCallState();
				if (flag && callState != TelephonyManager.CALL_STATE_RINGING
						&& callState != TelephonyManager.CALL_STATE_OFFHOOK) {
					setupview(null, 0);
				}
			} else {
				if (flag && isShow) {
					removeview();
				}
			}
		}
	};

	@SuppressLint("InlinedApi")
	@SuppressWarnings("deprecation")
	private void makeSound() {
		Log.d(TAG, "tran.thang makeSound");
		boolean flag = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).getBoolean(
				BasicSetting.KEY_SOUND_UNLOCK, true);
		if (flag) {
			if (mSoundPool == null) {
				sounds = new int[3];
				if ((android.os.Build.VERSION.SDK_INT) == 21) {
					AudioAttributes attr = new AudioAttributes.Builder()
							.setUsage(
									AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
							.setContentType(
									AudioAttributes.CONTENT_TYPE_SONIFICATION)
							.build();

					mSoundPool = new SoundPool.Builder().setMaxStreams(10)
							.setAudioAttributes(attr).build();
				} else {
					mSoundPool = new SoundPool(5, AudioManager.STREAM_MUSIC, 0);
				}

				mSoundPool
						.setOnLoadCompleteListener(new OnLoadCompleteListener() {

							@Override
							public void onLoadComplete(SoundPool arg0,
									int arg1, int arg2) {
								// TODO Auto-generated method stub
								loaded = true;
							}
						});
				sounds[SOUND_ID_TAB] = mSoundPool.load(mContext,
						R.raw.particle_tap, 1);
				sounds[SOUND_ID_DRAG] = mSoundPool.load(mContext,
						R.raw.particle_drag, 1);
				sounds[SOUND_ID_UNLOCK] = mSoundPool.load(mContext,
						R.raw.lens_flare_unlock, 1);
			}
		}
	}

	private void playSound(int soundId) {
		Log.e(TAG, "tuyenpx playSound() - loaded = " + loaded);
		if (loaded && mSoundPool != null) {
			Log.e(TAG, "tuyenpx playSound() - soundId = " + soundId);
			mSoundPool.play(sounds[soundId], leftVolumeMax, rightVolumeMax, 0,
					0, 1.0f);
		}
	}

	private void releaseSound() {
		if (mSoundPool != null) {
			mSoundPool.release();
			mSoundPool = null;
		}
	}

	private void settimedate() {
		c = Calendar.getInstance();

		sdf = new SimpleDateFormat("hh:mm a");
		String time = sdf.format(c.getTime());
		mTime.setText(time.substring(0, time.length() - 3));
		mNoon.setText(time.substring(time.length() - 2));

		sdf = new SimpleDateFormat("EE, d  MMM yyyy");
		String date = sdf.format(c.getTime());
		mDate.setText(date.substring(0, date.length() - 5));
	}

	private void setDateTime() {
		mTimerUpdate = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				settimedate();

			}
		};
	}

	private void disableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.disableKeyguard();
	}

	@SuppressWarnings("deprecation")
	private void enableKeyguard() {
		KeyguardManager mKM = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
		KeyguardManager.KeyguardLock mKL = mKM.newKeyguardLock("IN");
		mKL.reenableKeyguard();
	}

	private int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}

	public Bitmap decodeSampledBitmapFromResource(int resId, int reqWidth,
			int reqHeight) {
		if (bitmap != null) {
			bitmap.recycle();
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeResource(getResources(), resId, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeResource(getResources(), resId, options);
		Log.e("size", "leng bitmap = " + sizeOf(bitmap));
		return bitmap;
	}

	private Bitmap decodeSampledBitmapFromResource(String pathName,
			int reqWidth, int reqHeight) {
		if (bitmap != null) {
			bitmap.recycle();
		}

		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(pathName, options);
		options.inSampleSize = calculateInSampleSize(options, reqWidth,
				reqHeight);
		options.inJustDecodeBounds = false;
		bitmap = BitmapFactory.decodeFile(pathName, options);
		Log.e("size", "leng bitmap = " + sizeOf(bitmap));
		return bitmap;
	}

	public int sizeOf(Bitmap data) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB_MR1) {
			return data.getRowBytes() * data.getHeight();
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			return data.getByteCount();
		} else {
			return data.getAllocationByteCount();
		}
	}

	public void deleteSymbol() {
		if (mPasswd.length() <= 1) {
			mPasswd = "";
			txtenter.setText(mPasswd);
		} else {
			mPasswd = mPasswd.substring(0, mPasswd.length() - 1);
			txtenter.setText(mPasswd);
		}
	}

	public void checkOK() {
		SharedPreferences preferences = getApplicationContext()
				.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
						Context.MODE_MULTI_PROCESS);
		String pass = preferences.getString(Utils.PASSWORD, "");
		if (mPasswd.trim().equals(pass.trim())) {
			removeview();
		}

	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		switch (id) {
		case R.id.b0:
		case R.id.b1:
		case R.id.b2:
		case R.id.b3:
		case R.id.b4:
		case R.id.b5:
		case R.id.b6:
		case R.id.b7:
		case R.id.b8:
		case R.id.b9:
			addSymbol(((Button) v).getText().toString().trim() + "");
			checkOK();
			break;
		case R.id.back:
			deleteSymbol();
			break;
		case R.id.button1:
			// show password activity
			viewFlipper1.setInAnimation(mInfromLeft);
			viewFlipper1.setOutAnimation(mOutToRight);
			viewFlipper1.showNext();
			break;
		default:
			break;
		}

	}
}
