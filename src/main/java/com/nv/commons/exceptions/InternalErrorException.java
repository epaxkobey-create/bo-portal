package com.nv.commons.exceptions;

import com.nv.commons.constants.InternalErrorCodeType;

public class InternalErrorException extends RuntimeException {

//	private static final long serialVersionUID = -4945928682871663561L;

//	private final InternalErrorCodeType errorCodeType;

	public InternalErrorException(InternalErrorCodeType errorCodeType, String message, Throwable cause) {
		super(message, cause);
//		this.errorCodeType = errorCodeType;
	}

}
