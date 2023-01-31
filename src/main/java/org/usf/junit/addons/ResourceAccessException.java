package org.usf.junit.addons;

@SuppressWarnings("serial")
public final class ResourceAccessException extends RuntimeException {

	public ResourceAccessException(Throwable cause) {
		super(cause);
	}

	public ResourceAccessException(String message, Throwable cause) {
		super(message, cause);
	}
}
