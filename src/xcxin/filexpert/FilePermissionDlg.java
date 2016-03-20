package xcxin.filexpert;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;

public class FilePermissionDlg extends Dialog implements OnClickListener {

	private final static String LOG_TAG = "FilePermissionDlg";
	
	private final FeFile mFile;

	private final CheckBox mCbUserR;
	private final CheckBox mCbUserW;
	private final CheckBox mCbUserX;
	private final CheckBox mCbGroupR;
	private final CheckBox mCbGroupW;
	private final CheckBox mCbGroupX;
	private final CheckBox mCbAllR;
	private final CheckBox mCbAllW;
	private final CheckBox mCbAllX;

	public FilePermissionDlg(Context context, FeFile file) {
		super(context);
		
		setContentView(R.layout.file_permission);
		mFile = file;

		mCbUserR = (CheckBox) findViewById(R.id.user_r);
		mCbUserW = (CheckBox) findViewById(R.id.user_w);
		mCbUserX = (CheckBox) findViewById(R.id.user_x);
		mCbGroupR = (CheckBox) findViewById(R.id.group_r);
		mCbGroupW = (CheckBox) findViewById(R.id.group_w);
		mCbGroupX = (CheckBox) findViewById(R.id.group_x);
		mCbAllR = (CheckBox) findViewById(R.id.all_r);
		mCbAllW = (CheckBox) findViewById(R.id.all_w);
		mCbAllX = (CheckBox) findViewById(R.id.all_x);

		findViewById(R.id.set_perm_ok).setOnClickListener(this);
		findViewById(R.id.set_perm_cancel).setOnClickListener(this);

		init(file);
	}

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.set_perm_ok) {
			byte[] perms = new byte[9];

			perms[0] = (byte) (mCbUserR.isChecked() ? 'r' : '-');
			perms[1] = (byte) (mCbUserW.isChecked() ? 'w' : '-');
			perms[2] = (byte) (mCbUserX.isChecked() ? 'x' : '-');
			perms[3] = (byte) (mCbGroupR.isChecked() ? 'r' : '-');
			perms[4] = (byte) (mCbGroupW.isChecked() ? 'w' : '-');
			perms[5] = (byte) (mCbGroupX.isChecked() ? 'x' : '-');
			perms[6] = (byte) (mCbAllR.isChecked() ? 'r' : '-');
			perms[7] = (byte) (mCbAllW.isChecked() ? 'w' : '-');
			perms[8] = (byte) (mCbAllX.isChecked() ? 'x' : '-');

			RootShell.setPermission(mFile.getFile(), new String(perms));
		}
		
		dismiss();
	}

	private void init(FeFile file) {
		if (!file.isLocalFile())
			return;
		
		setTitle(file.getName());

		String perm = RootShell.getPermission(file.getFile());
		Log.d(LOG_TAG, file.getPath() + " perm: " + perm);
		byte[] rwx = perm.getBytes();

		mCbUserR.setChecked(rwx[0] != '-');
		mCbUserW.setChecked(rwx[1] != '-');
		mCbUserX.setChecked(rwx[2] != '-');
		mCbGroupR.setChecked(rwx[3] != '-');
		mCbGroupW.setChecked(rwx[4] != '-');
		mCbGroupX.setChecked(rwx[5] != '-');
		mCbAllR.setChecked(rwx[6] != '-');
		mCbAllW.setChecked(rwx[7] != '-');
		mCbAllX.setChecked(rwx[8] != '-');
	}

}
