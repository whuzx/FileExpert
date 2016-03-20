package xcxin.filexpert;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import jcifs.smb.SmbFileInputStream;
import android.util.Log;

public class FeFileInputStream {
	private FileInputStream mFis = null;
	private SmbFileInputStream mSmbFis = null;
	private boolean mLocal;

	public FeFileInputStream(FeFile file) {
		mLocal = file.isLocalFile();
		if (mLocal == true) {
			try {
				mFis = new FileInputStream(file.getFile());
			} catch (FileNotFoundException e) {
				return;
			}
			mSmbFis = null;
		} else {
			try {
				mSmbFis = new SmbFileInputStream(file.getSmbFile());
			} catch (Exception e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return;
			}
			mFis = null;
		}
	}
		
	public InputStream getInputStream() {
		if(mLocal == true) {
			return mFis;
		} else {
			return mSmbFis;
		}
	}
}
