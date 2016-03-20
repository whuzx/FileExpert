package xcxin.filexpert.Batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import xcxin.filexpert.FePackage;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.R;
import xcxin.filexpert.ProgressDlg.FeProgressWorker;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Environment;

public class AppBatch extends FeProgressWorker {

	public static final int APP_BACKUP = 0;
	public static final int APP_UNINSTALL = 1;

	private int mode;
	private Context m_context;
	private List<ApplicationInfo> apps;
	private List<Integer> items;
	private AtomicBoolean cancelAi;
	private AtomicBoolean BackgroudAi;
	private AtomicBoolean installAppSync;
	private int itemCount;

	public AppBatch(List<ApplicationInfo> info, Context context) {
		super();
		m_context = context;
		apps = info;
		mode = -1;
		cancelAi = new AtomicBoolean(false);
		BackgroudAi = new AtomicBoolean(false);
		installAppSync = new AtomicBoolean(false);
	}

	public void setMode(int m) {
		mode = m;
	}
	
	public void setWorkItems(List<Integer> sets) {
		items = sets;
	}

	public void goNext() {
		switch (mode) {
		case APP_UNINSTALL:
			installAppSync.set(false);
			break;
		}
	}

	@Override
	public void work(Context context) {
		boolean hasFe = false;
		int feIndex = 0;
		PackageManager pm = m_context.getPackageManager();
		if (items == null)
			return;
		if(items.size() > apps.size()) return;
		updateProgressMax(items.size());
		for (Integer index : items) {
			ApplicationInfo ai = apps.get(index);
			if (cancelAi.get() == true)
				return;
			updateProgressText(pm.getApplicationLabel(ai).toString());
			switch (mode) {
			case APP_BACKUP:
				FePackage.backupApp(ai, pm, true);
				break;
			case APP_UNINSTALL:
				if(ai.packageName.equals("xcxin.filexpert")) {
					hasFe = true;
					feIndex = index;
					continue;
				}				
				FileLister fl = (FileLister) m_context;
				if (fl.mSettings.isSlientUninstallApk()) {
					FePackage.uninstallPackageSilent(ai.packageName, pm);
				} else {
					installAppSync.set(true);
					FePackage.uninstallApp((FileLister) m_context, ai);
					while (installAppSync.get() != false);
				}
				break;
			default:
				;
			}
			updateProgressValue();
		}
		
		if(hasFe) {
			updateProgressValue();
			ApplicationInfo ai = apps.get(feIndex);
			FileLister fl = (FileLister) m_context;
			if (fl.mSettings.isSlientUninstallApk()) {
				FePackage.uninstallPackageSilent(ai.packageName, pm);
			} else {
				installAppSync.set(true);
				FePackage.uninstallApp((FileLister) m_context, ai);
				while (installAppSync.get() != false);
			}
		}
	}

	@Override
	public void onCancel() {
		cancelAi.set(true);
	}

	@Override
	public void onFinish() {
		if(((FileLister) m_context).mRunningMode == FileLister.APP_MODE) {
			updateView();
		}
		((FileLister) m_context).mContentsContainer.removeAllAppSel();
		if (mode == APP_BACKUP) {
			updateToastMessage(m_context.getString(R.string.backup_to)
					+ Environment.getExternalStorageDirectory().getPath()
					+ "/backup_apps");
		}
		((FileLister) m_context).mBatchMode = FileLister.NO_BATCH;
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
	public void updateProgressMax(int size) {
		if (BackgroudAi.get() == false) {
			super.updateProgressMax(size);
		}
	}

	public void updateProgressValue() {
		if (BackgroudAi.get() == false) {
			itemCount++;
			updateProgressValue(itemCount);
		}
	}

	public void startUpdateProgressValue1by1() {
		itemCount = 0;
	}
}
