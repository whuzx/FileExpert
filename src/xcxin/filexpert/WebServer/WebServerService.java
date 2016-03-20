package xcxin.filexpert.WebServer;

import java.io.IOException;

import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileExpertSettings;
import android.app.Service;
import android.content.Intent;
import android.os.Environment;
import android.os.IBinder;

public class WebServerService extends Service {

	private WebServer mServer;
	private boolean mStarted;

	@Override
	public void onCreate() {
		mStarted = false;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		try {
			mServer.stop();
		} catch (Exception e) {
			;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (mStarted == false) {
			super.onStart(intent, startId);
			try {
				FileExpertSettings	settings = new FileExpertSettings(this);
				String root;
				if (FeUtil.isSDCardInstalled() == true) {
					root = settings.getSharingDir();
					if (root == null) {
						root = Environment.getExternalStorageDirectory().getPath();
					}					
				} else {
					root = "/";
				}
				mServer = new WebServer(root, settings.getHttpPort(), this, settings);
				mStarted = true;
			} catch (IOException e) {
				this.stopSelf();
			}
		} else {
			// Change web root dir
			;
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}
}