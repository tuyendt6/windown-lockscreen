package com.bk.setting;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.bk.lockscreen.R;
import com.bk.lockscreen.utils.LockscreenService;
import com.bk.lockscreen.utils.Utils;

public class BasicSetting extends PreferenceFragment {

	private Preference mPreDisplayTimeOut;
	private Preference mPreWallPaper;
	private CheckBoxPreference mUseSmartLockScreen;
	private CheckBoxPreference mSoundUnLock;
	private Preference mReset2Default;
	private ColorPickerPreference mColorPickerPreference;

	private static final int CHANGE_BACK_GROUND = 1404;

	public static final String KEY_USE_SMART_LOCKSCREEN = "pref_use_lockscreen";
	private static final String KEY_CHANGE_WALLPAPER = "pref_wallpaper";
	public static final String KEY_SOUND_UNLOCK = "pref_sound_unlock";
	public static final String KEY_RESET_2_DEFAULT = "pref_reset2default";
	private static final String TAG = "BasicSetting";
	public static final String COLOR_DIALOG = "color";

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		addPreferencesFromResource(R.xml.pref_basic_setting);
		mPreWallPaper = (Preference) findPreference(KEY_CHANGE_WALLPAPER);
		mUseSmartLockScreen = (CheckBoxPreference) findPreference(KEY_USE_SMART_LOCKSCREEN);
		mColorPickerPreference = (ColorPickerPreference) findPreference(COLOR_DIALOG);
		mSoundUnLock = (CheckBoxPreference) findPreference(KEY_SOUND_UNLOCK);
		mReset2Default = (Preference) findPreference(KEY_RESET_2_DEFAULT);
		mReset2Default
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						AlertDialog alertDialog = new AlertDialog.Builder(
								getActivity())
								.setTitle(
										getResources().getString(
												R.string.reset_to_default))
								.setMessage(
										getResources().getString(
												R.string.ques_reset_to_defaut))
								.setPositiveButton(
										getResources().getString(R.string.yes),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {
												reset2defalt();

											}
										})
								.setNegativeButton(
										getResources().getString(R.string.no),
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface arg0,
													int arg1) {

											}
										}).create();
						alertDialog.getWindow().setType(
								WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);

						alertDialog.show();
						return false;
					}
				});
		mUseSmartLockScreen.setChecked(PreferenceManager
				.getDefaultSharedPreferences(getActivity()).getBoolean(
						KEY_USE_SMART_LOCKSCREEN, true));
		mUseSmartLockScreen
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {

						boolean flag = PreferenceManager
								.getDefaultSharedPreferences(
										getActivity().getApplicationContext())
								.getBoolean(KEY_USE_SMART_LOCKSCREEN, true);
						if (flag) {
							Intent i = new Intent(getActivity(),
									LockscreenService.class);
							getActivity().startService(i);
						} else {
							Intent i = new Intent(getActivity(),
									LockscreenService.class);
							getActivity().stopService(i);
						}
						return false;
					}
				});
		mSoundUnLock.setChecked(PreferenceManager.getDefaultSharedPreferences(
				getActivity().getApplicationContext()).getBoolean(
				KEY_SOUND_UNLOCK, true));

		mSoundUnLock
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference arg0) {
						SharedPreferences preferences = PreferenceManager
								.getDefaultSharedPreferences(getActivity()
										.getApplicationContext());
						boolean flag = preferences.getBoolean(KEY_SOUND_UNLOCK,
								true);
						Editor editor = preferences.edit();
						editor.putBoolean(KEY_SOUND_UNLOCK, !flag);
						editor.commit();
						return false;
					}
				});

		mPreWallPaper
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						// TODO Auto-generated method stub
						Intent i = new Intent(
								Intent.ACTION_PICK,
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
						startActivityForResult(i, CHANGE_BACK_GROUND);
						return false;
					}
				});
		mPreDisplayTimeOut = (Preference) findPreference("pref_time_out");
		mPreDisplayTimeOut
				.setOnPreferenceClickListener(new OnPreferenceClickListener() {

					@Override
					public boolean onPreferenceClick(Preference preference) {
						Intent mIntent = new Intent(getActivity(),
								LockSetupActivity.class);
						mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						mIntent.addFlags(WindowManager.LayoutParams.FIRST_APPLICATION_WINDOW);
						startActivity(mIntent);
						return false;
					}
				});
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	private void reset2defalt() {
		Intent intent = new Intent(getActivity(), LockscreenService.class);
		intent.setAction("changebackground");
		intent.putExtra(Utils.CURRENTBACKGROUND, "");
		intent.putExtra(BasicSetting.COLOR_DIALOG, -262401);
		getActivity().startService(intent);
		getActivity().finish();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHANGE_BACK_GROUND) {
			if (resultCode == getActivity().RESULT_OK) {
				Uri selectedImage = data.getData();
				String[] filePathColumn = { MediaStore.Images.Media.DATA };
				Cursor c = getActivity().getContentResolver().query(
						selectedImage, filePathColumn, null, null, null);
				c.moveToFirst();
				String imageUri = c.getString(c
						.getColumnIndexOrThrow(filePathColumn[0]));
				c.close();
				Intent intent = new Intent(getActivity(),
						LockscreenService.class);
				intent.setAction("changebackground");
				intent.putExtra(Utils.CURRENTBACKGROUND, imageUri);
				getActivity().startService(intent);
				getActivity().finish();

			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
}
