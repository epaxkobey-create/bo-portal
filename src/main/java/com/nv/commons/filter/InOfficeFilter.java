package com.nv.commons.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

/**
 * @author Neutec
 */
public class InOfficeFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException {
		//		HttpServletResponse httpResponse = (HttpServletResponse) response;
		//		String requestIpAddress = HostAddressUtils.getRealIPAddresses((HttpServletRequest) request);
		//		if (HostAddressUtils.isOfficeIP(requestIpAddress)) {
		chain.doFilter(request, response);
		//		} else {
		//			httpResponse.setStatus(HttpServletResponse.SC_NOT_FOUND);
		//		}
	}

	@Override
	public void destroy() {

	}
}
