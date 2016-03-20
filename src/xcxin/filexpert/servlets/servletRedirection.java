package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.OutputStream;

import org.apache.http.HttpStatus;

import xcxin.filexpert.WebServer.FeServletBase;

public class servletRedirection extends FeServletBase {

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public int getHttpStatusCode() {
		return HttpStatus.SC_MOVED_PERMANENTLY;
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}
