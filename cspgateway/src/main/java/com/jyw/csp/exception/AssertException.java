package com.jyw.csp.exception;

public class AssertException extends CommonRuntimeException
{
  private static final long serialVersionUID = -1645841914L;

  public AssertException(String code, String message, Throwable e)
  {
    super(code, message, e);
  }

  public AssertException(String code, String message) {
    this(code, message, null);
  }
}
