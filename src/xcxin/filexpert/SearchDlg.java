package xcxin.filexpert;

import xcxin.filexpert.Batch.SearchBatch;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SearchDlg extends Dialog implements OnClickListener, OnCheckedChangeListener {

	private FileLister mParent;
	private FeFile mTarget;
	private CheckBox mCb;
	private CheckBox mReg;
	private CheckBox mCase;
	private boolean mSubDir;
	private boolean mRegExp;
	private boolean mCaseIns;
	
	public SearchDlg(Context context) {
		super(context);
		setTitle(context.getString(R.string.search));
		setOwnerActivity((Activity) context);
		setContentView(R.layout.search_dlg);
		Button okay_btn = (Button)findViewById(R.id.sea_btn_okay);
		okay_btn.setOnClickListener(this);
		Button cancle_btn = (Button)findViewById(R.id.sea_btn_cancle);
		cancle_btn.setOnClickListener(this);
		mParent = (FileLister)context;
		
		mCb = (CheckBox)findViewById(R.id.sea_cb_subdir);
		mCb.setChecked(true);
		mSubDir = true;
		mCb.setOnCheckedChangeListener(this);

		mCase = (CheckBox)findViewById(R.id.sea_cb_case);
		mCase.setChecked(false);
		mCaseIns = false;
		mCase.setOnCheckedChangeListener(this);

		mReg = (CheckBox)findViewById(R.id.sea_cb_reg);
		mReg.setChecked(false);
		mRegExp = false;
		mReg.setOnCheckedChangeListener(this);
	}
	
	public void setTarget(FeFile target) {
		mTarget = target;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case	R.id.sea_btn_okay:
			TextView tv = (TextView)this.findViewById(R.id.sea_et_name);
			dismiss();
			SearchBatch sb = new SearchBatch(mParent, mSubDir, mRegExp, mCaseIns, mTarget, tv.getText().toString());
			sb.execute(null, null, null);
			break;
		case R.id.sea_btn_cancle:
			dismiss();
			break;
		}
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch(buttonView.getId()) {
		case	R.id.sea_cb_subdir:
			mSubDir = isChecked;
			break;
		case	R.id.sea_cb_reg:
			mRegExp = isChecked;
			break;
		case	R.id.sea_cb_case:
			mCaseIns = isChecked;
			break;
		}
	}
}