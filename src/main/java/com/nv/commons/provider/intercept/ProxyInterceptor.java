package com.nv.commons.provider.intercept;

import java.io.IOException;
import java.util.zip.GZIPInputStream;

import com.nv.commons.utils.DateUtils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.Buffer;
import okio.BufferedSource;
import okio.Okio;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.util.UuidUtil;
import org.jetbrains.annotations.NotNull;

public class ProxyInterceptor implements Interceptor {

	protected final Logger logger;
	protected final String action;
	protected String[] headers;

	public ProxyInterceptor(Logger logger, String action) {
		this.logger = logger;
		this.action = action;
	}

	public ProxyInterceptor(Logger logger, String action, String... headers) {
		this.logger = logger;
		this.action = action;
		this.headers = headers;
	}

	@NotNull
	@Override
	public Response intercept(
		@NotNull
		Chain chain) throws IOException {

		String uuid = UuidUtil.getTimeBasedUuid().toString(); //ELKUUidManager.getInstance().get();

		Request request = chain.request();

		long t1 = System.currentTimeMillis();

		logRequest(uuid, request);
		Response response = processGzip(chain.proceed(request));

		logResponse(uuid, response, t1);
		return response;
	}

	private void logRequest(String uuid, Request request) throws IOException {
		String requestBodyString = "";
		RequestBody requestBody = request.body();
		if (requestBody != null) {
			Buffer buffer = new Buffer();
			requestBody.writeTo(buffer);
			requestBodyString = buffer.readUtf8();
		}

		if (headers == null || headers.length == 0) {
			logger.info("[uid]:{}, [{}] - [url]:{}, [req]:{}", uuid, action, request.url(), requestBodyString);
		} else {
			StringBuilder headerLog = new StringBuilder();
			for (String header : headers) {
				headerLog.append(header).append("=").append(request.header(header)).append(";");
			}
			logger.info("[uid]:{}, [{}] - [url]:{}, [header]:{}, [req]:{}", uuid, action, request.url(),
				headerLog.toString(), requestBodyString);
		}

//		ESProviderRecordService.getInstance().updateRequest(ELKLogFormatUtils.convertOKHttpRequestToJson(request));

	}

	protected void logResponse(String uuid, Response response, long startTime) throws IOException {
		String responseBodyString = response.code() + "-";
		ResponseBody responseBody = response.body();

		if (responseBody != null) {
			BufferedSource source = responseBody.source();
			source.request(Long.MAX_VALUE);
			Buffer buffer = source.getBuffer();
			responseBodyString += buffer.clone().readUtf8();
		}

		logger.info("[uid]:{}, [time]:{}, [res]:{}", uuid, DateUtils.secondsElapsedSince(startTime),
			responseBodyString);

		// log 紀錄
//		ESProviderRecordService.getInstance().updateResponse(responseBodyString);
//		ESProviderRecordService.getInstance().commit();

	}

	private Response processGzip(Response response) throws IOException {
		if (response.header("Content-Encoding") != null && "gzip".equalsIgnoreCase(response.header("Content-Encoding"))) {
			ResponseBody originalBody = response.body();
			if (originalBody == null) {
				return response;
			}
			GZIPInputStream gzipInputStream = new GZIPInputStream(originalBody.byteStream());
			BufferedSource source = Okio.buffer(Okio.source(gzipInputStream));
			return response.newBuilder()
				.body(ResponseBody.create(source, originalBody.contentType(), originalBody.contentLength())).build();
		}
		return response;
	}
}
