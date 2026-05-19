package com.nv.commons.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.nio.charset.StandardCharsets;

// TODO 這邊可以統一換成ApacheHttpManager，那邊有connection pool
public class HttpRequester {

	/**
	 * 發送Get請求
	 *
	 * @param urlString
	 * @return
	 * @throws Exception
	 */
	public static String sendGet(String urlString) throws Exception {
		return send(urlString, "GET", null, -1);
	}

	private static String send(String urlString, String method, String data, int timeout) throws Exception {
		HttpURLConnection urlConnection = null;
		BufferedReader bufferedReader = null;
		try {
			urlConnection = (HttpURLConnection) URI.create(urlString).toURL().openConnection();
			urlConnection.setRequestMethod(method);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);
			if (timeout > 0) {
				urlConnection.setConnectTimeout(timeout);
				urlConnection.setReadTimeout(timeout);
			}
			if ("POST".equals(method)) {
				urlConnection.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
				urlConnection.getOutputStream().flush();
				urlConnection.getOutputStream().close();
			}

			bufferedReader = new BufferedReader(
				new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));

			StringBuilder temp = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				//temp.append(line).append("\r\n");
				//這邊不放入換行, 因為目前都是單行
				temp.append(line);
			}
			return temp.toString();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
	}

	public static String sendWithCookie(String urlString, String cookie) throws Exception {
		HttpURLConnection urlConnection = null;
		BufferedReader bufferedReader = null;
		try {
			urlConnection = (HttpURLConnection) URI.create(urlString).toURL().openConnection();
			urlConnection.setRequestMethod("GET");
			urlConnection.setRequestProperty("Cookie", cookie);
			urlConnection.setDoOutput(true);
			urlConnection.setDoInput(true);
			urlConnection.setUseCaches(false);

			bufferedReader = new BufferedReader(
				new InputStreamReader(urlConnection.getInputStream(), StandardCharsets.UTF_8));

			StringBuilder temp = new StringBuilder();
			String line = null;
			while ((line = bufferedReader.readLine()) != null) {
				//temp.append(line).append("\r\n");
				//這邊不放入換行, 因為目前都是單行
				temp.append(line);
			}
			return temp.toString();
		} finally {
			if (urlConnection != null) {
				urlConnection.disconnect();
			}
			if (bufferedReader != null) {
				bufferedReader.close();
			}
		}
	}

}
