package com.nv.commons.filter;

import java.io.IOException;

import com.nv.commons.cache.WebsiteInfoCache;
import com.nv.commons.constants.DeviceType;
import com.nv.commons.constants.WebSiteType;
import com.nv.commons.dto.WebsiteInfo;
import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.HostAddressUtils;
import com.nv.commons.utils.WebSiteTypeUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

/**
 * ContextFilter - Restricts access to raw files and configures servlet objects. for css jpg js
 */
//@WebFilter(urlPatterns = {
//	//目前沒有特別處理，所以先註解
////	"/js/*",
////	"/css/*",
//	"/upload/*",
//	"/download/*",
//	"/images/*"})
public class ContextFilter implements Filter {

	protected FilterConfig config = null;

	/**
	 *
	 */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;
		// this will speed up the initial sessionId generation
		new java.security.SecureRandom().nextLong();
	}

	/**
	 */

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		/*
		 * MEMO: for CDN 圖片 換線
		 */
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		String servletPath = httpRequest.getServletPath();

		HttpSession session = httpRequest.getSession(false);
		final WebSiteType webSiteType = WebSiteTypeUtils.getWebSiteTypeForImageCache(request, session);

		/*
		 */
//		StaticResourceCountManager resourceCountManager = StaticResourceCountManager.getInstance();
		String realIPAddresses = HostAddressUtils.getRealIPAddresses(httpRequest);
		String sessionId = (session == null) ? "null" : session.getId();
		String webOrH5 = DeviceType.getInstance(httpRequest).isMobile() ? "h5" : "web";

		if (servletPath.startsWith("/upload/") || servletPath.startsWith("/images/")) {

			//			if (session == null) {
			//				LogUtils.SYS.debug("session == null of " + servletPath);
			//			}

			if (webSiteType != null) {
				WebsiteInfo websiteInfo = WebsiteInfoCache.getInstance().getByWebType(webSiteType.unique());

				/*
				if (websiteInfo.isEnabledCacheServer()) {

					String cdnDomain = FrontendUtils.getCdnDomain(session, httpRequest, null);

					// 只有外部server才需要導頁，如果是自身Server就不需要，避免無限迴圈 ?
					if (!cdnDomain.equals("")) {
						((HttpServletResponse) response).sendRedirect(cdnDomain + servletPath);
						return;
					}
				} else {
				*/

					/*
					 * /upload/game/ 的圖片沒有分 webSite 放, 所以直接 redirect
					 * /upload/* 底下的其他圖片 需要經過 WebSiteFilter
					 */
				if (SystemInfo.getInstance().isHasExternalUpload() && servletPath.startsWith("/upload/game/")) {

						String realPath = "/external" + servletPath;
						((HttpServletResponse) response).sendRedirect(realPath);

						/*
						 */
						final int bytesLength = 0;
//						resourceCountManager.count(
//							webSiteType, realIPAddresses, sessionId, webOrH5, realPath, bytesLength);

						return;
					}
//				}
			}
		}

		chain.doFilter(request, response); // allow
		return;
	}

	/**
	 *
	 */
	public void destroy() {
		config = null;
	}

}
