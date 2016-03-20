package xcxin.filexpert.ProgressDlg;

import java.util.HashMap;

public class BackgroudBatchTaskMgr {
	
	private HashMap<Integer, BatchTask> tasks;
	private int index;
	
	public class BatchTask {
		FeProgressWorker worker;
		int batchMode;
		public BatchTask(FeProgressWorker w, int i) {
			worker = w;
			batchMode = i;
		}
	}
	
	public BackgroudBatchTaskMgr() {
		tasks = new HashMap<Integer, BatchTask>();
		index = 0;
	}
	
	public int addTask(FeProgressWorker worker, int mode) {
		BatchTask bt = new BatchTask(worker, mode);
		tasks.put(index, bt);
		int id = index;
		index++;
		return id;
	}
	
	public void removeTask(int id) {
		tasks.remove(id);
	}
	
	public BatchTask getTask(int id) {
		return tasks.get(id);
	}
}
