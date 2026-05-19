package com.nv.commons.exceptions;

import org.apache.logging.log4j.Logger;

import java.io.Serial;

public class ProviderTimeoutException extends ProviderException {

	@Serial
	private static final long serialVersionUID = 1L;

	public ProviderTimeoutException(Logger logger, String msg, String provider, String status, String desc) {
		super(logger, msg, provider, status, desc);
	}

}
