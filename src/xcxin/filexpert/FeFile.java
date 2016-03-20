package xcxin.filexpert;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import android.util.Log;

public class FeFile {
	private File mFile = null;
	private SmbFile mSmbFile = null;
	private boolean mLocal;

	public FeFile(String location) {
		if (location.indexOf("smb://") == 0) {
			try {
				if (location.charAt(location.length() - 1) == '/') {
					mSmbFile = new SmbFile(location);
				} else {
					mSmbFile = new SmbFile(location + "/");
				}
			} catch (MalformedURLException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				mSmbFile = null;
				return;
			}
			mFile = null;
			mLocal = false;
		} else {
			mFile = new File(location);
			mSmbFile = null;
			mLocal = true;
		}
	}

	public FeFile(URL url) {
		mSmbFile = new SmbFile(url);
		mFile = null;
		mLocal = false;
	}

	public FeFile(File file) {
		mFile = file;
		mSmbFile = null;
		mLocal = true;
	}

	public FeFile(SmbFile file) {
		mSmbFile = file;
		mFile = null;
		mLocal = false;
	}

	public FeFile(String path, String file) {
		if (path.indexOf("smb://") == 0) {
			try {
				String fullpath;
				if (path.charAt(path.length() - 1) == '/') {
					fullpath = path + file;
				} else {
					fullpath = path + "/" + file;
				}
				if (fullpath.charAt(fullpath.length() - 1) != '/') {
					fullpath = fullpath + "/";
				}
				mSmbFile = new SmbFile(fullpath);
			} catch (MalformedURLException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				mSmbFile = null;
			}
			mFile = null;
			mLocal = false;
		} else {
			mFile = new File(path, file);
			mSmbFile = null;
			mLocal = true;
		}
	}

	public boolean isLocalFile() {
		return mLocal;
	}

	public File getFile() {
		return mFile;
	}

	public SmbFile getSmbFile() {
		return mSmbFile;
	}

	public String getName() {
		if (mLocal == true) {
			return mFile.getName();
		} else {
			return mSmbFile.getName();
		}
	}

	public String getPath() {
		if (mLocal == true) {
			return mFile.getPath();
		} else {
			return mSmbFile.getPath();
		}
	}

	public String[] list() {
		if (mLocal == true) {
			if (mFile.canRead())
				return mFile.list();
			else
				return RootShell.list(mFile);
		} else {
			try {
				return mSmbFile.list();
			} catch (SmbException e) {
				Log.v("FE", "Exception occured. " + e.toString());
				return null;
			}
		}
	}

	public FeFile[] listFiles() {
		File[] files;
		SmbFile[] SmbFiles;
		FeFile[] fefiles = null;

		if (mLocal == true) {
			try {
				if (mFile.canRead())
					files = mFile.listFiles();
				else
					files = RootShell.listFiles(mFile);
				
				if (files == null) {
					return null;
				}
			} catch (Exception e) {
				return null;
			}
			fefiles = new FeFile[files.length];
			for (int index = 0; index < files.length; index++) {
				fefiles[index] = new FeFile(files[index]);
			}
		} else {
			try {
				SmbFiles = mSmbFile.listFiles();
				fefiles = new FeFile[SmbFiles.length];
				for (int index = 0; index < SmbFiles.length; index++) {
					fefiles[index] = new FeFile(SmbFiles[index]);
				}
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return null;
			}
		}

		return fefiles;
	}

	public boolean isDirectory() {
		if (mLocal == true) {
			return mFile.isDirectory();
		} else {
			try {
				return mSmbFile.isDirectory();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return false;
			}
		}
	}

	public boolean isFile() {
		if (mLocal == true) {
			return mFile.isFile();
		} else {
			try {
				return mSmbFile.isFile();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return false;
			}
		}
	}

	public boolean isHidden() {
		if (mLocal == true) {
			return mFile.isHidden();
		} else {
			try {
				return mSmbFile.isHidden();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return false;
			}
		}
	}

	public boolean delete() {
		if (mLocal == true) {
			if (mFile.canWrite() && mFile.getParentFile().canWrite())
				return mFile.delete();
			else
				return RootShell.delFile(mFile);
		} else {
			try {
				mSmbFile.delete();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return false;
			}
			return true;
		}
	}

	public boolean renameTo(FeFile dest) {
		if (mLocal == true) {
			if (mFile.canWrite() && mFile.getParentFile().canWrite())
				return mFile.renameTo(dest.getFile());
			else
				return RootShell.renameFile(mFile, dest.getFile());
		} else {
			try {
				mSmbFile.renameTo(dest.getSmbFile());
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return false;
			}
			return true;
		}
	}

	public boolean mkdir() {
		if (mLocal == true) {
			if (mFile.getParentFile().canWrite())
				return mFile.mkdir();
			else return RootShell.mkdir(mFile);
		} else {
			try {
				mSmbFile.mkdir();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return false;
			}
			return true;
		}
	}

	public boolean exists() {
		if (mLocal == true) {
			return mFile.exists();
		} else {
			boolean r = false;
			try {
				r = mSmbFile.exists();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				try {
					if (mSmbFile.length() > 0) {
						return true;
					}
				} catch (SmbException e1) {
					return false;
				}
			}
			return r;
		}
	}

	public long length() {
		if (mLocal == true) {
			return mFile.length();
		} else {
			try {
				return mSmbFile.length();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return -1;
			}
		}
	}

	public long lastModified() {
		if (mLocal == true) {
			return mFile.lastModified();
		} else {
			try {
				return mSmbFile.lastModified();
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return -1;
			}
		}
	}

	public InputStream getInputStream() {
		try {
			if (mLocal == true) {
				FileInputStream is = new FileInputStream(mFile);
				return is;
			} else {
				return mSmbFile.getInputStream();
			}
		} catch (Exception e) {
			Log.v("FE", "Exception occured:" + e.toString());
			return null;
		}
	}

	public OutputStream getOutputStream() {
		try {
			if (mLocal == true) {
				FileOutputStream is = new FileOutputStream(mFile);
				return is;
			} else {
				return mSmbFile.getOutputStream();
			}
		} catch (Exception e) {
			Log.v("FE", "Exception occured:" + e.toString());
			return null;
		}
	}

	public String getNameOnly() {
		if (mLocal == true) {
			if (mFile.isDirectory() == true) {
				return getName();
			}
		} else {
			try {
				if (mSmbFile.isDirectory() == true) {
					return getName();
				}
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return null;
			}
		}

		// File
		String name = getName().toLowerCase();
		return name.substring(0, name.lastIndexOf('.', name.length()));
	}

	public String getParent() {
		if (mLocal == true) {
			if (mFile.isDirectory() == true) {
				return mFile.getParent();
			}
		} else {
			try {
				if (mSmbFile.isDirectory() == true) {
					return mSmbFile.getParent();
				}
			} catch (SmbException e) {
				Log.v("FE", "SmbException occured:" + e.toString());
				return null;
			}
		}
		return null;
	}
}