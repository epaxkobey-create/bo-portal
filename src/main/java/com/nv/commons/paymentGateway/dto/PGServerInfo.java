package com.nv.commons.paymentGateway.dto;

import com.nv.commons.utils.HostAddressUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class PGServerInfo {

	public PGServerInfo(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		this.ip = HostAddressUtils.getRealIPAddresses(httpRequest);
		this.serverName = httpRequest.getServerName();
		this.serverPort = httpRequest.getServerPort();
		this.httpRequest = httpRequest;
		this.httpResponse = httpResponse;
	}

	private String ip;

	private String serverName;

	private int serverPort;

	private HttpServletRequest httpRequest;

	private HttpServletResponse httpResponse;

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public int getServerPort() {
		return serverPort;
	}

	public void setServerPort(int serverPort) {
		this.serverPort = serverPort;
	}

	public HttpServletRequest getHttpRequest() {
		return httpRequest;
	}

	public void setHttpRequest(HttpServletRequest httpRequest) {
		this.httpRequest = httpRequest;
	}

	public HttpServletResponse getHttpResponse() {
		return httpResponse;
	}

	public void setHttpResponse(HttpServletResponse httpResponse) {
		this.httpResponse = httpResponse;
	}

}
