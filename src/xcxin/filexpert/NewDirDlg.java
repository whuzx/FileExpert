package xcxin.filexpert;

import xcxin.filexpert.FileOperator.OperatorFolderException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class NewDirDlg implements DialogInterface.OnClickListener {

	private Context m_context;
	private View v;
	private String mPath;
	
	public NewDirDlg(Context context, String currentPath) {
		m_context = context;
		mPath = currentPath;
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		v = inflater.inflate(R.layout.new_folder_dlg, null);
		EditText et = (EditText) v.findViewById(R.id.new_dir_et);
		et.setText(R.string.default_new_folder_name);
		AlertDialog.Builder builder = new AlertDialog.Builder(m_context);
		builder.setPositiveButton(m_context.getString(R.string.Okay), this);
		builder.setNegativeButton(m_context.getString(R.string.cancel), this);
		builder.setTitle(m_context.getString(R.string.create_new_folder));
		builder.setView(v);
		builder.create();
		builder.show();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		if (which == DialogInterface.BUTTON_POSITIVE) {
			EditText et = (EditText) v.findViewById(R.id.new_dir_et);
			String folderName = et.getText().toString();
			String mesge = "";
			if (folderName != null) {
				boolean b = false;
				try {
					b = FileOperator.createFolder(folderName, mPath, false);
				} catch (OperatorFolderException e) {
					mesge = e.getMessage();
				}
				if (b == false) {
					((FileLister) m_context).showInfo(
							"Can not create new folder", mesge, false);
					return;
				}
				((FileLister) m_context).refresh();
			}
		}
	}
}
