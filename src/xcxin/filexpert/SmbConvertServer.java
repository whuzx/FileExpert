package xcxin.filexpert;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import org.apache.http.ConnectionClosedException;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.HttpStatus;
import org.apache.http.entity.BasicHttpEntity;
import org.apache.http.entity.ContentProducer;
import org.apache.http.entity.EntityTemplate;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import android.util.Log;

/**
 * Basic, yet fully functional and spec compliant, HTTP/1.1 file server.
 * <p>
 * Please note the purpose of this application is demonstrate the usage of
 * HttpCore APIs. It is NOT intended to demonstrate the most efficient way of
 * building an HTTP file server.
 * 
 * 
 * @version $Revision$
 */

public class SmbConvertServer {

	private RequestListenerThread mHttpServerThread;
	private int mPort;
	private boolean mStarted = false;
	private FeFile mSmbCovertTarget = null;
	private int mSmbBufSize;

	public SmbConvertServer(String rootDir, int port, FeFile smbConvert)
			throws IOException {
		mSmbCovertTarget = smbConvert;
		startServer(rootDir, port);
	}

	public SmbConvertServer(String rootDir, int port, String targetPath,
			int smbBufSize) throws IOException {
		mSmbCovertTarget = new FeFile(targetPath);
		if (smbBufSize > 0) {
			mSmbBufSize = smbBufSize;
		} else {
			mSmbBufSize = 512 * 1024;
		}
		startServer(rootDir, port);
	}

	public void startServer(String rootDir, int port) throws IOException {
		mPort = port;
		mHttpServerThread = new RequestListenerThread(port, rootDir,
				mSmbCovertTarget);
		mHttpServerThread.setDaemon(false);
		mHttpServerThread.start();
		mStarted = true;
	}

	public Thread getServerThread() {
		return mHttpServerThread;
	}

	public int getHttpPort() {
		return mPort;
	}

	public void stop() throws IOException {
		if (mStarted == true) {
			mHttpServerThread.stopThread();
			mStarted = false;
		}
	}

	public boolean getStartStatus() {
		return mStarted;
	}

	public class HttpFileHandler implements HttpRequestHandler {

		private final String docRoot;
		private FeFile mSmbConvert = null;

		public HttpFileHandler(final String docRoot, FeFile smbConvert) {
			super();
			this.docRoot = docRoot;
			mSmbConvert = smbConvert;
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			String target = request.getRequestLine().getUri();
			final File file = new File(this.docRoot, URLDecoder.decode(target));

			if (mSmbConvert != null) {
				if (file.getName().compareTo(
						mSmbConvert.getName().substring(0,
								mSmbConvert.getName().length() - 1)) == 0) {
					response.setStatusCode(HttpStatus.SC_OK);
					BasicHttpEntity smbConvertEntity = new BasicHttpEntity();
					try {
						BufferedInputStream bis = new BufferedInputStream(
								mSmbConvert.getInputStream(), mSmbBufSize);
						smbConvertEntity.setContent(bis);
					} catch (Exception e) {
						smbConvertEntity.setContent(mSmbConvert
								.getInputStream());
					}
					smbConvertEntity.setContentType(FileOperator
							.getContentType(FileOperator
									.getExtendFileName(mSmbConvert)));
					smbConvertEntity.setContentLength(mSmbConvert.length());
					response.setEntity(smbConvertEntity);
					return;
				}
			} else {
				response.setStatusCode(HttpStatus.SC_FORBIDDEN);
				EntityTemplate body = new EntityTemplate(new ContentProducer() {
					public void writeTo(final OutputStream outstream)
							throws IOException {
						OutputStreamWriter writer = new OutputStreamWriter(
								outstream, "UTF-8");
						writer.write("<html><body><h1>");
						writer.write("Access denied");
						writer.write("</h1></body></html>");
						writer.flush();
					}
				});
				body.setContentType("text/html; charset=UTF-8");
				response.setEntity(body);
				Log.v("FE", "Cannot read file " + file.getPath());
			}
		}
	}

	public class RequestListenerThread extends Thread {

		private ServerSocket serversocket;
		private final HttpParams params;
		private final HttpService httpService;

		public RequestListenerThread(int port, final String docroot,
				FeFile smbConvert) throws IOException {
			this.serversocket = new ServerSocket(port);
			this.params = new BasicHttpParams();
			this.params
					.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
					.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
							512 * 1024)
					.setBooleanParameter(
							CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
					.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
					.setParameter(CoreProtocolPNames.ORIGIN_SERVER,
							"HttpComponents/1.1");

			// Set up the HTTP protocol processor
			BasicHttpProcessor httpproc = new BasicHttpProcessor();
			httpproc.addInterceptor(new ResponseDate());
			httpproc.addInterceptor(new ResponseServer());
			httpproc.addInterceptor(new ResponseContent());
			httpproc.addInterceptor(new ResponseConnControl());

			// Set up request handlers
			HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
			reqistry.register("*", new HttpFileHandler(docroot, smbConvert));

			// Set up the HTTP service
			this.httpService = new HttpService(httpproc,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory());
			this.httpService.setParams(this.params);
			this.httpService.setHandlerResolver(reqistry);
		}

		public void run() {
			Log.v("FE", "Listening on port " + this.serversocket.getLocalPort());
			while (!Thread.interrupted()) {
				try {
					// Set up HTTP connection
					Socket socket = this.serversocket.accept();
					DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
					Log.v("FE",
							"Incoming connection from "
									+ socket.getInetAddress());
					conn.bind(socket, this.params);
					// Start worker thread
					Thread t = new WorkerThread(this.httpService, conn);
					t.setDaemon(true);
					t.start();
				} catch (InterruptedIOException ex) {
					break;
				} catch (IOException e) {
					Log.v("FE", "I/O error initialising connection thread: "
							+ e.getMessage());
					break;
				}
			}
		}

		public void stopThread() throws IOException {
			// stop();
			serversocket.close();
			serversocket = null;
			FeUtil.gc();
		}
	}

	public class WorkerThread extends Thread {

		private final HttpService httpservice;
		private final HttpServerConnection conn;

		public WorkerThread(final HttpService httpservice,
				final HttpServerConnection conn) {
			super();
			this.httpservice = httpservice;
			this.conn = conn;
		}

		public void run() {
			Log.v("FE", "New connection thread");
			HttpContext context = new BasicHttpContext(null);
			try {
				while (!Thread.interrupted() && this.conn.isOpen()) {
					this.httpservice.handleRequest(this.conn, context);
				}
			} catch (ConnectionClosedException ex) {
				Log.v("FE", "Client closed connection");
			} catch (IOException ex) {
				Log.v("FE", "I/O error: " + ex.getMessage());
			} catch (HttpException ex) {
				Log.v("FE",
						"Unrecoverable HTTP protocol violation: "
								+ ex.getMessage());
			} finally {
				try {
					this.conn.shutdown();
				} catch (IOException ignore) {
					Log.v("FE", "Exception: " + ignore.toString());
				}
			}
		}
	}
}
