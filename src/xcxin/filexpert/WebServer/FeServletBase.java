package xcxin.filexpert.WebServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.AbstractHttpEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.json.JSONObject;

import xcxin.filexpert.FeUtil;

import android.app.Service;

public abstract class FeServletBase extends Object implements ContentProducer {

	protected HttpRequest mHttpRequest;
	protected HttpResponse mHttpResponse;
	protected String mDocRoot;
	protected String mTarget;
	protected File mFile;
	protected Service mService;
	protected HashMap<String, String> paramsMap;
	protected boolean m404 = false;
	protected String mServerMessage;

	public FeServletBase(HttpRequest request, HttpResponse response) {
		mHttpRequest = request;
		mHttpResponse = response;
	}

	public FeServletBase() {
		paramsMap = new HashMap<String, String>();
		paramsMap.clear();
	}

	public void setParams(String key, String value) {
		paramsMap.put(key, value);
	}

	public String getValue(String key) {
		return paramsMap.get(key);
	}

	public void setHttpRequestAndResponse(HttpRequest request,
			HttpResponse response) {
		mHttpRequest = request;
		mHttpResponse = response;
	}

	public void setRootAndTarget(String root, String target) {
		mDocRoot = root;
		mTarget = target;
		if (mTarget != null) {
			parseParams();
		}
	}

	public void setFile(File file) {
		mFile = file;
	}

	public File getFile() {
		return mFile;
	}

	public void setService(Service service) {
		mService = service;
	}

	public Service getService() {
		return mService;
	}

	public String getRoot() {
		return mDocRoot;
	}

	public String getTarget() {
		return mTarget;
	}
	
	public String getServerMessage() {
		return mServerMessage;
	}

	public void setServerMessage(String message) {
		mServerMessage = message;
	}
	
	public HttpRequest getHttpRequest() {
		return mHttpRequest;
	}

	public HttpResponse getHttpResponse() {
		return mHttpResponse;
	}
	
	public String getHttpMethod() {
		return mHttpRequest.getRequestLine().getMethod()
				.toUpperCase(Locale.ENGLISH);
	}

	public String getTargetUrl() {
		return URLDecoder.decode(mHttpRequest.getRequestLine().getUri());
	}

	public InputStream getPostInputStream() throws IllegalStateException,
			IOException {
		if (mHttpRequest instanceof HttpEntityEnclosingRequest) {
			HttpEntity entity = ((HttpEntityEnclosingRequest) mHttpRequest)
					.getEntity();
			return entity.getContent();
		}
		return null;
	}

	public String getContentType() {
		return mHttpRequest.getFirstHeader("Content-Type").getValue();
	}

	public AbstractHttpEntity getEntity() {
		return new EntityTemplate(this);
	}

	public int getHttpStatusCode() {
		return HttpStatus.SC_OK;
	}

	public void execute() throws Exception {

	}

	protected void parseParams() {
		int start = mTarget.indexOf("?");
		if (start > 0) {
			String str = mTarget.substring(start + 1);
			String paras[] = str.split("&");
			for (String para : paras) {
				try {
					String kv[] = para.split("=");
					setParams(kv[0], kv[1]);
				} catch (Exception e) {
					;
				}
			}
		}
	}

	public void set404(boolean state) {
		m404 = state;
	}

	public void send404IfNeeded(OutputStream outstream) throws IOException {
		if (m404) {
			OutputStreamWriter writer = new OutputStreamWriter(outstream,
					"UTF-8");
			writer.write("<html><body><h1>");
			writer.write("File not found");
			writer.write("</h1></body></html>");
			writer.flush();
		}
	}
	
	public boolean is404set() {
		return m404;
	}

	public JSONObject getJsonFromPostData() throws Exception {
		if (getHttpMethod().toLowerCase().equals("post")) {
			InputStream is = getPostInputStream();
			String data = FeUtil.convertStreamToString(is);
			if (data != null) {
				data = URLDecoder.decode(data, "UTF-8");
				return new JSONObject(data.split("=")[1]);
			}
			return null;
		} else {
			return null;
		}
	}
	
	abstract public boolean isContentTypeSet();
}
