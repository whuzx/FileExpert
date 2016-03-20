package xcxin.filexpert;

import java.util.HashMap;

import android.view.View;
import android.widget.GridView;
import android.widget.ListView;

public class PathManager {
	
	private HashMap<String, Integer> maps;
	private int tabChangePos;
	private boolean tabPosSaved = false;
	
	public PathManager() {
		maps = new HashMap<String, Integer>();
	}
	
	public void recordPath(String path, int pos) {
		maps.put(path, pos);
	}
	
	public void recordPath(FileLister lister, int pos) {
		recordPath(lister.getCurrentPath(), pos);
	}
	
	public int getPos(String path, boolean removeRecord) {
		Integer pos = maps.get(path);
		if(pos == null) return 0;
		if(removeRecord) {
			maps.remove(path);
		}
		return pos;
	}
	
	public void restorePosition(FileLister lister, String path, boolean removeRecord) {
		View v = lister.getContentsView();
		int pos = getPos(path, removeRecord);
		if(lister.getListerMode() == FileLister.LIST_MODE) {
			ListView lv = (ListView)v;
			lv.setSelection(pos);
		} else {
			GridView gv = (GridView)v;
			gv.setSelection(pos);			
		}
	}
	
	public void tabChangeProcess(FileLister lister) {
		if(tabPosSaved) {
			tabChangeEnd(lister);
		}
		tabChangePos = lister.getVisiablePosition();
		tabPosSaved = true;
	}
	
	private void tabChangeEnd(FileLister lister) {
		View v = lister.getContentsView();
		if(lister.getListerMode() == FileLister.LIST_MODE) {
			ListView lv = (ListView)v;
			lv.setSelection(tabChangePos);
		} else {
			GridView gv = (GridView)v;
			gv.setSelection(tabChangePos);			
		}
	}
}