package com.jyw.csp.exception;

public class CommonRuntimeException extends RuntimeException implements
		IndexedMessage {

	private static final long serialVersionUID = -9466235672L;
	private String code;
	private String message;
	private Object[] params;
	private Throwable throwable;

	public Object[] getParams() {
		return params;
	}

	public void setParams(Object[] params) {
		this.params = params;
	}

	public CommonRuntimeException() {
		super();
	}

	public CommonRuntimeException(String code, String message, Throwable e) {
		super(e);
		this.code = code;
		this.message = message;
		this.throwable = e;
	}

	public CommonRuntimeException(String code, String message) {
		this.code = code;
		this.message = message;
	}

	
	public CommonRuntimeException(String code, Throwable e){
		this(code,"",e);
	}

	public CommonRuntimeException(String code) {
		this(code, "");
	}

	public CommonRuntimeException(Throwable e) {
		super(e);
		this.throwable = e;
	}
	
	public CommonRuntimeException(String code, Throwable e, Object[] params) {
	    this.code = code;
	    this.throwable = e;
	    this.params = params;
	  }

	  public CommonRuntimeException(String code, Object[] params) {
	    this(code, null, params);
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
