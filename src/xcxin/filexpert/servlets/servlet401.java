package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import org.apache.http.HttpStatus;

import xcxin.filexpert.WebServer.FeServletBase;

public class servlet401 extends FeServletBase {

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outstream, "UTF-8");
		writer.write("<html><title>401 Unauthorized</title><body><h1>");
		writer.write("401 Unauthorized</h1><br><br>");
		writer.write("Please input correct password and username</body></html>");
		writer.flush();
	}

	@Override
	public int getHttpStatusCode() {
		return HttpStatus.SC_UNAUTHORIZED;
	}

	@Override
	public void execute() throws Exception {
		super.execute();
		getHttpResponse().addHeader("WWW-Authenticate",
				"Basic realm=\"File Expert powered Android phone\"");
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}
