package com.nv.commons.exceptions;

public class ProviderAPINoSupportException extends Exception {

	/**
	 *
	 */
//	private static final long serialVersionUID = -4963618002251248408L;

//	public ProviderAPINoSupportException() {
//		super("Provider API No Support");
//	}

	public ProviderAPINoSupportException(String method) {
		super("Provider API No Support：" + method);
	}
}
