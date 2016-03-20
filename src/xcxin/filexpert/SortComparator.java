package xcxin.filexpert;

import java.util.Date;

public class SortComparator extends Object {

	public static class NameComparator extends Object implements FeComparator {

		@Override
		public int compare(String object1, String object2) {
			int r = object1.toLowerCase().compareTo(object2.toLowerCase());
			if (r > 0)
				return -1;
			if (r < 0)
				return 1;
			return 0;
		}

		public int compare(FeFile f1, FeFile f2) {
			return compare(f1.getName(), f2.getName());
		}
	}

	public static class TypeComparator extends Object implements FeComparator {

		private String m_path;

		public TypeComparator(String path) {
			m_path = path;
		}

		@Override
		public int compare(String object1, String object2) {
			FeFile f1, f2;
			int t1, t2;
			if (m_path.compareTo("/") == 0) {
				f1 = new FeFile("/" + object1);
				f2 = new FeFile("/" + object2);
			} else {
				f1 = new FeFile(m_path + "/" + object1);
				f2 = new FeFile(m_path + "/" + object2);
			}

			t1 = getType(f1);
			t2 = getType(f2);

			if (t1 > t2) {
				f1 = null;
				f2 = null;
				return 1;
			}

			if (t1 == t2) {
				f1 = null;
				f2 = null;
				return object2.toLowerCase().compareTo(object1.toLowerCase());
			}
			f1 = null;
			f2 = null;
			return -1;
		}

		public int compare(FeFile f1, FeFile f2) {
			int t1 = getType(f1);
			int t2 = getType(f2);
			if (t1 > t2) {
				return 1;
			}
			if (t1 == t2) {
				return f2.getName().toLowerCase()
						.compareTo(f1.getName().toLowerCase());
			}
			f1 = null;
			f2 = null;
			return -1;
		}

		private int getType(FeFile f) {
			if (f.isDirectory() == true)
				return 3;
			if (f.isFile() == true)
				return 2;
			return 1;
		}
	}

	public static class LastModifiedComparator extends Object implements
			FeComparator {

		private String m_path;

		public LastModifiedComparator(String path) {
			m_path = path;
		}

		@Override
		public int compare(String object1, String object2) {
			FeFile f1, f2;
			if (m_path.compareTo("/") == 0) {
				f1 = new FeFile(m_path + object1);
				f2 = new FeFile(m_path + object2);
			} else {
				f1 = new FeFile(m_path + "/" + object1);
				f2 = new FeFile(m_path + "/" + object2);
			}
			long d1 = f1.lastModified();
			long d2 = f2.lastModified();
			f1 = null;
			f2 = null;
			return compareDate(d1, d2);
		}

		public int compare(FeFile f1, FeFile f2) {
			long d1 = f1.lastModified();
			long d2 = f2.lastModified();
			return compareDate(d1, d2);
		}

		private int compareDate(long l1, long l2) {
			Date d1 = new Date(l1);
			Date d2 = new Date(l2);
			int r = d1.compareTo(d2);
			d1 = null;
			d2 = null;
			return r;
		}
	}

	public static class sizeComparator extends Object implements FeComparator {

		private String m_path;

		public sizeComparator(String path) {
			m_path = path;
		}

		@Override
		public int compare(String object1, String object2) {
			FeFile f1, f2;
			if (m_path.compareTo("/") == 0) {
				f1 = new FeFile(m_path + object1);
				f2 = new FeFile(m_path + object2);
			} else {
				f1 = new FeFile(m_path + "/" + object1);
				f2 = new FeFile(m_path + "/" + object2);
			}

			int t1 = getType(f1);
			int t2 = getType(f2);

			if (t1 == t2) {
				if (t1 == 2) {
					if (f1.length() > f2.length()) {
						f1 = null;
						f2 = null;
						return 1;
					}
					if (f1.length() == f2.length()) {
						f1 = null;
						f2 = null;
						return 0;
					}
					f1 = null;
					f2 = null;
					return -1;
				}
				f1 = null;
				f2 = null;
				return 0;
			}
			if (t1 > t2) {
				f1 = null;
				f2 = null;
				return 1;
			} else {
				f1 = null;
				f2 = null;
				return -1;
			}
		}

		public int compare(FeFile f1, FeFile f2) {
			int t1 = getType(f1);
			int t2 = getType(f2);
			if (t1 == t2) {
				if (t1 == 2) {
					if (f1.length() > f2.length()) {
						return 1;
					}
					if (f1.length() == f2.length()) {
						return 0;
					}
					return -1;
				}
				return 0;
			}
			if (t1 > t2) {
				return 1;
			} else {
				return -1;
			}
		}

		private int getType(FeFile f) {
			if (f.isDirectory() == true)
				return 3;
			if (f.isFile() == true)
				return 2;
			return 1;
		}
	}
}