package com.bk.setting;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.bk.lockscreen.R;
import com.bk.lockscreen.utils.Utils;

public class LockSetupActivity extends Activity implements OnClickListener {

	private Bitmap bitmap;
	private View relative;
	private ViewFlipper viewFlipper1;
	private Animation mInfromLeft;
	private Animation mOutToRight;
	private int count = 3;
	private String s = "";
	private Button btnsave;
	private EditText txtenter;

	private Button btn0, btn1, btn2, btn3, btn4, btn5, btn6, btn7, btn8, btn9,
			btnclear;
	private String mPasswd = "";

	public void addSymbol(String symbol) {
		mPasswd = mPasswd + symbol;
		txtenter.setText(mPasswd);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.setuplockscreen);
		relative = (View) findViewById(R.id.background);
		btnsave = (Button) findViewById(R.id.button1);

		btn0 = (Button) findViewById(R.id.b0);
		btn0.setOnClickListener(this);
		btn1 = (Button) findViewById(R.id.b1);
		btn1.setOnClickListener(this);
		btn2 = (Button) findViewById(R.id.b2);
		btn2.setOnClickListener(this);
		btn3 = (Button) findViewById(R.id.b3);
		btn3.setOnClickListener(this);
		btn4 = (Button) findViewById(R.id.b4);
		btn4.setOnClickListener(this);
		btn5 = (Button) findViewById(R.id.b5);
		btn5.setOnClickListener(this);
		btn6 = (Button) findViewById(R.id.b6);
		btn6.setOnClickListener(this);
		btn7 = (Button) findViewById(R.id.b7);
		btn7.setOnClickListener(this);
		btn8 = (Button) findViewById(R.id.b8);
		btn8.setOnClickListener(this);
		btn9 = (Button) findViewById(R.id.b9);
		btn9.setOnClickListener(this);

		btnclear = (Button) findViewById(R.id.back);
		btnclear.setOnClickListener(this);

		txtenter = (EditText) findViewById(R.id.pin);
		btnsave.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (txtenter.getText().toString().trim().equals("")) {
					Toast.makeText(getApplicationContext(), "please try again",
							Toast.LENGTH_SHORT).show();

				} else {
					SharedPreferences preferences = getApplicationContext()
							.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
									Context.MODE_MULTI_PROCESS);
					Editor editor = preferences.edit();
					editor.putString(Utils.PASSWORD, txtenter.getText()
							.toString().trim());
					editor.commit();
					finish();
					Toast.makeText(getApplicationContext(),
							"password is saved", Toast.LENGTH_SHORT).show();
				}

			}
		});
		viewFlipper1 = (ViewFlipper) findViewById(R.id.view_flipper1);
		viewFlipper1.setDisplayedChild(0);
		initAnimations();

		SharedPreferences preferencesss = getApplicationContext()
				.getSharedPreferences(Utils.BACKGROUNDPREFERENCE,
						Context.MODE_PRIVATE);
		String path = preferencesss.getString(Utils.CURRENTBACKGROUND, "");
		DisplayMetrics displaymetrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
		int height = displaymetrics.heightPixels;
		int width = displaymetrics.widthPixels;
		if (path.trim().equals("")) {
			BitmapDrawable ob = new BitmapDrawable(getResources(),
					decodeSampledBitmapFromResource(R.drawable.face, width,
							height));
			relative.setBackground(ob);
		} else {
			BitmapDrawable ob = new BitmapDrawable(getResources(),
					decodeSampledBitmapFromResource(path, width, height));
			relative.setBackground(ob);
		}

		relative.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View arg0, MotionEvent event) {
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:

					float initialTouchX = event.getRawX();
					float initialTouchY = event.getRawY();

					if (count == 3) {
						s = "";
					}

					Log.e("tuyenpx", "count = " + count);
					if (count == 1) {
						Log.e("tuyenpx", "s1 = " + s);
						s = s + initialTouchX + ":" + initialTouchY;
						Log.e("tuyenpx", "s2 = " + s);
						SharedPreferences preferences = getApplicationContext()
								.getSharedPreferences(
										Utils.BACKGROUNDPREFERENCE,
										Context.MODE_MULTI_PROCESS);
						Editor editor = preferences.edit();
						editor.putString(Utils.PICTURE_STRING, s);
						editor.commit();

						count = 3;
						viewFlipper1.setInAnimation(mInfromLeft);
						viewFlipper1.setOutAnimation(mOutToRight);
						viewFlipper1.showNext();

					}
					if (count == 3 || count == 2) {
						s = s + initialTouchX + ":" + initialTouchY + ";";
					}

					count--;

					return true;
				}
				return false;
			}
		});

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

		final GestureDetector gestureDetector;
		gestureDetector = new GestureDetector(new MyGestureDetector());

		viewFlipper1.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return false;
				} else {
					return true;
				}
			}
		});
	}

	private class MyGestureDetector extends
			GestureDetector.SimpleOnGestureListener {
		private int swipe_Min_Distance = 100;
		private int swipe_Min_Velocity = 100;

		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			final float xDistance = Math.abs(e1.getX() - e2.getX());
			final float yDistance = Math.abs(e1.getY() - e2.getY());
			velocityX = Math.abs(velocityX);
			velocityY = Math.abs(velocityY);
			if (velocityX > this.swipe_Min_Velocity
					&& xDistance > this.swipe_Min_Distance) {
				if (e1.getX() > e2.getX()) {
					viewFlipper1.setInAnimation(mInfromLeft);
					viewFlipper1.setOutAnimation(mOutToRight);
					viewFlipper1.showNext();
				} else {
					viewFlipper1.setInAnimation(mInfromLeft);
					viewFlipper1.setOutAnimation(mOutToRight);
					viewFlipper1.showNext();
				}
			}
			return true;
		}
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (bitmap != null) {
			bitmap.recycle();
			bitmap = null;
		}
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
			break;
		case R.id.back:
			deleteSymbol();
			break;
		default:
			break;
		}

	}

}
