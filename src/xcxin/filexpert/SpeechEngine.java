package xcxin.filexpert;

import java.util.List;

import android.content.Intent;
import android.speech.RecognizerIntent;

public class SpeechEngine {

	public static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;

	private boolean mStarted;
	private Intent regIntent;
	private FileLister mUi;

	public SpeechEngine(FileLister lister) {
		regIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
		regIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
				RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
		regIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN");
		mUi = lister;
	}

	public void start() {
		mUi.startActivityForResult(regIntent,
				SpeechEngine.VOICE_RECOGNITION_REQUEST_CODE);
		mStarted = true;
	}

	public boolean isStarted() {
		return mStarted;
	}

	private boolean findCommand(List<String> set, String key1, String key2) {
		for (int index = 0; index < set.size(); index++) {
			String cmd = set.get(index);
			if (cmd.compareToIgnoreCase(key1) == 0
					|| cmd.compareToIgnoreCase(key2) == 0) {
				return true;
			}
		}
		return false;
	}

	private List<String> getResults(Intent i) {
		return i.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
	}

	public boolean handleResults(Intent i) {
		List<String> results = getResults(i);

		if (findCommand(results, "copy", "复制")) {
			mUi.batchCopyProcess();
			return true;
		} else if (findCommand(results, "paste", "粘贴")) {
			mUi.pasteProcess();
			return true;
		} else if (findCommand(results, "speech", "语音")) {
			mStarted = false;
		} else if (findCommand(results, "delete", "删除")) {
			mUi.batchDeleteProcess();
			return true;
		} else if (findCommand(results, "search", "搜索")) {
			mUi.searchProcess(mUi.getCurrentPath());
			return true;
		} else if (findCommand(results, "cut", "剪切")) {
			mUi.batchCutProcess();
			return true;
		} else if (findCommand(results, "exit", "退出")) {
			mUi.finish();
			mStarted = false;
			return true;
		} else if (findCommand(results, "play", "播放")) {
			mUi.batchExecuteProcess();
			return true;
		} else if (findCommand(results, "up", "向上")) {
			if (mUi.mRunningMode == FileLister.LOCAL_DIR_MODE
					|| mUi.mRunningMode == FileLister.REMOTE_DIR_MODE) {
				mUi.Back();
			}
			return true;
		} else if (findCommand(results, "web", "http")) {
			mUi.httpProcess();
			return true;
		} else if (findCommand(results, "ftp", "ftp")) {
			mUi.ftpProcess();
		} else if (findCommand(results, "settings", "设置")) {
			mUi.openSettings();
			mStarted = false;
			return true;
		} else if (findCommand(results, "home", "主目录")) {
			mUi.openHomeDirectory();
			return true;
		} else if (findCommand(results, "select all", "全选")) {
			mUi.mContentsContainer.selectAll();
			mUi.refresh();
			return true;
		}

		for (int index = 0; index < results.size(); index++) {
			if (isDirectory(results.get(index))) {
				mUi.gotoDir(mUi.getFullPath(results.get(index)),
						FileLister.DIR_FORWARD);
				return true;
			}
		}

		return false;
	}

	private boolean isDirectory(String entry) {
		if (mUi.mRunningMode == FileLister.LOCAL_DIR_MODE
				|| mUi.mRunningMode == FileLister.REMOTE_DIR_MODE) {
			for (int index = 0; index < mUi.mContentsContainer.getCount(); index++) {
				String name = mUi.getContentName(index);
				if (name.compareToIgnoreCase(entry) == 0) {
					String fullpath = mUi.getFullPath(name);
					FeFile file = new FeFile(fullpath);
					if (file.isFile()) {
						file = null;
						return false;
					} else {
						file = null;
						return true;
					}
				}
			}
			return false;
		} else {
			return false;
		}
	}
}