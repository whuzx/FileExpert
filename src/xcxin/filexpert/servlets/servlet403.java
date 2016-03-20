package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpStatus;

import xcxin.filexpert.WebServer.FeServletBase;

public class servlet403 extends FeServletBase {

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(
				outstream, "UTF-8");
		writer.write("<html><body><h1>");
		writer.write("Access denied");
		writer.write("</h1></body></html>");
		writer.flush();
	}

	@Override
	public int getHttpStatusCode() {
		return HttpStatus.SC_FORBIDDEN;
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}