package xcxin.filexpert;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xcxin.filexpert.FileLister.ThumbGetterListener;
import xcxin.filexpert.Batch.WorkItem;
import android.app.ActivityManager.RunningTaskInfo;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FileAdapter extends ArrayAdapter<String> implements
		OnClickListener {

	private List<String> items;
	private LayoutInflater mInflater;
	private PackageManager mPm;
	private FileLister mFl;
	private ThumbGetterListener mThumbGetter;
	private List<Integer> mSelectedApp;
	private List<Integer> mSelectedTasks;

	public FileAdapter(Context context, int textViewResourceId,
			List<String> objects, FileLister.ThumbGetterListener thumbGetter) {
		super(context, textViewResourceId, objects);
		items = objects;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPm = context.getPackageManager();
		mFl = (FileLister) context;
		mThumbGetter = thumbGetter;
	}

	/*
	 * ViewHolder is a helper class that helps to improve the getView
	 * performance. Do the same work just once.
	 */
	private class ViewHolder {
		public TextView tv;
		public TextView tv_file_size;
		public TextView tv_file_last_mod;
		public ImageView iv;
		public CheckedTextView ctv_sel;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.ArrayAdapter#getView(int, android.view.View,
	 * android.view.ViewGroup)
	 * 
	 * According to document, we must override this method to support complex
	 * ListView elements
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		FileAdapter.ViewHolder vh;
		long size;
		if (convertView == null) {
			FeUtil.gc();
			vh = new FileAdapter.ViewHolder();
			switch (mFl.getListerMode()) {
			case FileLister.GRID_MODE: // GRID_MODE
				convertView = mInflater.inflate(R.layout.element, null);
				vh.tv = (TextView) convertView.findViewById(R.id.tv_file_name);
				vh.tv_file_size = (TextView) convertView
						.findViewById(R.id.tv_file_size);
				vh.iv = (ImageView) convertView.findViewById(R.id.iv_file_icon);
				break;
			case FileLister.LIST_MODE: // LIST_MODE
				convertView = mInflater.inflate(R.layout.element_list, null);
				vh.tv = (TextView) convertView
						.findViewById(R.id.tv_list_file_name);
				vh.tv_file_size = (TextView) convertView
						.findViewById(R.id.tv_list_file_size);
				vh.tv_file_last_mod = (TextView) convertView
						.findViewById(R.id.tv_list_file_last_mod);
				vh.iv = (ImageView) convertView
						.findViewById(R.id.iv_list_file_icon);
				vh.ctv_sel = (CheckedTextView) convertView
						.findViewById(R.id.ctv_sel);
				switch (mFl.mRunningMode) {
				case FileLister.LOCAL_DIR_MODE:
				case FileLister.REMOTE_DIR_MODE:
					vh.ctv_sel.setVisibility(View.VISIBLE);
					vh.ctv_sel.setOnClickListener(this);
					vh.ctv_sel.setChecked(isSelected(items.get(position)));
					break;
				case FileLister.APP_MODE:
					vh.ctv_sel.setVisibility(View.VISIBLE);
					vh.ctv_sel.setOnClickListener(this);
					vh.ctv_sel.setChecked(isAppSelected(position));
					break;
				case FileLister.TASK_MODE:
					vh.ctv_sel.setVisibility(View.VISIBLE);
					vh.ctv_sel.setOnClickListener(this);
					vh.ctv_sel.setChecked(isTaskSelected(position));
					break;
				default:
					vh.ctv_sel.setVisibility(View.GONE);
					break;
				}
				break;
			}
			convertView.setTag(vh);
		} else {
			vh = (FileAdapter.ViewHolder) convertView.getTag();
			if (mFl.getListerMode() == FileLister.LIST_MODE) {
				if (vh.ctv_sel != null) {
					switch (mFl.mRunningMode) {
					case FileLister.LOCAL_DIR_MODE:
					case FileLister.REMOTE_DIR_MODE:
						vh.ctv_sel.setVisibility(View.VISIBLE);
						vh.ctv_sel.setOnClickListener(this);
						vh.ctv_sel.setChecked(isSelected(items.get(position)));
						break;
					case FileLister.APP_MODE:
						vh.ctv_sel.setVisibility(View.VISIBLE);
						vh.ctv_sel.setOnClickListener(this);
						vh.ctv_sel.setChecked(isAppSelected(position));
						break;
					case FileLister.TASK_MODE:
						vh.ctv_sel.setVisibility(View.VISIBLE);
						vh.ctv_sel.setOnClickListener(this);
						vh.ctv_sel.setChecked(isTaskSelected(position));
						break;
					default:
						vh.ctv_sel.setVisibility(View.GONE);
						break;
					}
				}
			}
		}

		switch (mFl.getListerMode()) {
		case FileLister.LIST_MODE:
			vh.tv.setTextColor(mFl.getResources().getColor(
					android.R.color.white));
			vh.tv_file_size.setTextColor(mFl.getResources().getColor(
					android.R.color.white));
			if(vh.tv_file_last_mod != null) vh.tv_file_last_mod.setTextColor(mFl.getResources().getColor(
					android.R.color.white));
			break;
		case FileLister.GRID_MODE:
			vh.tv.setTextColor(mFl.getResources().getColor(
					android.R.color.white));
			vh.tv_file_size.setTextColor(mFl.getResources().getColor(
					android.R.color.white));
			if (isSelected(items.get(position)) == true) {
				vh.tv.setTextColor(Color.RED);
			}
			break;
		}

		if (mFl.mRunningMode == FileLister.APP_MODE) {
			vh.tv.setText(mPm.getApplicationLabel(mFl.mAllAppInfo.get(position)));
			vh.tv_file_size.setText("");
			Drawable icon = mPm.getApplicationIcon(mFl.mAllAppInfo
					.get(position));
			if (icon != null) {
				vh.iv.setImageDrawable(icon);
			} else {
				vh.iv.setImageDrawable(mFl.getPackageManager()
						.getDefaultActivityIcon());
			}
			if (mFl.getListerMode() == 1) {
				vh.tv_file_last_mod.setText("");
			}
			if (isAppSelected(position) == true
					&& mFl.getListerMode() == FileLister.GRID_MODE) {
				vh.tv.setTextColor(Color.RED);
			} else {
				vh.tv.setTextColor(mFl.getResources().getColor(
						android.R.color.white));
			}
			return convertView;
		} else if (mFl.mRunningMode == FileLister.TASK_MODE) {
			vh.tv.setText(items.get(position));
			RunningTaskInfo pti = mFl.mTaskManager.getRunningTaskInfo().get(
					position);
			Drawable icon = FePackage.getIcon(pti.baseActivity,
					mFl.getPackageManager());
			if (icon != null) {
				vh.iv.setImageDrawable(icon);
			} else {
				vh.iv.setImageResource(R.drawable.task);
			}
			if (mFl.getListerMode() == FileLister.LIST_MODE) {
				vh.tv_file_last_mod.setText("");
			}
			vh.tv_file_size.setText("");
			if (isTaskSelected(position) == true
					&& mFl.getListerMode() == FileLister.GRID_MODE) {
				vh.tv.setTextColor(Color.RED);
			} else {
				vh.tv.setTextColor(mFl.getResources().getColor(
						android.R.color.white));
			}
			return convertView;
		} else if (mFl.mRunningMode == FileLister.SMB_SERVER_MODE) {
			vh.tv.setText(mFl.mSmbServerMgr.getIp(position));
			vh.iv.setImageResource(R.drawable.server);
			if (mFl.mSmbServerMgr.isAllowAny(position) == true) {
				vh.tv_file_size.setText(mFl.getString(R.string.any_access));
			} else {
				vh.tv_file_size
						.setText(mFl.getString(R.string.password_access));
			}
			if (mFl.getListerMode() == FileLister.LIST_MODE) {
				vh.tv_file_last_mod.setText("");
			}
			return convertView;
		}

		String str = items.get(position);
		if (str != null) {
			if (mFl.getListerMode() == 0) {
				if (str.length() > 8) {
					vh.tv.setText(" " + str.substring(0, 5) + "...");
				} else {
					vh.tv.setText(" " + str);
				}
			} else {
				vh.tv.setText(str);
				if (mFl.mSimpleList == false) {
					vh.tv_file_last_mod.setText(getLastModified(mFl.getFileObj(
							str).lastModified()));
				} else {
					vh.tv_file_last_mod.setText("");
				}
			}

			if (mFl.mRunningMode == FileLister.BOOKMARK_MODE) {
				if (mFl.getListerMode() == FileLister.GRID_MODE) {
					if (mFl.mBookmarkMgr.getPath(position).indexOf("smb://") == 0) {
						vh.tv_file_size.setText(mFl.getString(R.string.remote));
					} else {
						vh.tv_file_size.setText(mFl.getString(R.string.local));
					}
				} else {
					// We have bug here.....
					// Some users found that we cannot get strings but null
					// returned
					String path = mFl.mBookmarkMgr.getPath(position);
					if (path != null) {
						// Update directory record
						if (path.indexOf("smb:\\") == 0) {
							// We need to remove user name & password!!
							int start = path.indexOf('@') + 1;
							path = path.substring(start, path.length());
						}
						if (path.length() > 30) {
							path = path.substring(0, 27) + "...";
						}
					}
				}
				size = 0;
			} else if (mFl.mRunningMode == FileLister.CUSTOMIZE_FS_MODE) {
				size = FileOperator.getSize(
						mFl.mContentsContainer.items.get(position), str);
			} else {
				try {
					size = FileOperator.getSize(mFl.getCurrentPath(), str);
				} catch (Exception e) {
					size = 0;
				}
			}

			if (mFl.mRunningMode != FileLister.BOOKMARK_MODE) {
				if (mFl.mSimpleList == false
						|| mFl.getListerMode() == FileLister.GRID_MODE) {
					if (size == -1 || size == 0) {
						vh.tv_file_size.setText(" ");
					} else {
						vh.tv_file_size
								.setText(FeUtil.getFileSizeShowStr(size));
					}
				} else {
					vh.tv_file_size.setText(" ");
				}
			}

			boolean isDir;
			if (mFl.mRunningMode == FileLister.BOOKMARK_MODE) {
				isDir = mFl.isDirectory(position);
				str = mFl.mBookmarkMgr.getFullPath(position);
			} else if (mFl.mRunningMode == FileLister.CUSTOMIZE_FS_MODE) {
				isDir = mFl.mFsContentsMgr.isDir(position);
				str = mFl.mFsContentsMgr.getFullPath(position);
			} else {
				isDir = mFl.isDirectory(str);
			}
			if (str != null) {
				vh.iv.setTag(mFl.getFullPath(str));
				if (isDir == true) {
					vh.iv.setImageResource(R.drawable.folder);
				} else if (mFl.isFile(str) == true
						|| mFl.mRunningMode == FileLister.CUSTOMIZE_FS_MODE
						|| mFl.mRunningMode == FileLister.BOOKMARK_MODE) {
					if (FileOperator.isAudioFile(str) == true) {
						setThumbIcon(vh.iv, str, R.drawable.audio);
					} else if (FileOperator.isImageFile(str) == true) {
						setThumbIcon(vh.iv, str, R.drawable.image);
					} else if (FileOperator.isPdfFile(str) == true) {
						vh.iv.setImageResource(R.drawable.pdf_icon);
					} else if (FileOperator.isZipFile(str) == true) {
						vh.iv.setImageResource(R.drawable.zip);
					} else if (FileOperator.isRarFile(str) == true) {
						vh.iv.setImageResource(R.drawable.rar);
					} else if (FileOperator.isWordFile(str) == true) {
						vh.iv.setImageResource(R.drawable.doc);
					} else if (FileOperator.isPowerPointFile(str) == true) {
						vh.iv.setImageResource(R.drawable.ppt);
					} else if (FileOperator.isExcelFile(str) == true) {
						vh.iv.setImageResource(R.drawable.xls);
					} else if (FileOperator.isApkPackage(str) == true) {
						Drawable icon = getApkIcon(str);
						if (icon != null) {
							vh.iv.setImageDrawable(icon);
						} else {
							vh.iv.setImageDrawable(mPm.getDefaultActivityIcon());
						}
					} else if (FileOperator.isVideoFile(str) == true) {
						setThumbIcon(vh.iv, str, R.drawable.video);
					} else {
						vh.iv.setImageResource(R.drawable.file);
					}
				} else {
					vh.iv.setImageResource(R.drawable.file);
				}
			} else {
				// Something wrong....
				Log.v("FE",
						"Unexpected software execution path: Couldn't get path string. Position id: "
								+ position + ", RunningMode: "
								+ mFl.mRunningMode);
			}
		}
		return convertView;
	}
	
	private void setThumbIcon(ImageView iv, String fileName, int resId) {
		if (mFl.mSettings.isShowThumbnails()) {
			String pathName = mFl.getFullPath(fileName);
			Drawable thumb = mThumbGetter.getThumb(pathName);
			if (thumb != null) {
				iv.setImageDrawable(thumb);
			} else {
				iv.setImageResource(resId);
				mThumbGetter.start(pathName, false);
			}
		} else {
			iv.setImageResource(resId);
		}
	}

	@Override
	public int getCount() {
		if (mFl.mRunningMode != FileLister.APP_MODE) {
			return items.size();
		} else if (mFl.isBookmarkMode() == true) {
			return mFl.mBookmarkMgr.getCount();
		} else {
			return mFl.mAllAppInfo.size();
		}
	}

	@Override
	public String getItem(int position) {
		switch (mFl.mRunningMode) {
		case FileLister.APP_MODE:
			return mFl.mAllAppInfo.get(position).name;
		default:
			return items.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void AddItem(String item) {
		items.add(item);
	}

	public void ClearList() {
		items.clear();
		Update();
	}

	public void Update() {
		notifyDataSetChanged();
	}

	public Drawable getApkIcon(String apk) {
		if (mFl.mRunningMode == FileLister.REMOTE_DIR_MODE) {
			return mPm.getDefaultActivityIcon();
		}
		String fullpath;
		if (mFl.mRunningMode == FileLister.CUSTOMIZE_FS_MODE) {
			fullpath = apk;
		} else {
			fullpath = mFl.getFullPath(apk);
		}
		if (mFl.mSettings.isShowApkIcon()) {
			mThumbGetter.start(fullpath, false);
			return mThumbGetter.getThumb(fullpath);
		}
		return mPm.getDefaultActivityIcon();
	}

	protected String getLastModified(long l) {
		Date date = new Date(l);
		return date.toLocaleString();
	}

	@Override
	public void onClick(View v) {
		CheckedTextView ctv = (CheckedTextView) v;
		ctv.toggle();
		LinearLayout ll = (LinearLayout) ((RelativeLayout) v.getParent())
				.findViewById(R.id.ll_list);
		TextView tv = (TextView) ll.findViewById(R.id.tv_list_file_name);
		if (ctv.isChecked()) {
			switch (mFl.mRunningMode) {
			case FileLister.LOCAL_DIR_MODE:
			case FileLister.REMOTE_DIR_MODE:
				WorkItem wi = new WorkItem();
				wi.mSrcName = new String(items.get(getPos(tv.getText()
						.toString())));
				wi.mSrcPath = new String(mFl.getCurrentPath());
				addSel(wi);
				break;
			case FileLister.APP_MODE:
				addAppSel(getPos(tv.getText().toString()));
				break;
			case FileLister.TASK_MODE:
				addTaskSel(getPos(tv.getText().toString()));
				break;
			}
		} else {
			switch (mFl.mRunningMode) {
			case FileLister.LOCAL_DIR_MODE:
			case FileLister.REMOTE_DIR_MODE:
				removeSel(items.get(getPos(tv.getText().toString())));
				break;
			case FileLister.APP_MODE:
				removeAppSel(getPos(tv.getText().toString()));
				break;
			case FileLister.TASK_MODE:
				removeTaskSel(getPos(tv.getText().toString()));
				break;
			}
		}
	}

	public void selectAll() {
		for (int index = 0; index < items.size(); index++) {
			switch (mFl.mRunningMode) {
			case FileLister.LOCAL_DIR_MODE:
				if (isSelected(items.get(index)) == false) {
					WorkItem wi = new WorkItem();
					wi.mSrcName = new String(items.get(index));
					wi.mSrcPath = new String(mFl.getCurrentPath());
					addSel(wi);
				}
				break;
			case FileLister.APP_MODE:
				if (isAppSelected(index) == false) {
					addAppSel(index);
				}
				break;
			case FileLister.TASK_MODE:
				if (isTaskSelected(index) == false) {
					addTaskSel(index);
				}
				break;
			}
		}
	}

	public void removeAllSel() {
		mSelectedApp = null;
		mSelectedTasks = null;
		FeUtil.gc();
	}

	public void addSel(WorkItem wi) {
		mFl.mWiMgr.add(wi);
	}

	public void addAppSel(Integer app_index) {
		if (mSelectedApp == null) {
			mSelectedApp = new ArrayList<Integer>();
		}
		mSelectedApp.add(app_index);
	}

	public void addTaskSel(Integer task_index) {
		if (mSelectedTasks == null) {
			mSelectedTasks = new ArrayList<Integer>();
		}
		mSelectedTasks.add(task_index);
	}

	public void removeSel(String SrcName) {
		WorkItem wi = new WorkItem();
		wi.mSrcName = SrcName;
		wi.mSrcPath = mFl.getCurrentPath();
		mFl.mWiMgr.remove(wi);
	}

	public void removeAppSel(Integer app_index) {
		if (mSelectedApp == null) {
			return;
		}
		for (int index = 0; index < mSelectedApp.size(); index++) {
			if (mSelectedApp.get(index) == app_index) {
				mSelectedApp.remove(index);
			}
		}
		if (mSelectedApp.size() == 0) {
			mSelectedApp = null;
		}
		FeUtil.gc();
		// System.gc();
	}

	public void removeTaskSel(Integer task_index) {
		if (mSelectedTasks == null) {
			return;
		}
		for (int index = 0; index < mSelectedTasks.size(); index++) {
			if (mSelectedTasks.get(index) == task_index) {
				mSelectedTasks.remove(index);
			}
		}
		if (mSelectedTasks.size() == 0) {
			mSelectedTasks = null;
		}
		FeUtil.gc();
	}

	public void removeAllAppSel() {
		mSelectedApp = null;
		FeUtil.gc();	}

	public void removeAllTaskSel() {
		mSelectedTasks = null;
		FeUtil.gc();
		// System.gc();
	}

	private boolean isSelected(String SrcName) {
		WorkItem wi = new WorkItem();
		wi.mSrcName = SrcName;
		wi.mSrcPath = mFl.getCurrentPath();		
		return mFl.mWiMgr.isAdded(wi);
	}

	private boolean isAppSelected(Integer app_index) {
		if (mSelectedApp == null)
			return false;
		for (int index = 0; index < mSelectedApp.size(); index++) {
			if (mSelectedApp.get(index) == app_index) {
				return true;
			}
		}
		return false;
	}

	private boolean isTaskSelected(Integer task_index) {
		if (mSelectedTasks == null)
			return false;
		for (int index = 0; index < mSelectedTasks.size(); index++) {
			if (mSelectedTasks.get(index) == task_index) {
				return true;
			}
		}
		return false;
	}

	public List<Integer> getAppSel() {
		return mSelectedApp;
	}

	public List<Integer> getTasksSel() {
		return mSelectedTasks;
	}

	private int getPos(String str) {
		if (str == null)
			return -1;
		for (int index = 0; index < items.size(); index++) {
			String itm_str = items.get(index);
			if (itm_str.compareTo(str) == 0) {
				return index;
			}
		}
		return -1;
	}
	
	public boolean hasAppSel() {
		if (mSelectedApp != null)
			return true;
		return false;
	}

	public boolean hasTaskSel() {
		if (mSelectedTasks != null)
			return true;
		return false;
	}
}