package xcxin.filexpert.Batch;

public class WorkItem {
	public String mSrcPath;
	public String mSrcName;
	public String mDst;
	@Override
	public boolean equals(Object o) {
		if(!(o instanceof WorkItem)) {
			return false;
		}
		WorkItem wi = (WorkItem)o;
		if(wi.mSrcName.compareTo(this.mSrcName) != 0) return false;
		if(wi.mSrcPath.compareTo(this.mSrcPath) != 0) return false;
		if(this.mDst != null && wi.mDst != null) {
			if(wi.mDst.compareTo(this.mDst) != 0) return false;
		}
		return true;
	}
}
