package com.jyw.csp.util.xml;


public class XMLException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public XMLException() {
		super();
	}
	
	public XMLException(String message) {
		super(message);
	}
	
	public XMLException(Throwable tr) {
		super(tr);
	}
	
	public XMLException(String message,Throwable tr) {
		super(message,tr);
	}
}