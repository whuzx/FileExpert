package xcxin.filexpert.Batch;

import java.util.concurrent.atomic.AtomicBoolean;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FePackage;
import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.R;
import xcxin.filexpert.SysInfo;
import xcxin.filexpert.ProgressDlg.FeProgressWorker;
import android.content.Context;

public class SilentPackageInstaller extends FeProgressWorker {

	private FeFile mTopDir;
	private AtomicBoolean cancelAi;
	private AtomicBoolean BackgroudAi;
	private int count;
	private int max_entry;

	public SilentPackageInstaller(FeFile topdir) {
		super();
		mTopDir = topdir;
		cancelAi = new AtomicBoolean(false);
		BackgroudAi = new AtomicBoolean(false);
	}

	public int getDirCount(FeFile dir) {
		int count = 0;
		FeFile files[] = dir.listFiles();
		if (files == null || files.length == 0)
			return count;
		for (FeFile f : files) {
			if (f.isDirectory()) {
				count += getDirCount(f);
			} else {
				if (f.getName().toLowerCase().indexOf(".apk") > 0) {
					count++;
				}
			}
		}
		return count;
	}

	@Override
	public void work(Context context) {
		if (mTopDir == null)
			return;
		updateProgressText("Preparing to install...");
		max_entry = getDirCount(mTopDir);
		updateProgressMax(max_entry);
		startUpdateProgressValue1by1();
		installAllPackages(mTopDir, context);
	}

	private void installAllPackages(FeFile file, Context context) {
		FileLister lister = (FileLister)context;
		if (file.isFile()) {
			if (file.getName().toLowerCase().indexOf(".apk") > 0) {
				updateProgressText(file.getName());
				// We can not install apk packages if there were spaces on the
				// file name, include whole path - let rename this file first
				FeFile temp = new FeFile(FeUtil.getTempDirName(), "temp.apk");
				if(temp.exists()) temp.delete();
				file.renameTo(temp);
				installPackage(temp.getPath(), lister);
				temp.renameTo(file);
				updateProgressValue();
			}
		} else {
			FeFile[] files = file.listFiles();
			if (files == null || files.length == 0)
				return;
			for (FeFile f : files) {
				installAllPackages(f, context);
			}
		}
	}

	private void installPackage(String fullpath, FileLister lister) {
		// Silent Install APKs
		String loc = lister.mSettings.getDefaultApkInstallLocation();
		int iLoc = FePackage.INSTALL_AUTO;
		if (SysInfo.getSDKVersion() >= 8) {
			if (loc.compareTo(lister.getString(R.string.apk_auto)) == 0) {
				iLoc = FePackage.INSTALL_AUTO;
			} else if (loc.compareTo(lister.getString(R.string.apk_sdcard)) == 0) {
				iLoc = FePackage.INSTALL_SDCARD;
			} else if (loc.compareTo(lister.getString(R.string.apk_flash)) == 0) {
				iLoc = FePackage.INSTALL_FLASH;
			}
			FePackage.installPackageSlient(fullpath, lister, iLoc);
		} else {
			FePackage.installPackageSlient(fullpath, lister,
					FePackage.INSTALL_AUTO);
		}
	}

	public boolean isCancel() {
		return cancelAi.get();
	}

	public boolean isBackgroud() {
		return BackgroudAi.get();
	}

	@Override
	public void onCancel() {
		cancelAi.set(true);
	}

	@Override
	public void onFinish() {
		cancelAi = null;
		BackgroudAi = null;
	}

	@Override
	public void onBackgroud() {
		BackgroudAi.set(true);
	}

	@Override
	public void updateProgressText(String text) {
		if (BackgroudAi.get() == false) {
			super.updateProgressText(text);
		}
	}

	@Override
	public void updateProgressValue(int value) {
		if (BackgroudAi.get() == false) {
			super.updateProgressValue(value);
		}
	}

	public void startUpdateProgressValue1by1() {
		count = 0;
	}

	public void updateProgressValue() {
		if (BackgroudAi.get() == false) {
			count++;
			updateProgressValue(count);
		}
	}

	public void updateProgressValueMax() {
		if (BackgroudAi.get() == false) {
			updateProgressValue(max_entry);
		}
	}

	@Override
	public void updateProgressMax(int size) {
		if (BackgroudAi.get() == false) {
			super.updateProgressMax(size);
		}
	}
}
