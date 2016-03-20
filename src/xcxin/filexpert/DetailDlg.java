package xcxin.filexpert;

import java.util.Date;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DetailDlg extends Dialog implements OnClickListener {

	private FeFile mTarget;
	private TextView mType;
	private TextView mLastModified;
	private TextView mSize;

	public DetailDlg(Context context, FeFile target) {
		super(context);
		setContentView(R.layout.detail_dlg);
		setTarget(target);
	}

	public void setTarget(FeFile target) {
		mTarget = target;
		setTitle(target.getName());
		mType = (TextView) findViewById(R.id.tv_detail_type);
		mLastModified = (TextView) findViewById(R.id.tv_detail_last_modified);
		mSize = (TextView) findViewById(R.id.tv_detail_size);
		((Button) findViewById(R.id.btn_detail_confirm))
				.setOnClickListener(this);

		if (mTarget.isDirectory() == false) {
			mType.setText(getContext().getString(R.string.detail_type)
					+ getContext().getString(R.string.file));
		} else {
			mType.setText(getContext().getString(R.string.detail_type)
					+ getContext().getString(R.string.directory));
		}
		Date lmDate = new Date(mTarget.lastModified());
		mLastModified.setText(getContext().getString(R.string.detail_lm)
				+ lmDate.toLocaleString());
		if (mTarget.isFile() == true) {
			mSize.setText(getContext().getString(R.string.detail_size)
					+ FeUtil.getFileSizeShowStr(mTarget.length()));
		} else {
			computeTask sizeTask = new computeTask();
			sizeTask.execute(null, null, null);
		}
	}

	@Override
	public void onClick(View v) {
		dismiss();
	}

	protected class computeTask extends AsyncTask<Void, Void, Void> {

		private long size;
		private ProgressDialog mpd;

		@Override
		protected Void doInBackground(Void... params) {
			try {
				size = computeDirSize(mTarget);
			} catch (Exception e) {
				size = 0;
			}
			publishProgress();
			System.gc();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			if (mpd != null) {
				mpd.dismiss();
			}
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			mpd = ProgressDialog.show(getContext(), getContext().getString(
					R.string.working), getContext().getString(
					R.string.computing_dir_size));
		}

		private long computeDirSize(FeFile dir) {
			long size = 0;
			try {
				FeFile items[] = dir.listFiles();
				if (items == null) {
					return 0;
				}
				for (int index = 0; index < items.length; index++) {
					if (items[index].isFile() == true) {
						size += items[index].length();
					} else {
						size += computeDirSize(items[index]);
					}
				}
				return size;
			} catch (Exception e) {
				return 0;
			}
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			mSize.setText(getContext().getString(R.string.detail_size) + FeUtil.getFileSizeShowStr(size));
		}
	}
}
