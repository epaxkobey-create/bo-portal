package com.nv.commons.exceptions;

import org.apache.logging.log4j.Logger;

public class ProviderException extends RuntimeException {

//	private static final long serialVersionUID = -3344002768584590824L;

	public ProviderException(String message, String provider, String status, String description) {
		super(buildMsg(message, provider, status, description));
	}
	
	public ProviderException(Logger logger, String message, String provider, String status, String description) {
		this(message, provider, status, description);
		logger.error(message, status, description);
	}

	private static String buildMsg(String message, String provider, String status, String description) {
		StringBuilder sb = new StringBuilder();
		sb.append(message);
		sb.append(", provider:").append(provider);
		sb.append(", status:").append(status);
		sb.append(", desc:").append(description);
		return sb.toString();
	}
}
