package xcxin.filexpert;

import java.io.BufferedInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import com.mobclick.android.MobclickAgent;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

public class TxtViewer extends Activity implements
		DialogInterface.OnClickListener, TextWatcher {
	private String filenameString;
	private boolean bNeedSave = false;
	private static final String gb2312 = "GB2312";
	private static String defaultCode = gb2312;
	public static final String FILE_NAME = "filename";

	private static final int DIALOG_ENCODING = 1;
	private final static int BIG_FILE_SIZE = 1024 * 200;

	private FileExpertSettings mSettings;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.filebrowser);
		mSettings = new FileExpertSettings(this);
		try {
			filenameString = getIntent().getExtras().getString(FILE_NAME);
		} catch (Exception e) {
			filenameString = null;
		}
		if (filenameString == null) {
			try {
				filenameString = getIntent().getData().getPath();
			} catch (Exception e) {
				finish();
			}
		}
		refreshGUI(defaultCode);
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onEvent(this, "TXT View");
		}
	}

	private void refreshGUI(String code) {
		String fileContent = getStringFromFile(code);
		if (fileContent == null)
			finish();

		EditText tv = (EditText) findViewById(R.id.view_contents);
		tv.setText(fileContent);
		tv.addTextChangedListener(this);
	}

	public String getStringFromFile(String code) {
		try {
			FeFile file = new FeFile(filenameString);
			if (!file.exists()) {
				return null;
			}

			// Big size file
			// When the file size is greater than BIG_FILE_SIZE,
			// use TxtReader to view the file.
			Log.d("TAG", "Size = " + file.length() + " BIG=" + BIG_FILE_SIZE);
			if (file.length() > BIG_FILE_SIZE) {
				Log.d("TAG", "YES");
				startActivity(new Intent().setClass(this, TxtReader.class)
						.putExtra(FILE_NAME, filenameString)
						.setData(getIntent().getData()));
				return null;
			}
			// END: Big size file

			setTitle(getTitle() + " - " + file.getName());
			BufferedInputStream bis = new BufferedInputStream(
					file.getInputStream());
			return FeUtil.convertStreamToString(bis, code);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (bNeedSave == true) {
				ask4Delete();
			} else {
				finish();
			}
			break;
		default:
			return super.onKeyDown(keyCode, event);
		}
		return true;
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		dialog.dismiss();
		switch (which) {
		case DialogInterface.BUTTON_NEGATIVE:
			finish();
			break;
		case DialogInterface.BUTTON_POSITIVE:
			saveText();
			finish();
			break;
		}
	}

	private void ask4Delete() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.save_ask));
		builder.setPositiveButton(getString(R.string.save), this);
		builder.setNegativeButton(getString(R.string.cancel), this);
		builder.setTitle(getString(R.string.save));
		builder.create();
		builder.show();
	}

	private void saveText() {
		try {
			FeFile file = new FeFile(filenameString);
			if (file.exists()) {
				file.delete();
			}
			EditText tv = (EditText) findViewById(R.id.view_contents);
			String fileContent = tv.getText().toString();
			OutputStream os = file.getOutputStream();
			PrintWriter pw = new PrintWriter(os);
			pw.print(fileContent);
			pw.flush();
			pw.close();
			FeUtil.showToast(this, getString(R.string.save_ok));
		} catch (Exception e) {
			FeUtil.showToast(this, getString(R.string.save_fail));
		}
	}

	@Override
	public void afterTextChanged(Editable s) {
		;
	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		if (bNeedSave != true) {
			setTitle(getTitle() + " * ");
			bNeedSave = true;
		}
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		;
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch (id) {
		case DIALOG_ENCODING:
			return new AlertDialog.Builder(this).setItems(
					R.array.txt_viewer_encodings,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							String selectedCode = TxtViewer.this.getResources()
									.getStringArray(
											R.array.txt_viewer_encodings)[which];

							if (selectedCode.compareTo(defaultCode) != 0) {
								defaultCode = selectedCode;
								refreshGUI(defaultCode);
							}
						}
					}).create();
		default:
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.text_viewer_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.txt_viewer_encoding:
			showDialog(DIALOG_ENCODING);
			return true;
		}
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onPause(this);
		}
	}

	@Override
	protected void onResume() {
		super.onPause();
		if (mSettings.isInfoCollectAllowed() == true) {
			MobclickAgent.onResume(this);
		}
	}
}
