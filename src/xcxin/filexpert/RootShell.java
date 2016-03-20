package xcxin.filexpert;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import android.text.TextUtils;
import android.util.Log;

public class RootShell {
	
	private final static String LOG_TAG = "RootShell";
	private static boolean mEnableRoot = true;
	
	public static void enableRoot(boolean enable) {
		if (enable && canExecRoot())
			mEnableRoot = true;
		else
			mEnableRoot = false;
	}
	
	public static boolean canExecRoot() {
		boolean enable = mEnableRoot;
		boolean ret = false;
		mEnableRoot = true;
		String res = execCmd("id");
		if (res != null && res.contains("uid=0") && res.contains("gid=0")) {
			ret = true;
		}
		mEnableRoot = enable;

		return ret;
	}
	
	public static boolean isRootEnabled() {
		return mEnableRoot;
	}

	public static String execCmd(String command) {
		if (!mEnableRoot)
			return null;
		Log.d(LOG_TAG, "Prepare to exec: " + command);
		String ret = null;
		InputStream is = null;
		Process process = null;
		DataOutputStream os = null;
		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			is = process.getInputStream();
			os.write((command + "\n").getBytes("UTF-8"));
			os.writeBytes("exit\n");
			os.flush();
			process.waitFor();
		} catch (Exception e) {
			return null;
		} finally {
			try {
				if (os != null) {
					ret = readToString(is);
					os.close();
				}
				process.destroy();
			} catch (Exception e) {
			}
		}
		return ret;
	}
	
	public static String readToString(InputStream in) throws IOException {
		String content = "";
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] buf = new byte[1024];
		int len = 0;
		while ((len = in.read(buf)) > 0) {
			baos.write(buf, 0, len);
		}
		content = baos.toString();
		return content;
	}
	
	private static boolean exec(String cmd) {
		if (TextUtils.isEmpty(cmd))
			return false;
		
		String result = RootShell.execCmd(cmd);
		if (result == null)
			return false;

		return true;
	}
	
	public static String[] list(File file) {
		String result = RootShell.execCmd("ls -a '" + file.getPath() + "'");
		if (result == null || TextUtils.isEmpty(result.trim()))
			return new String[0];
		
		ArrayList<String> list = new ArrayList<String>();
		
		String[] fList = result.split("\n");
		for (String f : fList) {
			if (".".equals(f) || "..".equals(f))
				continue;
			list.add(f);
		}
		
		return list.toArray(new String[list.size()]);
	}
	
	public static File[] listFiles(File file) {
		String[] list = list(file);
		File files[] = new File[list.length];
		for (int index = list.length - 1; index >= 0; index--) {
			files[index] = new File(file.getPath() + "/" + list[index]);
		}
		return files;
	}
	
	public static boolean copyFile(File src, File dst) {
//		return exec("cat '" + src.getPath() + "' > '" + src.getPath() + "'");
		return exec("dd if='" + src + "' of='" + dst + "'");
	}
	
	public static boolean cutFile(File src, File dst) {
		return renameFile(src, dst);
	}
	
	public static boolean renameFile(File oldName, File newName) {
		return exec("mv '" + oldName.getPath() + "' '" + newName.getPath() + "'");
	}
	
	public static boolean delFile(File file) {
		if (file.isDirectory())
			return exec("rm -r '" + file.getPath() + "'");//Shall we add "rm -r" to delete directory?
		else
			return exec("rm '" + file.getPath() + "'");
	}
	
	public static boolean mkdir(File file) {
		return exec("mkdir '" + file.getPath() + "'");
	}
	
	public static boolean isFile(File file) {
		String parent = file.getParent();
		String result = execCmd("ls -a -l '" + parent +"'");
		
		if (result != null) {
			String name = file.getName();
			String[] files = result.trim().split("\n");
			for (String f : files) {
				if (f.endsWith(name)) {
					if (f.startsWith("d"))
						return false;
					break;
				}
			}
		}
		
		return true;
	}
	
	public static boolean isDirectory(File file) {
		return !isFile(file);
	}
	
	private static boolean isRw(String line) {
		if (!TextUtils.isEmpty(line)
				&& (line.startsWith("rw")
						|| line.startsWith("(rw")))
			return true;
		return false;
	}
	
	public static boolean isSystemReadWritable() {
		boolean isSystemRw = false;
		boolean isRootRw = false;
		
		try {
			FileReader file = new FileReader("/proc/mounts");
			char[] buffers = new char[1024];
			int len = file.read(buffers);
			String content = new String(buffers, 0, len);

			String[] list = content.split("\n");
			for (String str : list) {
				String[] detail = str.split(" ");
				if ("/system".equals(detail[1])) {
					isSystemRw = isRw(detail[3]);
				} else if ("/".equals(detail[1])) {
					isRootRw = isRw(detail[3]);
				}
			}
		} catch (IOException e) {
		}

		return (isSystemRw && isRootRw);
	}
	
	private static boolean remount(boolean rw, String devpath, String point) {
		String cmd = "mount " + (rw ? "-rw" : "-r") + " -o remount " + devpath
				+ " " + point;
		return exec(cmd);
	}
	
	public static boolean remount(boolean rw) {
		String result = RootShell.execCmd("mount");
		if (result == null)
			return false;
		
		boolean isSystemMounted = false;
		boolean isRootMounted = false;
		
		String[] list = result.split("\n");
		for (String str : list) {
			if (str.contains("/system")) {
				String[] detail = str.split(" ");
				if ("/system".equals(detail[1])) {
					isSystemMounted = remount(rw, detail[0], "/system");
				} else if ("/".equals(detail[1])) {
					isSystemMounted = remount(rw, detail[0], "/");
				}
			}
		}
		
		return (isSystemMounted && isRootMounted);
	}
	
	public static String getPermission(File file) {
		String parent = file.getParent();
		String result = RootShell.execCmd("ls -a -l '" + parent + "'");

		if (result == null || TextUtils.isEmpty(result.trim()))
			return "---------";

		String name = file.getName();

		String[] list = result.split("\n");
		for (String str : list) {
			if (str.endsWith(name)) {
				String perm = str.substring(0, str.indexOf(' '));
				return perm.substring(1, 10);
			}
		}

		return "---------";
	}
	
	/**
	 * Set the permission for the path.
	 * 
	 * @param path  Path of file or directory.
	 * @param perm __MUST__ be as 'rwxr--r--x' or '755' or '0755'.
	 * @return true if successfully
	 */
	public static boolean setPermission(File file, String perm) {
		if (TextUtils.isEmpty(perm))
			return false;
		
		if (perm.matches("[0-7]{3,4}")) {
			return exec("chmod " + perm + " '" + file.getPath() + "'");
		}
		
		if (perm.length() != 9)
			return false;
		
		byte[] perms = new byte[]{'0', '0' , '0'};
		byte[] bytes = perm.getBytes();
		for (int i = 0; i < bytes.length; i++) {
			perms[i / 3] += perm2num(bytes[i]); 
		}

		return exec("chmod " + new String(perms) + " '" + file.getPath() + "'");
	}
	
	private static int perm2num(byte perm) {
		switch(perm) {
		case 'r':
			return 4;
		case 'w':
			return 2;
		case 'x':
			return 1;
		default:
			return 0;
		}
	}
}
