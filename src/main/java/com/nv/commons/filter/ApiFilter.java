package com.nv.commons.filter;

import static com.nv.commons.utils.ServerInfoUtils.isAPIServer;
import static com.nv.commons.utils.ServerInfoUtils.isBackendApiServer;

import java.io.IOException;

import com.nv.commons.system.SystemInfo;
import com.nv.commons.utils.LogUtils;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * @author Luke Chi
 */
//@WebFilter(urlPatterns = {"/page/api/*", "/pg/*", "/api/*"})
public class ApiFilter implements Filter {

	/**
	 * init
	 */
	@Override
	public void init(FilterConfig config) {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {

		if (!isAPIServer() && !isBackendApiServer()) {

			((HttpServletResponse) response).setStatus(HttpServletResponse.SC_NOT_FOUND);

			LogUtils.filter.warn("Request tried to access api content on non-api server. ServerID: {}, RequestURI: {}, RequestIP: {}",
				SystemInfo.getInstance().getServerID(),
				((HttpServletRequest) request).getRequestURI(), request.getRemoteAddr());
			return;
		}

		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}
}
