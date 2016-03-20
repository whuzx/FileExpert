package xcxin.filexpert.Batch;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import xcxin.filexpert.FeTaskManager;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.R;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.os.AsyncTask;

public abstract class TaskBatchBase extends AsyncTask<Void, Integer, Long> {

	private List<ActivityManager.RunningTaskInfo> taskInfo;
	private List<Integer> items;
	protected AtomicInteger mSync;
	private FeTaskManager mtm;
	protected TaskWorker m_Worker = null;
	protected ProgressDialog m_pd = null;
	protected Context m_context;

	public TaskBatchBase(List<ActivityManager.RunningTaskInfo> info,
			FeTaskManager tm, Context context) {
		taskInfo = info;
		mSync = new AtomicInteger();
		mtm = tm;
		m_context = context;
		mSync.set(0);
	}

	public void setWorkItems(List<Integer> sets) {
		items = sets;
	}

	public void setWorker(TaskWorker worker) {
		m_Worker = worker;
	}

	@Override
	protected Long doInBackground(Void... params) {
		long count = 0;
		for (int index = 0; index < items.size(); index++) {
			mSync.set(0);
			publishProgress(items.get(index));
			boolean r = thread_work(taskInfo.get(items.get(index)), mtm);
			if (r == false) {
				mSync.set(0);
				return count;
			}
			while (mSync.get() != 1)
				;
			count++;
		}
		return count;
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		m_pd.setMessage(m_context.getString(R.string.deal_with) + mtm.getTaskName(taskInfo.get(values[0])));
		boolean r = m_Worker.process(taskInfo.get(values[0]), mtm);
		if (r == false) {
			r = onProcessFaild(taskInfo.get(values[0]), mtm);
			if (r == false) {
				cancel(true);
				return;
			} else {
				mSync.set(1);
			}
		}
	}

	abstract public boolean onProcessFaild(RunningTaskInfo ti, FeTaskManager tm);
	abstract public boolean thread_work(RunningTaskInfo ti, FeTaskManager tm);

	@Override
	protected void onPostExecute(Long result) {
		super.onPostExecute(result);
		if (m_pd != null) {
			m_pd.dismiss();
			m_pd = null;
		}
		((FileLister) m_context).mBatchMode = FileLister.NO_BATCH;
		((FileLister) m_context).refresh();
		System.gc();
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		m_pd = ProgressDialog.show(m_context, m_context
				.getString(R.string.tasks), " ");
	}
}