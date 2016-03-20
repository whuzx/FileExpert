package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import xcxin.filexpert.FeUtil;
import xcxin.filexpert.WebServer.FeServletBase;

public class phone extends FeServletBase {

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		InputStream is = getService().getAssets().open("default.html");
		FeUtil.moveInputToOutput(is, outstream, 4096);
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}
