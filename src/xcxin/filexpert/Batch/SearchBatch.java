package xcxin.filexpert.Batch;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import xcxin.filexpert.DirTreeHelper;
import xcxin.filexpert.FeFile;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.R;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

public class SearchBatch extends DirBatchBase implements BatchWorker,
		Handler.Callback {

	private String mTarget;
	private Handler mHandler;
	private Bundle mBdl;
	private ProgressDialog m_pd = null;
	private FileLister mParent;
	private boolean mFound;
	private boolean mReg;
	private boolean mCase;

	public SearchBatch(FileLister act, boolean mode, boolean reg,
			boolean ignore_case, FeFile TopDir, String Target) {
		super(act, mode, TopDir);
		mTarget = Target;
		mParent = act;
		mReg = reg;
		mCase = ignore_case;
		mHandler = new Handler(this);
		mBdl = new Bundle();
		mFound = false;
		act.mFsContentsMgr.clear();
		setWorker(this);
	}

	@Override
	public boolean onProcessFaild(WorkItem wi) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean thread_work(WorkItem wi) {
		if (match(wi.mSrcName, mTarget) != -1) {
			mFound = true;
			if (wi.mSrcName.indexOf(".") != -1) {
				// File
				m_Activity.mFsContentsMgr.add(DirTreeHelper
						.getPreviousDir(wi.mSrcPath), wi.mSrcName, false);
			} else {
				// Dir
				m_Activity.mFsContentsMgr.add(DirTreeHelper
						.getPreviousDir(wi.mSrcPath), wi.mSrcName, true);
			}
			mSync.set(1);
			return true;
		}
		FeFile file = new FeFile(wi.mSrcPath);
		FeFile files[];
		try {
			files = file.listFiles();
			if (files == null) {
				mSync.set(1);
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		for (int index = 0; index < files.length; index++) {
			if (files[index].isDirectory() == true)
				continue;
			if (match(files[index].getName(), mTarget) != -1) {
				mFound = true;
				if (files[index].getName().indexOf('.') != -1) {
					// File
					m_Activity.mFsContentsMgr.add(DirTreeHelper
							.getPreviousDir(files[index].getPath()),
							files[index].getName(), false);
				} else {
					// Dir
					m_Activity.mFsContentsMgr.add(DirTreeHelper
							.getPreviousDir(files[index].getPath()),
							files[index].getName(), true);
				}
			}
		}
		mSync.set(1);
		return true;
	}

	@Override
	public boolean process(WorkItem wi) {
		if (m_pd == null) {
			m_pd = ProgressDialog.show(mParent, mParent
					.getString(R.string.deal_with), wi.mSrcPath);
		} else {
			m_pd.setMessage(wi.mSrcPath);
		}
		return true;
	}

	private void sendMsg(String key, String message) {
		mBdl.clear();
		Message msg = mHandler.obtainMessage();
		mBdl.putString(key, message);
		msg.setData(mBdl);
		mHandler.sendMessage(msg);
	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle bdl = msg.getData();

		String dlg_msg = bdl.getString("not_found");
		if (dlg_msg != null) {
			m_Activity.showInfo(m_Activity.getString(R.string.not_found),
					m_Activity.getString(R.string.error), false);
		}

		dlg_msg = bdl.getString("found");
		if (dlg_msg != null) {
			m_Activity.gotoDir(m_Activity.getString(R.string.search_result));
		}
		if (m_pd != null) {
			m_pd.dismiss();
			m_pd = null;
		}

		return true;
	}

	private int match(String src, String target) {
		if (mReg == false) {
			if (mCase == false) {
				return src.indexOf(target);
			} else {
				return src.toLowerCase().indexOf(target.toLowerCase());
			}
		} else {
			try {
				if (mCase == true) {
					Pattern p = Pattern.compile(target,
							Pattern.CASE_INSENSITIVE);
					Matcher m = p.matcher(src);
					if (m.find() == true)
						return 1;
					return -1;
				} else {
					Pattern p = Pattern.compile(target);
					Matcher m = p.matcher(src);
					if (m.find() == true)
						return 1;
					return -1;
				}
			} catch (Exception e) {
				return -1;
			}
		}
	}

	@Override
	public void onFinish(boolean processAll, boolean userStop) {
		if (mFound == false) {
			// It seems we have already tried all entries, but no one is found
			sendMsg("not_found", " ");
		} else {
			sendMsg("found", " ");
		}
	}
}