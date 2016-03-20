package xcxin.filexpert;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

public class FileExpertSettings {
	SharedPreferences mSettings;
	SharedPreferences.Editor mEditor;
	String defApkLocation;
	
	public static final String SETTINGS_NAME = "FileExpertSettings";
	public static final String AUTO_SDCARD = "FileExpertAutoSdCard";
	public static final String DEF_LIST_MODE = "FileExpertDefaultListMode";
	public static final String HTTP_WAKELOCK = "FileExpertHttpWakelock";
	public static final String FTP_WAKELOCK = "FileExpertFtpWakelock";
	public static final String HTTP_PORT = "FileExpertHttpPort";
	public static final String START_DIR = "FileExpertStartDir";
	public static final String HTTP_STREAM_NAME = "FileExpertHttpStreamName";
	public static final String SORT_METHOD_NAME = "FileExpertSort";
	public static final String FTP_USER_NAME = "FileExpertFtpUserName";
	public static final String FTP_PASSWORD = "FileExpertFtpUserPassword";
	public static final String FTP_PORT = "FileExpertFtpPort";
	public static final String SMB_STREAM_BUF_SIZE = "FileExpertSmbStreamBufSize";
	public static final String SMB_STREAM_PATH = "FileExpertSmbStreamPath";
	public static final String SMB_STREAM_NAME = "FileExpertSmbStreamName";
	public static final String SMB_STREAM_SERVER_START_FLAG = "FileExpertSmbServerStartFlag";
	public static final String DEFAULE_SHARING_DIR = "FileExpertDefaultSharingDir";
	public static final String SIMPLE_LIST = "FileExpertSimpleList";
	public static final String INFO_COLLECT="FileExpertInfoCollect";
	public static final String INFO_COLLECT_ASK="FileExpertInfoCollectAsk";
	public static final String SHARING_DIR="FileExpertSharingDir";
	public static final String DONATE_DLG_APPEAR ="FileExpertDonateDlg";
	public static final String DONATE = "FileExpertDonate";
	public static final String SHOW_HIDDEN_DIRS ="FileExpertShowHiddenDirs";
	public static final String FE_VERSION ="FileExpertLastVersion";
	public static final String FE_FTP_ENCODING="FileExpertFtpEncoding";
	public static final String FE_SLIENT_APK_INSTALL="FileExpertSlientInstallApk";
	public static final String FE_APK_INSTALL_LOCATION="FileExpertApkInstallLocation";
	public static final String FE_SLIENT_APK_UNINSTALL="FileExpertSlientUninstallApk";
	public static final String FE_SHOW_TABS="FileExpertShowTabs";
	public static final String FE_SHOW_TOOLBAR="FileExpertShowToolBar";
	public static final String FE_THUMBNAILS="FileExpertThumbnails";
	public static final String FE_APK_ICON="FileExpertApkIcon";
	public static final String FE_WEB_PAGE_INDEX="FileExpertWebPageIndex";
	public static final String FE_ALL_APP="FileExpertShowAllApp";
	public static final String FE_NOTIFY_ICON="FileExpertNotifyIcon";
	public static final String FE_WEB_USERNAME="FileExpertWebUsername";
	public static final String FE_WEB_PASSWORD="FileExpertWebPassword";
	public static final String FE_WEB_LOGIN="FileExpertWebLogin";
	public static final String FE_SAVE_MUL="FileExpertMultipleSelect";
	public static final String FE_WEB_READ_ONLY="FileExpertWebReadOnly";
	
	public FileExpertSettings(Activity act) {
		mSettings = act.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		mEditor = mSettings.edit();
		defApkLocation = act.getString(R.string.apk_auto);
	}

	public FileExpertSettings(Service service) {
		mSettings = service.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		mEditor = mSettings.edit();
		defApkLocation = service.getString(R.string.apk_auto);
	}

	public FileExpertSettings(Context ctx) {
		mSettings = ctx.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		mEditor = mSettings.edit();
		defApkLocation = ctx.getString(R.string.apk_auto);
	}

	public void commit() {
		mEditor.commit();
	}

	public void setAutoSdCard(boolean state) {
		mEditor.putBoolean(AUTO_SDCARD, state);
		mEditor.commit();
	}

	public boolean getAutoSdCard() {
		return mSettings.getBoolean(AUTO_SDCARD, true);
	}
	
	public void setSaveMulSelect(boolean state) {
		mEditor.putBoolean(FE_SAVE_MUL, state);
		mEditor.commit();
	}
	
	public boolean isMulSelectEnabled(){
		return mSettings.getBoolean(FE_SAVE_MUL, false);
	}
	
	public void setShowTabsState(boolean state) {
		mEditor.putBoolean(FE_SHOW_TABS, state);
		mEditor.commit();
	}

	public boolean isTabsShow() {
		return mSettings.getBoolean(FE_SHOW_TABS, true);
	}

	public void setShowToolBarState(boolean state) {
		mEditor.putBoolean(FE_SHOW_TOOLBAR, state);
		mEditor.commit();
	}

	public boolean isToolBarShow() {
		return mSettings.getBoolean(FE_SHOW_TOOLBAR, true);
	}
	
	public void setShowHiddenDirs(boolean state) {
		mEditor.putBoolean(SHOW_HIDDEN_DIRS, state);
		mEditor.commit();
	}

	public boolean isShowHiddenDirs() {
		return mSettings.getBoolean(SHOW_HIDDEN_DIRS, true);
	}
	
	public void setSimpleList(boolean state) {
		mEditor.putBoolean(SIMPLE_LIST, state);
		mEditor.commit();
	}

	public boolean isInfoCollectAllowed() {
		return mSettings.getBoolean(INFO_COLLECT, false);
	}

	public void setInfoCollectStatus(boolean state) {
		mEditor.putBoolean(INFO_COLLECT, state);
		mEditor.commit();
	}

	public boolean isInfoCollectAsked() {
		return mSettings.getBoolean(INFO_COLLECT_ASK, false);
	}
	
	public void setInfoCollectAsked() {
		mEditor.putBoolean(INFO_COLLECT_ASK, true);
		mEditor.commit();
	}

	public boolean isDonateAsked() {
		return mSettings.getBoolean(DONATE_DLG_APPEAR, false);
	}
	
	public void setDonateAsked() {
		mEditor.putBoolean(DONATE_DLG_APPEAR, true);
		mEditor.commit();
	}

	public boolean isShowThumbnails() {
		return mSettings.getBoolean(FE_THUMBNAILS, true);
	}
	
	public void setThumbnailsState(boolean state) {
		mEditor.putBoolean(FE_THUMBNAILS, state);
		mEditor.commit();
	}

	public boolean isShowApkIcon() {
		return mSettings.getBoolean(FE_APK_ICON, true);
	}
	
	public void setShowApkIcon(boolean state) {
		mEditor.putBoolean(FE_APK_ICON, state);
		mEditor.commit();
	}

	public boolean isWebPageIndex() {
		return mSettings.getBoolean(FE_WEB_PAGE_INDEX, false);
	}
	
	public void setWebPageIndex(boolean state) {
		mEditor.putBoolean(FE_WEB_PAGE_INDEX, state);
		mEditor.commit();
	}

	public boolean isWebReadOnly() {
		return mSettings.getBoolean(FE_WEB_READ_ONLY, false);
	}
	
	public void setWebReadOnly(boolean state) {
		mEditor.putBoolean(FE_WEB_READ_ONLY, state);
		mEditor.commit();
	}

	public boolean isShowAllApp() {
		return mSettings.getBoolean(FE_ALL_APP, false);
	}
	
	public void setShowAllApp(boolean state) {
		mEditor.putBoolean(FE_ALL_APP, state);
		mEditor.commit();
	}

	public boolean isShowNotifyIcon() {
		return mSettings.getBoolean(FE_NOTIFY_ICON, true);
	}
	
	public void setShowNotifyIconState(boolean state) {
		mEditor.putBoolean(FE_NOTIFY_ICON, state);
		mEditor.commit();
	}
	 
	public boolean isSlientInstallApk() {
		return mSettings.getBoolean(FE_SLIENT_APK_INSTALL, false);
	}
	
	public void setSlientInstallApk(boolean state) {
		mEditor.putBoolean(FE_SLIENT_APK_INSTALL, state);
		mEditor.commit();
	}

	public boolean isSlientUninstallApk() {
		return mSettings.getBoolean(FE_SLIENT_APK_UNINSTALL, false);
	}
	
	public void setSlientUninstallApk(boolean state) {
		mEditor.putBoolean(FE_SLIENT_APK_UNINSTALL, state);
		mEditor.commit();
	}
	
	public boolean isSimpleList() {
		return mSettings.getBoolean(SIMPLE_LIST, true);
	}

	public void setDefListMode(boolean state) {
		mEditor.putBoolean(DEF_LIST_MODE, state);
		mEditor.commit();
	}

	public boolean getDefListMode() {
		return mSettings.getBoolean(DEF_LIST_MODE, true);
	}

	public void setHttpWakelock(boolean state) {
		mEditor.putBoolean(HTTP_WAKELOCK, state);
		mEditor.commit();
	}

	public boolean getHttpWakelock() {
		return mSettings.getBoolean(HTTP_WAKELOCK, true);
	}

	public void setFtpWakelock(boolean state) {
		mEditor.putBoolean(FTP_WAKELOCK, state);
		mEditor.commit();
	}

	public boolean getFtpWakelock() {
		return mSettings.getBoolean(FTP_WAKELOCK, true);
	}
	
	public String getDefaultSharingDir() {
		return mSettings.getString(DEFAULE_SHARING_DIR, null);
	}

	public void setDefaultSharingDir(String dir) {
		mEditor.putString(DEFAULE_SHARING_DIR, dir);
		commit();
	}

	public String getDefaultApkInstallLocation() {
		return mSettings.getString(FE_APK_INSTALL_LOCATION, defApkLocation);
	}

	public void setDefaultApkInstallLocation(String location) {
		mEditor.putString(FE_APK_INSTALL_LOCATION, location);
		commit();
	}
	
	public boolean isUserDonated() {
		return mSettings.getBoolean(DONATE, false);
	}

	public void setUserDonate() {
		mEditor.putBoolean(DONATE, true);
		commit();
	}
	
	public String getPreviousFEVersion() {
		return mSettings.getString(FE_VERSION, null);
	}

	public void setFEVersion(String version) {
		mEditor.putString(FE_VERSION, version);
		commit();
	}

	public String getFtpEncoding() {
		return mSettings.getString(FE_FTP_ENCODING, "UTF-8");
	}

	public void setFtpEncoding(String encoding) {
		mEditor.putString(FE_FTP_ENCODING, encoding);
		commit();
	}
	
	public String getSharingDir() {
		return mSettings.getString(SHARING_DIR, null);
	}

	public void setSharingDir(String dir) {
		mEditor.putString(SHARING_DIR, dir);
		commit();
	}

	public String getWebUsername() {
		return mSettings.getString(FE_WEB_USERNAME, "fe");
	}

	public void setWebUsername(String username) {
		mEditor.putString(FE_WEB_USERNAME, username);
		commit();
	}

	public String getWebPassword() {
		return mSettings.getString(FE_WEB_PASSWORD, "filexpert");
	}

	public void setWebPassword(String password) {
		mEditor.putString(FE_WEB_PASSWORD, password);
		commit();
	}

	public boolean isWebLoginEnable() {
		return mSettings.getBoolean(FE_WEB_LOGIN, true);
	}

	public void setWebLoginState(boolean state) {
		mEditor.putBoolean(FE_WEB_LOGIN, state);
		commit();
	}
	
	public String getHttpStreamName() {
		return mSettings.getString(HTTP_STREAM_NAME, "NONAME");
	}

	public void setHttpStreamName(String Name) {
		mEditor.putString(HTTP_STREAM_NAME, Name);
		commit();
	}

	public int getHttpPort() {
		return mSettings.getInt(HTTP_PORT, 8080);
	}

	public void setHttpPort(int port) {
		mEditor.putInt(HTTP_PORT, port);
		commit();
	}

	public String getStartDir() {
		return mSettings.getString(START_DIR, Environment
				.getExternalStorageDirectory().getPath());
	}

	public void setStartDir(String dir) {
		mEditor.putString(START_DIR, dir);
		commit();
	}

	public int getSortMode() {
		return mSettings.getInt(SORT_METHOD_NAME, 1);
	}

	public void setSortMode(int mode) {
		mEditor.putInt(SORT_METHOD_NAME, mode);
		commit();
	}

	/*
	public int getStyle() {
		return mSettings.getInt(STYLE_NAME, FileLister.STYLE_CLASSIC);
	}

	public void setStyle(int style) {
		mEditor.putInt(STYLE_NAME, style);
		commit();
	}
	*/
	
	public String getFtpUserName() {
		return mSettings.getString(FTP_USER_NAME, "fe");
	}

	public void setFtpUserName(String name) {
		mEditor.putString(FTP_USER_NAME, name);
		commit();
	}

	public String getFtpPassword() {
		return mSettings.getString(FTP_PASSWORD, "filexpert");
	}

	public void setFtpPassword(String pass) {
		mEditor.putString(FTP_PASSWORD, pass);
		commit();
	}

	public int getFtpPort() {
		return mSettings.getInt(FTP_PORT, 2211);
	}

	public void setFtpPort(int port) {
		mEditor.putInt(FTP_PORT, port);
		commit();
	}

	public String getSmbStreamBufSize() {
		return mSettings.getString(SMB_STREAM_BUF_SIZE, "524288");
	}

	public void setSmbStreamBufSize(String size) {
		mEditor.putString(SMB_STREAM_BUF_SIZE, size);
		commit();
	}

	public String getSmbStreamPath() {
		return mSettings.getString(SMB_STREAM_PATH, null);
	}

	public void setSmbStreamPath(String path) {
		mEditor.putString(SMB_STREAM_PATH, path);
		commit();
	}

	public String getSmbStreamName() {
		return mSettings.getString(SMB_STREAM_NAME, null);
	}

	public void setSmbStreamName(String name) {
		mEditor.putString(SMB_STREAM_NAME, name);
		commit();
	}

	public boolean getSmbServerStartFlag() {
		return mSettings.getBoolean(SMB_STREAM_SERVER_START_FLAG, false);
	}

	public void setSmbServerStartFlag(boolean flag) {
		mEditor.putBoolean(SMB_STREAM_SERVER_START_FLAG, flag);
		commit();
	}
}