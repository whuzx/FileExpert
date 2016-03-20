package xcxin.filexpert.Batch;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

public interface AppWorker {
	public boolean process (ApplicationInfo ai, PackageManager pm);
}
