package com.jyw.csp.resource.httpresource;

public class HttpResouceException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public HttpResouceException(){
		super();
	}
	
	public HttpResouceException(String msg){
		super(msg);
	}
	
	public HttpResouceException(Throwable t){
		super(t);
	}
	
	public HttpResouceException(String msg,Throwable t){
		super("Resouce available:"+msg, t);
	}
}