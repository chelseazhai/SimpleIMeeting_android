package com.richitec.simpleimeeting;

import java.util.HashMap;
import java.util.Map;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.richitec.commontoolkit.activityextension.AppLaunchActivity;
import com.richitec.commontoolkit.addressbook.AddressBookManager;
import com.richitec.commontoolkit.user.UserBean;
import com.richitec.commontoolkit.user.UserManager;
import com.richitec.commontoolkit.utils.DataStorageUtils;
import com.richitec.commontoolkit.utils.DeviceUtils;
import com.richitec.commontoolkit.utils.HttpUtils;
import com.richitec.commontoolkit.utils.HttpUtils.HttpRequestType;
import com.richitec.commontoolkit.utils.HttpUtils.PostRequestFormat;
import com.richitec.simpleimeeting.assistant.httprequestlistener.BindedAccountLoginHttpRequestListener;
import com.richitec.simpleimeeting.assistant.httprequestlistener.RegAndLoginWithDeviceIdHttpRequestListener;
import com.richitec.simpleimeeting.content.ContactsSelectView;
import com.richitec.simpleimeeting.content.SimpleIMeetingActivity;
import com.richitec.simpleimeeting.extension.user.SIMUserExtension;
import com.richitec.simpleimeeting.extension.user.SIMUserExtension.ComUserAttributes;
import com.richitec.simpleimeeting.extension.user.SIMUserExtension.SIMUserExtAttributes;

public class SimpleIMeetingAppLaunchActivity extends AppLaunchActivity {

	private static final String LOG_TAG = SimpleIMeetingAppLaunchActivity.class
			.getCanonicalName();

	// simple iMeeting application launch toast
	private Toast _mSimpleIMeetingAppLaunchToast;

	// target intent activity for simple iMeeting application
	private Intent _mIntentActivity;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// init simple iMeeting application launch toast
		_mSimpleIMeetingAppLaunchToast = Toast.makeText(this, "",
				Toast.LENGTH_LONG);
	}

	@Override
	public void onBackPressed() {
		// exit simple imeeting project
		System.exit(0);
	}

	@Override
	public Drawable splashImg() {
		return getResources().getDrawable(R.drawable.ic_splash);
	}

	@Override
	public Intent intentActivity() {
		// check the got intent activity and set target intent
		if (null == _mIntentActivity) {
			// target intent activity, simple iMeeting main activity
			_mIntentActivity = new Intent(this, SimpleIMeetingActivity.class);
		}

		return _mIntentActivity;
	}

	@Override
	public boolean didFinishLaunching() {
		// traversal address book
		AddressBookManager.getInstance().traversalAddressBook();

		// init all name phonetic sorted contacts info array
		ContactsSelectView.initNamePhoneticSortedContactsInfoArray();

		// get binded account login user info from storage and add to user
		// manager
		UserBean _localStorageUser = new UserBean();

		// set bind contact info and password
		_localStorageUser.setPassword(DataStorageUtils
				.getString(ComUserAttributes.PASSWORD.name()));
		SIMUserExtension
				.setUserBindContactInfo(
						_localStorageUser,
						DataStorageUtils
								.getString(SIMUserExtAttributes.BIND_CONTACTINFO
										.name()));

		// save user bean and add to user manager
		UserManager.getInstance().setUser(_localStorageUser);

		// get local storage user bind contact info and password
		String _bindContactInfo = SIMUserExtension
				.getUserBindContactInfo(_localStorageUser);
		String _password = _localStorageUser.getPassword();

		// check user bind contact info and password
		if (null != _bindContactInfo && !"".equalsIgnoreCase(_bindContactInfo)
				&& null != _password && !"".equalsIgnoreCase(_password)) {
			// binded account user login
			// generate binded account login param map
			Map<String, String> _bindedAccountLoginParamMap = new HashMap<String, String>();

			// set some params
			_bindedAccountLoginParamMap.put(
					getResources().getString(R.string.rbgServer_userLoginName),
					SIMUserExtension.getUserBindContactInfo(_localStorageUser));
			_bindedAccountLoginParamMap.put(
					getResources().getString(R.string.rbgServer_userLoginPwd),
					_localStorageUser.getPassword());

			// post the http request
			try {
				HttpUtils.postRequest(
						getResources().getString(R.string.server_url)
								+ getResources().getString(R.string.login_url),
						PostRequestFormat.URLENCODED,
						_bindedAccountLoginParamMap, null,
						HttpRequestType.SYNCHRONOUS,
						new BindedAccountLoginHttpRequestListener(
								SimpleIMeetingAppLaunchActivity.this));
			} catch (Exception e) {
				Log.e(LOG_TAG,
						"Send binded account login post http request error, exception message = "
								+ e.getMessage());

				e.printStackTrace();
			}
		} else {
			// register and login using device combined unique id
			sendReg7LoginWithDeviceCombinedUniqueIdHttpRequest();
		}

		return true;
	}

	// update target intent activity
	public void updateIntentActivity(Intent intentActivity) {
		_mIntentActivity = intentActivity;
	}

	public Toast getSimpleIMeetingAppLaunchToast() {
		return _mSimpleIMeetingAppLaunchToast;
	}

	// send register and login with device combined unique id post http request
	public void sendReg7LoginWithDeviceCombinedUniqueIdHttpRequest() {
		// generate register and login with device combined unique id param map
		Map<String, String> _reg7LoginWithDeviceIdParamMap = new HashMap<String, String>();

		// set some params
		_reg7LoginWithDeviceIdParamMap
				.put(getResources()
						.getString(
								R.string.rbgServer_reg7LoginWithDeviceId6ContactInfoBind_deviceId),
						DeviceUtils.combinedUniqueId());

		// post the http request
		try {
			HttpUtils.postRequest(
					getResources().getString(R.string.server_url)
							+ getResources().getString(
									R.string.reg7login_withDeviceId_url),
					PostRequestFormat.URLENCODED,
					_reg7LoginWithDeviceIdParamMap, null,
					HttpRequestType.SYNCHRONOUS,
					new RegAndLoginWithDeviceIdHttpRequestListener(
							SimpleIMeetingAppLaunchActivity.this));
		} catch (Exception e) {
			Log.e(LOG_TAG,
					"Send register and login with device combined id post http request error, exception message = "
							+ e.getMessage());

			e.printStackTrace();
		}
	}

}
