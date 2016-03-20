package xcxin.filexpert;

public class DirTreeHelper extends Object {
	public static String getPreviousDir(String dir) {
		try {
			FeFile file = new FeFile(dir);
			String rootPath = file.getParent();
			if (rootPath == null)
				return "/";
			if (rootPath.equals("smb://")) {
				return "fe://smbservers";
			}
			return rootPath;
		} catch (Exception e) {
			return "/";
		}
	}

	public static String getSmbPreviousDir(String dir) {
		int smbIndex = dir.indexOf("smb");
		String temp = dir.substring(smbIndex + 5, dir.length() - 1);
		int end = temp.lastIndexOf('/');
		if (end == 0) {
			return "/";
		} else {
			return dir.substring(0, smbIndex + 5) + temp.substring(0, end)
					+ "/";
		}
	}

	public static String getNameFromPath(String path) {
		int end = path.lastIndexOf('/');
		if (end == 0)
			return path;
		return path.substring(end + 1, path.length());
	}
}