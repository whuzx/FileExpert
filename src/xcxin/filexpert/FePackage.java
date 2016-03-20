package xcxin.filexpert;

import java.util.ArrayList;
import java.util.List;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

public class FePackage extends Object {

	public static final int INSTALL_SDCARD = 1;
	public static final int INSTALL_FLASH = 2;
	public static final int INSTALL_AUTO = 3;

	public static void runApplication(FileLister mainUI, ApplicationInfo ai) {
		try {
			Intent i = mainUI.getPackageManager().getLaunchIntentForPackage(
					ai.packageName);
			mainUI.startActivity(i);
		} catch (Exception e) {
			Log.v("FE", "Exception occured: " + e.toString());
			mainUI.showInfo(mainUI.getString(R.string.lunch_fault),
					mainUI.getString(R.string.error), false);
		}
	}

	public static void uninstallApp(FileLister mainUI, ApplicationInfo ai) {
		Uri packageURI = Uri.parse("package:" + ai.packageName);
		try {
			Intent uninstallIntent = new Intent(Intent.ACTION_DELETE,
					packageURI);
			mainUI.startActivity(uninstallIntent);
		} catch (Exception e) {
			return;
		}
	}

	public static boolean backupApp(ApplicationInfo ai, PackageManager pm,
			boolean dst_folder_create) {
		FeFile src = new FeFile(ai.sourceDir);
		if (src.exists() != true) {
			return false;
		}
		String dst_path = Environment.getExternalStorageDirectory().getPath()
				+ "/backup_apps";
		if (dst_folder_create) {
			FeFile dst_folder = new FeFile(dst_path);
			if (dst_folder.exists() == false) {
				dst_folder.mkdir();
			}
		}
		String dst_name;
		try {
			dst_name = pm.getApplicationLabel(ai)
					+ "_"
					+ pm.getPackageInfo(ai.packageName,
							PackageManager.GET_META_DATA).versionName + ".apk";
		} catch (NameNotFoundException e1) {
			dst_name = pm.getApplicationLabel(ai) + ".apk";
		}
		FeFile dst = new FeFile(dst_path, dst_name);
		boolean r;
		try {
			r = FileOperator.copyFileUseChannel(src, dst);
		} catch (Exception e) {
			r = false;
		}
		src = null;
		dst = null;
		return r;
	}

	public static Intent openAppWithMarketIntent(ApplicationInfo ai) {
		String market_addr = "market://search?q=pname:" + ai.packageName;
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(market_addr));
		return i;
	}

	public static Intent openAppWithPackageName(String packagename) {
		String market_addr = "market://search?q=pname:" + packagename;
		Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(market_addr));
		return i;
	}

	static public List<ApplicationInfo> listAllApplications(PackageManager pm,
			boolean allapp) {
		List<ApplicationInfo> orig_list = new ArrayList<ApplicationInfo>();
		final Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
		mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> pkgAppsList = pm.queryIntentActivities(
				mainIntent, 0);
		if (pkgAppsList == null)
			return null;
		for (int index = 0; index < pkgAppsList.size(); index++) {
			ApplicationInfo ai = pkgAppsList.get(index).activityInfo.applicationInfo;
			if (ai != null) {
				if (allapp == false) {
					if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
						orig_list.add(ai);
					}
				} else {
					orig_list.add(ai);
				}
			}
		}

		return orig_list;
	}

	static public String getVersion(String packageName, PackageManager pm) {
		try {
			return pm.getPackageInfo(packageName, PackageManager.GET_META_DATA).versionName;
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	static public String getAppLabel(String packageName, PackageManager pm) {
		try {
			ApplicationInfo ai = pm.getApplicationInfo(packageName,
					PackageManager.GET_META_DATA);
			return pm.getApplicationLabel(ai).toString();
		} catch (NameNotFoundException e) {
			return null;
		}
	}

	static public Drawable getIcon(ComponentName name, PackageManager pm) {
		Drawable icon;
		try {
			icon = pm.getActivityIcon(name);
		} catch (NameNotFoundException e) {
			return null;
		}
		return icon;
	}

	static public boolean installPackageSlient(String src, FileLister app,
			int location) {
		if (src == null || app == null)
			return false;
		PackageManager pm = app.getPackageManager();
		String pkgName = pm.getPackageArchiveInfo(src,
				PackageManager.GET_ACTIVITIES).packageName;
		boolean r = false;
		switch (location) {
		case INSTALL_SDCARD:
			r = FeUtil.runCmd("pm install -r -s " + "\"" + src + "\"");
			break;
		case INSTALL_FLASH:
			r = FeUtil.runCmd("pm install -r -f " + "\"" + src + "\"");
			break;
		case INSTALL_AUTO:
			r = FeUtil.runCmd("pm install -r " + "\"" + src + "\"");
			break;
		}
		if (r == false)
			return false;
		return isPackageInstalled(pkgName, pm);
	}

	public static boolean isPackageInstalled(String pkgName, PackageManager pm) {
		try {
			@SuppressWarnings("unused")
			PackageInfo pi = pm.getPackageInfo(pkgName,
					PackageManager.GET_ACTIVITIES
							| PackageManager.GET_META_DATA);
		} catch (PackageManager.NameNotFoundException e) {
			return false;
		}
		return true;
	}

	public static boolean uninstallPackageSilent(String pkgName,
			PackageManager pm) {
		if (isPackageInstalled(pkgName, pm) == false)
			return true;
		FeUtil.runCmd("pm uninstall " + pkgName);
		boolean r = isPackageInstalled(pkgName, pm);
		if (r == false)
			return true;
		return false;
	}
}