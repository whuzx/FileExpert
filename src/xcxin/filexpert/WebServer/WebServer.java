package xcxin.filexpert.WebServer;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.util.Locale;

import jcifs.util.Base64;

import org.apache.http.ConnectionClosedException;
import org.apache.http.Header;
import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpServerConnection;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.entity.AbstractHttpEntity;
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

import xcxin.filexpert.FeUtil;
import xcxin.filexpert.FileExpertSettings;
import xcxin.filexpert.servlets.servlet401;
import xcxin.filexpert.servlets.servlet403;
import xcxin.filexpert.servlets.servlet404;
import xcxin.filexpert.servlets.servlet500;
import xcxin.filexpert.servlets.servletFileTransfer;
import xcxin.filexpert.servlets.welcomeServlet;
import android.app.Service;
import android.util.Log;

public class WebServer {
	private RequestListenerThread mHttpServerThread;
	private int mPort;
	private boolean mStarted = false;
	private Service mService;
	private FileExpertSettings mSettings;
	private String authString = null;

	public WebServer(String rootDir, int port, Service service,
			FileExpertSettings settings) throws IOException {
		mService = service;
		mSettings = settings;
		retriveAuthString();
		startServer(rootDir, port);
	}

	public void retriveAuthString() {
		authString = mSettings.getWebUsername() + ":"
				+ mSettings.getWebPassword();
	}

	public String getAuthString() {
		return authString;
	}

	public void startServer(String rootDir, int port) throws IOException {
		mPort = port;
		mHttpServerThread = new RequestListenerThread(port, rootDir, mSettings);
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
		private final Service mService;
		private final FileExpertSettings mSettings;

		public HttpFileHandler(final String docRoot, final Service service,
				final FileExpertSettings settings) {
			super();
			this.docRoot = docRoot;
			this.mSettings = settings;
			this.mService = service;
		}

		public void handle(final HttpRequest request,
				final HttpResponse response, final HttpContext context)
				throws HttpException, IOException {

			String method = request.getRequestLine().getMethod()
					.toUpperCase(Locale.ENGLISH);
			if (!method.equals("GET") && !method.equals("HEAD")
					&& !method.equals("POST")) {
				throw new MethodNotSupportedException(method
						+ " method not supported");
			}

			String target = URLDecoder.decode(
					request.getRequestLine().getUri(), "UTF-8");

			FeServletBase servlet = null;
			File file = null;

			if (mSettings.isWebLoginEnable()) {
				try {
					Header[] headers = request.getHeaders("Authorization");
					if (headers == null || headers.length == 0) {
						servlet = new servlet401();
						servletProcess(servlet, request, response, file,
								mService, docRoot, target, "Server Running OK");
						return;
					} else {
						for (Header header : headers) {
							String value = header.getValue();
							value = value.substring(value.indexOf("Basic") + 6);
							byte[] password_name = Base64.decode(value);
							String password_name_str = new String(password_name);
							if (!password_name_str.equals(getAuthString())) {
								// maybe user has changed its password or
								// username?
								retriveAuthString();
								if (!password_name_str.equals(getAuthString())) {
									servlet = new servlet401();
									servletProcess(servlet, request, response,
											file, mService, docRoot, target,
											"Server Running OK");
									return;
								} else {
									break;
								}
							} else {
								break;
							}
						}
					}
				} catch (Exception e) {
					servlet = new servlet500();
					try {
						servletProcess(
								servlet,
								request,
								response,
								file,
								mService,
								docRoot,
								target,
								e.toString()
										+ "<br>"
										+ FeUtil.generateStackTrace(
												e.getStackTrace(), "<br>"));
					} catch (Exception e1) {
						// Fetal Error....
						Log.v("FE",
								"Fetal error...\n"
										+ FeUtil.generateStackTrace(
												e1.getStackTrace(), "\n"));
					}
					return;
				}
			}

			// Find which servlets we should use to process this request!
			//
			if (target.indexOf("webapps") == 1) {
				// Yes, let's try to run servlets
				String servlets = target;
				if (servlets.indexOf("?") > 0) {
					servlets = servlets.substring(0, servlets.indexOf("?"));
				}
				servlets = servlets.substring(9).replace('/', '.');
				if (servlets.charAt(servlets.length() - 1) == '.') {
					servlets = servlets.substring(0, servlets.length() - 2);
				}
				servlets = "xcxin.filexpert.servlets." + servlets.toLowerCase();
				Class<?> servobj = null;
				try {
					servobj = getClass().getClassLoader().loadClass(servlets);
					servlet = (FeServletBase) servobj.newInstance();
				} catch (ClassNotFoundException e) {
					// send 404 or load default servlet???
					servlet = new servlet404();
				} catch (IllegalAccessException e) {
					servlet = new servlet500();
					servlet.setServerMessage(e.toString()
							+ "<br>"
							+ FeUtil.generateStackTrace(e.getStackTrace(),
									"<br>"));
				} catch (InstantiationException e) {
					servlet = new servlet500();
					servlet.setServerMessage(e.toString()
							+ "<br>"
							+ FeUtil.generateStackTrace(e.getStackTrace(),
									"<br>"));
				}
			} else {
				// Yes, normal file HTTP transfer
				file = new File(this.docRoot, target);
				if (file.exists() == false) {
					servlet = new servlet404();
				} else if (file.canRead() == false) {
					servlet = new servlet403();
				} else if (file.isDirectory()) {
					// Directory - Do we need to run listing servlet?
					servlet = new welcomeServlet();
				} else {
					// Serving file
					servlet = new servletFileTransfer();
				}
			}

			try {
				servlet.setHttpRequestAndResponse(request, response);
				servletProcess(servlet, request, response, file, mService,
						docRoot, target, "Server Running OK");
			} catch (Exception e) {
				servlet = new servlet500();
				try {
					servletProcess(
							servlet,
							request,
							response,
							file,
							mService,
							docRoot,
							target,
							e.toString()
									+ "<br>"
									+ FeUtil.generateStackTrace(
											e.getStackTrace(), "<br>"));
				} catch (Exception e1) {
					// Fetal Error....
					Log.v("FE",
							"Fetal error...\n"
									+ FeUtil.generateStackTrace(
											e1.getStackTrace(), "\n"));
				}
			}
		}

		private void servletProcess(FeServletBase servlet, HttpRequest request,
				HttpResponse response, File file, Service service, String root,
				String target, String message) throws Exception {
			if (servlet == null)
				return;
			servlet.setHttpRequestAndResponse(request, response);
			servlet.setRootAndTarget(root, target);
			servlet.setFile(file);
			servlet.setService(service);
			servlet.setServerMessage(message);
			servlet.execute();
			AbstractHttpEntity entity = servlet.getEntity();
			if (servlet.isContentTypeSet() == false) {
				entity.setContentType("text/html; charset=UTF-8");
			} else {
				entity.setContentType(servlet.getContentType()
						+ "; charset=UTF-8");
			}
			response.setStatusCode(servlet.getHttpStatusCode());
			response.setEntity(entity);
		}
	}

	public class RequestListenerThread extends Thread {

		private final ServerSocket serversocket;
		private final HttpParams params;
		private final HttpService httpService;
		private final FileExpertSettings mSettings;

		public RequestListenerThread(int port, final String docroot,
				final FileExpertSettings settings) throws IOException {
			this.mSettings = settings;
			this.serversocket = new ServerSocket(port);
			this.params = new BasicHttpParams();
			this.params
					.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
					.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
							8 * 1024)
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
			reqistry.register("*", new HttpFileHandler(docroot, mService,
					mSettings));

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
