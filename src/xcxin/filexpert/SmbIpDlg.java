package xcxin.filexpert;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SmbIpDlg extends Dialog implements OnClickListener,
		OnCheckedChangeListener {

	public static final int NEW_MODE = 0;
	public static final int MODIFY_MODE = 1;

	private EditText mUser;
	private EditText mPassword;
	private EditText mIp;
	private EditText mDomain;
	private String OldIp;
	private int m_Mode;
	private SmbServerMgr mSmbMgr;
	private FileLister mParent;
	private CheckBox mCb;
	private boolean mAny;

	public SmbIpDlg(Context context, SmbServerMgr mgr, String domain, String ip, String user,
			String pass, boolean any, int mode) {
		super(context);
		setTitle(context.getString(R.string.share_pc_info));
		setOwnerActivity((Activity) context);
		setContentView(R.layout.smb_ip_dlg);
		Button btn = (Button) findViewById(R.id.btn_smb_ip_ok);
		btn.setOnClickListener(this);
		btn = (Button) findViewById(R.id.btn_smb_ip_cancel);
		btn.setOnClickListener(this);
		CheckBox mCb = (CheckBox) findViewById(R.id.smb_cb);
		mCb.setOnCheckedChangeListener(this);
		EditText ev = (EditText) this.findViewById(R.id.smb_ip_et);
		ev.setText("");

		mUser = (EditText) this.findViewById(R.id.smb_user_et);
		if (user != null) {
			mUser.setText(user);
		} else {
			mUser.setText("");
		}

		mPassword = (EditText) this.findViewById(R.id.smb_password_et);
		if (pass != null) {
			mPassword.setText(pass);
		} else {
			mPassword.setText("");
		}

		mIp = (EditText) this.findViewById(R.id.smb_ip_et);
		if (ip != null) {
			mIp.setText(ip);
		} else {
			mIp.setText("");
		}

		mDomain = (EditText) this.findViewById(R.id.smb_domain_et);
		if (domain != null) {
			mDomain.setText(domain);
		} else {
			mDomain.setText("");
		}
		
		if (mode == MODIFY_MODE) {
			OldIp = ip;
		}
		m_Mode = mode; 

		mAny = any;
		mCb.setChecked(mAny);

		mSmbMgr = mgr;
		mParent = (FileLister) context;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_smb_ip_ok:
			/*
			if (FileLister.isValidIP(mIp.getText().toString()) == false) {
				mParent.showInfo(mParent.getString(R.string.invalid_ip), mParent
						.getString(R.string.error), false);
				return;
			}
			*/
			if (m_Mode == NEW_MODE) {
				mSmbMgr.add(mDomain.getText().toString(), mIp.getText().toString(), mAny, mUser.getText()
						.toString(), mPassword.getText().toString());
			} else {
				mSmbMgr.modify(OldIp, mDomain.getText().toString(), mIp.getText().toString(), mAny, mUser.getText()
						.toString(), mPassword.getText().toString());
			}
			mSmbMgr.SmbServerUpdateCache();
			mParent.refresh();
			dismiss();
			break;
		case R.id.btn_smb_ip_cancel:
			dismiss();
			break;
		case R.id.btn_smb_ip_clear:
			mIp.setText("");
			mDomain.setText("");
			mPassword.setText("");
			mUser.setText("");
			mCb.setChecked(false);
			mAny = false;
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton v, boolean isChecked) {
		boolean r = (isChecked == true ? false : true);
		mAny = isChecked;
		mUser.setEnabled(r);
		mPassword.setEnabled(r);
	}
	
	public void setParameters (String ip, String user, String pass, boolean any, int mode) {
		this.OldIp = ip;
		this.mIp = (EditText) this.findViewById(R.id.smb_ip_et);
		this.mIp.setText(ip);
		this.mUser = (EditText) this.findViewById(R.id.smb_user_et);
		this.mUser.setText(user);
		this.mPassword = (EditText) this.findViewById(R.id.smb_password_et);
		this.mPassword.setText(pass);
		this.mAny = any;
		this.mCb = (CheckBox) findViewById(R.id.smb_cb);
		this.mCb.setChecked(mAny);
		this.m_Mode = mode;
	}
}