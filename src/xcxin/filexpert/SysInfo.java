package xcxin.filexpert;

import android.os.Build;

public class SysInfo extends Object {
	
	public static int getSDKVersion() {
		return Build.VERSION.SDK_INT;
	}
	
	public static String getSysInfo() {
		String info = new String();
		info = "BOARD: " + Build.BOARD + "\n";
		info = info + "BRAND: " + Build.BRAND + "\n";
		info = info + "DEVICE: " + Build.DEVICE + "\n";
		info = info + "ID: " + Build.ID + "\n";
		info = info + "MODEL: " + Build.MODEL + "\n";
		info = info + "MANUFACTURER: " + Build.MANUFACTURER + "\n";
		info = info + "PRODUCT: " + Build.PRODUCT;
		return info;
	}
}