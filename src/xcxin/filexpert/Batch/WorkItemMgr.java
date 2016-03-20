package xcxin.filexpert.Batch;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class WorkItemMgr {
	
	private HashSet<WorkItem> mDirSelSet = new HashSet<WorkItem>();
	
	public void add(WorkItem wi) {
		mDirSelSet.add(wi);
	}
	
	public void remove(WorkItem wi) {
		Iterator<WorkItem> i = mDirSelSet.iterator();
		while(i.hasNext()) {
			WorkItem w = i.next();
			if(w.equals(wi)) {
				mDirSelSet.remove(w);
				return;
			}
		}
	}
	
	public void clear() {
		mDirSelSet.clear();
	}
		
	public ArrayList<WorkItem> listWorkItems() {
		return new ArrayList<WorkItem>(mDirSelSet);
	}
	
	public boolean isReady() {
		if(mDirSelSet.isEmpty()) {
			return false;
		}
		return true;
	}
	
	public boolean isAdded(WorkItem wi) {
		Iterator<WorkItem> i = mDirSelSet.iterator();
		while(i.hasNext()) {
			WorkItem w = i.next();
			if(w.equals(wi)) {
				return true;
			}
		}
		return false;
	}
	
	public List<WorkItem> getSel(String path) {
		List<WorkItem> sel = null;
		Iterator<WorkItem> i = mDirSelSet.iterator();
		while(i.hasNext()) {
			WorkItem w = i.next();
			if(w.mSrcPath.equals(path)) {
				if(sel == null) {
					sel = new ArrayList<WorkItem>();
				}
				sel.add(w);
			}
		}
		return sel;
	}
}