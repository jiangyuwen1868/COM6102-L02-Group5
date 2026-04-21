package com.jyw.csp.exception;

public class CommonException extends Exception implements IndexedMessage {
	private static final long serialVersionUID = -946602172L;
	private String code;
	private String message;
	private Throwable throwable;

	public CommonException() {
		super();
	}

	public CommonException(String code, String message, Throwable e) {
		super(e);
		this.code = code;
		this.message = message;
		this.throwable = e;
	}

	public CommonException(String code, String message) {
		this.code = code;
		this.message = message;
	}

	public CommonException(String code) {
		this(code, "");
	}

	public CommonException(Throwable e) {
		super(e);
		this.throwable = e;
	}

	public String getMessage() {
		return this.message;
	}

	public String getCode() {
		return this.code;
	}

	public Throwable getThrowable() {
		return this.throwable;
	}

	public String toString() {
		String s = getClass().getName();
		String message = getMessage();
		return ((message != null) ? new StringBuilder().append(s).append(": ")
				.append(message).toString() : s);
	}

	public Object[] getParameters() {
		return new Object[] { this.code, this.message };
	}
}
