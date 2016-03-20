package xcxin.filexpert;

import com.mobclick.android.MobclickAgent;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class NewSettings extends PreferenceActivity implements
		OnSharedPreferenceChangeListener, OnClickListener {

	private SharedPreferences prefs;
	private FileExpertSettings mSettings;
	SharedPreferences.Editor mEditor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSettings = new FileExpertSettings(this);
		addPreferencesFromResource(R.xml.new_settings);
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		mEditor = prefs.edit();
		setValues();
		getPreferenceScreen().removeAll();
		addPreferencesFromResource(R.xml.new_settings);
		prefs.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setValues();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onResume(this);
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onPause(this);
		}
	}

	private void setValues() {
		mEditor.putBoolean(FileExpertSettings.AUTO_SDCARD,
				mSettings.getAutoSdCard());
		mEditor.putBoolean(FileExpertSettings.FE_SAVE_MUL,
				mSettings.isMulSelectEnabled());
		mEditor.putBoolean(FileExpertSettings.DEF_LIST_MODE,
				mSettings.getDefListMode());
		mEditor.putBoolean(FileExpertSettings.SIMPLE_LIST,
				mSettings.isSimpleList());
		mEditor.putBoolean(FileExpertSettings.SHOW_HIDDEN_DIRS,
				mSettings.isShowHiddenDirs());
		mEditor.putBoolean(FileExpertSettings.HTTP_WAKELOCK,
				mSettings.getHttpWakelock());
		mEditor.putBoolean(FileExpertSettings.FTP_WAKELOCK,
				mSettings.getFtpWakelock());
		mEditor.putBoolean(FileExpertSettings.FE_THUMBNAILS,
				mSettings.isShowThumbnails());
		mEditor.putBoolean(FileExpertSettings.FE_APK_ICON,
				mSettings.isShowApkIcon());
		mEditor.putBoolean(FileExpertSettings.FE_WEB_PAGE_INDEX,
				mSettings.isWebPageIndex());
		mEditor.putBoolean(FileExpertSettings.FE_ALL_APP,
				mSettings.isShowAllApp());
		mEditor.putBoolean(FileExpertSettings.FE_NOTIFY_ICON,
				mSettings.isShowNotifyIcon());
		mEditor.putBoolean(FileExpertSettings.FE_WEB_LOGIN,
				mSettings.isWebLoginEnable());
		
		int value = mSettings.getFtpPort();
		mEditor.putString(FileExpertSettings.FTP_PORT + "String",
				String.valueOf(value));
		value = mSettings.getHttpPort();
		mEditor.putString(FileExpertSettings.HTTP_PORT + "String",
				String.valueOf(value));

		mEditor.putString(FileExpertSettings.FTP_USER_NAME,
				mSettings.getFtpUserName());
		mEditor.putString(FileExpertSettings.FTP_PASSWORD,
				mSettings.getFtpPassword());

		mEditor.putString(FileExpertSettings.FE_WEB_USERNAME,
				mSettings.getWebUsername());
		mEditor.putString(FileExpertSettings.FE_WEB_PASSWORD,
				mSettings.getWebPassword());

		mEditor.putString(FileExpertSettings.SMB_STREAM_BUF_SIZE,
				mSettings.getSmbStreamBufSize());

		mEditor.putString(FileExpertSettings.FE_FTP_ENCODING,
				mSettings.getFtpEncoding());

		mEditor.putBoolean(FileExpertSettings.FE_SLIENT_APK_INSTALL,
				mSettings.isSlientInstallApk());

		mEditor.putString(FileExpertSettings.FE_APK_INSTALL_LOCATION,
				mSettings.getDefaultApkInstallLocation());

		mEditor.putBoolean(FileExpertSettings.FE_SLIENT_APK_UNINSTALL,
				mSettings.isSlientUninstallApk());

		mEditor.commit();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.compareTo(FileExpertSettings.AUTO_SDCARD) == 0) {
			mSettings.setAutoSdCard(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.FE_SAVE_MUL) == 0){
			mSettings.setSaveMulSelect(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.DEF_LIST_MODE) == 0) {
			mSettings.setDefListMode(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.SIMPLE_LIST) == 0) {
			mSettings.setSimpleList(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.SHOW_HIDDEN_DIRS) == 0) {
			mSettings
					.setShowHiddenDirs(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.HTTP_WAKELOCK) == 0) {
			mSettings.setHttpWakelock(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.FTP_WAKELOCK) == 0) {
			mSettings.setFtpWakelock(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.FE_THUMBNAILS) == 0) {
			mSettings.setThumbnailsState(sharedPreferences
					.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.FE_APK_ICON) == 0) {
			mSettings.setShowApkIcon(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.FE_WEB_PAGE_INDEX) == 0) {
			mSettings.setWebPageIndex(sharedPreferences.getBoolean(key, false));
		} else if (key.compareTo(FileExpertSettings.FE_WEB_LOGIN) == 0) {
			mSettings.setWebLoginState(sharedPreferences.getBoolean(key, true));
		} else if (key.compareTo(FileExpertSettings.FE_ALL_APP) == 0) {
			mSettings.setShowAllApp(sharedPreferences.getBoolean(key, false));
		} else if (key.compareTo(FileExpertSettings.FE_NOTIFY_ICON) == 0) {
			mSettings.setShowNotifyIconState(sharedPreferences.getBoolean(key,
					false));
		} else if (key.compareTo(FileExpertSettings.FTP_USER_NAME) == 0) {
			mSettings.setFtpUserName((sharedPreferences.getString(key, "fe")));
		} else if (key.compareTo(FileExpertSettings.FTP_PASSWORD) == 0) {
			mSettings.setFtpPassword((sharedPreferences.getString(key,
					"filexpert")));
		} else if (key.compareTo(FileExpertSettings.FE_WEB_USERNAME) == 0) {
			mSettings.setWebUsername((sharedPreferences.getString(key, "fe")));
		} else if (key.compareTo(FileExpertSettings.FE_WEB_PASSWORD) == 0) {
			mSettings.setWebPassword((sharedPreferences.getString(key,
					"filexpert")));
		} else if (key.compareTo(FileExpertSettings.FTP_PORT + "String") == 0) {
			String fport = sharedPreferences.getString(key, "2211");
			try {
				int port = Integer.parseInt(fport);
				if ((port < 1024 || (port >= 4000 && port <= 4199))) {
					showInfo(getString(R.string.port_root_warning),
							getString(R.string.warning));
				}
				mSettings.setFtpPort(port);
			} catch (Exception e) {
				showInfo(getString(R.string.input_correct_value),
						getString(R.string.warning));
				mSettings.setFtpPort(2211);
			}
		} else if (key.compareTo(FileExpertSettings.HTTP_PORT + "String") == 0) {
			String hport = sharedPreferences.getString(key, "8080");
			try {
				int port = Integer.parseInt(hport);
				if (port < 1024) {
					showInfo(getString(R.string.port_root_warning),
							getString(R.string.warning));
				}
				mSettings.setHttpPort(port);
			} catch (Exception e) {
				showInfo(getString(R.string.input_correct_value),
						getString(R.string.warning));
				mSettings.setHttpPort(8080);
			}
		} else if (key.compareTo(FileExpertSettings.SMB_STREAM_BUF_SIZE) == 0) {
			mSettings.setSmbStreamBufSize((sharedPreferences.getString(key,
					"524288")));
		} else if (key.compareTo(FileExpertSettings.FE_FTP_ENCODING) == 0) {
			mSettings.setFtpEncoding(sharedPreferences.getString(
					FileExpertSettings.FE_FTP_ENCODING, "UTF-8"));
		} else if (key.compareTo(FileExpertSettings.FE_SLIENT_APK_INSTALL) == 0) {
			mSettings.setSlientInstallApk(sharedPreferences.getBoolean(key,
					false));
		} else if (key.compareTo(FileExpertSettings.FE_APK_INSTALL_LOCATION) == 0) {
			mSettings.setDefaultApkInstallLocation(sharedPreferences.getString(
					FileExpertSettings.FE_APK_INSTALL_LOCATION,
					getString(R.string.apk_auto)));
		} else if (key.compareTo(FileExpertSettings.FE_SLIENT_APK_UNINSTALL) == 0) {
			mSettings.setSlientUninstallApk(sharedPreferences.getBoolean(key,
					false));
		}
	}

	public void showInfo(String info, String title) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		builder.setNegativeButton(getString(R.string.Okay), this);
		builder.create();
		builder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
	}
}