package xcxin.filexpert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

//Compress   files   and   folders 
public class Compressor {
	static final int BUFFER = 4096;

	public class Helper extends Thread implements Handler.Callback {

		private ProgressDialog m_pd = null;
		private Context m_context;
		private int m_mode;
		private String m_target;
		private String m_path;
		private String m_zipname;
		private Handler m_handler;

		public final int ZIP_MODE = 0;
		public final int UNZIP_MODE = 1;

		public Helper(Context context, int mode, String target,
				String path_or_zip_name) {
			m_context = context;
			m_mode = mode;
			m_target = target;
			if (mode == UNZIP_MODE) {
				m_path = path_or_zip_name;
			} else {
				m_zipname = path_or_zip_name;
			}
			m_handler = new Handler(this);
		}

		@Override
		public boolean handleMessage(Message msg) {

			Bundle bdl = msg.getData();

			String msg_str = bdl.getString("unzip_msg");
			if (msg_str != null) {
				m_pd.setMessage(m_context.getString(R.string.deal_with)
						+ msg_str);
			}

			msg_str = bdl.getString("zip_msg");
			if (msg_str != null) {
				m_pd.setMessage(m_context.getString(R.string.deal_with)
						+ msg_str);
			}

			msg_str = bdl.getString("end_msg");
			if (msg_str != null) {
				m_pd.dismiss();
				((FileLister) m_context).refresh();
			}

			return true;
		}

		@Override
		public void start() {
			if (m_mode == UNZIP_MODE) {
				m_pd = ProgressDialog.show(m_context,
						m_context.getString(R.string.unziping), " ");
			} else if (m_mode == ZIP_MODE) {
				m_pd = ProgressDialog.show(m_context,
						m_context.getString(R.string.ziping), " ");
			} else {
				// Error parameter passed
				return;
			}

			super.start();
		}

		public void run() {
			try {
				if (m_mode == UNZIP_MODE) {
					unzip(m_target, m_path, m_handler);
				} else if (m_mode == ZIP_MODE) {
					zip(m_target, m_zipname, m_handler);
				}
			} catch (Exception e) {
				Log.v("FE", "Exception while unziping: " + e.toString());
			}

			Message msg = m_handler.obtainMessage();
			Bundle bdl = new Bundle();
			bdl.putString("end_msg", "end");
			msg.setData(bdl);
			m_handler.sendMessage(msg);
		}
	}

	public static void unzip(String zip_file, String path, Handler handler)
			throws Exception {
		BufferedOutputStream dest = null;
		BufferedInputStream is = null;
		ZipEntry entry;

		FeFile target_folder = new FeFile(path);
		if (target_folder.exists() == false) {
			target_folder.mkdir();
		}
		target_folder = null;

		ZipFile zipfile = new ZipFile(zip_file);
		Enumeration<?> e = zipfile.entries();

		while (e.hasMoreElements()) {

			entry = (ZipEntry) e.nextElement();
			Log.v("FE", "Extracting: " + entry);

			if (handler != null) {
				Message msg = handler.obtainMessage();
				Bundle bdl = new Bundle();
				bdl.putString("unzip_msg", entry.getName());
				msg.setData(bdl);
				handler.sendMessage(msg);
			}

			if (entry.isDirectory() == true) {
				// Directory - let's create it!
				String dir = null;
				if (path.compareTo("/") == 0) {
					dir = path + entry.getName();
				} else {
					dir = path + "/" + entry.getName();
				}
				createAllMissingDirs(dir);
				continue;
			}

			is = new BufferedInputStream(zipfile.getInputStream(entry));
			int count;
			byte data[] = new byte[BUFFER];

			String out_file = null;
			if (path.compareTo("/") == 0) {
				out_file = path + entry.getName();
			} else {
				out_file = path + "/" + entry.getName();
			}

			createAllMissingDirs(out_file);
			FileOutputStream fos = new FileOutputStream(out_file);
			dest = new BufferedOutputStream(fos, BUFFER);
			while ((count = is.read(data, 0, BUFFER)) != -1) {
				dest.write(data, 0, count);
			}
			dest.flush();
			dest.close();
			is.close();
			data = null;
			FeUtil.gc();
		}
	}

	public static void zip(String target_name, String zip_name, Handler handler) {
		try {
			BufferedInputStream origin = null;
			FileOutputStream dest = new FileOutputStream(zip_name);
			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			out.setMethod(ZipOutputStream.DEFLATED);

			byte data[] = new byte[BUFFER];
			// get a list of files from current directory
			FeFile f = new FeFile(target_name);
			if (f.isDirectory() == true) {
				zip(f.getFile(), f.getFile(), out, handler);
			} else {
				if (handler != null) {
					Message msg = handler.obtainMessage();
					Bundle bdl = new Bundle();
					bdl.putString("zip_msg", f.getNameOnly());
					msg.setData(bdl);
					handler.sendMessage(msg);
				}
				FileInputStream fi = new FileInputStream(f.getFile());
				origin = new BufferedInputStream(fi, BUFFER);
				ZipEntry entry = new ZipEntry(f.getName());
				out.putNextEntry(entry);
				int count;
				while ((count = origin.read(data, 0, BUFFER)) != -1) {
					out.write(data, 0, count);
				}
				origin.close();
			}
			out.close();
		} catch (Exception e) {
			Log.v("FE", "Error occured while ziping: " + e.toString());
		}
	}

	private static void zip(File directory, File base, ZipOutputStream zos, Handler handler)
			throws IOException {
		File[] files = directory.listFiles();
		byte[] buffer = new byte[BUFFER];
		int read = 0;
		for (int i = 0, n = files.length; i < n; i++) {
			if (handler != null) {
				Message msg = handler.obtainMessage();
				Bundle bdl = new Bundle();
				bdl.putString("zip_msg", files[i].getName());
				msg.setData(bdl);
				handler.sendMessage(msg);
			}			
			if (files[i].isDirectory()) {
				zos.putNextEntry(new ZipEntry(files[i].getPath().substring(
						base.getPath().length() + 1) + File.separator));
				zip(files[i], base, zos, handler);
			} else {
				FileInputStream in = new FileInputStream(files[i]);
				ZipEntry entry = new ZipEntry(files[i].getPath().substring(
						base.getPath().length() + 1));
				zos.putNextEntry(entry);
				while (-1 != (read = in.read(buffer))) {
					zos.write(buffer, 0, read);
				}
				in.close();
			}
		}
	}

	public static Drawable getApkIconFromPackage(String src) throws IOException {
		ZipFile zipfile = new ZipFile(src);
		Enumeration<?> e = zipfile.entries();
		ZipEntry entry;
		while (e.hasMoreElements()) {
			entry = (ZipEntry) e.nextElement();
			if (entry.isDirectory() == true)
				continue;
			String fname = DirTreeHelper.getNameFromPath(entry.getName());
			if (fname.compareToIgnoreCase("icon.png") == 0) {
				Drawable ret = PictureDrawable.createFromStream(
						zipfile.getInputStream(entry), entry.getName());
				zipfile.close();
				zipfile = null;
				e = null;
				return ret;
			}
		}
		zipfile.close();
		return null;
	}

	public static void createAllMissingDirs(String path) {
		String prevDir = DirTreeHelper.getPreviousDir(path);
		String prevOfprevDir = DirTreeHelper.getPreviousDir(prevDir);
		FeFile pp = new FeFile(prevOfprevDir);
		FeFile pd = new FeFile(prevDir);
		if (pp.exists() == true) {
			pp = null;
			if (pd.exists() == false) {
				pd.mkdir();
			}
			pd = null;
			System.gc();
			return;
		} else {
			createAllMissingDirs(prevDir);
		}
		if (pp.exists() == true) {
			if (pd.exists() == false) {
				pd.mkdir();
			}
		}
		pp = null;
		pd = null;
		System.gc();
	}

	public static ArrayList<String> getZipNames(FeFile zipFile)
			throws ZipException, IOException {
		ArrayList<String> entryNames = new ArrayList<String>();
		ZipFile zf = new ZipFile(zipFile.getPath());
		Enumeration<?> entries = zf.entries();
		String prefix = zipFile.getName() + File.separator;
		while (entries.hasMoreElements()) {
			ZipEntry entry = ((ZipEntry) entries.nextElement());
			addPathIfNotExisting(entry.getName(), entryNames, prefix);
			entryNames.add(prefix
					+ new String(entry.getName().getBytes("GB2312"), "8859_1"));

		}
		return entryNames;
	}
	
	private static void addPathIfNotExisting(String entryName,
			List<String> root, String prefix) {
		String parent = getParent(entryName);
		if (parent != null && !root.contains(prefix + parent)) {
			Log.d("TEST", "parent=" + parent);
			root.add(prefix + parent);
			addPathIfNotExisting(parent, root, prefix);			
		}
	}
	
	private static String getParent(String path) {
		String parent = path;
		
		if (path.endsWith(File.separator))
			parent = path.substring(0, path.length() - 1);
		
		int sep = parent.lastIndexOf(File.separator);

		if (sep != -1 && sep != parent.length() - 1) {
			parent = parent.substring(0, sep + 1);
			return parent;
		}
		
		return null;
	}

	public static List<String> getChildNames(List<String> root, String dir) {
		List<String> list = new ArrayList<String>();

		if (root == null || TextUtils.isEmpty(dir))
			return list;

		if (!dir.endsWith(File.separator))
			dir += File.separator;

		int len = dir.length();

		for (String name : root) {
			if (name.startsWith(dir)) {
				if (name.indexOf(File.separator, len) == name.length() - 1
						|| (len - 1) == name.lastIndexOf(File.separator)) {
					Log.d("TEST", "name=" + name);
					list.add(name);
				}
			}
		}

		if (!list.isEmpty() && list.contains(dir)) {
			list.remove(dir); // remove self
		}

		return list;
	}
}