package xcxin.filexpert;

import java.util.ArrayList;
import java.util.List;

public class FsContentsMgr extends Object {

	private List<Contents> mContents;

	private class Contents extends Object {
		public String path;
		public String name;
		boolean isDir;
	}

	public FsContentsMgr() {
		mContents = new ArrayList<Contents>();
	}

	public void clear() {
		mContents.clear();
	}

	public void add(String path, String name, boolean isDir) {
		Contents itm = new Contents();
		itm.isDir = isDir;
		itm.path = path;
		itm.name = name;
		mContents.add(itm);
	}
	
	public void remove(int index) {
		mContents.remove(index);
	}

	public String getPath(int index) {
		return mContents.get(index).path;
	}

	public String getName(int index) {
		return mContents.get(index).name;
	}

	public boolean isDir(int index) {
		return mContents.get(index).isDir;
	}

	public int getCount() {
		return mContents.size();
	}

	public String getFullPath(int index) {
		if (getPath(index).compareTo("/") == 0) {
			return getPath(index) + getName(index);
		} else {
			return getPath(index) + "/" + getName(index);
		}
	}
}