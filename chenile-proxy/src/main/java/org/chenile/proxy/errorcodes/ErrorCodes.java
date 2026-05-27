package org.chenile.proxy.errorcodes;

/**
 * Chenile Proxy error codes
 */
public enum ErrorCodes {
	
	CANNOT_CONNECT("650"), CANNOT_INVOKE("651"), MISSING_BODY("652"),
	;
	final String subError;
	private ErrorCodes(String subError) {
		this.subError = subError;
	}
	
	public String getSubError() {
		return this.subError;
	}
}
