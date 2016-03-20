package xcxin.filexpert.Batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.FileOperator;
import xcxin.filexpert.ProgressDlg.FeProgressDialog;
import xcxin.filexpert.ProgressDlg.FeProgressWorker;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;

public class FileCopyWorker extends FeProgressWorker {

	private List<WorkItem> items;
	private AtomicBoolean cancelAi;
	private AtomicBoolean BackgroudAi;
	private String mDstPath;
	private boolean mCut;
	private int itemCount;
	private int max_entry;

	public boolean skip_existing = true;
	public boolean auto_overwrite = false;

	public FileCopyWorker(boolean cutmode) {
		super();
		mCut = cutmode;
		cancelAi = new AtomicBoolean(false);
		BackgroudAi = new AtomicBoolean(false);
	}

	public void setDstPath(String path) {
		mDstPath = path;
	}

	@Override
	public void work(Context context) {
		if (items == null || items.size() == 0)
			return;
		if (mDstPath == null)
			return;
		updateProgressText("Preparing to copy...");
		max_entry = getWorkItemsCount();
		updateProgressMax(max_entry);
		startUpdateProgressValue1by1();
		for (WorkItem wi : items) {
			if (cancelAi.get() == true) {
				updateView();
				return;
			}
			try {
				FileOperator.copyTo(this, context, wi.mSrcPath, wi.mSrcName, mDstPath,
						mCut, true, skip_existing);
			} catch (Exception e) {
				;
			}
		}
		if (BackgroudAi.get() == false) {
			updateView();
		} else {
			FileLister lister = (FileLister) context;
			if (lister.getCurrentPath().equals(mDstPath)) {
				updateView();
			}
		}
	}

	public void alertUserAndWait(String fname) {
		setState(WAIT_STATE);
		Bundle bdl = new Bundle();
		bdl.putString(FeProgressDialog.COPY_ALERT, fname);
		Message msg = getHandler().obtainMessage();
		msg.setData(bdl);
		getHandler().sendMessage(msg);
		waitUntilFinish();
	}

	public int getWorkItemsCount() {
		if (items == null || items.size() == 0)
			return 0;
		int count = 0;
		for (WorkItem w : items) {
			FeFile file = new FeFile(w.mSrcPath, w.mSrcName);
			if (file.isDirectory()) {
				count += getDirCount(file);
			} else {
				count++;
			}
		}
		return count;
	}

	public int getDirCount(FeFile dir) {
		int count = 1;
		FeFile files[] = dir.listFiles();
		if (files == null || files.length == 0)
			return count;
		for (FeFile f : files) {
			if (f.isDirectory()) {
				count += getDirCount(f);
			} else {
				count++;
			}
		}
		return count;
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

	public void setWorkItems(List<WorkItem> workItems) {
		items = workItems;
	}

	public boolean isReady() {
		return items != null && !items.isEmpty();
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
		itemCount = 0;
	}
	
	public void updateProgressValue() {
		if (BackgroudAi.get() == false) {
			itemCount++;
			updateProgressValue(itemCount);
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
