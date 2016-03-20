package xcxin.filexpert.Batch;

import java.util.List;
import xcxin.filexpert.FeFile;

public class BatchUtil extends Object {
	
	public static boolean generateWorkItems (String file, List<WorkItem> itm_list) {
		
		if (itm_list == null || file == null) return false;
		itm_list.clear();
		
		FeFile fefile = new FeFile (file);
		if (fefile.exists() == false) return false;
		if (fefile.isFile() == true) {
			WorkItem itm = new WorkItem();
			itm.mSrcPath = fefile.getPath();
			itm.mSrcName = fefile.getName();
			itm_list.add(itm);
		} else {
			String SrcDirContents[] = fefile.list();
			String path = fefile.getPath();
			for (int index = 0; index < SrcDirContents.length; index++) {
				if (path.compareTo("/") == 0) {
					generateWorkItems ("/" + SrcDirContents[index], itm_list);
				} else {
					generateWorkItems (path + "/" + SrcDirContents[index], itm_list);
				}
			}
		}
		return true;
	}
}