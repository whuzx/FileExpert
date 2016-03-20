package xcxin.filexpert;

import java.lang.reflect.Method;
import java.util.List;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

public class FeTaskManager {

	private Context m_context;
	private ActivityManager mAm;
	@SuppressWarnings("unused")
	private boolean mInit = false;
	private List<RunningTaskInfo> mTaskList;

	public FeTaskManager(Context context) {
		mInit = false;
		m_context = context;
		mAm = (ActivityManager) m_context
				.getSystemService(Context.ACTIVITY_SERVICE);
		refreshRunningTaskInfo(true);
	}

	protected void refreshRunningTaskInfo(final boolean init) {
		// Fire off a thread to do some work that we shouldn't do directly in
		// the UI thread
		Thread t = new Thread() {
			public void run() {
				if (mTaskList != null) {
					mTaskList = null;
				}
				mTaskList = mAm.getRunningTasks(500);
				if (init == true) {
					mInit = true;
				}
				System.gc();
			}
		};
		t.start();
	}

	public List<RunningTaskInfo> refreshRunningTaskInfo() {
		if (mTaskList != null) {
			mTaskList = null;
		}
		mTaskList = mAm.getRunningTasks(500);
		return mTaskList;
	}

	public List<RunningTaskInfo> getRunningTaskInfo() {
		return mTaskList;
	}

	public long getAvailMemory() {
		ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
		mAm.getMemoryInfo(info);
		return info.availMem;
	}

	public void KillTask(int index) {
		if (index > mTaskList.size()) {
			return;
		}
		String pkg = mTaskList.get(index).baseActivity.getPackageName();
		KillTask(pkg);
	}

	public void KillTask(String pkg) {
		List<RunningAppProcessInfo> api = mAm.getRunningAppProcesses();
		for (RunningAppProcessInfo pi : api) {
			if (pi.pkgList != null) {
				for (int index = 0; index < pi.pkgList.length; index++) {
					if (pkg.compareTo(pi.pkgList[index]) == 0) {
						if (SysInfo.getSDKVersion() >= 8) {
							;
						}
						for (int i = 0; i < pi.pkgList.length; i++) {
							mAm.restartPackage(pi.pkgList[i]);
						}
					}
				}
			}
		}
	}

	public void killTaskFroyo(String pkgName) {
		try {
			Class<?> c = Class.forName("android.app.ActivityManagerNative");
			Method getDefaultMethod = c.getMethod("getDefault");
			getDefaultMethod.setAccessible(true);
			Object nativeManager = getDefaultMethod.invoke(null);
			c = nativeManager.getClass();
			Method forceStopPackageMethod = c.getMethod("forceStopPackage",
					String.class);
			forceStopPackageMethod.setAccessible(true);
			forceStopPackageMethod.invoke(nativeManager, pkgName);
		} catch (Exception e) {
			;
		}
	}

	public String getTaskName(RunningTaskInfo ti) {
		if (ti == null) {
			return " ";
		}
		return FePackage.getAppLabel(ti.baseActivity.getPackageName(),
				m_context.getPackageManager());
	}
}