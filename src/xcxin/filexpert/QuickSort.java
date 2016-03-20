package xcxin.filexpert;

public class QuickSort {
	public static String[] perform(String[] pData, int left, int right,
			FeComparator comparator) {
		int i = left, j = right;
		String middle, strTemp;

		middle = pData[(left + right) / 2];

		do {
			while ((comparator.compare(pData[i], middle) > 0) && (i < right))
				i++;
			while ((comparator.compare(pData[j], middle) < 0) && (j > left))
				j--;
			if (i <= j) {
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		} while (i <= j);

		if (left < j) {
			perform(pData, left, j, comparator);
		}

		if (right > i)
			perform(pData, i, right, comparator);

		return pData;
	}

	public static FeFile[] perform(FeFile[] pData, int left, int right,
			FeComparator comparator) {
		int i = left, j = right;
		FeFile middle, strTemp;

		middle = pData[(left + right) / 2];

		do {
			while ((comparator.compare(pData[i], middle) > 0) && (i < right))
				i++;
			while ((comparator.compare(pData[j], middle) < 0) && (j > left))
				j--;
			if (i <= j) {
				strTemp = pData[i];
				pData[i] = pData[j];
				pData[j] = strTemp;
				i++;
				j--;
			}
		} while (i <= j);

		if (left < j) {
			perform(pData, left, j, comparator);
		}

		if (right > i)
			perform(pData, i, right, comparator);

		return pData;
	}
}
