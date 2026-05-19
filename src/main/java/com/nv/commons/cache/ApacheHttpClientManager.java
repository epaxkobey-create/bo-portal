package com.nv.commons.cache;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;

import com.nv.commons.provider.proxy.ProviderProxy;
import com.nv.commons.utils.ResponseUtils;
import com.nv.commons.utils.ServerInfoUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.Consts;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpVersion;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.config.MessageConstraints;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.apache.http.util.EntityUtils;

/**
 * ApacheHttpClientManager
 *
 * @author Stan
 */
/**
 *
 */
public class ApacheHttpClientManager {

	// 不直接變動爲LogUtils下的log，保留ApacheHttpClientManager被抽取到library的彈性
	private final static Log log = LogFactory.getLog(ApacheHttpClientManager.class);

	// 從connection pool獲取connection的等待超時時間，單位毫秒
	public static int CONNECTION_REQUEST_TIMEOUT = 5 * 1000;

	// 請求建立連接的超時時間，單位毫秒
	public static int CONNECTION_TIMEOUT = 5 * 1000;

	/**
	 * 建立連接後，獲取數據的閒置超時時間，單位毫秒。
	 * 兩個數據封包之間的時間大於該時間則認為超時，假設設置1秒超時，如果每隔0.8秒傳輸一次數據，傳輸10次，總共8秒，這樣是不超時的
	 */
	public static int SOCKET_HOLD_TIMEOUT = 30 * 1000;

	private final static String DEFAULT_CHARSET = StandardCharsets.UTF_8.name();

	// 對同一站點最大可以發起的連線數
	public static int MAX_PER_ROUTE = 200;

	// 可以同時發起的連線數量
	public static int MAX_TOTAL = 500;

	private final static int VALIDATE_AFTER_INACTIVITY = 3000;

	private ScheduledExecutorService cleaner = null;

	private PoolingHttpClientConnectionManager cm = null;

	private CloseableHttpClient httpClient = null;

	public boolean isTrace = false;

	public boolean isTraceTimeout = true;

	private ConnectionKeepAliveStrategy keepAliveStrategy = null;

	protected RequestConfig requestConfig = null;

	private final static ApacheHttpClientManager theInstance = new ApacheHttpClientManager();

	public static ApacheHttpClientManager getInstance() {
		return theInstance;
	}

	private ApacheHttpClientManager() {

		try {

			initConnectionManager();

			initKeepAliveStrategy();

			connectionCleaner();

			initHttpClient();

		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
	}

	/**
	 * connectionCleaner
	 */
	private void connectionCleaner() {
		cleaner = Executors
			.newSingleThreadScheduledExecutor(ThreadFactoryUtils.getDaemonThreadFactory("ApacheHttpClientManager"));
		Runnable clean = () -> {
			cm.closeExpiredConnections();
			cm.closeIdleConnections(6, TimeUnit.SECONDS);
		};
		cleaner.scheduleAtFixedRate(clean, 6, 3, TimeUnit.SECONDS);
	}

	private void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	private String getResponse(HttpEntity entity) throws Exception {
		if (entity == null) {
			return null;
		}
		InputStream in = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		StringBuilder contentSb = new StringBuilder();
		try {
			in = new BufferedInputStream(entity.getContent());
			isr = new InputStreamReader(in, StandardCharsets.UTF_8);
			br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				contentSb.append(line.trim());
			}
			EntityUtils.consumeQuietly(entity);
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			throw e;
		} finally {
			close(br);
			close(isr);
			close(in);
		}
		return contentSb.toString();
	}

	public void init(String propertiesFile) {
		try (InputStream in = ProviderProxy.class.getResourceAsStream(propertiesFile)) {
			Properties props = new Properties();
			props.load(in);
			CONNECTION_REQUEST_TIMEOUT = Integer.parseInt(props.getProperty("connection.request.timeout"));
			CONNECTION_TIMEOUT = Integer.parseInt(props.getProperty("connection.timeout"));
			SOCKET_HOLD_TIMEOUT = Integer.parseInt(props.getProperty("socket.timeout"));
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}

	private String getQueryString(Map<String, String> parameters, String charset) throws UnsupportedEncodingException {

		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> entry : parameters.entrySet()) {
			sb.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue(), charset)).append("&");
		}
		if (!sb.isEmpty()) {
			sb.deleteCharAt(sb.length() - 1);
		}

		return sb.toString();
	}

	/**
	 * 執行get的request，因為request的相關設定比較多，所以不適合採用參數傳遞的方式，組合太多
	 *
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public HTTPResponse execute(HttpGetRequest request) throws Exception {
		String url = request.getUrl();
		if (request.parameters != null && !request.parameters.isEmpty()) {
			url = url + "?" + getQueryString(request.parameters, request.charset);
		}

		HttpGet httpGet = new HttpGet(url);

		String charset = request.charset;
		if (charset == null) {
			charset = DEFAULT_CHARSET;
		}
		httpGet.setHeader("Accept-Charset", charset);

		httpGet.setProtocolVersion(HttpVersion.HTTP_1_1);

		/* 設定連線timeout時間 */
		RequestConfig.Builder requestConfigbuilder = RequestConfig
			.copy(ApacheHttpClientManager.getInstance().requestConfig);
		requestConfigbuilder.setSocketTimeout(request.socketTimeout);
		requestConfigbuilder.setConnectTimeout(request.connectTimeout);
		requestConfigbuilder.setConnectionRequestTimeout(request.connectionRequestTimeout);

		/* 設定 proxy */
		HttpClientContext proxyHttpContext = null;
		if (request.proxyAddress != null && request.proxyPort > 0) {
			//PROXY_IMPL2
			//HttpHost httpHost = new HttpHost(request.proxyAddress, request.proxyPort);
			//requestConfigbuilder.setProxy(httpHost);

			proxyHttpContext = HttpClientContext.create();
			InetSocketAddress socksaddr = new InetSocketAddress(request.proxyAddress, request.proxyPort);
			proxyHttpContext.setAttribute("socks.address", socksaddr);
		}

		httpGet.setConfig(requestConfigbuilder.build());


		/* 設定 header */
		Map<String, String> headers = request.headers;
		if (headers != null && !headers.isEmpty()) {
			List<Header> headerList = new ArrayList<>();
			for (Entry<String, String> entry : headers.entrySet()) {
				headerList.add(new BasicHeader(entry.getKey(), entry.getValue()));
			}
			httpGet.setHeaders(headerList.toArray(new Header[0]));
		}

		CloseableHttpResponse response = null;
		HTTPResponse httpResponse = new HTTPResponse();
		try {
			if (proxyHttpContext != null) {
				response = httpClient.execute(httpGet, proxyHttpContext);
				if (request.receiveCookie()) {
					httpResponse.setCookieStore(proxyHttpContext.getCookieStore());
				}
			} else {
				if (request.receiveCookie()) {
					HttpClientContext httpClientContext = HttpClientContext.create();
					response = httpClient.execute(httpGet, httpClientContext);
					httpResponse.setCookieStore(httpClientContext.getCookieStore());
				} else {
					response = httpClient.execute(httpGet);
				}
			}
			httpResponse.setHeader(response.getAllHeaders());

			httpResponse.setContent(getResponse(response.getEntity()));

			int statusCode = response.getStatusLine().getStatusCode();
			httpResponse.setStatusCode(statusCode);
			if (statusCode != 200 && statusCode != 404) {
				log.info(
					String.format("HTTP Status-Code (%d) Length w/o headers: [%d] %s", statusCode, url.length(), url));
			}
		} catch (HttpHostConnectException e) {
			//			log.error(e.getMessage(), e);
			httpGet.abort();
			HttpHost host = e.getHost();
			log.info(String.format("Total PoolStats: %s & %s PoolStats: %s.", cm.getTotalStats(), host.toHostString(),
				cm.getStats(new HttpRoute(host))));
			throw e;
		} catch (Exception e) {
			//			log.error(e.getMessage(), e);
			httpGet.abort();
			throw e;
		} finally {
			close(response);
			httpGet.releaseConnection();
		}

		if (isTrace) {
			log.info("HttpGet content:" + httpResponse.getContent());
		}
		return httpResponse;
	}

	public HttpPostRequest getHttpPostRequest(String url) {
		return new HttpPostRequest(url);
	}

	/**
	 * TODO: singleton 全系統共用的 httpClient 傳出去不應該是 CloseableHttpClient，不然會被誤關.
	 * 但是現在用的地方太多了，再找時間改
	 */
	public CloseableHttpClient getHttpClient() {
		return this.httpClient;
	}

	public CloseableHttpClient getHttpClient(LayeredConnectionSocketFactory ssLSocketFactory) {
		return HttpClients.custom().setKeepAliveStrategy(keepAliveStrategy).setDefaultRequestConfig(requestConfig)
			.setUserAgent(
				"mozilla/4.0 (compatible; msie 8.0; windows nt 6.1; wow64; trident/4.0; slcc2; .net clr 2.0.50727; .net clr 3.5.30729; .net clr 3.0.30729; media center pc 6.0; masn)")
			.disableAutomaticRetries().setSSLSocketFactory(ssLSocketFactory).build();
	}

	/**
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public HTTPResponse execute(HttpPostRequest request) throws Exception {
		String url = request.getUrl();
		HttpPost httpPost = new HttpPost(url);

		httpPost.setHeader("Accept-Charset", "UTF-8");

		httpPost.setProtocolVersion(HttpVersion.HTTP_1_1);

		/* 設定連線timeout時間 */
		RequestConfig.Builder requestConfigbuilder = RequestConfig
			.copy(ApacheHttpClientManager.getInstance().requestConfig);
		requestConfigbuilder.setSocketTimeout(request.socketTimeout);
		requestConfigbuilder.setConnectTimeout(request.connectTimeout);
		requestConfigbuilder.setConnectionRequestTimeout(request.connectionRequestTimeout);


		/* 設定 proxy */
		HttpClientContext proxyHttpContext = null;
		if (request.proxyAddress != null && request.proxyPort > 0) {
			//PROXY_IMPL2
			//HttpHost httpHost = new HttpHost(request.proxyAddress, request.proxyPort);
			//requestConfigbuilder.setProxy(httpHost);

			proxyHttpContext = HttpClientContext.create();
			InetSocketAddress socksaddr = new InetSocketAddress(request.proxyAddress, request.proxyPort);
			proxyHttpContext.setAttribute("socks.address", socksaddr);
		}

		httpPost.setConfig(requestConfigbuilder.build());

		/* 設定 header */
		Map<String, String> headers = request.headers;
		if (headers != null && !headers.isEmpty()) {
			List<Header> headerList = new ArrayList<>();
			for (Entry<String, String> entry : headers.entrySet()) {
				headerList.add(new BasicHeader(entry.getKey(), entry.getValue()));
			}
			httpPost.setHeaders(headerList.toArray(new Header[0]));
		}

		/* 設定 參數 */
		HttpEntity httpEntity = request.getHttpEntity();
		if (httpEntity != null) {
			httpPost.setEntity(httpEntity);
		}

		CloseableHttpResponse response = null;
		HTTPResponse httpResponse = new HTTPResponse();
		try {

			if (proxyHttpContext != null) {
				response = httpClient.execute(httpPost, proxyHttpContext);
				if (request.receiveCookie()) {
					httpResponse.setCookieStore(proxyHttpContext.getCookieStore());
				}
			} else {
				if (request.receiveCookie()) {
					HttpClientContext httpClientContext = HttpClientContext.create();
					response = httpClient.execute(httpPost, httpClientContext);
					httpResponse.setCookieStore(httpClientContext.getCookieStore());
				} else {
					response = httpClient.execute(httpPost);
				}
			}
			httpResponse.setHeader(response.getAllHeaders());

			httpResponse.setContent(getResponse(response.getEntity()));

			int statusCode = response.getStatusLine().getStatusCode();
			httpResponse.setStatusCode(statusCode);
			if (statusCode != 200 && statusCode != 404) {
				log.info(
					String.format("HTTP Status-Code (%d) Length w/o headers: [%d] %s", statusCode, url.length(), url));
			}

		} catch (IOException e) {
			//			log.error(e.getMessage(), e);
			httpPost.abort();
			throw e;
		} finally {
			close(response);
			httpPost.releaseConnection();
		}
		return httpResponse;
	}

	protected void close() {
		try {
			cleaner.shutdown();
			cm.close();
		} catch (Exception ignore) {

		}
	}

	/**
	 * initConnectionManager
	 *
	 * @throws NoSuchAlgorithmException
	 * @throws KeyStoreException
	 * @throws KeyManagementException
	 */
	private void initConnectionManager() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {

		log.info("ApacheHttpClient init  ...");

		SSLContextBuilder builder = SSLContexts.custom();
		builder.loadTrustMaterial(null, (TrustStrategy) (arg0, arg1) -> true);
		SSLContext sslContext = builder.build();
		HostnameVerifier verifier = (s, sslSession) -> true;
		Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder
			.<ConnectionSocketFactory>create()
			.register("https", new MySSLConnectionSocketFactory(sslContext, verifier))
			.register("http", new MyConnectionSocketFactory()).build();

		DnsResolver dnsResolver = new SystemDefaultDnsResolver() {

			private AtomicInteger count = new AtomicInteger();

			public String printStackTrace(StackTraceElement[] stackTrace) {
				StringBuilder sb = new StringBuilder();
				for (StackTraceElement stackStr : stackTrace) {
					sb.append(stackStr).append(System.lineSeparator());
				}
				return sb.toString();
			}

			@Override
			public InetAddress[] resolve(final String host) throws UnknownHostException {

				// Detect 50 records
				if (host != null && host.contains("192.168.") && count.get() < 50) {
					int currnetCount = count.incrementAndGet();
					log.error("DNS ERROR Count : " + currnetCount + " | HOST : " + host + "\n" + printStackTrace(
						new Throwable().getStackTrace()));
				}
				return super.resolve(host);
			}
		};
		cm = new PoolingHttpClientConnectionManager(socketFactoryRegistry, dnsResolver);
		// Increase max total connection to 500
		cm.setMaxTotal(MAX_TOTAL);
		// Increase default max connection per route to 50
		cm.setDefaultMaxPerRoute(MAX_PER_ROUTE);

		cm.setValidateAfterInactivity(VALIDATE_AFTER_INACTIVITY);

		SocketConfig defaultSocketConfig = SocketConfig.custom().setTcpNoDelay(true).setSoKeepAlive(true)
			.setSoReuseAddress(true).build();
		cm.setDefaultSocketConfig(defaultSocketConfig);

		MessageConstraints messageConstraints = MessageConstraints.custom().setMaxHeaderCount(200).build();

		ConnectionConfig defaultConnectionConfig = ConnectionConfig.custom()
			.setMessageConstraints(messageConstraints)
			.setMalformedInputAction(CodingErrorAction.IGNORE)
			.setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).build();

		cm.setDefaultConnectionConfig(defaultConnectionConfig);

	}

	/**
	 * initHttpClient
	 */
	private void initHttpClient() {

		if (ServerInfoUtils.isPlayerServer() || ServerInfoUtils.isBackendApiServer()) {
			SOCKET_HOLD_TIMEOUT = 15 * 1000;
		}

		requestConfig = RequestConfig.custom()
			.setSocketTimeout(SOCKET_HOLD_TIMEOUT)
			.setConnectTimeout(CONNECTION_TIMEOUT)
			.setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
			.setCookieSpec(CookieSpecs.STANDARD)
			.build();

		httpClient = HttpClients.custom()
			.setConnectionManager(cm)
			//			.setConnectionManagerShared(true)
			.setKeepAliveStrategy(keepAliveStrategy)
			.setDefaultRequestConfig(requestConfig)
			.setUserAgent(
				"mozilla/4.0 (compatible; msie 8.0; windows nt 6.1; wow64; trident/4.0; slcc2; .net clr 2.0.50727; .net clr 3.5.30729; .net clr 3.0.30729; media center pc 6.0; masn)")
			.disableAutomaticRetries()
			.build();
	}

	/**
	 * initKeepAliveStrategy
	 */
	private void initKeepAliveStrategy() {
		keepAliveStrategy = (response, context) -> {
			// Honor 'keep-alive' header

			Header[] headers = response.getHeaders(HTTP.CONN_KEEP_ALIVE);
			for (Header header : headers) {
				String param = header.getName();
				String value = header.getValue();
				if (value != null && param.equalsIgnoreCase("timeout")) {
					try {
						if (isTraceTimeout && Long.parseLong(value) * 1000 > 6) {
							log.info(Arrays.toString(headers));
						}
						return Long.parseLong(value) * 1000;
					} catch (NumberFormatException ignore) {
					}
				}
			}

			// Keep alive for 6 seconds only
			return 6 * 1000;
		};
	}

	static class MyConnectionSocketFactory implements ConnectionSocketFactory {

		@Override
		public Socket createSocket(final HttpContext context) {
			InetSocketAddress socketAddress = (InetSocketAddress) context.getAttribute("socks.address");

			if (socketAddress == null) {
				return new Socket();
			}
			return new Socket(new Proxy(Proxy.Type.SOCKS, socketAddress));
		}

		@Override
		public Socket connectSocket(final int connectTimeout, final Socket socket, final HttpHost host,
			final InetSocketAddress remoteAddress, final InetSocketAddress localAddress,
			final HttpContext context) throws IOException, ConnectTimeoutException {

			Socket sock;
			if (socket != null) {
				sock = socket;
			} else {
				sock = createSocket(context);
			}
			if (localAddress != null) {
				sock.bind(localAddress);
			}
			try {
				sock.connect(remoteAddress, connectTimeout);
			} catch (SocketTimeoutException ex) {
				throw new ConnectTimeoutException(ex, host, remoteAddress.getAddress());
			}
			return sock;
		}

	}

	static class MySSLConnectionSocketFactory extends SSLConnectionSocketFactory {

		public MySSLConnectionSocketFactory(SSLContext sslContext, HostnameVerifier hostnameVerifier) {
			super(sslContext, hostnameVerifier);
		}

		@Override
		public Socket createSocket(final HttpContext context) {
			InetSocketAddress socketAddress = (InetSocketAddress) context.getAttribute("socks.address");

			if (socketAddress == null) {
				return new Socket();
			}
			return new Socket(new Proxy(Proxy.Type.SOCKS, socketAddress));
		}
	}

	/**
	 * 1. 對外不暴露HttpClient，避免耦合
	 * 2. 因為參數組合過多，所以採用builder的設計模式
	 */
	public static class HttpGetRequest {

		private String url;

		private Map<String, String> headers = new HashMap<>();

		private Map<String, String> parameters = null;

		private int socketTimeout = SOCKET_HOLD_TIMEOUT;

		private int connectTimeout = CONNECTION_TIMEOUT;

		private int connectionRequestTimeout = CONNECTION_REQUEST_TIMEOUT;

		private String proxyAddress = null;

		private int proxyPort = -1;

		private String charset = "UTF-8";

		private boolean cookie = false;

		public HttpGetRequest(String url) {
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}

		/**
		 * 指定 Timeout 時間  單位為毫秒
		 *
		 * @param connectTimeout           設置連接超時時間，單位毫秒
		 * @param socketTimeout            請求獲取數據的超時時間，單位毫秒。 如果訪問一個接口，多少時間內無法返回數據，就直接放棄此次調用。
		 * @param connectionRequestTimeout 設置從connect Manager獲取Connection 超時時間，單位毫秒。這個屬性是新加的屬性，因為目前版本是可以共享連接池的。
		 * @return
		 */
		public void setTimeout(int socketTimeout, int connectTimeout, int connectionRequestTimeout) {
			this.socketTimeout = socketTimeout;
			this.connectTimeout = connectTimeout;
			this.connectionRequestTimeout = connectionRequestTimeout;
		}

		public void setTimeout(int socketTimeout) {
			this.socketTimeout = socketTimeout;
		}

		public void setParameters(Map<String, String> parameters) throws Exception {
			setParameters(parameters, DEFAULT_CHARSET);
		}

		public void setParameters(Map<String, String> parameters, String charset) {
			getParameters().putAll(parameters);
			this.charset = charset;
		}

		public Map<String, String> getParameters() {
			if (this.parameters == null) {
				this.parameters = new HashMap<>();
			}
			return this.parameters;
		}

		public void setHeaders(Map<String, String> headers) {
			getHeaders().putAll(headers);
		}

		public Map<String, String> getHeaders() {
			if (this.headers == null) {
				this.headers = new HashMap<>();
			}
			return this.headers;
		}

		public boolean receiveCookie() {
			return this.cookie;
		}

		public HTTPResponse execute() throws Exception {
			return ApacheHttpClientManager.getInstance().execute(this);
		}
	}

	/**
	 * 1. 對外不暴露HttpClient，避免耦合
	 * 2. 因為參數組合過多，所以採用builder的設計模式
	 */
	public static class HttpPostRequest {

		private String url;

		private Map<String, String> headers = new HashMap<>();

		private Map<String, String> parameters = null;

		private int socketTimeout = SOCKET_HOLD_TIMEOUT;

		private int connectTimeout = CONNECTION_TIMEOUT;

		private int connectionRequestTimeout = CONNECTION_REQUEST_TIMEOUT;

		private String proxyAddress = null;

		private int proxyPort = -1;

		private String charset = "UTF-8";

		private boolean cookie = false;

		public HttpPostRequest(String url) {
			this.url = url;
		}

		public String getUrl() {
			return this.url;
		}

		/**
		 * 指定 Timeout 時間  單位為毫秒
		 *
		 * @param connectTimeout           設置連接超時時間，單位毫秒
		 * @param socketTimeout            請求獲取數據的超時時間，單位毫秒。 如果訪問一個接口，多少時間內無法返回數據，就直接放棄此次調用。
		 * @param connectionRequestTimeout 設置從connect Manager獲取Connection 超時時間，單位毫秒。這個屬性是新加的屬性，因為目前版本是可以共享連接池的。
		 * @return
		 */
		public void setTimeout(int socketTimeout, int connectTimeout, int connectionRequestTimeout) {
			this.socketTimeout = socketTimeout;
			this.connectTimeout = connectTimeout;
			this.connectionRequestTimeout = connectionRequestTimeout;
		}

		public void setTimeout(int socketTimeout) {
			this.socketTimeout = socketTimeout;
		}

		public void setConnectTimeout(int connectTimeout) {
			this.connectTimeout = connectTimeout;
		}

		public void setProxy(String proxyAddress, int proxyPort) {
			this.proxyAddress = proxyAddress;
			this.proxyPort = proxyPort;
		}

		public void addParameter(String key, String value) {
			getParameters().put(key, value);
		}

		public void setParameters(Map<String, String> parameters) throws Exception {
			setParameters(parameters, DEFAULT_CHARSET);
		}

		public void setParameters(Map<String, String> parameters, String charset) {
			getParameters().putAll(parameters);
			this.charset = charset;
		}

		public Map<String, String> getParameters() {
			if (this.parameters == null) {
				this.parameters = new HashMap<>();
			}
			return this.parameters;
		}

		public void addHeader(String key, String value) {
			getHeaders().put(key, value);
		}

		public void setHeaders(Map<String, String> headers) {
			getHeaders().putAll(headers);
		}

		public Map<String, String> getHeaders() {
			if (this.headers == null) {
				this.headers = new HashMap<>();
			}
			return this.headers;
		}

		public String getCharset() {
			return charset;
		}

		protected HttpEntity getHttpEntity() throws Exception {
			HttpEntity httpEntity = null;
			if (this.parameters != null && !this.parameters.isEmpty()) {
				List<NameValuePair> nameValuePairs = new ArrayList<>();
				for (Entry<String, String> entry : parameters.entrySet()) {
					nameValuePairs.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
				}
				httpEntity = new UrlEncodedFormEntity(nameValuePairs, this.charset);
			}
			return httpEntity;
		}

		public boolean receiveCookie() {
			return this.cookie;
		}

		public HTTPResponse execute() throws Exception {
			return ApacheHttpClientManager.getInstance().execute(this);
		}
	}

	public static class HttpJsonPostRequest extends HttpPostRequest {

		private String content;

		public HttpJsonPostRequest(String url) {
			super(url);
			super.getHeaders().put(HttpHeaders.CONTENT_TYPE, ResponseUtils.JSON_CONTENT_TYPE);
			//			httpPost.setEntity(new StringEntity(paramJson));
		}

		public void setContent(String content) {
			this.content = content;
		}

		@Override
		protected HttpEntity getHttpEntity() throws UnsupportedEncodingException {
			return new StringEntity(this.content);
		}
	}

	static public class HTTPResponse {

		private int statusCode = -1;

		private String content;

		private Header[] header;

		private CookieStore cookieStore;

		public void setHeader(Header[] header) {
			this.header = header;
		}

		public void setStatusCode(int statusCode) {
			this.statusCode = statusCode;
		}

		public int getStatusCode() {
			return statusCode;
		}

		public void setContent(String content) {
			this.content = content;
		}

		public String getContent() {
			return content;
		}

		public String getHeader(String name) {
			if (header == null) {
				return null;
			}

			for (Header header : header) {
				HeaderElement[] headerElement = header.getElements();
				for (HeaderElement element : headerElement) {
					if (element.getName().equals(name)) {
						return element.getValue();
					}
				}
			}
			return "";
		}

		public void setCookieStore(CookieStore cookieStore) {
			this.cookieStore = cookieStore;
		}

		public List<Cookie> getCookies() {
			return cookieStore.getCookies();
		}
	}

	public int getPoolSize() {
		return cm.getTotalStats().getAvailable();
	}

}

class ThreadFactoryUtils {

	private static class DaemonThreadFactory implements ThreadFactory {

		private String threadName = null;

		DaemonThreadFactory() {
		}

		DaemonThreadFactory(String threadName) {
			this.threadName = threadName;
		}

		public Thread newThread(Runnable r) {
			Thread thread = new Thread(r);
			thread.setDaemon(true);
			if (threadName != null && !threadName.isEmpty()) {
				thread.setName(threadName);
			}
			return thread;
		}
	}

	public static ThreadFactory getDaemonThreadFactory(String threadName) {
		return new DaemonThreadFactory(threadName);
	}

}

