package xcxin.filexpert.ProgressDlg;

import xcxin.filexpert.CopyAlertDiag;
import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileLister;
import xcxin.filexpert.R;
import xcxin.filexpert.Batch.FileCopyWorker;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

public class FeProgressDialog extends Thread implements
		DialogInterface.OnClickListener, Handler.Callback {

	private Context mContext;
	private FeProgressWorker mWorker;
	private Handler mHandler;
	private View mProgressView;
	private AlertDialog dlg;
	private ProgressBar pbar;

	public static final String UPDATE_MSG = "update_msg";
	public static final String UPDATE_VALUE = "update_value";
	public static final String UPDATE_VIEW = "update_view";
	public static final String UPDATE_SIZE = "update_size";
	public static final String COPY_ALERT = "copy_alert";
	public static final String TOAST_MESSAGE = "toast_message"; 

	public FeProgressDialog(Context context, FeProgressWorker worker) {
		mContext = context;
		mWorker = worker;
		mHandler = new Handler(this);
		mWorker.attachHandler(mHandler);
	}

	public void attachWorker(FeProgressWorker worker) {
		mWorker = worker;
		mWorker.attachHandler(mHandler);
	}

	@Override
	public void run() {
		super.run();
		mWorker.work(mContext);
		if (dlg != null)
			dlg.dismiss();		
		mWorker.onFinish();
	}

	@Override
	public synchronized void start() {
		LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mProgressView = inflater.inflate(R.layout.feprogress, null);
		pbar = (ProgressBar) mProgressView.findViewById(R.id.progress_bar);
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setNegativeButton(R.string.cancel, this);
		//builder.setNeutralButton("Backgroud", this);
		builder.setTitle(R.string.deal_with);
		builder.setView(mProgressView);
		builder.create();
		dlg = builder.show();
		super.start();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		FileLister lister = (FileLister) mContext;
		switch (which) {
		case Dialog.BUTTON_NEGATIVE:
			mWorker.onCancel();
			dialog.dismiss();
			lister.refresh();			
			break;
		case Dialog.BUTTON_NEUTRAL:
			mWorker.onBackgroud();
			dialog.dismiss();
			lister.mNm.showBackgroudTaskNotify(mContext, true);
			break;
		}
	}

	public Handler getHandler() {
		return mHandler;
	}

	@Override
	public boolean handleMessage(Message msg) {
		Bundle bdl = msg.getData();
		String cmd = bdl.getString(UPDATE_MSG);
		if (cmd != null) {
			TextView tv = (TextView) mProgressView
					.findViewById(R.id.progress_text);
			tv.setText(cmd);
			mWorker.resumeExecution();
			return true;
		}
		String value = bdl.getString(UPDATE_VALUE);
		if (value != null) {
			pbar.setProgress(Integer.parseInt(value));
			mWorker.resumeExecution();
			return true;
		}
		String viewstr = bdl.getString(UPDATE_VIEW);
		if (viewstr != null) {
			FileLister lister = (FileLister) mContext;
			lister.refresh();
			mWorker.resumeExecution();
			return true;
		}
		String size = bdl.getString(UPDATE_SIZE);
		if (size != null) {
			pbar.setMax(Integer.parseInt(size));
			mWorker.resumeExecution();
			return true;
		}
		String fname = bdl.getString(COPY_ALERT);
		if(fname != null) {
			FileCopyWorker fworker = (FileCopyWorker)mWorker;
			CopyAlertDiag dlg = new CopyAlertDiag(mContext, fworker, fname);
			dlg.run();
			return true;
		}
		String toastmsg = bdl.getString(TOAST_MESSAGE);
		if(toastmsg != null) {
			FeUtil.showToast(mContext, toastmsg);
			mWorker.resumeExecution();
			return true;
		}
		mWorker.resumeExecution();
		return false;
	}
}