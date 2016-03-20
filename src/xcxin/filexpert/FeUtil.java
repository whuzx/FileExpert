package xcxin.filexpert;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import xcxin.filexpert.FileOperator.OperatorFolderException;
import xcxin.filexpert.Batch.WorkItem;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.telephony.TelephonyManager;
import android.widget.Toast;

public class FeUtil extends Object {

	public static boolean requirement4Root() {
		try {
			DataOutputStream os = null;
			Process p = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(p.getOutputStream());
			os.writeBytes("exit\n");
			int exitValue = p.waitFor();
			if (os != null)
				p.destroy();
			if (exitValue != 0) {
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	public static String getLocalFilePermission(String file_path) {
		try {
			Process p = Runtime.getRuntime().exec("ls -l" + " " + file_path);
			InputStream is = p.getInputStream();
			InputStreamReader reader = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(reader);
			String line = br.readLine();
			if (line == null)
				return null;
			return line.substring(0, line.indexOf(" "));
		} catch (Exception e) {
			return null;
		}
	}

	public static FeFilePermission getPermission(String file_path) {
		String raw_per = getLocalFilePermission(file_path);
		if (raw_per == null)
			return null;
		FeFilePermission permission = new FeFilePermission();
		return permission;
	}

	public static String mountCifs(String ip, String user, String pass,
			String remotePath) throws IOException {

		boolean r = false;
		String mount_to = Environment.getExternalStorageDirectory().getPath()
				+ "/" + ip;
		File mt = new File(mount_to);
		if (mt.exists() == false) {
			mt.mkdir();
		}

		FeFile moduleRoot = new FeFile("/system/lib/modules");
		String cifsFile = findFile(moduleRoot, "cifs.ko", true);
		if (cifsFile == null) {
			FeFile cifsSdCard = new FeFile(
					Environment.getExternalStorageDirectory() + "/cifs.ko");
			if (cifsSdCard.exists()) {
				cifsFile = Environment.getExternalStorageDirectory()
						+ "/cifs.ko";
			}
		}
		if (cifsFile == null) {
			if (runCmd("modprobe cifs") != true) {
				return null;
			}
		} else {
			if (runCmd("insmod " + cifsFile) != true) {
				if (runCmd("modprobe cifs") != true) {
					return null;
				}
			}
		}

		String mount = "mount " + "-t cifs " + remotePath + " " + mount_to;
		if (user != null) {
			mount = mount + " " + "-o username=" + user;
			if (pass != null) {
				mount = mount + "," + "password=" + pass;
			}
		} else {
			mount = mount + " -o guest";
		}

		r = runCmd(mount);
		if (r == false) {
			return null;
		}

		return mount_to;
	}

	public static void unmountCifs(String mt) {
		if (mt == null)
			return;
		runCmd("umount " + mt);
		runCmd("rmmod cifs");
	}

	public static boolean runCmd(String cmd) {
		try {
			Process p = Runtime.getRuntime().exec("su");
			DataOutputStream os = new DataOutputStream(p.getOutputStream());
			os.writeBytes(cmd + "\n");
			os.writeBytes("exit\n");
			os.flush();
			int ret = p.waitFor();
			if (ret != 0) {
				return false;
			}
			if (os != null) {
				p.destroy();
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean isWifiConnected(Context context) {
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo ni = conMan.getActiveNetworkInfo();
		if (ni == null) {
			return false;
		}
		if (ni.getType() == ConnectivityManager.TYPE_WIFI) {
			return ni.isConnected();
		}
		return false;
	}

	public static String getPhoneIMEI(Context context) {
		TelephonyManager tm = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getDeviceId();
	}

	public static List<WorkItem> listAllApkWorkItems(FeFile dir) {
		List<WorkItem> apks = new ArrayList<WorkItem>();
		FeFile fl[] = dir.listFiles();
		if (fl == null)
			return null;
		for (int index = 0; index < fl.length; index++) {
			if (fl[index].isDirectory() == true)
				continue;
			String name = fl[index].getName().toLowerCase();
			name = name.substring(name.lastIndexOf('.') + 1, name.length());
			if (name.compareTo("apk") == 0) {
				WorkItem wi = new WorkItem();
				wi.mSrcName = fl[index].getName();
				wi.mSrcPath = DirTreeHelper.getPreviousDir(fl[index].getPath());
				apks.add(wi);
			}
		}
		return apks;
	}

	public static void showToast(Context context, String str) {
		Toast.makeText(context, str, Toast.LENGTH_SHORT).show();
	}

	public static String convertStreamToString(InputStream is)
			throws IOException {
		return convertStreamToString(is, "UTF-8");
	}

	public static String convertStreamToString(InputStream is, String encoding)
			throws IOException {
		if (is != null) {
			StringBuilder sb = new StringBuilder();
			String line;
			try {
				BufferedReader reader = new BufferedReader(
						new InputStreamReader(is, encoding));
				while ((line = reader.readLine()) != null) {
					sb.append(line).append("\n");
				}
			} finally {
				is.close();
			}
			return sb.toString();
		} else {
			return "";
		}
	}

	public static boolean copyFileFromRaw(String dest, String rawName,
			Context context, boolean deleteOnExit) {
		try {
			InputStream is = context.getAssets().open(rawName);
			File destFile = new File(dest + "/" + rawName);
			if (destFile.exists() == true) {
				destFile.delete();
			}
			FileOutputStream fos = new FileOutputStream(destFile);
			byte[] buffer = new byte[(int) 4096];
			int bytes_read;
			while (true) {
				bytes_read = is.read(buffer);
				if (bytes_read == -1) {
					fos.close();
					fos.flush();
					fos.close();
					buffer = null;
					if (deleteOnExit) {
						destFile.deleteOnExit();
					}
					System.gc();
					return true;
				} else {
					fos.write(buffer, 0, bytes_read);
				}
			}
		} catch (IOException e) {
			return false;
		}
	}

	public static long getMBfromBytes(long bytes) {
		if (bytes < (1024 * 1024)) {
			return 0;
		}
		return (bytes / (1024 * 1024));
	}

	public static String getFileSizeShowStr(long filesize) {
		if (filesize < 1024) {
			return new String(filesize + " bytes");
		}
		if (filesize < 1024 * 1024) {
			return new String((filesize / 1024) + " KB");
		}
		return new String(filesize / (1024 * 1024) + " MB");
	}

	public static void gc() {
		// Fire off a thread to do some work that we shouldn't do directly in
		// the UI thread
		Thread t = new Thread() {
			public void run() {
				System.gc();
			}
		};
		t.start();
	}

	public static boolean isSDCardInstalled() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	public static void createTempFolderIfNotExisted() {
		final String tempFolderName = ".FileExpert";
		if (isSDCardInstalled() == false) {
			// We cannot create temp folder if there's no SD card installed
			return;
		}
		try {
			FileOperator.createFolder(tempFolderName, Environment
					.getExternalStorageDirectory().getPath());
		} catch (OperatorFolderException e) {
			e.printStackTrace();
		}
	}

	public static String getTempDirName() {
		return Environment.getExternalStorageDirectory().getPath() + "/"
				+ ".FileExpert";
	}

	public static void startShareMediaActivity(String pathName,
			Activity parent, boolean tempFile) {
		File f = new File(pathName);
		startShareMediaActivity(f, parent, tempFile);
	}

	public static void startShareMediaActivity(File file, Activity parent,
			boolean tempFile) {
		String mimeType = FileOperator.getContentType(FileOperator
				.getExtendFileName(file));
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_SEND);
		intent.setType(mimeType);
		intent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(file));
		try {
			parent.startActivity(Intent.createChooser(intent,
					parent.getString(R.string.share_to)));
		} catch (android.content.ActivityNotFoundException ex) {
			FeUtil.showToast(parent, parent.getString(R.string.share_failed));
		}
		if (tempFile) {
			file.deleteOnExit();
		}
	}

	public static String findFile(FeFile srcFile, String fileName,
			boolean subDir) {
		if (srcFile.isFile()) {
			if (srcFile.getName().equals(fileName)) {
				return srcFile.getPath();
			}
			return null;
		}
		FeFile[] files = srcFile.listFiles();
		if (files != null && files.length > 0) {
			for (FeFile file : files) {
				if (file.isFile()) {
					if (file.getName().equals(fileName)) {
						return file.getPath();
					}
				}
				if (file.isDirectory()) {
					if (subDir) {
						String result = findFile(file, fileName, subDir);
						if (result != null) {
							return result;
						}
					}
				}
			}
		}
		return null;
	}

	public static String generateStackTrace(StackTraceElement[] traces,
			String newlineCR) {
		String res = null;
		for (StackTraceElement trace : traces) {
			if (res == null) {
				res = trace.toString();
			} else {
				res = res + newlineCR + trace.toString();
			}
		}
		return res;
	}

	public static void moveInputToOutput(InputStream is, OutputStream os,
			int length) throws IOException {
		BufferedOutputStream bos = new BufferedOutputStream(os);
		BufferedInputStream bis = new BufferedInputStream(is);
		byte[] buffer = new byte[length];
		while (true) {
			int bytes_read = bis.read(buffer);
			if (bytes_read == -1) {
				bis.close();
				bos.flush();
				bos.close();
				return;
			} else {
				bos.write(buffer, 0, bytes_read);
			}
		}
	}

	public static String getLastModifiedString(long num) {
		Date date = new Date(num);
		return date.toLocaleString();
	}
}
