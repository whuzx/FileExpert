package xcxin.filexpert;

import xcxin.filexpert.Batch.FileCopyWorker;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;

public class CopyAlertDiag extends Object implements
		DialogInterface.OnClickListener {

	private Context m_context;
	private String m_title;
	private String m_info;
	private FileCopyWorker m_worker;

	public CopyAlertDiag(Context context, FileCopyWorker worker, String fn) {
		super();
		m_context = context;
		m_worker = worker;
		CreateDiag(m_context.getString(R.string.warning), fn + " "
				+ m_context.getString(R.string.already_exist));
	}
	
	protected void CreateDiag(String title, String info) {
		m_title = title;
		m_info = info;
		// run();
	}

	public void run() {
		AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
		builder.setTitle(m_title);
		builder.setMessage(m_info);
		builder.setNegativeButton(m_context.getString(R.string.overwrite_all), this);
		builder.setPositiveButton(m_context.getString(R.string.skip), this);
		builder.setNeutralButton(m_context.getString(R.string.overwrite), this);
		builder.setCancelable(false);
		builder.create();
		builder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		if (which == AlertDialog.BUTTON_POSITIVE) {
			// Skip exist contents
			m_worker.skip_existing = true;
		} else if (which == AlertDialog.BUTTON_NEUTRAL) {
			// Overwrite
			m_worker.skip_existing = false;
		} else if (which == AlertDialog.BUTTON_NEGATIVE) {
			// Always overwrite
			m_worker.auto_overwrite = true;
			m_worker.skip_existing = false;
		} else {
			// Skip by default or other key pressed
			m_worker.skip_existing = true;
		}
		// Tell copy thread continue work
		m_worker.resumeExecution();
	}
}
