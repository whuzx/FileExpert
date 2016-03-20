package xcxin.filexpert.smb_server;

import java.io.File;

import xcxin.filexpert.Compressor;
import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileOperator;
import android.content.Context;

public class SmbServer {
	
	public static boolean prepareEnv(Context context) {
		
		String tempFolder = FeUtil.getTempDirName();
		if(FeUtil.copyFileFromRaw(tempFolder, "fesmbserver.zip", context, true) == false) {
			return false;
		}
		
		FeUtil.runCmd("mkdir /data/data/xcxin.filexpert/samba");
		try {
			Compressor.unzip(tempFolder + "/" + "fesmbserver.zip",  "/data/data/xcxin.filexpert/samba", null);
		} catch (Exception e) {
			return false;
		}
		
		String sambaPath = "/data/data/xcxin.filexpert/samba";
		FeUtil.runCmd("chmod 777 " + sambaPath);
		FeUtil.runCmd("chmod 777 " + sambaPath + "/COPYING");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/diffs");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/nmbd");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/lib");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/private");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/samba-rc");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/smb.conf");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/smbd");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/smbpasswdbin");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/var");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/var/locks");
		FeUtil.runCmd("chmod 777 " + sambaPath + "/var/temps");

		return true;
	}
}