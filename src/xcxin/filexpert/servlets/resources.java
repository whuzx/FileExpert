package xcxin.filexpert.servlets;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.apache.http.HttpStatus;

import xcxin.filexpert.WebServer.FeServletBase;

public class resources extends FeServletBase {

	private BufferedInputStream bis = null;

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (bis != null) {
			byte[] buffer = new byte[(int) 1024];
			int bytes_read;
			while (true) {
				bytes_read = bis.read(buffer);
				if (bytes_read == -1) {
					bis.close();
					outstream.flush();
					return;
				} else {
					outstream.write(buffer, 0, bytes_read);
				}
			}
		} else {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			writer.write("<html><body><h1>");
			writer.write("File not found");
			writer.write("</h1></body></html>");
			writer.flush();
		}
	}

	@Override
	public void execute() throws Exception {
		super.execute();
		String ids = getValue("id");
		if (ids != null) {
			int id = Integer.parseInt(ids);
			bis = new BufferedInputStream(getService().getResources()
					.openRawResource(id));
		}
	}

	@Override
	public int getHttpStatusCode() {
		if (bis == null)
			return HttpStatus.SC_NOT_FOUND;
		return super.getHttpStatusCode();
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}
