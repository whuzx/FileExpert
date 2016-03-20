package xcxin.filexpert.Batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileOperator;
import xcxin.filexpert.ProgressDlg.FeProgressWorker;
import android.content.Context;

public class FileDeleteWorker extends FeProgressWorker {
	public FileDeleteWorker() {
		super();
		backgroud = new AtomicBoolean(false);
		cancel = new AtomicBoolean(false);
	}

	@Override
	public void work(Context context) {
		if (workItems == null || workItems.size() == 0) {
			return;
		}
		// 更新view
		updateProgressView();
		// 初始化进度条
		initSeekBar();
		// 执行删除
		doDelete();
	}

	private void doDelete() {
		for (int i = 0, size = workItems.size(); i < size; i++) {
			WorkItem wi = workItems.get(i);
			FileOperator.delete(this, wi.mSrcPath, wi.mSrcName);
		}
	}

	private void updateProgressView() {
		// 要删的总数
		seekBarLength = countWorkItem();
		// 更新进度条长度
		updateProgressMax(seekBarLength);
	}

	private int countWorkItem() {
		if (workItems == null || workItems.size() == 0) {
			return 0;
		}

		int count = 0;
		for (WorkItem w : workItems) {
			FeFile file = new FeFile(w.mSrcPath, w.mSrcName);
			if (file.isDirectory()) {
				count += getDirCount(file);
			} else {
				count++;
			}
		}
		return count;
	}

	public int getSeekBarLength() {
		return seekBarLength;
	}

	public void setSeekBarLength(int seekBarLength) {
		this.seekBarLength = seekBarLength;
	}

	private int getDirCount(FeFile dir) {
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

	private void initSeekBar() {
		int i = this.countWorkItem();
		this.updateProgressMax(i);
	}

	@Override
	public void onCancel() {
		cancel.set(true);
	}

	@Override
	public void onFinish() {
		this.updateView();
	}

	@Override
	public void onBackgroud() {
		this.backgroud.set(true);
	}

	@Override
	public void updateProgressText(String text) {
		super.updateProgressText(text);
	}

	@Override
	public void updateProgressValue(int value) {
		super.updateProgressValue(value);
	}

	public void startUpdateProgressValue1by1() {
		itemCount = 0;
	}

	public void updateProgressValue() {
		itemCount++;
		updateProgressValue(itemCount);
	}

	@Override
	public void updateProgressMax(int size) {
		super.updateProgressMax(size);
	}

	private List<WorkItem> workItems;
	private AtomicBoolean cancel;
	private AtomicBoolean backgroud;
	private int itemCount;
	private int seekBarLength;
	private String srcPath;

	public List<WorkItem> getWorkItems() {
		return workItems;
	}

	public void setWorkItems(List<WorkItem> workItems) {
		this.workItems = workItems;
	}

	public boolean isCancel() {
		return cancel.get();
	}

	public void setCancel(boolean cancel) {
		this.cancel.set(cancel);
	}

	public AtomicBoolean getBackgroud() {
		return backgroud;
	}

	public void setBackgroud(AtomicBoolean backgroud) {
		this.backgroud = backgroud;
	}

	public String getSrcPath() {
		return srcPath;
	}

	public void setSrcPath(String srcPath) {
		this.srcPath = srcPath;
	}

}
