package xcxin.filexpert.servlets;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import xcxin.filexpert.WebServer.FeServletBase;

public class welcomeServlet extends FeServletBase {
	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		OutputStreamWriter writer = new OutputStreamWriter(outstream,
				"UTF-8");
		writer.write("<html><head></head><body>");
		writer.write("<script type='text/javascript'>window.location.href='/webapps/file/listing?path=/'</script>");
		writer.write("</body></html>");
		writer.flush();
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}
