package xcxin.filexpert.Batch;

import xcxin.filexpert.FeTaskManager;
import android.app.ActivityManager;

public interface TaskWorker {
	public boolean process (ActivityManager.RunningTaskInfo ti, FeTaskManager tm);
}
