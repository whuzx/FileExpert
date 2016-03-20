package xcxin.filexpert.Batch;

import java.util.List;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.FileOperator;

public class ExecuteBatch extends BatchBase implements BatchWorker {

	private FileLister mMainUi;
	private boolean isFinish;
	@SuppressWarnings("unused")
	private List<WorkItem> mSel;

	public ExecuteBatch(FileLister act) {
		super(act);
		mMainUi = act;
		setWorker(this);
		mSel = mMainUi.mWiMgr.getSel(mMainUi.getCurrentPath());
	}

	@Override
	public boolean onProcessFaild(WorkItem wi) {
		return true;
	}

	@Override
	public boolean process(WorkItem wi) {
		return true;
	}

	@Override
	public boolean thread_work(WorkItem wi) {
		FeFile target = new FeFile(wi.mSrcPath, wi.mSrcName);
		FileOperator.perform_file_operation(target, mMainUi);
		return true;
	}

	public void goNext() {
		if (isFinish != true) {
			mSync.set(1);
		}
	}

	@Override
	protected void onPostExecute(Long result) {
		isFinish = true;
		mMainUi.mBatchMode = FileLister.NO_BATCH;
		mMainUi.refresh();
	}
}