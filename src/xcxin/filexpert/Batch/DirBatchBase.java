package xcxin.filexpert.Batch;

import java.util.List;

import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileLister;

public abstract class DirBatchBase extends BatchBase {

	public boolean mSubDir;
	public FeFile mTopDir;
	private boolean mStop;
	private boolean mProcessAll;

	public DirBatchBase(FileLister act, boolean mode, FeFile TopDir) {
		super(act);
		mSubDir = mode;
		mTopDir = TopDir;
		mProcessAll = false;
	}

	public void setSubDir(boolean mode) {
		mSubDir = mode;
	}

	abstract public boolean onProcessFaild(WorkItem wi);

	abstract public boolean thread_work(WorkItem wi);

	abstract public void onFinish(boolean process_all, boolean user_stop);

	@Override
	public void AddWorkItem(String srcPath, String srcName, String dst) {
		return;
	}

	@Override
	public void AddWorkItem(WorkItem wi) {
		return;
	}

	@Override
	public void AddWorkItems(List<WorkItem> wiList) {
		return;
	}

	@Override
	protected Long doInBackground(Void... params) {
		if (m_Worker == null || mTopDir == null)
			return null;
		if (mTopDir.isFile() == true)
			return null;
		mStop = false;
		m_Activity.mFsContentsMgr.clear();
		return workWithDir(mTopDir, false);
	}

	private Long workWithDir(FeFile dir, boolean subcall) {
		long ret = 0;
		mProcessAll = false;
		mStop = false;
		WorkItem wi = new WorkItem();
		boolean r;
		if (subcall == false) {
			//
			// Prevent duplicate work with sub call
			//
			wi.mSrcPath = dir.getPath();
			wi.mSrcName = dir.getName();
			publishProgress(wi);
			r = thread_work(wi);
			if (r == false) {
				mSync.set(0);
				return ret;
			}
			while (mSync.get() != 1)
				;
			ret++;
		}
		if (mSubDir == false) {
			mProcessAll = true;
			if (subcall == false) {
				onFinish(mProcessAll, mStop);
			}
			return ret;
		}

		// Search all sub directories
		int index;
		FeFile files[] = dir.listFiles();
		if(files == null) {
			if (subcall == false) {
				onFinish(mProcessAll, mStop);
			}			
			return ret;
		}
		for (index = 0; index < files.length; index++) {
			if (files[index].isDirectory() == true) {
				mSync.set(0);
				wi.mSrcPath = files[index].getPath();
				wi.mSrcName = files[index].getName();
				publishProgress(wi);
				r = thread_work(wi);
				if (r == false) {
					mSync.set(0);
					break;
				}
				while (mSync.get() != 1)
					;
				ret++;
				if (mStop == true) {
					break;
				}
				if (mSubDir == true) {
					try {
						ret += workWithDir(files[index], true);
					} catch (Exception e) {
						;
					}
				}
				if (mStop == true) {
					if (index == files.length - 1) {
						mProcessAll = true;
					}
					break;
				}
			}
		}

		if (mProcessAll == false) {
			if (index == files.length) {
				mProcessAll = true;
			}
		}

		if (subcall == false) {
			onFinish(mProcessAll, mStop);
		}

		return ret;
	}

	void Stop() {
		mStop = true;
	}
}