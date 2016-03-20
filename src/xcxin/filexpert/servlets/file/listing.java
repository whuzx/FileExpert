package xcxin.filexpert.servlets.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.StringTokenizer;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.FileEntity;

import xcxin.filexpert.FileOperator;
import xcxin.filexpert.MultipartStream;
import xcxin.filexpert.WebServer.FeServletBase;

public class listing extends FeServletBase {

	protected File mOperFile;
	protected boolean mIsFile = false;

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (mOperFile != null && is404set() == false) {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			DirectoryHtmlGenerator dhg = new DirectoryHtmlGenerator(writer,
					mOperFile, getService(), getRoot());
			dhg.Generate();
		} else {
			set404(true);
			send404IfNeeded(outstream);
		}
	}

	public void processUpload(HttpRequest request, HttpEntity entity,
			String folder) throws IOException, HttpException {
		/* Find the boundary and the content length. */
		String contentType = request.getFirstHeader("Content-Type").getValue();
		String boundary = contentType.substring(contentType
				.indexOf("boundary=") + "boundary=".length());
		InputStream input = entity.getContent();

		MultipartStream multipartStream = new MultipartStream(input,
				boundary.getBytes());
		String headers = multipartStream.readHeaders();

		/* Get the filename. */
		StringTokenizer tokens = new StringTokenizer(headers, ";", false);
		String filename = null;
		while (tokens.hasMoreTokens() && filename == null) {
			String token = tokens.nextToken().trim();
			if (token.startsWith("filename=")) {
				filename = URLDecoder.decode(
						token.substring("filename=\"".length(),
								token.lastIndexOf("\"")), "utf8");
			}
		}

		if (filename.compareTo("") == 0) {
			return;
		}

		if (filename.indexOf("\\") > 0) {
			// Maybe we run in IE?
			filename = filename.substring(filename.lastIndexOf("\\") + 1);
		}

		/* Write the file and add it to the shared folder. */
		String real_folder;
		if (folder.compareTo("/") == 0) {
			real_folder = getRoot();
		} else {
			real_folder = getRoot() + folder;
		}
		File uploadFile = new File(real_folder, filename);
		FileOutputStream output = new FileOutputStream(uploadFile);
		multipartStream.readBodyData(output);
		output.close();
	}

	@Override
	public void execute() throws Exception {

		super.execute();

		String method = getHttpMethod();
		HttpRequest request = getHttpRequest();
		String target = getValue("path");
		if (target == null) {
			target = getTarget();
		}
		
		if (method.equals("POST")) {
			if (request instanceof HttpEntityEnclosingRequest) {
				HttpEntity entity = ((HttpEntityEnclosingRequest) request)
						.getEntity();
				try {
					processUpload(request, entity, target);
				} catch (IOException e) {
					e.printStackTrace();
				} catch (HttpException e) {
					e.printStackTrace();
				}

			}
		}

		mOperFile = new File(getRoot(), target);
		String filename = getValue("createfolder");
		if (filename != null) {
			FileOperator.createFolder(filename, mOperFile.getPath(), false);
			return;
		}
		filename = getValue("delete");
		if (filename != null) {
			String filenames[] = filename.split("!!");
			for (String name : filenames) {
				File tf = new File(mOperFile.getPath() + "/" + name);
				if (tf.exists() && method.equals("GET")) {
					FileOperator.delete(tf);
				}
			}
			return;
		}
		String oldfile = getValue("oldname");
		if (oldfile != null) {
			String newfile = getValue("newname");
			if (newfile != null) {
				FileOperator.rename(oldfile, newfile, mOperFile.getPath());
				return;
			}
		}
		if (!mOperFile.exists()) {
			set404(true);
		}
		if (mOperFile.isFile()) {
			set404(true);
		}
	}

	@Override
	public AbstractHttpEntity getEntity() {
		if (mIsFile == false || is404set()) {
			return super.getEntity();
		}
		return new FileEntity(mOperFile,
				FileOperator.getContentType(FileOperator
						.getExtendFileName(mOperFile)));
	}

	@Override
	public int getHttpStatusCode() {
		if (is404set())
			return HttpStatus.SC_NOT_FOUND;
		return super.getHttpStatusCode();
	}
	@Override
	public boolean isContentTypeSet() {
		return false;
	}
}
