package xcxin.filexpert;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

import jcifs.smb.SmbFileOutputStream;

public class FeFileOutputStream {
	private FileOutputStream mFos = null;
	private SmbFileOutputStream mSmbFos = null;
	private boolean mLocal;

	public FeFileOutputStream(FeFile file) {
		mLocal = file.isLocalFile();
		if (mLocal == true) {
			try {
				mFos = new FileOutputStream(file.getFile());
			} catch (FileNotFoundException e) {
				return;
			}
			mSmbFos = null;
		} else {
			try {
				mSmbFos = new SmbFileOutputStream(file.getSmbFile());
			} catch (Exception e) {
				return;
			}
			mFos = null;
		}
	}

	public OutputStream getOutputStream() {
		if(mLocal == true) {
			return mFos;
		} else {
			return mSmbFos;
		}
	}
}
