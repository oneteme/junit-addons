package org.usf.junit.addons;

@SuppressWarnings("serial")
public final class ResourceAccesException extends RuntimeException {

	public ResourceAccesException(Throwable cause) {
		super(cause);
	}

	public ResourceAccesException(String message, Throwable cause) {
		super(message, cause);
	}
}
