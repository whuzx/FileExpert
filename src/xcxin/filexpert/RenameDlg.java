package xcxin.filexpert;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class RenameDlg implements DialogInterface.OnClickListener {

	private FileLister mLister;
	private EditText m_ev;

	public RenameDlg(Context context) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.rename_dlg, null);
		mLister = (FileLister) context;
		m_ev = (EditText) v.findViewById(R.id.et_rename);
		m_ev.setText(mLister
				.getContentName(mLister.mActiviedContextMenuInfo.position));
		AlertDialog.Builder builder = new AlertDialog.Builder(mLister);
		builder.setPositiveButton(R.string.Okay, this);
		builder.setNegativeButton(R.string.cancel, this);
		builder.setTitle(R.string.rename);
		builder.setView(v);
		builder.create();
		builder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			boolean r = FileOperator.rename(mLister
					.getContentName(mLister.mActiviedContextMenuInfo.position),
					m_ev.getText().toString(), mLister.getCurrentPath());
			if (r == false) {
				mLister.showInfo(R.string.cannot_rename, R.string.error, false);
				return;
			} else {
				mLister.refresh();
			}
		} else {
			mLister.refresh();
		}
	}
}