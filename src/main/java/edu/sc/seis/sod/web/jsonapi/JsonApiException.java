package edu.sc.seis.sod.web.jsonapi;

public class JsonApiException extends Exception {

	public JsonApiException() {
		super();
	}

	public JsonApiException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public JsonApiException(String message, Throwable cause) {
		super(message, cause);
	}

	public JsonApiException(String message) {
		super(message);
	}

	public JsonApiException(Throwable cause) {
		super(cause);
	}

}
