package com.jyw.csp.resource.httpresource;

import java.util.concurrent.Callable;

public class HttpTaskCallable implements Callable<HttpTaskVo> {
	
	private HttpCommService srv;
	private String httpURL;
	private String msg;
	
	public HttpTaskCallable(String httpURL, String msg) {
		this.httpURL = httpURL;
		this.msg = msg;
	}
	
	public HttpTaskCallable(HttpCommService srv, String msg) {
		this.srv = srv;
		this.httpURL = srv.httpURL;
		this.msg = msg;
	}

	@Override
	public HttpTaskVo call() throws Exception {
		String rspMsg = srv.comm(httpURL, msg);
		HttpTaskVo vo = new HttpTaskVo();
		vo.setHttpURL(httpURL);
		vo.setReqMsg(msg);
		vo.setRespMsg(rspMsg);
		
		return vo;
	}

}
