package xcxin.filexpert.Batch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import xcxin.filexpert.FileLister;
import android.os.AsyncTask;

public abstract class BatchBase extends AsyncTask<Void, WorkItem, Long> {
	
	private List<WorkItem> m_wi = new ArrayList<WorkItem> ();
	protected BatchWorker m_Worker = null;
	public FileLister m_Activity;
	public AtomicInteger mSync;
	
	public final int UPDATE_BY_SRC = 0;
	public final int UPDATE_BY_DST = 1;
	
	public BatchBase (FileLister act) {
		m_wi.clear();
		mSync = new AtomicInteger();
		m_Activity = act;
	}
	
	public void setWorker (BatchWorker worker) {
		m_Worker = worker;
	}
	
	public void AddWorkItem(String src_path, String src_name, String dst) {
		WorkItem wi = new WorkItem();
		wi.mSrcPath = src_path;
		wi.mSrcName = src_name;
		wi.mDst = dst;
		m_wi.add(wi);
	}
	
	public void AddWorkItem(WorkItem wi) {
		m_wi.add(wi);
	}
	
	public void AddWorkItems (List<WorkItem> wi_list) {
		if(wi_list == null) return;
		for (int index = 0; index < wi_list.size(); index++) {
			AddWorkItem (wi_list.get(index));
		}
	}
		
	@Override
	protected Long doInBackground(Void... params) {
		if (m_Worker == null) return null;
		long ret = 0;
		for (int index = 0; index < m_wi.size(); index ++) {
			mSync.set(0);
			publishProgress(m_wi.get(index));
			boolean r = thread_work (m_wi.get(index));
			if (r == false) {
				mSync.set(0);
				return ret;
			}
			while (mSync.get() != 1);
			ret ++;
		}
		m_wi = null;
		System.gc();
		return ret;
	}

	@Override
	protected void onProgressUpdate(WorkItem... values) {
		super.onProgressUpdate(values);
		boolean r = m_Worker.process(values[0]);
		if (r == false) {
			r = onProcessFaild (values[0]);
			if (r == false) {
				cancel(true);
				return;
			} else {
				mSync.set(1);
			}
		}
	}
	
	abstract public boolean onProcessFaild (WorkItem wi);
	abstract public boolean thread_work(WorkItem wi);
}