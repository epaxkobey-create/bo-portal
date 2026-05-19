package com.nv.commons.filter;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.nv.commons.constants.WebSiteType;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.LogUtils;
import com.nv.commons.utils.ServerInfoUtils;
import com.nv.commons.utils.WebSiteTypeUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;

/**
 * Filter for Guest/Player multi-site 取名 WebSiteJspFilter 而不用 JspFilter 是為了讓這個
 * filter 排在 PlayerFilter 之後 (tomcat 會按照字母順序依序執行 filter)
 *
 * @author Luke Chi
 */
//@WebFilter(urlPatterns = {
//	"/page/maintainVendor.jsp",
//	"/page/guest/*",
//	"/page/player/*",
//	"/h5/guest/*",
//	"/h5/player/*",
//	"/css/h5/site/*",
//	"/js/h5/site/*",
//  "/js/web/core/*",
//  "/js/h5/core/*",
//	"/images/*",
//	"/upload/*",
//	"/robots.txt"
//	"/sitemap.xml"
//	"/google*.html"
//  "/h5/site/component/*"
//}, dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD})
public final class WebSiteFilter implements Filter {

	private FilterConfig filterConfig;

	// [site, [oldPath, newPath]]
	private final Map<WebSiteType, Map<String, String>> sitePathMap = new EnumMap<>(WebSiteType.class);

	/**
	 * init
	 */
	@Override
	public void init(FilterConfig config) throws ServletException {
		this.filterConfig = config;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		try {
			HttpServletRequest httpRequest = (HttpServletRequest) request;
			final String serverName = httpRequest.getServerName();

			WebSiteType webSiteType = WebSiteTypeUtils.getWebSiteTypeForManagerOrPlayer(serverName);

			if (webSiteType == null) {

				if (ServerInfoUtils.isAPIServer() || ServerInfoUtils.isBackendApiServer()) {
					// 因為 api server 不會有 webSiteType，不需要 WebSiteFilter 處理
					chain.doFilter(request, response);
					return;
				} else {
					((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND);

					LogUtils.filter.warn("Request tried to access WEB content on non-WEB server. ServerID: {}, RequestURI: {}, RequestIP: {}",
						SystemInfo.getInstance().getServerID(),
						((HttpServletRequest) request).getRequestURI(), request.getRemoteAddr());
					return;
				}
			}

			String _servletPath = httpRequest.getServletPath();

			if (isSeoFileOnRoot(_servletPath)) {
				_servletPath = "/page" + _servletPath;
			}

			final String servletPath = _servletPath;

			/*
			 * for back office bet report
			 */
			if (ServerInfoUtils.isManagerServer()) {

				if (servletPath.startsWith("/betReport")) {
					//					getBetReport(httpRequest, response, servletPath, webSiteType); // TODO - Ken: uncomment
					return;
				}
				if (servletPath.startsWith("/exportReport")) {
					//					getExportReport(httpRequest, response, servletPath, webSiteType); // TODO - Ken: uncomment
					return;
				}
			}

			// BO possible access /upload/ folder, need forward to /site/ folder
			if (servletPath.startsWith("/upload/")) {

				if (servletPath.contains("/site/" + webSiteType.getSiteFolder() + "/")) {
					chain.doFilter(request, response);
					return;
				}

				// other path need to do filter, do not pass
			} else {
				chain.doFilter(request, response);
				return;
			}

			String realPath = servletPath;
			Map<String, String> pathMap = sitePathMap.computeIfAbsent(webSiteType, k -> new HashMap<>());

			final String clearCache = request.getParameter("clc");

			if ("true".equals(clearCache)) {

				final String oldResult = pathMap.remove(servletPath);

			}

			if (!servletPath.startsWith("/upload/game/")) {

				realPath = pathMap.computeIfAbsent(servletPath,
					// 只有第一次進來的 servletPath 會呼叫 genRealPath() , 之後都是直接從 pathMap 取出
					path -> genRealPath(path));

//				if ("true".equals(clearCache)) {
//					LogUtils.SYS.debug("realPath: " + realPath);
//				}

			} else {
				// for "/upload/game/"
				if (SystemInfo.getInstance().isHasExternalUpload()) {

					realPath = pathMap.computeIfAbsent(servletPath, path ->
						path.replaceFirst("/upload/", "/external/upload/"));

				} else {
					// Avoid "/upload/game/" infinite "request forward" loop
					chain.doFilter(request, response);
					return;
				}
			}

			/*
			 * MEMO: fix loop with httpRequest.getServletPath().equals(realPath)
			 */
			if (realPath.startsWith("/external/upload/")) {

				String queryString = ((HttpServletRequest) request).getQueryString();

				if (StringUtils.isNotEmpty(queryString)) {
					realPath += "?" + queryString;
				}

				((HttpServletResponse) response).sendRedirect(realPath);

			} else if (httpRequest.getServletPath().equals(realPath)) {

				chain.doFilter(request, response);

			} else {
				//				interceptImagePath(servletPath);

				// todo: temp fix if image not found will cause infinite loop
				if (!realPath.trim().isEmpty()) {
					request.getRequestDispatcher(realPath).forward(request, response);
				}

			}

		} catch (Exception e) {
			LogUtils.SYS.error(((HttpServletRequest) request).getServletPath());
			LogUtils.SYS.error(e.getMessage(), e);
		}

	}

	/**
	 *
	 */
	private boolean isSeoFileOnRoot(String _servletPath) {
		return "/robots.txt".equalsIgnoreCase(_servletPath)
			|| "/sitemap.xml".equalsIgnoreCase(_servletPath)
			|| (_servletPath.startsWith("/google") && _servletPath.endsWith(".html"));
	}

	/*
	 *
	 */
	private String genRealPath(final String servletPath) {

		// MEMO: only handle /page/google***.html, ignore other *.html
		final boolean notGoogleHtml = servletPath.endsWith(".html") && !isGoogleSeoHtml(servletPath);
		// for js
		final boolean isJsPlugins = servletPath.startsWith("/js/plugins/");

		if (notGoogleHtml || isJsPlugins) {
			return servletPath;
		}

		final boolean isUploadPath = servletPath.startsWith("/upload/");

		// default
		String realPath = "";

		/*
		 * for access images in external /upload folder
		 */
		if (SystemInfo.getInstance().isHasExternalUpload() && isUploadPath) {
			realPath = realPath.replaceFirst("/upload/", "/external/upload/");
		}

		return realPath;
	}

	@Override
	public void destroy() {
	}

	private boolean isGoogleSeoHtml(String _servletPath) {
		return _servletPath.startsWith("/google") || _servletPath.startsWith("/page/google");
	}

}
