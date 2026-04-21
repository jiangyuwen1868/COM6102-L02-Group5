package com.jyw.csp.exception;

public class ActionFlowExceedException extends CommonRuntimeException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 564538811814032386L;

	public ActionFlowExceedException() {
		super();
	}

	public ActionFlowExceedException(final String s) {
		super(s);
	}
	
	public ActionFlowExceedException(final String s, final String msg) {
		super(s, msg);
	}
}