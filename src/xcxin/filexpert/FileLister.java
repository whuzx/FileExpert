package xcxin.filexpert;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import xcxin.filexpert.Batch.AppBatch;
import xcxin.filexpert.Batch.ExecuteBatch;
import xcxin.filexpert.Batch.FileCopyWorker;
import xcxin.filexpert.Batch.SilentPackageInstaller;
import xcxin.filexpert.Batch.FileDeleteWorker;
import xcxin.filexpert.Batch.TaskKillBatch;
import xcxin.filexpert.Batch.WorkItem;
import xcxin.filexpert.Batch.WorkItemMgr;
import xcxin.filexpert.ProgressDlg.FeProgressDialog;
import xcxin.filexpert.Thumbnails.ThumbGetter;
import android.app.Activity;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.mobclick.android.MobclickAgent;

public class FileLister extends TabActivity implements
		DialogInterface.OnClickListener, View.OnClickListener,
		OnItemClickListener, OnTabChangeListener, AppVersionChecker {

	public FileAdapter mContentsContainer;
	private List<String> mContents = new ArrayList<String>();
	public List<ApplicationInfo> mAllAppInfo = null;;
	private GridView mContentsGridView;
	private ListView mContentsListView;
	private PathManager mPathMgr;
	private String mStartDir;
	private FileOperator.DeleteHelper mDelHelper = null;
	public AtomicInteger m_sync;
	public AdapterContextMenuInfo mActiviedContextMenuInfo;
	private int mListerMode;
	public SmbConvertServer mSmbConvertServer = null;
	private String mShareIp = null;
	private PowerManager mPm;
	private WifiManager mWm;
	PowerManager.WakeLock mWakeLock;
	WifiManager.WifiLock mWifiLock;
	public FileExpertSettings mSettings;
	public FeBookmarkMgr mBookmarkMgr;
	public SmbServerMgr mSmbServerMgr;
	public WorkItemMgr mWiMgr;
	public FsContentsMgr mFsContentsMgr;
	public FeTaskManager mTaskManager;
	public NotifyMgr mNm;
	private TabHost mTabHost;
	private boolean m_file_cut = false;
	public int m_sort_mode;
	private SmbConvertServer mSmbStreamServer;

	// ----------- For zip viewer ------------
	private List<String> mZipRootList = null;
	private String mCurZipDir = null;
	private boolean mIsZipViewer = false;
	// ---------------------------------------
	private SmbIpDlg mSmbIpDlg = null;

	private String smb_domain;
	private String smb_ip;
	private String smb_user;
	private String smb_pass;
	private String smb_remote_path;
	private boolean smb_any;
	private int m_smb_dlg_mode;

	private boolean m_TabDisplay = true;
	private TabWidget mTabWidget;

	private ExecuteBatch mExecuteWorker;
	private AppBatch mAppBatchWorker;
	private TaskKillBatch mTaskKillWorker;
	private SilentPackageInstaller mSilentPackageInstaller;
	private FileCopyWorker m_copy_worker;
	private WorkItem wi;

	private FeFile mSearchDir;
	private SearchDlg mSearchDlg;

	public final static int DIR_FORWARD = 0;
	public final int DIR_BACK = 1;

	public static final int GRID_MODE = 0;
	public static final int LIST_MODE = 1;

	public static final int SORT_BY_NAME = 0;
	public static final int SORT_BY_TYPE = 1;
	public static final int SORT_BY_SIZE = 2;
	public static final int SORT_BY_LAST_MODIFY = 4;

	public static final int LOCAL_TAB = 0;
	public static final int NETWORK_TAB = 1;
	public static final int BOOKMARK_TAB = 2;
	public static final int APP_TAB = 3;

	public static final int NO_BATCH = -1;
	public static final int BATCH_COPY = 0;
	public static final int BATCH_CUT = 1;
	public static final int BATCH_DELETE = 2;
	public static final int BATCH_EXECUTE = 3;
	public static final int BATCH_APP_UNINSTALL = 4;
	public static final int BATCH_APP_BACKUP = 5;
	public static final int BATCH_TASK_KILL = 6;
	public int mBatchMode;

	public static final int LOCAL_DIR_MODE = 0;
	public static final int REMOTE_DIR_MODE = 1;
	public static final int BOOKMARK_MODE = 2;
	public static final int SMB_SERVER_MODE = 3;
	public static final int APP_MODE = 4;
	public static final int CUSTOMIZE_FS_MODE = 5;
	public static final int TASK_MODE = 6;
	public static final int GET_CONTENT_MODE = 7;
	public int mRunningMode;

	private boolean mIsFileSelector = false;

	private boolean m_goto_in_progress = false;

	private int m_dlg_type;
	public static final int NO_DLG = -1;
	public static final int DELETE_CONFIRM = 0;
	public static final int ABOUT_BOX = 1;
	public static final int CHANGE_LOG = 2;
	public static final int INFO_DLG = 3;
	public static final int SMB_SERVER_WIFI_WARN = 4;
	public static final int MOUNT_WARN = 5;
	public static final int SMB_SEARCH_CONFIRM = 6;
	public static final int INFO_COLLECT_CONFIRM = 7;
	public static final int BATCH_DELETE_CONFIRM = 8;
	public static final int DONATE_DLG = 9;
	public static final int WHATSNEW_DLG = 10;
	public static final int ADD_SMB_SERVER = 11;

	public static final int PAYPAL_REQUEST = 0x10011;

	private SmbServerMgr.smbServer mSmbServer;

	public boolean mMulitiMode;

	private String curPath = null;
	private String curLocalPath = null;
	private String curRemotePath = "fe://smbservers";

	private boolean m_MainBarDisplay;

	public boolean mDonatePlugin = false;
	public boolean mSmbMountPlugin = false;

	private boolean mDonateDlgDisplayed = false;

	private String mDetailPath;
	private DetailDlg detailDlg;

	private SpeechEngine mSpeech;
	private ThumbGetterListener mThumbGetter;

	public static final int img_btn_id[] = { R.id.btn_back, R.id.btn_exit,
			R.id.btn_home, R.id.btn_http, R.id.btn_ftp,
			R.id.btn_file_operation, R.id.btn_main_tbar, R.id.btn_new,
			R.id.btn_file_copy, R.id.btn_file_cut, R.id.btn_file_delete,
			R.id.btn_settings, R.id.btn_app_backup, R.id.btn_app_uninstall,
			R.id.btn_sel_all, R.id.btn_sel_all_1, R.id.btn_sel_all_2,
			R.id.btn_clr_all, R.id.btn_clr_all_1, R.id.btn_clr_all_2,
			R.id.btn_back_1, R.id.btn_paste_1, R.id.btn_mul_on,
			R.id.btn_mul_off, R.id.btn_mul_on_1, R.id.btn_mul_off_1,
			R.id.btn_mul_on_2, R.id.btn_mul_off_2, R.id.btn_chg_view,
			R.id.btn_chg_view_1, R.id.btn_search, R.id.btn_detail,
			R.id.btn_kill_task, R.id.btn_speech };

	public boolean isRoot = false;
	public String mMount_to = null;

	private boolean mBackPressedFirst;
	private boolean mShowToolbar;
	public boolean mSimpleList;

	private int iSmbPort;
	private boolean mIsOpenSettings;
	private ServerController mServerController;

	private String mLastTag;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initApp("onCreate", savedInstanceState);
	}

	public void initApp(String caller, Bundle savedInstanceState) {
		mSettings = new FileExpertSettings(this);
		mThumbGetter = new ThumbGetterListener(this);
		mServerController = new ServerController(this);
		mSimpleList = mSettings.isSimpleList();
		setContentView(R.layout.main);
		mContentsContainer = new FileAdapter(this, R.layout.element, mContents,
				mThumbGetter);
		startFileListerInit();
		mPathMgr = new PathManager();

		RootShell.enableRoot(true);
		
		getStartDir();
		Intent intent = getIntent();
		if (intent != null) {
			if (intent.getAction() != null
					&& intent.getAction().equals(Intent.ACTION_GET_CONTENT)) {
				mIsFileSelector = true;
				Uri uri = intent.getData();
				if (uri != null && uri.toString().startsWith("file://")) {
					mStartDir = uri.getPath();
				}
			}
		}

		initView();
		if (mSettings.getDefListMode() == false) {
			setListerMode(GRID_MODE, false, true);
		} else {
			setListerMode(LIST_MODE, false, true);
		}

		// Set Tabs
		mTabHost = this.getTabHost();
		mTabWidget = (TabWidget) this.findViewById(android.R.id.tabs);
		mTabHost.addTab(mTabHost
				.newTabSpec("local_tab")
				.setIndicator(getString(R.string.local),
						getResources().getDrawable(R.drawable.local_tab))
				.setContent(R.id.tv_test));
		mTabHost.addTab(mTabHost
				.newTabSpec("network_tab")
				.setIndicator(getString(R.string.remote),
						getResources().getDrawable(R.drawable.network_tab))
				.setContent(R.id.tv_test));
		mTabHost.addTab(mTabHost
				.newTabSpec("bookmark_tab")
				.setIndicator(getString(R.string.favourite),
						getResources().getDrawable(R.drawable.bookmarks_tab))
				.setContent(R.id.tv_test));
		mTabHost.addTab(mTabHost
				.newTabSpec("app_tab")
				.setIndicator(getString(R.string.program),
						getResources().getDrawable(R.drawable.app_tab))
				.setContent(R.id.tv_test));
		mLastTag = "local_tab";

		if (SysInfo.getSDKVersion() < 8) {
			mTabHost.addTab(mTabHost
					.newTabSpec("task_tab")
					.setIndicator(getString(R.string.tasks),
							getResources().getDrawable(R.drawable.tasks))
					.setContent(R.id.tv_test));
		}

		mTabHost.setCurrentTab(LOCAL_TAB);
		mTabHost.setOnTabChangedListener(this);

		m_sort_mode = mSettings.getSortMode();
		mBatchMode = NO_BATCH;

		m_dlg_type = NO_DLG;

		SetStyle();
		if (savedInstanceState == null) {
			// First run
			rebuildPathStack(mStartDir);
			curPath = mStartDir;
			mMulitiMode = false;
			m_MainBarDisplay = true;
		} else {
			// Restore from backgroud
			mMulitiMode = savedInstanceState.getBoolean("muliti");
			m_MainBarDisplay = savedInstanceState.getBoolean("mainbar");
			curPath = savedInstanceState.getString("path");
			mRunningMode = savedInstanceState.getInt("mode");
			switch (mRunningMode) {
			case FileLister.APP_MODE:
				mTabHost.setCurrentTab(APP_TAB);
				gotoDir("fe://OpenInstalledApplications", DIR_FORWARD);
				break;
			case FileLister.BOOKMARK_MODE:
				mTabHost.setCurrentTab(BOOKMARK_TAB);
				gotoDir("fe://bookmark", DIR_FORWARD);
				break;
			case FileLister.REMOTE_DIR_MODE:
				mTabHost.setCurrentTab(NETWORK_TAB);
				gotoDir(curPath, DIR_FORWARD);
				break;
			case FileLister.LOCAL_DIR_MODE:
				mTabHost.setCurrentTab(LOCAL_TAB);
				rebuildPathStack(curPath);
				gotoDir(curPath, DIR_FORWARD);
				break;
			case FileLister.SMB_SERVER_MODE:
				mTabHost.setCurrentTab(NETWORK_TAB);
				gotoDir("fe://smbservers", DIR_FORWARD);
				break;
			}
		}

		if (mSettings.isInfoCollectAsked() == false) {
			ask4InfoCollect();
		} else {
			if (mSettings.isDonateAsked() == false
					&& mSettings.isUserDonated() == false) {
				ask4Donate();
			} else {
				showChangLog();
			}
		}

		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onError(this);
		}

		processTabsToolbarState();
		refresh();
	}

	protected void startFileListerInit() {
		// Fire off a thread to do some work that we shouldn't do directly in
		// the UI thread
		Thread t = new Thread() {
			public void run() {
				initFileLister();
			}
		};
		t.start();
	}

	private void initFileLister() {
		// Initialize Work Item servers
		mWiMgr = new WorkItemMgr();
		mServerController.updateSharingButtonImg();
		setupAllImageButtons();
		m_sync = new AtomicInteger(0);
		// Initialize bookmark service
		mBookmarkMgr = new FeBookmarkMgr(this);
		// Initialize SmbServer service
		mSmbServerMgr = new SmbServerMgr(this);
		// Initialize FS Contents Mgr
		mFsContentsMgr = new FsContentsMgr();
		// Initialize Task Manager
		mTaskManager = new FeTaskManager(this);
		// Init Speech Service if supported
		mSpeech = new SpeechEngine(this);
		// Init NM
		mNm = new NotifyMgr(this);
		// Get Power Manager & Wifi Manager
		mPm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		mWakeLock = mPm.newWakeLock(PowerManager.FULL_WAKE_LOCK,
				"File Expert Lock");
		mWifiLock = mWm.createWifiLock(WifiManager.WIFI_MODE_FULL, "wifi_lock");
		findAllInstalledPlugins();
		mShowToolbar = true;
		FeUtil.createTempFolderIfNotExisted();
		iSmbPort = 1223;
	}

	private void initView() {
		mContentsGridView = (GridView) this.findViewById(R.id.entry_grid);
		mContentsGridView.setAdapter(mContentsContainer);
		mContentsGridView.setOnItemClickListener(this);
		registerForContextMenu(mContentsGridView);
		mContentsListView = (ListView) this.findViewById(R.id.entry_list);
		mContentsListView.setAdapter(mContentsContainer);
		mContentsListView.setOnItemClickListener(this);
		registerForContextMenu(mContentsListView);
	}

	public class ThumbGetterListener extends ThumbGetter {

		public ThumbGetterListener(Context ctx) {
			super(ctx);
		}

		@Override
		public void onThumbDone(String path, Drawable thumb) {
			ImageView iv = null;

			if (mContentsGridView != null
					&& mContentsGridView.getVisibility() == View.VISIBLE) {
				iv = (ImageView) mContentsGridView.findViewWithTag(path);
			} else if (mContentsListView != null
					&& mContentsListView.getVisibility() == View.VISIBLE) {
				iv = (ImageView) mContentsListView.findViewWithTag(path);
			}

			if (thumb != null && iv != null)
				iv.setImageDrawable(thumb);
		}
	}

	public void setListerMode(int mode, boolean isRefresh) {
		setListerMode(mode, isRefresh, mMulitiMode);
	}

	public void setListerMode(int mode, boolean isRefresh, boolean mulitimode) {
		TextView tv = (TextView) this.findViewById(R.id.tv_view_mode);
		TextView tv_1 = (TextView) this.findViewById(R.id.tv_view_mode_1);
		mListerMode = mode;
		switch (mode) {
		case GRID_MODE:
			mContentsListView.setVisibility(View.GONE);
			mContentsGridView.setVisibility(View.VISIBLE);
			tv.setText(getString(R.string.list_mode));
			tv_1.setText(getString(R.string.list_mode));
			LinearLayout mul_on = (LinearLayout) this
					.findViewById(R.id.ll_mul_on);
			LinearLayout mul_off = (LinearLayout) this
					.findViewById(R.id.ll_mul_off);
			if (mulitimode == true) {
				mul_on.setVisibility(View.GONE);
				mul_off.setVisibility(View.VISIBLE);
			} else {
				mul_on.setVisibility(View.VISIBLE);
				mul_off.setVisibility(View.GONE);
			}
			break;
		case LIST_MODE:
			mContentsGridView.setVisibility(View.GONE);
			mContentsListView.setVisibility(View.VISIBLE);
			tv.setText(getString(R.string.grid_mode));
			tv_1.setText(getString(R.string.grid_mode));
			this.findViewById(R.id.ll_mul_on).setVisibility(View.GONE);
			this.findViewById(R.id.ll_mul_off).setVisibility(View.GONE);
			break;
		default:
			return;
		}
		if (isRefresh == true) {
			refresh();
		}
	}

	public int getListerMode() {
		return mListerMode;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.SYS_EXIT:
			finish();
			break;
		case R.id.SYS_PASTE:
			pasteProcess();
			break;
		case R.id.SYS_ABOUT:
			showAboutInfo();
			break;
		case R.id.SYS_NEW_DIR:
			new NewDirDlg(this, getCurrentPath());
			break;
		case R.id.SYS_NEW_SMB_SERVER:
			createNewSMBServer();
			break;
		case R.id.SYS_SEARCH_SMB_SERVER:
			ask4SearchSmb();
			break;
		case R.id.SYS_MODECHANGE:
			switch (mListerMode) {
			case GRID_MODE:
				setListerMode(LIST_MODE, true);
				break;
			case LIST_MODE:
				setListerMode(GRID_MODE, true);
				break;
			}
			break;
		case R.id.SYS_SETTINGS:
			openSettings();
			break;
		case R.id.SYS_OPEN_SMB_NETWORK:
			mTabHost.setCurrentTabByTag("network_tab");
			break;
		case R.id.SYS_LIST_ALL_APP:
			mTabHost.setCurrentTabByTag("app_tab");
			break;
		case R.id.SYS_OPEN_BOOKMARK:
			mTabHost.setCurrentTabByTag("bookmark_tab");
			break;
		case R.id.SYS_SORT_BY_NAME:
			SortComparator.NameComparator nameCompare = new SortComparator.NameComparator();
			sortItems(nameCompare, getCurrentPath());
			mContentsContainer.Update();
			m_sort_mode = SORT_BY_NAME;
			mSettings.setSortMode(m_sort_mode);
			break;
		case R.id.SYS_SORT_BY_TYPE:
			if (this.mRunningMode == APP_MODE)
				return true;
			SortComparator.TypeComparator typeCompare = new SortComparator.TypeComparator(
					getCurrentPath());
			sortItems(typeCompare, getCurrentPath());
			mContentsContainer.Update();
			m_sort_mode = SORT_BY_TYPE;
			mSettings.setSortMode(m_sort_mode);
			break;
		case R.id.SYS_SORT_BY_LAST_MODIFY:
			if (this.mRunningMode == APP_MODE)
				return true;
			SortComparator.LastModifiedComparator lastModifyCompare = new SortComparator.LastModifiedComparator(
					getCurrentPath());
			sortItems(lastModifyCompare, getCurrentPath());
			mContentsContainer.Update();
			m_sort_mode = SORT_BY_LAST_MODIFY;
			mSettings.setSortMode(m_sort_mode);
			break;
		case R.id.SYS_SORT_BY_SIZE:
			if (this.mRunningMode == APP_MODE)
				return true;
			SortComparator.sizeComparator sizeModifyCompare = new SortComparator.sizeComparator(
					getCurrentPath());
			sortItems(sizeModifyCompare, getCurrentPath());
			mContentsContainer.Update();
			m_sort_mode = SORT_BY_SIZE;
			mSettings.setSortMode(m_sort_mode);
			break;
		case R.id.SYS_BATCH_SELALL:
			mContentsContainer.selectAll();
			refresh();
			break;
		case R.id.SYS_BATCH_CANCELALL:
			mContentsContainer.removeAllSel();
			refresh();
			break;
		case R.id.SYS_BATCH_COPY:
			batchCopyProcess();
			break;
		case R.id.SYS_BATCH_CUT:
			batchCutProcess();
			break;
		case R.id.SYS_BATCH_DELETE:
			ask4BatchDelete();
			break;
		case R.id.SYS_BATCH_EXECUTE:
			batchExecuteProcess();
			break;
		case R.id.SYS_BATCH_BACKUP:
		case R.id.SYS_BATCH_UNINSTALL:
			batchBackupOrUninstall(item.getItemId());
			break;
		case R.id.SYS_CLOSE_SHARE:
			mServerController.shutdownHttpServer(true);
			mServerController.shutdownFtpServer(false);
			break;
		case R.id.SYS_INFO:
			showInfo(SysInfo.getSysInfo(), this.getString(R.string.info), true);
			break;
		case R.id.SYS_HIDE_TABS:
			if (m_TabDisplay == true) {
				mTabWidget.setVisibility(View.GONE);
				item.setTitle(getString(R.string.show_tabs));
				m_TabDisplay = false;
			} else {
				mTabWidget.setVisibility(View.VISIBLE);
				item.setTitle(getString(R.string.hide_tabs));
				m_TabDisplay = true;
			}
			mSettings.setShowTabsState(m_TabDisplay);
			refresh();
			break;
		case R.id.SYS_HIDE_TOOLBAR:
			if (mShowToolbar == true) {
				item.setTitle(getString(R.string.show_toolbar));
				hideToolbar();
			} else {
				item.setTitle(getString(R.string.hide_toolbar));
				showToolbar();
			}
			mSettings.setShowToolBarState(mShowToolbar);
			refresh();
			break;
		case R.id.SYS_DONATE:
			// donateViaMarket();
			// donateViaPaypal();
			donateViaWeb();
			break;
		case R.id.SYS_MOUNT_SYSTEM:
			remount();
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

		switch (mRunningMode) {
		case APP_MODE:
		case TASK_MODE:
			if (mMulitiMode == true) {
				if (mListerMode == FileLister.GRID_MODE) {
					int def_color;
					def_color = getResources().getColor(android.R.color.white);
					TextView tv = (TextView) v.findViewById(R.id.tv_file_name);
					if (tv != null) {
						if (tv.getTextColors().getDefaultColor() == Color.RED) {
							tv.setTextColor(def_color);
							if (mRunningMode == APP_MODE) {
								mContentsContainer.removeAppSel(position);
							} else {
								mContentsContainer.removeTaskSel(position);
							}
						} else {
							tv.setTextColor(Color.RED);
							if (mRunningMode == APP_MODE) {
								mContentsContainer.addAppSel(position);
							} else {
								mContentsContainer.addTaskSel(position);
							}
						}
					}
					return;
				}
			} else {
				if (mRunningMode == APP_MODE) {
					FePackage.runApplication(this, mAllAppInfo.get(position));
				}
			}
			return;
		case SMB_SERVER_MODE:
			openSmbNetwork(mSmbServerMgr.getServer(position), true);
			return;
		}

		if (mMulitiMode == true) {
			if (mListerMode == FileLister.GRID_MODE) {
				int def_color;
				def_color = getResources().getColor(android.R.color.white);
				TextView tv = (TextView) v.findViewById(R.id.tv_file_name);
				if (tv != null) {
					if (tv.getTextColors().getDefaultColor() == Color.RED) {
						tv.setTextColor(def_color);
						mContentsContainer.removeSel(mContents.get(position));
					} else {
						tv.setTextColor(Color.RED);
						WorkItem wi = new WorkItem();
						wi.mSrcName = new String(mContents.get(position));
						wi.mSrcPath = new String(getCurrentPath());
						mContentsContainer.addSel(wi);
					}
				}
				return;
			}
		}

		String FullPath;
		String SelectPath;
		FeFile SelectFile;

		// Get user selected path
		if (this.isBookmarkMode() == true) {
			FullPath = this.mBookmarkMgr.getFullPath(position);
		} else if (this.mRunningMode == FileLister.CUSTOMIZE_FS_MODE) {
			FullPath = this.mFsContentsMgr.getFullPath(position);
			if (mIsZipViewer && this.mFsContentsMgr.isDir(position)) {
				viewZipFile(mZipRootList, FullPath);
				return;
			}
		} else {
			SelectPath = new String(getContentName(position));
			FullPath = getFullPath(SelectPath);
		}

		if (FullPath == null) {
			return;
		}

		SelectFile = new FeFile(FullPath);
		// Is it a file or a directory?
		if (SelectFile.isDirectory()) {
			mBackPressedFirst = false;
			// Directory - let's go into that directory
			// gotoDir(FullPath, DIR_FORWARD);
			gotoDir(SelectFile, DIR_FORWARD);
		} else if (SelectFile.isFile()) {
			if (mIsFileSelector) {
				if (sendContentBack(SelectFile)) {
					mIsFileSelector = false;
					// Maybe we should just make Filexpert hidden here instead
					// of closing this application. But now I don't know the api
					// to hide the app.
					finish();
				}
			} else if (FileOperator.isZipFile(SelectFile.getName())) {
				// Put it here? or put it into
				// FileOperator.perform_file_operation() ?
				try {
					mZipRootList = Compressor.getZipNames(SelectFile);
				} catch (Exception e) {
					return;
				}
				viewZipFile(mZipRootList, SelectFile.getName());
			} else {
				FileOperator.perform_file_operation(SelectFile, this);
			}
		}
	}

	private boolean sendContentBack(FeFile file) {
		// Maybe we should add remote file too.
		if (file.isLocalFile()) {
			Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

			String pathName = file.getPath();
			Uri data = Uri.fromFile(new File(pathName));
			String type = FileOperator.getContentType(FileOperator
					.getExtendFileName(file));
			intent.setDataAndType(data, type);

			setResult(RESULT_OK, intent);

			return true;
		}

		return false;
	}

	private void viewZipFile(List<String> root, String dir) {
		if (dir == null || root == null)
			return;

		try {
			List<String> list = Compressor.getChildNames(root, dir);

			if (list == null/* || list.isEmpty() */)
				return;

			if (!dir.endsWith(File.separator))
				dir += File.separator;

			mFsContentsMgr.clear();

			for (String pathName : list) {
				int sep = pathName.lastIndexOf(File.separator);
				if (pathName.endsWith(File.separator)) {
					mFsContentsMgr.add(dir.substring(0, dir.length() - 1),
							pathName.substring(dir.length(), sep), true);
				} else {
					mFsContentsMgr.add(pathName.substring(0, sep),
							pathName.substring(sep + 1), false);
				}
			}

			mCurZipDir = dir;
			mIsZipViewer = true;

			// int sep = dir.indexOf(File.separator);
			gotoDir(dir/* .substring(0, (sep == -1 ? dir.length() : sep)) */);
		} catch (Exception e) {
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.sysmenu, menu);
		MenuItem mi = menu.getItem(8);
		if (m_TabDisplay == true) {
			mi.setTitle(getString(R.string.hide_tabs));
		} else {
			mi.setTitle(getString(R.string.show_tabs));
		}
		mi = menu.getItem(9);
		if (mShowToolbar == true) {
			mi.setTitle(getString(R.string.hide_toolbar));
		} else {
			mi.setTitle(getString(R.string.show_toolbar));
		}
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (mDonatePlugin == true || mSettings.isUserDonated() == true) {
			// User has already made a donation to us!!
			menu.findItem(R.id.SYS_DONATE).setVisible(false);
		}
		if (this.mListerMode == FileLister.GRID_MODE) {
			menu.findItem(R.id.SYS_BATCH).setVisible(false);
		} else {
			menu.findItem(R.id.SYS_BATCH).setVisible(true);
			menu.findItem(R.id.SYS_BATCH_SELALL).setVisible(true);
			menu.findItem(R.id.SYS_BATCH_CANCELALL).setVisible(true);
			menu.findItem(R.id.SYS_BATCH_COPY).setVisible(false);
			menu.findItem(R.id.SYS_BATCH_DELETE).setVisible(false);
			menu.findItem(R.id.SYS_BATCH_CUT).setVisible(false);
			menu.findItem(R.id.SYS_BATCH_EXECUTE).setVisible(false);
			menu.findItem(R.id.SYS_BATCH_BACKUP).setVisible(false);
			menu.findItem(R.id.SYS_BATCH_UNINSTALL).setVisible(false);
		}
		switch (this.mRunningMode) {
		case FileLister.APP_MODE:
			menu.findItem(R.id.SYS_PASTE).setVisible(false);
			if (mContentsContainer.hasAppSel() == true) {
				menu.findItem(R.id.SYS_BATCH_COPY).setVisible(false);
				menu.findItem(R.id.SYS_BATCH_DELETE).setVisible(false);
				menu.findItem(R.id.SYS_BATCH_CUT).setVisible(false);
				menu.findItem(R.id.SYS_BATCH_EXECUTE).setVisible(false);
				menu.findItem(R.id.SYS_BATCH_BACKUP).setVisible(true);
				menu.findItem(R.id.SYS_BATCH_UNINSTALL).setVisible(true);
			}
			break;
		case FileLister.BOOKMARK_MODE:
		case FileLister.CUSTOMIZE_FS_MODE:
		case FileLister.SMB_SERVER_MODE:
			menu.findItem(R.id.SYS_PASTE).setVisible(false);
			menu.findItem(R.id.SYS_BATCH).setVisible(false);
			break;
		case FileLister.LOCAL_DIR_MODE:
			if (mWiMgr.isReady() == false) {
				;
			} else {
				menu.findItem(R.id.SYS_BATCH_COPY).setVisible(true);
				menu.findItem(R.id.SYS_BATCH_DELETE).setVisible(true);
				menu.findItem(R.id.SYS_BATCH_CUT).setVisible(true);
				menu.findItem(R.id.SYS_BATCH_EXECUTE).setVisible(true);
				menu.findItem(R.id.SYS_BATCH_BACKUP).setVisible(false);
				menu.findItem(R.id.SYS_BATCH_UNINSTALL).setVisible(false);
			}
			menu.findItem(R.id.SYS_PASTE).setVisible(true);
			break;
		case FileLister.REMOTE_DIR_MODE:
			menu.findItem(R.id.SYS_BATCH).setVisible(false);
			menu.findItem(R.id.SYS_PASTE).setVisible(true);
			break;
		}

		if (mServerController.isServerStarted()) {
			menu.findItem(R.id.SYS_CLOSE_SHARE).setVisible(true);
		} else {
			menu.findItem(R.id.SYS_CLOSE_SHARE).setVisible(false);
		}

		if (mMulitiMode == true) {
			menu.findItem(R.id.SYS_BATCH).setVisible(true);
		}

		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
		boolean bFile = false;
		boolean bZipFile = false;
		boolean bLocalFile = false;
		boolean media = false;
		FeFile target = null;
		switch (mRunningMode) {
		case FileLister.LOCAL_DIR_MODE:
			bLocalFile = true;
			target = new FeFile(getFullName(info.position));
			File media_file = new File(target.getPath(), ".nomedia");
			if (media_file.exists() == true) {
				media = true;
			}
			media_file = null;
			break;
		case FileLister.REMOTE_DIR_MODE:
			bLocalFile = false;
			target = new FeFile(getFullName(info.position));
			break;
		case FileLister.CUSTOMIZE_FS_MODE:
			target = new FeFile(mFsContentsMgr.getFullPath(info.position));
			break;
		case FileLister.BOOKMARK_MODE:
			target = new FeFile(this.mBookmarkMgr.getFullPath(info.position));
			break;
		}

		if (target != null) {
			if (target.isDirectory() == false) {
				bFile = true;
				if (FileOperator.extendFileNameCompare(target, "zip") == true) {
					bZipFile = true;
				}
			}
		}

		target = null;
		MenuInflater inflater = getMenuInflater();
		if (mRunningMode == SMB_SERVER_MODE) {
			inflater.inflate(R.menu.smb_server, menu);
			menu.setHeaderTitle(getString(R.string.smb_ser_oper));
		} else if (this.mRunningMode == APP_MODE) {
			inflater.inflate(R.menu.app_menu, menu);
			menu.setHeaderTitle(getString(R.string.package_oper));
		} else if (this.mRunningMode == TASK_MODE) {
			inflater.inflate(R.menu.task_menu, menu);
			menu.setHeaderTitle(getString(R.string.tasks));
		} else if (!mIsZipViewer) {
			inflater.inflate(R.menu.filemenu, menu);
			menu.setHeaderTitle(getString(R.string.operation));
			MenuItem mi = menu.findItem(R.id.FILE_BOOKMARK);
			if (this.isBookmarkMode() == true) {
				menu.findItem(R.id.FILE_ZIP).setVisible(false);
				mi.setTitle(R.string.remove_bookmark);
			} else {
				mi.setTitle(R.string.add_bookmark);
			}
			if (media == true) {
				menu.findItem(R.id.FILE_MEDIA).setTitle(
						getString(R.string.enable_media_pic));
			} else {
				MenuItem mt = menu.findItem(R.id.FILE_MEDIA);
				mt.setTitle(getString(R.string.disable_media_pic));
			}
			switch (mRunningMode) {
			case FileLister.BOOKMARK_MODE:
			case FileLister.CUSTOMIZE_FS_MODE:
			case FileLister.LOCAL_DIR_MODE:
				menu.findItem(R.id.FILE_BOOKMARK).setVisible(true);
				menu.findItem(R.id.FILE_ZIP).setVisible(true);
				menu.findItem(R.id.SMB_MOUNT).setVisible(false);
				if (bFile == true) {
					menu.findItem(R.id.FILE_OPEN_WITH).setVisible(true);
					menu.findItem(R.id.FILE_SHARE).setVisible(false);
					menu.findItem(R.id.INSTALL_ALL_APK).setVisible(false);
					menu.findItem(R.id.FILE_START_DIR).setVisible(false);
					menu.findItem(R.id.FILE_SHARING_DIR).setVisible(false);
					if (bZipFile == false) {
						menu.findItem(R.id.FILE_ZIP).setTitle(
								getString(R.string.zip));
					} else {
						menu.findItem(R.id.FILE_ZIP).setTitle(
								getString(R.string.unzip));
					}
					menu.findItem(R.id.FILE_SEARCH).setVisible(false);
					menu.findItem(R.id.FILE_MEDIA).setVisible(false);
					menu.findItem(R.id.FILE_SHARE_WITH).setVisible(true);
				} else {
					menu.findItem(R.id.FILE_OPEN_WITH).setVisible(false);
					menu.findItem(R.id.FILE_START_DIR).setVisible(bLocalFile);
					menu.findItem(R.id.FILE_SHARING_DIR).setVisible(bLocalFile);
					menu.findItem(R.id.FILE_SHARE).setVisible(true);
					menu.findItem(R.id.INSTALL_ALL_APK).setVisible(true);
					menu.findItem(R.id.FILE_ZIP).setTitle(
							getString(R.string.zip));
					menu.findItem(R.id.FILE_SEARCH).setVisible(true);
					menu.findItem(R.id.FILE_MEDIA).setVisible(true);
					menu.findItem(R.id.FILE_SHARE_WITH).setVisible(false);
				}
				break;
			case FileLister.REMOTE_DIR_MODE:
				menu.findItem(R.id.FILE_START_DIR).setVisible(false);
				menu.findItem(R.id.FILE_SHARE).setVisible(false);
				menu.findItem(R.id.FILE_ZIP).setVisible(false);
				menu.findItem(R.id.INSTALL_ALL_APK).setVisible(false);
				menu.findItem(R.id.FILE_SEARCH).setVisible(false);
				menu.findItem(R.id.FILE_SHARING_DIR).setVisible(false);
				menu.findItem(R.id.FILE_SHARE_WITH).setVisible(false);
				if (bFile == false) {
					// menu.findItem(R.id.SMB_MOUNT).setVisible(true);
				} else {
					menu.findItem(R.id.SMB_MOUNT).setVisible(false);
				}
				menu.findItem(R.id.FILE_MEDIA).setVisible(false);
				if (mSmbMountPlugin == true) {
					menu.findItem(R.id.SMB_MOUNT).setVisible(true);
				} else {
					menu.findItem(R.id.SMB_MOUNT).setVisible(false);
				}
				break;
			}
		}

		super.onCreateContextMenu(menu, v, menuInfo);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item
				.getMenuInfo();
		if (info != null) {
			mActiviedContextMenuInfo = info;
		}
		switch (item.getItemId()) {
		case R.id.FILE_CUT:
		case R.id.FILE_COPY:
			if (item.getItemId() == R.id.FILE_COPY) {
				m_file_cut = false;
			} else {
				m_file_cut = true;
			}
			if (wi != null) {
				wi = null;
			}
			wi = new WorkItem();
			m_sync.set(0);
			switch (this.mRunningMode) {
			case FileLister.BOOKMARK_MODE:
				wi.mSrcPath = this.mBookmarkMgr
						.getPath(mActiviedContextMenuInfo.position);
				break;
			case FileLister.CUSTOMIZE_FS_MODE:
				wi.mSrcPath = this.mFsContentsMgr
						.getPath(mActiviedContextMenuInfo.position);
				break;
			default:
				wi.mSrcPath = getCurrentPath();
				break;
			}
			wi.mSrcName = getContentName(mActiviedContextMenuInfo.position);
			break;
		case R.id.FILE_DELETE:
			mDelHelper = (new FileOperator()).new DeleteHelper(this, m_sync);
			switch (this.mRunningMode) {
			case FileLister.BOOKMARK_MODE:
				mDelHelper.setDelParams(this.mBookmarkMgr
						.getPath(mActiviedContextMenuInfo.position),
						getContentName(mActiviedContextMenuInfo.position));
				mBookmarkMgr.remove(mActiviedContextMenuInfo.position);
				break;
			case FileLister.CUSTOMIZE_FS_MODE:
				mDelHelper.setDelParams(mFsContentsMgr
						.getPath(mActiviedContextMenuInfo.position),
						mFsContentsMgr
								.getName(mActiviedContextMenuInfo.position));
				mFsContentsMgr.remove(mActiviedContextMenuInfo.position);
				break;
			default:
				mDelHelper.setDelParams(getCurrentPath(),
						getContentName(mActiviedContextMenuInfo.position));
				break;
			}
			ask4Delete(getContentName(mActiviedContextMenuInfo.position));
			break;
		case R.id.FILE_RENAME:
			new RenameDlg(this);
			break;
		case R.id.FILE_SHARE_WITH:
			FeFile file = new FeFile(
					getFullName(mActiviedContextMenuInfo.position));
			if (file.isFile()) {
				if (file.isLocalFile()) {
					FeUtil.startShareMediaActivity(file.getFile(), this, false);
				} else {
					// To do : copy it to temp dir first
					;
				}
			}
			break;
		case R.id.INSTALL_ALL_APK:
			FeFile f = new FeFile(
					getFullName(mActiviedContextMenuInfo.position));
			if (f.isDirectory() != true) {
				showInfo(getString(R.string.folder_only),
						getString(R.string.error), false);
			} else {
				if (installAllApk(f) == false) {
					showInfo(getString(R.string.install_error),
							getString(R.string.error), false);
				}
			}
			break;
		case R.id.FILE_ZIP:
			FeFile zip_f = new FeFile(
					getFullName(mActiviedContextMenuInfo.position));
			if (zipUnzip(zip_f) == false) {
				showInfo(getString(R.string.next_version_support),
						getString(R.string.error), false);
			}
			break;
		case R.id.FILE_SHARE_HTTP:
			if (isBookmarkMode() == false) {
				mServerController
						.startHttpSharing(getFullName(mActiviedContextMenuInfo.position));
			} else {
				mServerController.startHttpSharing(mBookmarkMgr
						.getFullPath(mActiviedContextMenuInfo.position));
			}
			break;
		case R.id.FILE_SHARE_FTP:
			if (isBookmarkMode() == false) {
				mServerController
						.startFtpSharing(getFullName(mActiviedContextMenuInfo.position));
			} else {
				mServerController.startFtpSharing(mBookmarkMgr
						.getFullPath(mActiviedContextMenuInfo.position));
			}
			break;
		case R.id.FILE_BOOKMARK:
			if (this.isBookmarkMode() == false) {
				if (this.mBookmarkMgr.isAdded(
						getContentName(mActiviedContextMenuInfo.position),
						getCurrentPath()) == true) {
					showInfo(getString(R.string.already_bookmark),
							getString(R.string.error), false);
					break;
				}
				mBookmarkMgr.add(
						getContentName(mActiviedContextMenuInfo.position),
						getCurrentPath());
			} else {
				mBookmarkMgr.remove(mActiviedContextMenuInfo.position);
				refresh();
			}
			break;
		case R.id.SMB_SERVER_DELETE:
			mSmbServerMgr.remove(mActiviedContextMenuInfo.position);
			mSmbServerMgr.SmbServerUpdateCache();
			refresh();
			break;
		case R.id.SMB_SERVER_EDIT:
			smb_domain = mSmbServerMgr
					.getDomain(mActiviedContextMenuInfo.position);
			smb_ip = mSmbServerMgr.getIp(mActiviedContextMenuInfo.position);
			smb_user = mSmbServerMgr
					.getUserName(mActiviedContextMenuInfo.position);
			smb_pass = mSmbServerMgr
					.getPassword(mActiviedContextMenuInfo.position);
			smb_any = this.mSmbServerMgr
					.isAllowAny(mActiviedContextMenuInfo.position);
			m_smb_dlg_mode = SmbIpDlg.MODIFY_MODE;
			if (this.mSmbIpDlg != null) {
				mSmbIpDlg.setParameters(smb_ip, smb_user, smb_pass, smb_any,
						m_smb_dlg_mode);
			}
			showDialog(R.id.smb_ip_dlg);
			break;
		case R.id.SMB_MOUNT:
			smb_remote_path = getFullName(mActiviedContextMenuInfo.position);
			smb_remote_path = "//"
					+ smb_remote_path
							.substring(smb_remote_path.indexOf(smb_ip));
			smb_remote_path = smb_remote_path.substring(0,
					smb_remote_path.length() - 1);
			ask4Mount();
			break;
		case R.id.FILE_START_DIR:
			String path = getFullName(mActiviedContextMenuInfo.position);
			mSettings.setAutoSdCard(false);
			mSettings.setStartDir(path);
			break;
		case R.id.FILE_SHARING_DIR:
			mSettings
					.setDefaultSharingDir(getFullName(mActiviedContextMenuInfo.position));
			break;
		case R.id.FILE_SEARCH:
			searchProcess(mActiviedContextMenuInfo.position);
			break;
		case R.id.APP_UNINSTALL:
			FePackage.uninstallApp(this,
					mAllAppInfo.get(mActiviedContextMenuInfo.position));
			break;
		case R.id.APP_BACKUP:
			if (mAppBatchWorker != null) {
				mAppBatchWorker = null;
			}
			List<Integer> l = new ArrayList<Integer>();
			l.add(mActiviedContextMenuInfo.position);
			mAppBatchWorker = new AppBatch(mAllAppInfo, this);
			mAppBatchWorker.setMode(AppBatch.APP_BACKUP);
			mAppBatchWorker.setWorkItems(l);
			mBatchMode = FileLister.BATCH_APP_BACKUP;
			FeProgressDialog dlg = new FeProgressDialog(this, mAppBatchWorker);
			dlg.start();
			break;
		case R.id.APP_MARKET:
			try {
				Intent i = FePackage.openAppWithMarketIntent(mAllAppInfo
						.get(mActiviedContextMenuInfo.position));
				if (i != null) {
					startActivity(i);
				}
			} catch (Exception e) {
				;
			}
			break;
		case R.id.FILE_MEDIA:
			FeFile media_file = new FeFile(
					getFullName(mActiviedContextMenuInfo.position), ".nomedia");
			if (media_file.exists() == true) {
				// Enable
				media_file.delete();
			} else {
				// Disable
				OutputStream os = media_file.getOutputStream();
				try {
					os.flush();
					os.close();
					os = null;
				} catch (IOException e) {
					;
				}
			}
			media_file = null;
			showInfo(getString(R.string.reboot_requ), getString(R.string.info),
					false);
			break;
		case R.id.FILE_DETAIL:
			detailProcess(getFullName(mActiviedContextMenuInfo.position));
			break;
		case R.id.TASK_KILL:
			mTaskManager.KillTask(mActiviedContextMenuInfo.position);
			if (mSettings.isInfoCollectAllowed() == true) {
				MobclickAgent.onEvent(this, "Task Kill");
			}
			break;
		case R.id.FILE_OPEN_WITH:
			FileOperator.open_file_with_type(new FeFile(
					getFullName(mActiviedContextMenuInfo.position)), "*/*",
					this);
			break;
		case R.id.FILE_PERMISSION:
			if (RootShell.isRootEnabled()) {
				FilePermissionDlg permDlg = new FilePermissionDlg(this, new FeFile(
						getFullName(mActiviedContextMenuInfo.position)));
				permDlg.show();
			}	
			break;
		default:
			return super.onContextItemSelected(item);
		}
		refresh();
		return true;
	}

	@Override
	protected Dialog onCreateDialog(int id) {

		switch (id) {
		case R.id.new_dir_dlg:
			// return new NewDirDlg(this);
			break;
		case R.id.rename_dlg:
			// return new RenameDlg(this);
			break;
		case R.id.smb_ip_dlg:
			mSmbIpDlg = new SmbIpDlg(this, mSmbServerMgr, smb_domain, smb_ip,
					smb_user, smb_pass, smb_any, m_smb_dlg_mode);
			return mSmbIpDlg;
		case R.id.search_dlg:
			mSearchDlg = new SearchDlg(this);
			mSearchDlg.setTarget(mSearchDir);
			return mSearchDlg;
			// case DONATE_DLG_ID:
			// return new DonateDlg(this);
		case R.id.ll_detail:
			if (mDetailPath != null) {
				detailDlg = new DetailDlg(this, new FeFile(mDetailPath));
				mDetailPath = null;
				return detailDlg;
			}
			break;
		}
		return super.onCreateDialog(id);
	}

	@Override
	public void onTabChanged(String tabId) {

		// We will set current tab during GOTO
		if (m_goto_in_progress == true)
			return;

		if (mLastTag.equals("network_tab")) {
			curRemotePath = getCurrentPath();
			if (!curRemotePath.contains("smb://")) {
				curRemotePath = "fe://smbservers";
			}
		} else {
			if (mLastTag.equals("local_tab")) {
				curLocalPath = getCurrentPath();
			}
		}

		mBackPressedFirst = false;

		// Re-enable context menu
		registerForContextMenu(mContentsGridView);
		registerForContextMenu(mContentsListView);
		if (tabId.compareTo("local_tab") == 0) {
			gotoDir(curLocalPath, DIR_BACK);
			mLastTag = tabId;
		} else if (tabId.compareTo("network_tab") == 0) {
			if (mSmbServerMgr.getCount() == 0) {
				ask4AddSmbServer();
				mTabHost.setCurrentTabByTag("local_tab");
			} else {
				gotoDir(curRemotePath, DIR_BACK);
				mLastTag = tabId;
			}
		} else if (tabId.compareTo("bookmark_tab") == 0) {
			mWiMgr.clear();
			gotoDir("fe://bookmark", DIR_FORWARD);
			mLastTag = tabId;
		} else if (tabId.compareTo("app_tab") == 0) {
			mWiMgr.clear();
			gotoDir("fe://OpenInstalledApplications", DIR_FORWARD);
			mLastTag = tabId;
		}
		if (tabId.compareTo("task_tab") == 0) {
			mWiMgr.clear();
			gotoDir("fe://TaskManager", DIR_FORWARD);
			mLastTag = tabId;
		}
		if (tabId.compareTo("app_tab") != 0) {
			if (mAllAppInfo != null) {
				mAllAppInfo = null;
			}
		}
		FeUtil.gc();
	}

	public void gotoDir(String title) {

		mThumbGetter.stop();

		if (m_goto_in_progress == true)
			return;
		m_goto_in_progress = true;
		if (this.mFsContentsMgr.getCount() == 0 && !mIsZipViewer) {
			m_goto_in_progress = false;
			return;
		}
		mRunningMode = FileLister.CUSTOMIZE_FS_MODE;
		int index = 0;
		mContentsContainer.clear();
		for (index = 0; index < mFsContentsMgr.getCount(); index++) {
			mContentsContainer.add(mFsContentsMgr.getName(index));
		}
		setTitle(title);
		mContentsContainer.Update();
		m_goto_in_progress = false;
		// curPath = title;
	}

	public void gotoDir(String path, int mode) {

		mThumbGetter.stop();

		if (m_goto_in_progress == true)
			return;
		m_goto_in_progress = true;

		if (path == null) {
			m_goto_in_progress = false;
			return;
		}

		if (path.compareTo("fe://bookmark") != 0) {
			if (path.compareTo("fe://OpenInstalledApplications") == 0) {
				mRunningMode = APP_MODE;
				SortComparator.NameComparator nameCompare = new SortComparator.NameComparator();
				sortItems(nameCompare, path);
				setTitle(getString(R.string.installed_app));
				EnterAppOperationMode();
			} else if (path.compareTo("fe://smbservers") == 0) {
				mRunningMode = SMB_SERVER_MODE;
				SortComparator.NameComparator nameCompare = new SortComparator.NameComparator();
				sortItems(nameCompare, path);
				setTitle(getString(R.string.smb_servers));
				EnterSMBServerMode();
			} else if (path.compareTo("fe://TaskManager") == 0) {
				while (mTaskManager == null)
					;
				EnterTaskManagerMode();
				mTaskManager.refreshRunningTaskInfo();
				List<RunningTaskInfo> pti = mTaskManager.getRunningTaskInfo();
				mContentsContainer.clear();
				for (int index = 0; index < pti.size(); index++) {
					RunningTaskInfo info = pti.get(index);
					mContentsContainer.add(mTaskManager.getTaskName(info));
				}
				setTitle(getString(R.string.running_tasks) + " "
						+ getString(R.string.avail_mem) + " : "
						+ FeUtil.getMBfromBytes(mTaskManager.getAvailMemory())
						+ "MB");
				mRunningMode = TASK_MODE;
			} else {
				m_goto_in_progress = false;
				gotoDir(new FeFile(path), mode);
				return;
			}
		} else {
			mRunningMode = BOOKMARK_MODE;
			// Bookmark mode
			List<String> bookmark_name_list = mBookmarkMgr.list();
			int index = 0;
			mContentsContainer.clear();
			for (index = 0; index < bookmark_name_list.size(); index++) {
				mContentsContainer.add(bookmark_name_list.get(index));
			}
			setTitle(getString(R.string.bookmark));
			EnterBookmarkMode();
		}

		mContentsContainer.Update();
		m_goto_in_progress = false;

		if (mode == DIR_BACK) {
			restorePosition(true);
		}
	}

	public void gotoDir(FeFile file, int mode) {

		int pos;

		mThumbGetter.stop();

		if (m_goto_in_progress == true)
			return;
		m_goto_in_progress = true;

		int Count = 0;
		int oldRunningMode = mRunningMode;

		if (file == null) {
			m_goto_in_progress = false;
			return;
		}

		String path = file.getPath();

		// Get the current position
		pos = getVisiablePosition();

		if (!path.equals(curPath)) {
			if (!mSettings.isMulSelectEnabled()) {
				mWiMgr.clear();
			}
		}
		if (mode == DIR_FORWARD) {
			mPathMgr.recordPath(curPath, pos);
		}
		switch (m_sort_mode) {
		case SORT_BY_NAME:
			SortComparator.NameComparator nameCompare = new SortComparator.NameComparator();
			Count = sortItems(nameCompare, file);
			break;
		case SORT_BY_TYPE:
			SortComparator.TypeComparator typeCompare = new SortComparator.TypeComparator(
					path);
			Count = sortItems(typeCompare, file);
			break;
		case SORT_BY_LAST_MODIFY:
			SortComparator.LastModifiedComparator lastModifyCompare = new SortComparator.LastModifiedComparator(
					path);
			Count = sortItems(lastModifyCompare, file);
			break;
		case SORT_BY_SIZE:
			SortComparator.sizeComparator sizeModifyCompare = new SortComparator.sizeComparator(
					path);
			Count = sortItems(sizeModifyCompare, file);
			break;
		}
		if (Count == -1) {
			// Something error?
			if (mRunningMode == FileLister.SMB_SERVER_MODE) {
				showInfo(getString(R.string.smb_server_access_error),
						getString(R.string.warning), false);
			} else {
				showInfo(getString(R.string.no_folder_access_permission),
						getString(R.string.warning), false);
			}
			mRunningMode = oldRunningMode;
			m_goto_in_progress = false;
			return;
		}

		// Directory mode
		EnterFileOperationMode(!m_MainBarDisplay);
		// Update directory record
		if (path.indexOf("smb") == 0) {
			// We need to remove user name & password!!
			int start = path.indexOf('@') + 1;
			setTitle("//" + path.substring(start, path.length()));
		} else {
			setTitle(path);
		}
		curPath = path;

		mContentsContainer.Update();
		m_goto_in_progress = false;

		if (mode == DIR_BACK) {
			restorePosition(true);
		}
	}

	public void refresh() {
		switch (mRunningMode) {
		case REMOTE_DIR_MODE:
		case LOCAL_DIR_MODE:
			mPathMgr.recordPath(getCurrentPath(), getVisiablePosition());
			gotoDir(new FeFile(getCurrentPath()), DIR_BACK);
			break;
		case APP_MODE:
			gotoDir("fe://OpenInstalledApplications", DIR_BACK);
			break;
		case BOOKMARK_MODE:
			gotoDir("fe://bookmark", DIR_BACK);
			break;
		case SMB_SERVER_MODE:
			gotoDir("fe://smbservers", DIR_BACK);
			break;
		case FileLister.CUSTOMIZE_FS_MODE:
			gotoDir(this.getTitle().toString());
			break;
		case FileLister.TASK_MODE:
			gotoDir("fe://TaskManager", DIR_BACK);
			break;
		}
		FeUtil.gc();
	}

	public void Back() {
		if (this.mRunningMode == FileLister.REMOTE_DIR_MODE) {
			String ip = getShareIp();
			int start = ip.indexOf('@') + 1;
			ip = "//" + ip.substring(start, ip.length());
			String title = getTitle().toString();
			if (title.equals(ip)) {
				gotoDir("fe://smbservers", DIR_BACK);
				return;
			} else if (title.equals("fe://smbservers")) {
				mTabHost.setCurrentTabByTag("local_tab");
				return;
			}
		}
		gotoDir(DirTreeHelper.getPreviousDir(getCurrentPath()), DIR_BACK);
	}

	public String getContentName(int position) {
		return mContents.get(position);
	}

	public String getFullPath(String name) {

		String FullPath;
		if (this.mRunningMode == FileLister.CUSTOMIZE_FS_MODE) {
			for (int index = 0; index < this.mFsContentsMgr.getCount(); index++) {
				if (this.mFsContentsMgr.getName(index).compareTo(name) == 0) {
					return this.mFsContentsMgr.getFullPath(index);
				}
			}
		}

		if (getCurrentPath().equals("/")) {
			FullPath = "/" + name;
		} else {
			if (mRunningMode == FileLister.LOCAL_DIR_MODE) {
				FullPath = getCurrentPath() + "/" + name;
			} else {
				FullPath = getCurrentPath() + name;
			}
		}
		return FullPath;
	}

	public String getCurrentPath() {
		return curPath;
	}

	public boolean isDirectory(String path) {

		String FullPath = null;
		FullPath = getFullPath(path);
		if (FullPath == null)
			return false;
		FeFile Oper = new FeFile(FullPath);
		boolean r = Oper.isDirectory();
		Oper = null;
		return r;
	}

	public boolean isDirectory(int bookmark_id) {

		FeBookmark bm = mBookmarkMgr.getBookmark(bookmark_id);
		if (bm == null || bm.path == null)
			return false;
		if (bm.path.compareTo("/") == 0) {
			FeFile Oper = new FeFile(bm.path + bm.name);
			boolean r = Oper.isDirectory();
			Oper = null;
			return r;
		} else {
			FeFile Oper = new FeFile(bm.path + "/" + bm.name);
			boolean r = Oper.isDirectory();
			Oper = null;
			return r;
		}
	}

	public boolean isBookmarkMode() {
		if (this.mRunningMode == FileLister.BOOKMARK_MODE)
			return true;
		return false;
	}

	public FeFile getFileObj(String name) {
		String FullPath = getFullPath(name);
		if (FullPath == null)
			return null;
		FeFile FeOper = new FeFile(FullPath);
		return FeOper;
	}

	public boolean isFile(String path) {
		String FullPath = getFullPath(path);
		if (FullPath == null)
			return false;
		FeFile Oper = new FeFile(FullPath);
		return Oper.isFile();
	}

	public boolean isRootDisplay() {
		if (this.mRunningMode == FileLister.LOCAL_DIR_MODE
				|| this.mRunningMode == FileLister.REMOTE_DIR_MODE) {
			if (getCurrentPath().equals("/"))
				return true;
		}
		return false;
	}

	public void showInfo(String info, String title, boolean showWebBtn) {
		m_dlg_type = INFO_DLG;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		if (showWebBtn == true) {
			builder.setPositiveButton(getString(R.string.go2web), this);
		}
		builder.setNegativeButton(getString(R.string.Okay), this);
		builder.create();
		builder.show();
	}

	public void showInfo(int info, int title, boolean showWebBtn) {
		m_dlg_type = INFO_DLG;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(title);
		builder.setMessage(info);
		if (showWebBtn == true) {
			builder.setPositiveButton(getString(R.string.go2web), this);
		}
		builder.setNegativeButton(getString(R.string.Okay), this);
		builder.create();
		builder.show();
	}

	public void ask4InfoCollect() {
		m_dlg_type = INFO_COLLECT_CONFIRM;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.info_collect_warn));
		builder.setPositiveButton(getString(R.string.info_yes), this);
		builder.setNegativeButton(getString(R.string.info_no), this);
		builder.setTitle(getString(R.string.info_title));
		builder.create();
		builder.show();
	}

	public void ask4Donate() {
		m_dlg_type = DONATE_DLG;
		mDonateDlgDisplayed = true;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.donate_dlg_msg));
		builder.setPositiveButton(getString(R.string.donate_yes), this);
		builder.setNegativeButton(getString(R.string.donate_no), this);
		builder.setTitle(getString(R.string.donation));
		builder.create();
		builder.show();
	}

	public void ask4Delete(String title) {
		m_dlg_type = DELETE_CONFIRM;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.delete_confirm));
		builder.setPositiveButton(getString(R.string.confirm), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.setTitle(title);
		builder.create();
		builder.show();
	}

	public void ask4BatchDelete() {
		if (!this.mWiMgr.isReady()) {
			return;
		}
		m_dlg_type = BATCH_DELETE_CONFIRM;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.delete_confirm));
		builder.setPositiveButton(getString(R.string.confirm), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.setTitle(getString(R.string.delete));
		builder.create();
		builder.show();
	}

	public void ask4SearchSmb() {
		m_dlg_type = SMB_SEARCH_CONFIRM;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.search_smb_war));
		builder.setPositiveButton(getString(R.string.confirm), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.setTitle(getString(R.string.search_smb_svr));
		builder.create();
		builder.show();
	}

	public void ask4Mount() {
		m_dlg_type = MOUNT_WARN;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.mount));
		builder.setMessage(getString(R.string.mount_warn));
		builder.setPositiveButton(getString(R.string.confirm), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.create();
		builder.show();
	}

	public void ask4AddSmbServer() {
		m_dlg_type = ADD_SMB_SERVER;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.warning));
		builder.setMessage(getString(R.string.no_smb_server));
		builder.setPositiveButton(getString(R.string.confirm), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.create();
		builder.show();
	}

	public void showAboutInfo() {
		m_dlg_type = ABOUT_BOX;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(getString(R.string.about));
		builder.setMessage(getString(R.string.about_info));
		builder.setPositiveButton(getString(R.string.go2web), this);
		builder.setNegativeButton(getString(R.string.Okay), this);
		builder.setNeutralButton(getString(R.string.change_log), this);
		builder.create();
		builder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		int dlg_type = m_dlg_type;
		dialog.dismiss();
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			switch (dlg_type) {
			case DELETE_CONFIRM:
				break;
			case INFO_COLLECT_CONFIRM:
				mSettings.setInfoCollectStatus(true);
				mSettings.setInfoCollectAsked();
				if (mSettings.isDonateAsked() == false
						&& mSettings.isUserDonated() == false) {
					this.ask4Donate();
				}
				MobclickAgent.onEvent(this, "No In Collect");
				break;
			case DONATE_DLG:
				mSettings.setDonateAsked();
				break;
			}
			break;
		case DialogInterface.BUTTON_POSITIVE:
			switch (dlg_type) {
			case DELETE_CONFIRM:
				this.deleteOne();
				break;
			case BATCH_DELETE_CONFIRM:
				batchDeleteProcess();
				break;
			case INFO_DLG:
			case ABOUT_BOX:
				Intent i = new Intent(Intent.ACTION_VIEW,
						Uri.parse("http://www.wifisharing.mobi/"));
				this.startActivity(i);
				break;
			case SMB_SERVER_WIFI_WARN:
				dialog.dismiss();
				if (mSmbServer != null) {
					openSmbNetwork(mSmbServer, false);
				}
				mSmbServer = null;
				break;
			case MOUNT_WARN:
				if (isRoot == false) {
					if (FeUtil.requirement4Root() != true) {
						FeUtil.showToast(this, getString(R.string.root_fail));
						break;
					} else {
						isRoot = true;
					}
				}

				if (mMount_to != null) {
					FeUtil.unmountCifs(mMount_to);
				}
				try {
					mMount_to = FeUtil.mountCifs(smb_ip, smb_user, smb_pass,
							smb_remote_path);
					if (mMount_to == null) {
						if (mSettings.isInfoCollectAllowed() == true) {
							MobclickAgent.onEvent(this, "Mount Fail");
						}
						FeUtil.showToast(this, getString(R.string.mount_fail));
					} else {
						if (mSettings.isInfoCollectAllowed() == true) {
							MobclickAgent.onEvent(this, "Mount Success");
						}
						FeUtil.showToast(this, getString(R.string.mount_ok)
								+ mMount_to);
					}
				} catch (Exception e) {
					;
				}
				break;
			case SMB_SEARCH_CONFIRM:
				SmbServerFinder finder = new SmbServerFinder(this,
						mSmbServerMgr);
				finder.start();
				break;
			case INFO_COLLECT_CONFIRM:
				mSettings.setInfoCollectStatus(true);
				mSettings.setInfoCollectAsked();
				if (mSettings.isDonateAsked() == false
						&& mSettings.isUserDonated() == false) {
					this.ask4Donate();
				}
				break;
			case DONATE_DLG:
				mSettings.setDonateAsked();
				// donateViaMarket();
				// donateViaPaypal();
				donateViaWeb();
				break;
			case ADD_SMB_SERVER:
				createNewSMBServer();
				break;
			}
			break;
		case DialogInterface.BUTTON_NEUTRAL:
			switch (dlg_type) {
			case ABOUT_BOX:
				showInfo(getString(R.string.log),
						getString(R.string.change_log), true);
				break;
			}
			break;
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			backProcess();
			break;
		case KeyEvent.KEYCODE_SEARCH:
			if (mRunningMode == FileLister.LOCAL_DIR_MODE
					|| mRunningMode == FileLister.REMOTE_DIR_MODE) {
				searchProcess(this.getCurrentPath());
			} else {
				return super.onKeyDown(keyCode, event);
			}
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btn_back:
		case R.id.btn_back_1:
			backProcess();
			break;
		case R.id.btn_exit:
			finish();
			break;
		case R.id.btn_home:
			openHomeDirectory();
			break;
		case R.id.btn_http:
			httpProcess();
			break;
		case R.id.btn_ftp:
			ftpProcess();
			break;
		case R.id.btn_file_operation:
			EnterFileOperationMode(true);
			break;
		case R.id.btn_main_tbar:
			EnterFileOperationMode(false);
			break;
		case R.id.btn_new:
			new NewDirDlg(this, getCurrentPath());
			break;
		case R.id.btn_file_copy:
			batchCopyProcess();
			break;
		case R.id.btn_paste_1:
			pasteProcess();
			break;
		case R.id.btn_file_cut:
			batchCutProcess();
			break;
		case R.id.btn_file_delete:
			ask4BatchDelete();
			break;
		case R.id.btn_settings:
			openSettings();
			break;
		case R.id.btn_sel_all:
		case R.id.btn_sel_all_1:
		case R.id.btn_sel_all_2:
			mContentsContainer.selectAll();
			refresh();
			break;
		case R.id.btn_clr_all:
			mWiMgr.clear();
			refresh();
			break;
		case R.id.btn_clr_all_1:
		case R.id.btn_clr_all_2:
			mContentsContainer.removeAllSel();
			refresh();
			break;
		case R.id.btn_app_backup:
			batchBackupOrUninstall(R.id.SYS_BATCH_BACKUP);
			break;
		case R.id.btn_app_uninstall:
			batchBackupOrUninstall(R.id.APP_UNINSTALL);
			break;
		case R.id.btn_mul_on:
		case R.id.btn_mul_off:
			processMulBtn(v.getId(), R.id.btn_mul_on, R.id.btn_mul_off,
					R.id.ll_mul_on, R.id.ll_mul_off);
			break;
		case R.id.btn_mul_on_1:
		case R.id.btn_mul_off_1:
			processMulBtn(v.getId(), R.id.btn_mul_on_1, R.id.btn_mul_off_1,
					R.id.ll_mul_on_1, R.id.ll_mul_off_1);
			break;
		case R.id.btn_mul_on_2:
		case R.id.btn_mul_off_2:
			processMulBtn(v.getId(), R.id.btn_mul_on_2, R.id.btn_mul_off_2,
					R.id.ll_mul_on_2, R.id.ll_mul_off_2);
			break;
		case R.id.btn_chg_view:
		case R.id.btn_chg_view_1:
			switch (mListerMode) {
			case GRID_MODE:
				setListerMode(LIST_MODE, true);
				break;
			case LIST_MODE:
				setListerMode(GRID_MODE, true);
				break;
			}
			break;
		case R.id.btn_search:
			searchProcess(getCurrentPath());
			break;
		case R.id.btn_detail:
			detailProcess(getCurrentPath());
			break;
		case R.id.btn_kill_task:
			batchTaskKill();
			break;
		case R.id.btn_speech:
			speechProcess();
			break;
		default:
			;
		}
	}

	public String getFullName(int position) {
		return getFullPath(new String(getContentName(position)));
	}

	public boolean installAllApk(FeFile dir) {
		if (mSettings.isSlientInstallApk()) {
			if (mSilentPackageInstaller != null) {
				mSilentPackageInstaller = null;
			}
			mSilentPackageInstaller = new SilentPackageInstaller(dir);
			FeProgressDialog dlg = new FeProgressDialog(this,
					mSilentPackageInstaller);
			dlg.start();
		} else {
			if (mExecuteWorker != null)
				mExecuteWorker = null;
			mExecuteWorker = new ExecuteBatch(this);
			mExecuteWorker.AddWorkItems(FeUtil.listAllApkWorkItems(dir));
			mBatchMode = BATCH_EXECUTE;
			mExecuteWorker.execute(null, null, null);
		}
		return true;
	}

	public void copyFinish() {
		FeUtil.gc();
	}

	public static String getLocalIpAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (inetAddress.getHostAddress().toString()
								.substring(0, 3).compareTo("172") != 0) {
							return inetAddress.getHostAddress().toString();
						}
					}
				}
			}
			return "localhost";
		} catch (Exception e) {
			return "localhost";
		}
	}

	public static InetAddress getLocalInetAddress() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface
					.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf
						.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress()) {
						if (inetAddress.getHostAddress().toString()
								.substring(0, 3).compareTo("172") != 0) {
							return inetAddress;
						}
					}
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	public void setShareIp(String ip) {
		mShareIp = ip;
	}

	public String getShareIp() {
		return mShareIp;
	}

	public void shutdownSmbConvertServer() {
		stopSmbStreamService();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mServerController.shutdownHttpServer(false);
		mServerController.shutdownFtpServer(false);
		shutdownSmbConvertServer();
		stopSmbStreamService();
		FeUtil.unmountCifs(mMount_to);
		mBookmarkMgr.commitBookmarks();
		mSmbServerMgr.commitSmbServers();
		mSettings.setSortMode(m_sort_mode);
		System.gc();
		System.exit(0);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (mNm != null && isFinishing() == false && mIsOpenSettings == false
				&& mSettings.isShowNotifyIcon()) {
			mNm.showFeNotify(this, true);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		shutdownSmbConvertServer();
		if (mBatchMode == FileLister.BATCH_EXECUTE
				|| mBatchMode == FileLister.BATCH_APP_UNINSTALL) {
			if (mExecuteWorker != null) {
				mExecuteWorker.goNext();
			}
			if (mAppBatchWorker != null) {
				mAppBatchWorker.goNext();
			}
		}
		SetStyle();
		// refresh();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onResume(this);
		}
		if (mNm != null && mSettings.isShowNotifyIcon()) {
			mNm.showFeNotify(this, false);
		}
		mIsOpenSettings = false;
		FeUtil.gc();
	}

	public boolean zipUnzip(FeFile src) {
		Compressor.Helper zip_helper = null;
		if (src.isFile() == true) {
			if (FileOperator.extendFileNameCompare(src, "zip") == true) {
				// Unzip
				try {
					if (getCurrentPath().compareTo("/") == 0) {
						zip_helper = (new Compressor()).new Helper(this, 1,
								src.getPath(), getCurrentPath()
										+ src.getNameOnly());
					} else {
						zip_helper = (new Compressor()).new Helper(this, 1,
								src.getPath(), getCurrentPath() + "/"
										+ src.getNameOnly());
					}
					zip_helper.start();
				} catch (Exception e) {
					Log.v("FE",
							"Exception occured while unpacking: "
									+ e.toString());
					return false;
				}
				return true;
			}
		}
		// Directory - zip
		try {
			if (getCurrentPath().compareTo("/") == 0) {
				zip_helper = (new Compressor()).new Helper(this, 0,
						src.getPath(), getCurrentPath() + src.getNameOnly()
								+ ".zip");
			} else {
				zip_helper = (new Compressor()).new Helper(this, 0,
						src.getPath(), getCurrentPath() + "/"
								+ src.getNameOnly() + ".zip");
			}
			zip_helper.start();
		} catch (Exception e) {
			Log.v("FE", "Exception occured while unpacking: " + e.toString());
			return false;
		}
		return true;
	}

	public int sortItems(FeComparator comparator, String path) {

		int item_count = 0;
		String[] AllServers = null;
		PackageManager pm = getPackageManager();

		if (path.compareTo("fe://OpenInstalledApplications") == 0) {
			mAllAppInfo = FePackage.listAllApplications(getPackageManager(),
					mSettings.isShowAllApp());
			ApplicationInfo mid;
			boolean changed = true;
			while (changed) {
				changed = false;
				for (int index = 0; index < mAllAppInfo.size() - 1; index++) {
					if (comparator.compare(
							pm.getApplicationLabel(mAllAppInfo.get(index))
									.toString(),
							pm.getApplicationLabel(mAllAppInfo.get(index + 1))
									.toString()) < 0) {
						mid = mAllAppInfo.get(index);
						mAllAppInfo.set(index, mAllAppInfo.get(index + 1));
						mAllAppInfo.set(index + 1, mid);
						changed = true;
					}
				}
			}
			item_count = mAllAppInfo.size();
		} else if (path.compareTo("fe://smbservers") == 0) {
			List<String> allSmbServers = mSmbServerMgr.list();
			if (allSmbServers == null) {
				return -1;
			}
			AllServers = new String[allSmbServers.size()];
			AllServers = allSmbServers.toArray(AllServers);
			item_count = AllServers.length;
		} else {
			FeFile files = new FeFile(path);
			return sortItems(comparator, files);
		}

		mContentsContainer.clear();
		boolean showHidden = mSettings.isShowHiddenDirs();

		if (AllServers != null) {
			for (int index = 0; index < item_count; index++) {
				if (showHidden == true) {
					mContentsContainer.add(AllServers[index]);
				} else {
					if (AllServers[index].charAt(0) != '.') {
						mContentsContainer.add(AllServers[index]);
					}
				}
			}
			AllServers = null;
		} else {
			for (int index = 0; index < item_count; index++) {
				mContentsContainer.add(pm.getApplicationLabel(
						mAllAppInfo.get(index)).toString());
			}
		}

		FeUtil.gc();
		return item_count;
	}

	public int sortItems(FeComparator comparator, FeFile files) {

		int item_count;
		FeFile[] AllFeFiles = null;

		if (files.isLocalFile() == true) {
			this.mRunningMode = LOCAL_DIR_MODE;
			mTabHost.setCurrentTab(LOCAL_TAB);
		} else {
			this.mRunningMode = REMOTE_DIR_MODE;
			mTabHost.setCurrentTab(NETWORK_TAB);
		}

		AllFeFiles = files.listFiles();
		if (AllFeFiles != null) {
			if (AllFeFiles.length > 1) {
				AllFeFiles = QuickSort.perform(AllFeFiles, 0,
						AllFeFiles.length - 1, comparator);
			}
			item_count = AllFeFiles.length;
		} else {
			return -1;
		}

		mContentsContainer.clear();
		boolean showHidden = mSettings.isShowHiddenDirs();

		for (int index = 0; index < item_count; index++) {
			if (AllFeFiles[index].isLocalFile()) {
				if (showHidden == true) {
					mContentsContainer.add(AllFeFiles[index].getName());
				} else {
					if (AllFeFiles[index].getName().charAt(0) != '.') {
						mContentsContainer.add(AllFeFiles[index].getName());
					}
				}
			} else {
				// We need to remove last '/'
				String name = AllFeFiles[index].getName();
				if (AllFeFiles[index].isDirectory()) {
					mContentsContainer
							.add(name.substring(0, name.length() - 1));
				} else {
					mContentsContainer.add(name);
				}
			}
		}

		AllFeFiles = null;
		FeUtil.gc();
		return item_count;
	}

	public void openSmbNetwork(SmbServerMgr.smbServer smbServer,
			boolean testWifi) {

		if (testWifi == true) {
			if (FeUtil.isWifiConnected(this) == false) {
				warnWifiConnection();
				mSmbServer = smbServer;
				return;
			}
		}

		if (smbServer.mAny == false) {
			smb_user = smbServer.mUser;
			smb_pass = smbServer.mPassword;
		} else {
			smb_user = null;
			smb_pass = null;
		}
		smb_ip = smbServer.mIp;

		if (smbServer.mAny == false) {
			if (smbServer.mDomain != null && smbServer.mDomain.length() > 0) {
				setShareIp("smb://" + smbServer.mDomain + ";" + smbServer.mUser
						+ ":" + smbServer.mPassword + "@" + smbServer.mIp);
			} else {
				setShareIp("smb://" + smbServer.mUser + ":"
						+ smbServer.mPassword + "@" + smbServer.mIp);
			}
		} else {
			if (smbServer.mDomain != null && smbServer.mDomain.length() > 0) {
				setShareIp("smb://" + smbServer.mDomain + ";" + smbServer.mIp);
			} else {
				setShareIp("smb://" + smbServer.mIp);
			}
		}
		gotoDir(getShareIp(), DIR_FORWARD);
	}

	public static boolean isValidIP(String checkStr) {
		try {
			String number = checkStr.substring(0, checkStr.indexOf('.'));
			if (Integer.parseInt(number) > 255)
				return false;
			checkStr = checkStr.substring(checkStr.indexOf('.') + 1);
			number = checkStr.substring(0, checkStr.indexOf('.'));
			if (Integer.parseInt(number) > 255)
				return false;
			checkStr = checkStr.substring(checkStr.indexOf('.') + 1);
			number = checkStr.substring(0, checkStr.indexOf('.'));
			if (Integer.parseInt(number) > 255)
				return false;
			number = checkStr.substring(checkStr.indexOf('.') + 1);
			if (Integer.parseInt(number) > 255)
				return false;
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public String getLocalStartDir() {
		return mStartDir;
	}

	public void SetStyle() {
		LinearLayout linearMain = (LinearLayout) this
				.findViewById(R.id.ll_main_act);
		linearMain.setBackgroundColor(android.R.color.background_light);
	}

	public boolean startSmbStreamService(FeFile target, String type) {
		String path = target.getPath();
		String name = target.getName().substring(0,
				(int) target.getName().length() - 1);
		Integer smbBuf = Integer.parseInt(mSettings.getSmbStreamBufSize());
		try {
			mSmbStreamServer = new SmbConvertServer(Environment
					.getExternalStorageDirectory().getPath(), 1223, path,
					smbBuf);
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("http://localhost:1223" + "/" + name));
			iSmbPort++;
			startActivity(i);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			stopSmbStreamService();
			return false;
		}
	}

	public void stopSmbStreamService() {
		if (mSmbStreamServer != null) {
			try {
				mSmbStreamServer.stop();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mSmbStreamServer = null;
		}
	}

	private void rebuildPathStack(String startDir) {
		List<String> dir_list = new ArrayList<String>();
		String dir = startDir;
		do {
			dir = DirTreeHelper.getPreviousDir(dir);
			if (dir.compareTo("/") != 0) {
				dir_list.add(dir);
			} else {
				break;
			}
		} while (true);
		for (int index = dir_list.size() - 1; index >= 0; index--) {
			mPathMgr.recordPath(dir_list.get(index), 0);
		}
		if (startDir.compareTo("/") != 0) {
			mPathMgr.recordPath(startDir, 0);
		}
	}

	private void warnWifiConnection() {
		m_dlg_type = FileLister.SMB_SERVER_WIFI_WARN;
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.wifi_disconnect));
		builder.setPositiveButton(getString(R.string.confirm), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.setTitle(getString(R.string.warning));
		builder.create();
		builder.show();
	}

	private void setupAllImageButtons() {
		for (int index = 0; index < img_btn_id.length; index++) {
			ImageButton btn = (ImageButton) findViewById(img_btn_id[index]);
			btn.setOnClickListener(this);
			btn.setMaxWidth(32);
			btn.setMaxHeight(32);
		}
	}

	public String getLanguage() {
		return getString(R.string.language);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putInt("mode", this.mRunningMode);
		outState.putBoolean("muliti", mMulitiMode);
		outState.putBoolean("mainbar", m_MainBarDisplay);
		outState.putString("path", this.getCurrentPath());
		FeUtil.gc();
		// System.gc();
	}

	@Override
	public void onVersionCheckFinish(int VersionCode) {
		Log.v("FE", "Version check finish, version: " + VersionCode);
	}

	private void EnterFileOperationMode(boolean state) {

		if (mShowToolbar == false)
			return;

		LinearLayout mainbar = (LinearLayout) this
				.findViewById(R.id.ll_main_toolbar);
		LinearLayout filebar = (LinearLayout) this
				.findViewById(R.id.ll_file_oper);
		LinearLayout appBar = (LinearLayout) this
				.findViewById(R.id.ll_app_oper);
		LinearLayout searchBtn = (LinearLayout) this
				.findViewById(R.id.ll_search);
		LinearLayout taskBar = (LinearLayout) this
				.findViewById(R.id.ll_task_oper);

		taskBar.setVisibility(View.GONE);
		appBar.setVisibility(View.GONE);

		if (this.mRunningMode == FileLister.LOCAL_DIR_MODE) {
			searchBtn.setVisibility(View.VISIBLE);
		} else {
			searchBtn.setVisibility(View.GONE);
		}

		if (state == true) {
			mainbar.setVisibility(LinearLayout.GONE);
			filebar.setVisibility(LinearLayout.VISIBLE);
			m_MainBarDisplay = false;
		} else {
			mainbar.setVisibility(LinearLayout.VISIBLE);
			filebar.setVisibility(LinearLayout.GONE);
			m_MainBarDisplay = true;
		}

		LinearLayout mul_on = (LinearLayout) this.findViewById(R.id.ll_mul_on);
		LinearLayout mul_off = (LinearLayout) this
				.findViewById(R.id.ll_mul_off);

		if (this.mListerMode == FileLister.GRID_MODE) {
			if (mMulitiMode == true) {
				mul_on.setVisibility(View.GONE);
				mul_off.setVisibility(View.VISIBLE);
			} else {
				mul_on.setVisibility(View.VISIBLE);
				mul_off.setVisibility(View.GONE);
			}
		} else {
			mul_on.setVisibility(View.GONE);
			mul_off.setVisibility(View.GONE);
		}
	}

	private void EnterAppOperationMode() {

		if (mShowToolbar == false)
			return;

		LinearLayout mainbar = (LinearLayout) this
				.findViewById(R.id.ll_main_toolbar);
		LinearLayout filebar = (LinearLayout) this
				.findViewById(R.id.ll_file_oper);
		LinearLayout appBar = (LinearLayout) this
				.findViewById(R.id.ll_app_oper);
		LinearLayout taskBar = (LinearLayout) this
				.findViewById(R.id.ll_task_oper);

		appBar.setVisibility(View.VISIBLE);
		mainbar.setVisibility(View.GONE);
		filebar.setVisibility(View.GONE);
		taskBar.setVisibility(View.GONE);

		LinearLayout mul_on = (LinearLayout) this
				.findViewById(R.id.ll_mul_on_1);
		LinearLayout mul_off = (LinearLayout) this
				.findViewById(R.id.ll_mul_off_1);

		if (this.mListerMode == FileLister.GRID_MODE) {
			if (mMulitiMode == true) {
				mul_on.setVisibility(View.GONE);
				mul_off.setVisibility(View.VISIBLE);
			} else {
				mul_on.setVisibility(View.VISIBLE);
				mul_off.setVisibility(View.GONE);
			}
		} else {
			mul_on.setVisibility(View.GONE);
			mul_off.setVisibility(View.GONE);
		}
	}

	private void EnterBookmarkMode() {

		if (mShowToolbar == false)
			return;

		LinearLayout mainbar = (LinearLayout) this
				.findViewById(R.id.ll_main_toolbar);
		LinearLayout filebar = (LinearLayout) this
				.findViewById(R.id.ll_file_oper);
		LinearLayout appBar = (LinearLayout) this
				.findViewById(R.id.ll_app_oper);
		LinearLayout taskBar = (LinearLayout) this
				.findViewById(R.id.ll_task_oper);

		appBar.setVisibility(View.GONE);
		mainbar.setVisibility(View.GONE);
		filebar.setVisibility(View.GONE);
		taskBar.setVisibility(View.GONE);
	}

	private void EnterSMBServerMode() {

		if (mShowToolbar == false)
			return;

		LinearLayout mainbar = (LinearLayout) this
				.findViewById(R.id.ll_main_toolbar);
		LinearLayout filebar = (LinearLayout) this
				.findViewById(R.id.ll_file_oper);
		LinearLayout appBar = (LinearLayout) this
				.findViewById(R.id.ll_app_oper);
		LinearLayout taskBar = (LinearLayout) this
				.findViewById(R.id.ll_task_oper);

		appBar.setVisibility(View.GONE);
		mainbar.setVisibility(View.GONE);
		filebar.setVisibility(View.GONE);
		taskBar.setVisibility(View.GONE);
	}

	private void EnterTaskManagerMode() {

		if (mShowToolbar == false)
			return;

		LinearLayout mainbar = (LinearLayout) this
				.findViewById(R.id.ll_main_toolbar);
		LinearLayout filebar = (LinearLayout) this
				.findViewById(R.id.ll_file_oper);
		LinearLayout appBar = (LinearLayout) this
				.findViewById(R.id.ll_app_oper);
		LinearLayout taskBar = (LinearLayout) this
				.findViewById(R.id.ll_task_oper);

		appBar.setVisibility(View.GONE);
		mainbar.setVisibility(View.GONE);
		filebar.setVisibility(View.GONE);
		taskBar.setVisibility(View.VISIBLE);

		LinearLayout mul_on = (LinearLayout) this
				.findViewById(R.id.ll_mul_on_2);
		LinearLayout mul_off = (LinearLayout) this
				.findViewById(R.id.ll_mul_off_2);

		if (this.mListerMode == FileLister.GRID_MODE) {
			if (mMulitiMode == true) {
				mul_on.setVisibility(View.GONE);
				mul_off.setVisibility(View.VISIBLE);
			} else {
				mul_on.setVisibility(View.VISIBLE);
				mul_off.setVisibility(View.GONE);
			}
		} else {
			mul_on.setVisibility(View.GONE);
			mul_off.setVisibility(View.GONE);
		}
	}

	public void pasteProcess() {
		if (mBatchMode == FileLister.BATCH_COPY
				|| mBatchMode == FileLister.BATCH_CUT) {
			if (m_copy_worker == null) {
				Log.v("FE",
						"Warning: No cut worker created but goes into execution process.");
				return;
			}
			if (!m_copy_worker.isReady()) {
				return;
			}
			m_copy_worker.setDstPath(getCurrentPath());
			FeProgressDialog dlg = new FeProgressDialog(this, m_copy_worker);
			dlg.start();
			mBatchMode = NO_BATCH;
		} else {
			// Normal copy
			if (wi == null)
				return;
			WorkItem w = new WorkItem();
			w.mSrcName = wi.mSrcName;
			w.mSrcPath = wi.mSrcPath;
			if (isRootDisplay() == true) {
				w.mDst = "/";
			} else {
				w.mDst = getCurrentPath();
			}
			if (m_copy_worker != null) {
				m_copy_worker = null;
			}
			m_copy_worker = new FileCopyWorker(m_file_cut);
			m_copy_worker.setDstPath(w.mDst);
			List<WorkItem> l = new ArrayList<WorkItem>();
			l.add(w);
			m_copy_worker.setWorkItems(l);
			FeProgressDialog dlg = new FeProgressDialog(this, m_copy_worker);
			dlg.start();
			wi = null;
		}
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Copy");
		}
	}

	public void batchCopyProcess() {
		if (m_copy_worker != null) {
			m_copy_worker = null;
		}
		List<WorkItem> wis = mWiMgr.listWorkItems();
		if (wis == null)
			return;
		m_copy_worker = new FileCopyWorker(false);
		m_copy_worker.setWorkItems(wis);
		mBatchMode = BATCH_COPY;
	}

	public void batchCutProcess() {
		if (m_copy_worker != null) {
			m_copy_worker = null;
		}
		List<WorkItem> wis = mWiMgr.listWorkItems();
		if (wis == null || wis.isEmpty())
			return;
		m_copy_worker = new FileCopyWorker(true);
		m_copy_worker.setWorkItems(wis);
		mBatchMode = BATCH_CUT;
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Cut");
		}
	}

	public void batchExecuteProcess() {
		if (mExecuteWorker != null)
			mExecuteWorker = null;
		List<WorkItem> wis = mWiMgr.listWorkItems();
		if (wis == null)
			return;
		mExecuteWorker = new ExecuteBatch(this);
		mExecuteWorker.AddWorkItems(wis);
		mBatchMode = BATCH_EXECUTE;
		mExecuteWorker.execute(null, null, null);
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Batch Execute");
		}
	}

	private void batchBackupOrUninstall(int operation) {
		if (mAppBatchWorker != null) {
			mAppBatchWorker = null;
		}
		List<Integer> wis = mContentsContainer.getAppSel();
		if (wis == null)
			return;
		mAppBatchWorker = new AppBatch(mAllAppInfo, this);
		if (operation == R.id.SYS_BATCH_BACKUP) {
			mAppBatchWorker.setMode(AppBatch.APP_BACKUP);
			mBatchMode = FileLister.BATCH_APP_BACKUP;
			if (mSettings.isInfoCollectAllowed() == true) {
				MobclickAgent.onEvent(this, "App Backup");
			}
		} else {
			mAppBatchWorker.setMode(AppBatch.APP_UNINSTALL);
			mBatchMode = FileLister.BATCH_APP_UNINSTALL;
			if (mSettings.isInfoCollectAllowed() == true) {
				MobclickAgent.onEvent(this, "App Uninstall");
			}
		}
		mAppBatchWorker.setWorkItems(wis);
		FeProgressDialog dlg = new FeProgressDialog(this, mAppBatchWorker);
		dlg.start();
	}

	private void batchTaskKill() {
		if (mTaskKillWorker != null) {
			mTaskKillWorker = null;
		}
		List<Integer> wis = mContentsContainer.getTasksSel();
		if (wis == null)
			return;
		mTaskKillWorker = new TaskKillBatch(mTaskManager.getRunningTaskInfo(),
				mTaskManager, this);
		mBatchMode = FileLister.BATCH_TASK_KILL;
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Task Kill");
		}
		mTaskKillWorker.setWorkItems(wis);
		mTaskKillWorker.execute(null, null, null);
	}

	public void batchDeleteProcess() {
		FileDeleteWorker dw = new FileDeleteWorker();
		List<WorkItem> items = mWiMgr.listWorkItems();
		dw.setWorkItems(items);
		mWiMgr.clear();
		FeProgressDialog dlg = new FeProgressDialog(this, dw);
		dlg.start();
	}

	public void deleteOne() {
		m_sync.set(0);
		FileDeleteWorker dw = new FileDeleteWorker();
		List<WorkItem> items = new ArrayList<WorkItem>();
		WorkItem item = new WorkItem();
		item.mSrcPath = getCurrentPath();
		item.mSrcName = getContentName(mActiviedContextMenuInfo.position);
		items.add(item);
		dw.setWorkItems(items);
		mWiMgr.clear();
		FeProgressDialog dlg = new FeProgressDialog(this, dw);
		dlg.start();
	}

	public void searchProcess(int position) {
		mSearchDir = new FeFile(getFullName(position));
		if (mSearchDir.isFile() == true) {
			showInfo("Can not search in a file", getString(R.string.error),
					false);
			mSearchDir = null;
		} else {
			if (mSearchDlg != null) {
				mSearchDlg.setTarget(mSearchDir);
			}
			showDialog(R.id.search_dlg);
		}
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Search");
		}
	}

	public void searchProcess(String dir) {
		mSearchDir = new FeFile(dir);
		if (mSearchDir.isFile() == true) {
			showInfo("Can not search in a file", getString(R.string.error),
					false);
			mSearchDir = null;
		} else {
			if (mSearchDlg != null) {
				mSearchDlg.setTarget(mSearchDir);
			}
			showDialog(R.id.search_dlg);
		}
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "Search");
		}
	}

	public void donateViaMarket() {
		Intent donatei = FePackage.openAppWithPackageName("xcxin.fedonate");
		if (donatei != null) {
			try {
				startActivity(donatei);
				if (mSettings.isInfoCollectAllowed() == true) {
					MobclickAgent.onEvent(this, "donateViaMarket");
				}
			} catch (Exception e) {
				donateViaPaypal();
				if (mSettings.isInfoCollectAllowed() == true) {
					MobclickAgent.onEvent(this, "donateViaPayPal");
				}
			}
		}
	}

	public void donateViaWeb() {
		Intent donatei = new Intent(Intent.ACTION_VIEW);
		donatei.setData(Uri.parse("http://www.wifisharing.mobi/en/?page_id=96"));
		startActivity(donatei);
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "donateViaWeb");
		}
	}

	private void donateViaPaypal() {
		/*
		 * if (mPp == null) { try { Thread.sleep(2000); } catch
		 * (InterruptedException e) { return; } if (mPp == null) { return; } }
		 * 
		 * showDialog(DONATE_DLG_ID); if (mSettings.isInfoCollectAllowed() ==
		 * true) { MobclickAgent.onEvent(this, "donateViaPayPal"); }
		 */
	}

	private void findAllInstalledPlugins() {
		PackageManager pm = this.getPackageManager();
		mDonatePlugin = FePackage.isPackageInstalled("xcxin.fedonate", pm);
		if (mDonatePlugin && mSettings.isUserDonated() != true) {
			mSettings.setUserDonate();
		}
		mSmbMountPlugin = FePackage.isPackageInstalled("xcxin.fesmbmount", pm);
	}

	private void showChangLog() {
		String curVersion = FePackage.getVersion("xcxin.filexpert",
				getPackageManager());
		String preVersion = mSettings.getPreviousFEVersion();
		if (preVersion != null) {
			if (preVersion.compareTo(curVersion) != 0) {
				// Version missmatch
				if (this.mDonateDlgDisplayed == false) {
					showInfo(getString(R.string.log),
							getString(R.string.change_log), false);
					mSettings.setFEVersion(curVersion);
				}
			}
		} else {
			mSettings.setFEVersion(curVersion);
		}
	}

	private void detailProcess(String file) {
		mDetailPath = file;
		if (detailDlg != null) {
			showDialog(R.id.ll_detail);
			detailDlg.setTarget(new FeFile(mDetailPath));
		} else {
			showDialog(R.id.ll_detail);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case PAYPAL_REQUEST:
			switch (resultCode) {
			case Activity.RESULT_OK:
				// User has successfully make a donation
				mSettings.setUserDonate();
				this.showInfo(getString(R.string.donate_thank_you),
						getString(R.string.donation), false);
				if (mSettings.isInfoCollectAllowed() == true) {
					MobclickAgent.onEvent(this, "Donate Success");
				}
				break;
			}
			break;
		case 1234:
			switch (resultCode) {
			case Activity.RESULT_OK:
				boolean r = mSpeech.handleResults(data);
				if (r == false) {
					FeUtil.showToast(this, getString(R.string.speech_cmd_ok));
				} else {
					FeUtil.showToast(this, getString(R.string.undefined_cmd));
				}
				if (mSpeech.isStarted()) {
					// Chain call, in order to make continue speech commands
					mSpeech.start();
				}
				break;
			}
		}
	}

	public static void startPaymentProcess(Activity activity) {
		/*
		 * try { PayPalPayment ppp = new PayPalPayment(); //
		 * ppp.setCurrencyType("USD"); ppp.setCurrency("USD"); //
		 * ppp.setSubtotal(new BigDecimal((float)2.99)); ppp.setAmount((float)
		 * 2.99); ppp.setRecipient("alex.xin.china@gmail.com");
		 * ppp.setItemDescription("File Expert Donation");
		 * ppp.setMerchantName("Alex Xin"); Intent i = new Intent(activity,
		 * PayPalActivity.class); i.putExtra(PayPalActivity.EXTRA_PAYMENT_INFO,
		 * ppp); activity.startActivityForResult(i, FileLister.PAYPAL_REQUEST);
		 * } catch (Exception e) { return; }
		 */
	}

	protected void ActiviteNetwork() {
		// Fire off a thread to do some work that we shouldn't do directly in
		// the UI thread
		Thread t = new Thread() {
			public void run() {
				try {
					Socket socket = new Socket("http://www.google.com/", 80);
					if (socket.isConnected()) {
						sleep(500);
						socket.close();
					}
					socket = null;
				} catch (UnknownHostException e) {
					;
				} catch (IOException e) {
					;
				} catch (InterruptedException e) {
					;
				}
			}
		};
		t.start();
	}

	private void processMulBtn(int id, int btn_on, int btn_off, int ll_on,
			int ll_off) {
		LinearLayout mul_on = (LinearLayout) this.findViewById(ll_on);
		LinearLayout mul_off = (LinearLayout) this.findViewById(ll_off);
		if (id == btn_on) {
			mul_on.setVisibility(View.GONE);
			mul_off.setVisibility(View.VISIBLE);
			mMulitiMode = true;
		} else {
			mul_on.setVisibility(View.VISIBLE);
			mul_off.setVisibility(View.GONE);
			mMulitiMode = false;
		}
	}

	public void openSettings() {
		mIsOpenSettings = true;
		Intent i = new Intent(this, NewSettings.class);
		startActivity(i);
	}

	public void openHomeDirectory() {
		gotoDir(mStartDir, DIR_FORWARD);
	}

	private void speechProcess() {
		try {
			if (mSpeech != null) {
				mSpeech.start();
			}
		} catch (Exception e) {
			FeUtil.showToast(this, "No speech support on current ROM");
			return;
		}
	}

	public void httpProcess() {
		if (mServerController.isHttpServerStarted()) {
			mServerController.shutdownHttpServer(true);
			refresh();
		} else {
			String dir = mSettings.getDefaultSharingDir();
			if (dir == null) {
				dir = Environment.getExternalStorageDirectory().getPath();
			}
			mServerController.startHttpSharing(dir);
		}
	}

	public void ftpProcess() {
		if (mServerController.isFtpServerStarted()) {
			mServerController.shutdownFtpServer(true);
			refresh();
		} else {
			String dir = mSettings.getDefaultSharingDir();
			if (dir == null) {
				dir = Environment.getExternalStorageDirectory().getPath();
			}
			mServerController.startFtpSharing(dir);
		}
	}

	private void backProcess() {
		if (this.mRunningMode == FileLister.CUSTOMIZE_FS_MODE) {
			if (mIsZipViewer) {
				String parent = mCurZipDir;

				if (mCurZipDir.endsWith(File.separator))
					parent = mCurZipDir.substring(0, mCurZipDir.length() - 1);

				int sep = parent.lastIndexOf(File.separator);

				if (sep != -1 && sep != parent.length() - 1) {
					parent = parent.substring(0, sep + 1);
					viewZipFile(mZipRootList, parent);
				} else {
					mIsZipViewer = false;
					gotoDir(getCurrentPath(), DIR_FORWARD);
				}
				return;
			} else
				gotoDir(mStartDir, DIR_FORWARD);
		} else {
			if (isRootDisplay() == true) {
				if (mBackPressedFirst == false) {
					FeUtil.showToast(this, getString(R.string.exit_twice));
					mBackPressedFirst = true;
				} else {
					finish();
				}
			} else {
				mBackPressedFirst = false;
				if (mRunningMode != LOCAL_DIR_MODE
						&& mRunningMode != REMOTE_DIR_MODE) {
					mTabHost.setCurrentTabByTag("local_tab");
				} else {
					Back();
				}
			}
		}
	}

	private void hideToolbar() {
		LinearLayout mainbar = (LinearLayout) this
				.findViewById(R.id.ll_main_toolbar);
		LinearLayout filebar = (LinearLayout) this
				.findViewById(R.id.ll_file_oper);
		LinearLayout appBar = (LinearLayout) this
				.findViewById(R.id.ll_app_oper);
		LinearLayout taskBar = (LinearLayout) this
				.findViewById(R.id.ll_task_oper);

		appBar.setVisibility(View.GONE);
		mainbar.setVisibility(View.GONE);
		filebar.setVisibility(View.GONE);
		taskBar.setVisibility(View.GONE);

		mShowToolbar = false;
	}

	private void showToolbar() {
		mShowToolbar = true;
	}

	public void finish() {
		mThumbGetter.stop();
		super.finish();
	}

	protected void getStartDir() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			if (mSettings.getAutoSdCard() == false) {
				mStartDir = mSettings.getStartDir();
			} else {
				mStartDir = Environment.getExternalStorageDirectory().getPath();
			}
		} else {
			// There's no SD card installed on current system
			mStartDir = "/";
		}
	}

	protected void processTabsToolbarState() {
		if (mSettings.isTabsShow()) {
			mTabWidget.setVisibility(View.VISIBLE);
			m_TabDisplay = true;
		} else {
			mTabWidget.setVisibility(View.GONE);
			m_TabDisplay = false;
		}
		if (mSettings.isToolBarShow()) {
			showToolbar();
		} else {
			hideToolbar();
		}
	}

	public void createNewSMBServer() {
		smb_domain = null;
		smb_ip = null;
		smb_user = null;
		smb_pass = null;
		smb_any = false;
		m_smb_dlg_mode = SmbIpDlg.NEW_MODE;
		showDialog(R.id.smb_ip_dlg);
	}

	public void restorePosition(boolean removeRecord) {
		mPathMgr.restorePosition(this, getCurrentPath(), removeRecord);
	}

	public View getContentsView() {
		if (getListerMode() == LIST_MODE) {
			return mContentsListView;
		} else {
			return mContentsGridView;
		}
	}

	public int getVisiablePosition() {
		if (getListerMode() == LIST_MODE) {
			return mContentsListView.getFirstVisiblePosition();
		} else {
			return mContentsGridView.getFirstVisiblePosition();
		}
	}
	
	private void remount() {
		final boolean rw = RootShell.isSystemReadWritable();

		Dialog dlg = new AlertDialog.Builder(this)
		.setMessage(rw ? R.string.mount_read : R.string.mount_read_write)
		.setPositiveButton(R.string.apply,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						RootShell.remount(!rw);
						if (RootShell.isSystemReadWritable() == rw)
							Toast.makeText(FileLister.this, R.string.mount_fail, Toast.LENGTH_LONG).show();
					}
				}).create();
		dlg.show();
	}
}