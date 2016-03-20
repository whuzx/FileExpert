package xcxin.filexpert;

import java.io.IOException;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;

public class SmbStreamService extends Service {

	private SmbConvertServer mServer;
	private boolean mStarted;
	private FileExpertSettings mSettings;

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
		} catch (IOException e) {
			;
		}
	}

	@Override
	public void onStart(Intent intent, int startId) {
		if (mStarted == false) {
			mSettings = new FileExpertSettings(this);
			super.onStart(intent, startId);
			try {
				// String path = intent.getExtras().getString("path");
				String path = mSettings.getSmbStreamPath();
				String name = mSettings.getSmbStreamName();
				// Integer smbBuf =
				// Integer.parseInt(intent.getExtras().getString("bufsize"));
				Integer smbBuf = Integer.parseInt(mSettings
						.getSmbStreamBufSize());
				mServer = new SmbConvertServer(Environment
						.getExternalStorageDirectory().getPath(), 1223, path,
						smbBuf);
				Intent i = new Intent(Intent.ACTION_VIEW, Uri
						.parse("http://localhost:1223/" + name));
				startActivity(i);
				mStarted = true;
			} catch (IOException e) {
				this.stopSelf();
				mSettings.setSmbServerStartFlag(false);
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