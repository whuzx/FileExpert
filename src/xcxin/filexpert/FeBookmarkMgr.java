package xcxin.filexpert;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.SharedPreferences;

public class FeBookmarkMgr extends Object {

	SharedPreferences mSettings;
	SharedPreferences.Editor mEditor;
	private int mCount;
	private List<FeBookmark> mBookmarks = new ArrayList<FeBookmark>();

	private final String SETTINGS_NAME = "FE_BOOKMARK";
	private final String BOOKMARK_COUNT = "NumberOfBookmarks";

	public FeBookmarkMgr(Context act) {
		mSettings = act.getSharedPreferences(SETTINGS_NAME,
				Context.MODE_PRIVATE);
		mCount = mSettings.getInt(BOOKMARK_COUNT, 0);
		mBookmarks.clear();
		mEditor = mSettings.edit();
		if (mCount != 0) {
			for (int index = 0; index < mCount; index++) {
				FeBookmark bm = new FeBookmark();
				bm.name = mSettings.getString("KEY" + index, null);
				bm.path = mSettings.getString("PATH" + index, null);
				if (bm.path != null) {
					mBookmarks.add(bm);
				}
			}
		}
		clearAllBookmarks();
	}
	
	public void clearAllBookmarks() {
		int count = mSettings.getInt(BOOKMARK_COUNT, 0);
		for (int index = 0; index < count; index++) {
			mEditor.remove("KEY" + index);
			mEditor.remove("PATH" + index);			
		}
		mEditor.remove(BOOKMARK_COUNT);
	}

	public void commitBookmarks() {
		clearAllBookmarks();
		FeBookmark bm = null;
		for (int index = 0; index < mBookmarks.size(); index++) {
			bm = mBookmarks.get(index);
			mEditor.putString("KEY" + index, bm.name);
			mEditor.commit();
			mEditor.putString("PATH" + index, bm.path);
			mEditor.commit();
		}
		mEditor.putInt(BOOKMARK_COUNT, mBookmarks.size());
		mEditor.commit();
	}

	public List<String> list() {
		List<String> bm_list = new ArrayList<String>();
		bm_list.clear();
		for (int index = 0; index < mBookmarks.size(); index++) {
			bm_list.add(mBookmarks.get(index).name);
		}
		return bm_list;
	}

	public boolean isAdded(String name, String path) {
		FeBookmark bm;
		for (int index = 0; index < mBookmarks.size(); index++) {
			bm = mBookmarks.get(index);
			if (name.compareTo(bm.name) == 0) {
				if (path.compareTo(bm.path) == 0) {
					return true;
				}
			}
		}
		return false;
	}

	public String getPath(int id) {
		if (id > mBookmarks.size()) {
			return null;
		}
		return mBookmarks.get(id).path;
	}

	public FeBookmark getBookmark(int id) {
		if (id > mBookmarks.size()) {
			return null;
		}
		return mBookmarks.get(id);
	}

	public String getFullPath(int id) {
		FeBookmark bm = getBookmark(id);
		if (bm == null || bm.path == null)
			return null;
		if (bm.path.compareTo("/") == 0) {
			return bm.path + bm.name;
		} else {
			if (bm.path.indexOf("smb://") == 0) {
				return bm.path + "/" + bm.name + "/";
			} else {
				return bm.path + "/" + bm.name;
			}
		}
	}

	public void add(String name, String path) {
		if (name == null || path == null) return;
		FeBookmark bm = new FeBookmark();
		bm.name = name;
		bm.path = path;
		mBookmarks.add(bm);
		commitBookmarks();
	}

	public void remove(int id) {
		if (id > mBookmarks.size()) {
			return;
		}
		mBookmarks.remove(id);
		commitBookmarks();
	}

	public int getCount() {
		return mBookmarks.size();
	}
}
