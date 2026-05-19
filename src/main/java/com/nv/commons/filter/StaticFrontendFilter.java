package com.nv.commons.filter;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.nv.commons.constants.DeviceType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.utils.FrontendUtils;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.commons.utils.WebSiteTypeUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public class StaticFrontendFilter implements Filter {

	private final Map<String, byte[]> fileBytesMap = new ConcurrentHashMap<>();

	private final Map<String, Long> fileLastModifiedTimeMap = new ConcurrentHashMap<>();

	private String folder = "/var/www/static/";

	/**
	 *
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {

		final String frontendDist = config.getServletContext().getInitParameter("frontendDist");

		if (frontendDist != null) {
			folder = frontendDist;
		}
	}

	@Override
	public void destroy() {
	}

	private static final String[] skipPaths = new String[] {
		"/admin/",
		"/affiliate/",
		"/api/",
		"/app/api/",
		"/captcha/",
		"/client/",
		"/guest/",
		"/manager/",
		"/notifycontroller",
		"/pg/",
		"/playerFE/",
		"/upload/",
		"/ws",
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws ServletException, IOException {

		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;

		String requestUri = req.getRequestURI();

		final WebSiteType webSiteByFeDomain = WebSiteTypeUtils.getWebSiteByFeDomain();

		if (webSiteByFeDomain == null) {
			chain.doFilter(request, response);
			return;
		}

		final boolean isPlayerServer = ServerInfoUtils.isPlayerServer();

		if (!isPlayerServer || (Arrays.stream(skipPaths).anyMatch(requestUri::contains))) {
			chain.doFilter(request, response);
			return;
		}

		File file = null;

		final DeviceType deviceType = DeviceType.getInstance(req);

		final String fullFilename = URLDecoder.decode(
			refineRequestUri(requestUri).substring(1), "UTF-8");

		//		String siteFolder = MarketingGroup.HT.getMarketingName();
		String siteFolder = "rsg";

		//		FrontendUtils.addAppNameCookie(req, resp);

		FrontendUtils.setBtag(req, resp);

		// only h5 source, even RWD Model
		final String deviceFolder = "h5";
		final String vuePrefix = "/main";

		if (vuePrefix.equals(requestUri) || requestUri.startsWith(vuePrefix + "/")) {
			// fallback to root "/
			final String rootHtml = URLDecoder.decode(refineRequestUri("/").substring(1),
				"UTF-8");

			file = new File(folder
				+ deviceFolder + File.separator
				+ siteFolder + File.separator
				+ "browser" + File.separator,
				rootHtml);
		} else {
			// MEMO: default assume Angular SSR "/browser" folder
			file = new File(folder
				+ deviceFolder + File.separator
				+ siteFolder + File.separator
				+ "browser" + File.separator,
				fullFilename);
		}

		if (!file.exists() && requestUri.indexOf(".") < 0) {
			file = new File(folder
				+ deviceFolder + File.separator
				+ siteFolder + File.separator
				+ "browser" + File.separator,
				"index.html");
		}

		if (file.exists()) {

			serveStaticFile(req, resp, fullFilename, file);

		} else {
			chain.doFilter(request, response);
		}
	}

	/**
	 *
	 */
	private String refineRequestUri(String requestUri) {

		String requestUriStr = requestUri;

		if (requestUri.endsWith("/")) {
			requestUriStr += "/index.html";
		}

		final String[] split = requestUriStr.split("/");

		if (!split[split.length - 1].contains(".")) {
			requestUriStr += "/index.html";
		}

		return requestUriStr;
	}

	/**
	 *
	 */
	private void serveStaticFile(HttpServletRequest req, HttpServletResponse resp, String fullFilename, File file) {

		HttpSession session = req.getSession(false);
		if (session == null) {
			req.getSession(true);
		}

		resp.setHeader("Content-Type", req.getServletContext().getMimeType(file.getName()));
		resp.setHeader("Content-Length", String.valueOf(file.length()));
		resp.setHeader("Content-Disposition", "inline; filename=\"" + file.getName() + "\"");

		// Files.copy(file.toPath(), outputStream);
		try {
			final long lastModifiedTime = Files.readAttributes(Paths.get(file.getAbsolutePath()),
					BasicFileAttributes.class)
				.lastModifiedTime().toMillis();

			final long oldTime = fileLastModifiedTimeMap.computeIfAbsent(fullFilename, k -> lastModifiedTime);

			if (oldTime != lastModifiedTime) {
				fileBytesMap.remove(fullFilename);
				fileLastModifiedTimeMap.put(fullFilename, lastModifiedTime);
			}

			byte[] bytes = fileBytesMap.computeIfAbsent(fullFilename, (k) -> {
				try {
					return Files.readAllBytes(file.toPath());

				} catch (IOException e) {
					LogUtils.SYS.error(e.getMessage(), e);
				}
				return new byte[0];
			});

			// fail safe
			if (bytes.length != file.length()) {
				bytes = Files.readAllBytes(file.toPath());
				fileBytesMap.clear();
			}

			ServletOutputStream outputStream = resp.getOutputStream();

			outputStream.write(bytes);
			outputStream.flush();
			//	outputStream.close();

		} catch (Exception e) {
			if (e instanceof org.apache.catalina.connector.ClientAbortException) {
				// ignore
			} else {
				LogUtils.SYS.error(fullFilename);
				LogUtils.SYS.error(e.getMessage(), e);
			}
		}
	}
}
