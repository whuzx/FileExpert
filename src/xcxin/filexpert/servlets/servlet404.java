package xcxin.filexpert.servlets;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.http.HttpStatus;

import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileOperator;
import xcxin.filexpert.WebServer.FeServletBase;

public class servlet404 extends FeServletBase {

	private InputStream mIs = null;

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (mIs != null) {
			FeUtil.moveInputToOutput(mIs, outstream, 4096);
		} else {
			send404IfNeeded(outstream);
		}
	}

	@Override
	public int getHttpStatusCode() {
		if (mIs != null)
			return HttpStatus.SC_OK;
		return HttpStatus.SC_NOT_FOUND;
	}

	@Override
	public void execute() {
		File file = getFile();
		if (file == null || !file.exists()) {
			try {
				mIs = getService().getAssets().open(file.getName());
				set404(false);
			} catch (Exception e) {
				mIs = null;
				set404(true);
				return;
			}
		}
		set404(true);
	}
	@Override
	public boolean isContentTypeSet() {
		if (mIs != null)
			return true;
		return false;
	}

	@Override
	public String getContentType() {
		if (mIs != null) {
			return FileOperator.getContentType(FileOperator
					.getExtendFileName(getFile()));
		}
		return super.getContentType();
	}
}
