package xcxin.filexpert.Batch;

import java.util.List;

import xcxin.filexpert.FeTaskManager;
import xcxin.filexpert.FileLister;
import android.app.Activity;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;

public class TaskKillBatch extends TaskBatchBase implements TaskWorker {
	
	private boolean bKillFE;
	
	public TaskKillBatch(List<RunningTaskInfo> info, FeTaskManager tm,
			Context context) {
		super(info, tm, context);
		setWorker(this);
		bKillFE = false;
	}

	@Override
	public boolean onProcessFaild(RunningTaskInfo ti, FeTaskManager tm) {
		return false;
	}

	@Override
	public boolean thread_work(RunningTaskInfo ti, FeTaskManager tm) {
		// Does user want to kill FE? We cannot kill FE during kill batch operation
		if(tm.getTaskName(ti).compareTo("File Expert") == 0 || tm.getTaskName(ti).compareTo("文件大师") == 0) {
			// Yes, we're trying to kill ourself
			bKillFE = true;
			mSync.set(1);
			return true;
		}
		// Kill IT!!!
		tm.KillTask(ti.baseActivity.getPackageName());
		mSync.set(1);
		return true;
	}

	@Override
	public boolean process(RunningTaskInfo ti, FeTaskManager tm) {
		return true;
	}

	@Override
	protected void onPostExecute(Long result) {	
		((FileLister)m_context).mContentsContainer.removeAllTaskSel();
		super.onPostExecute(result);
		if(bKillFE == true) {
			// It's right time to finish myself :(
			((Activity) m_context).finish();
		}
	}
}