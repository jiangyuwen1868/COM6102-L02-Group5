package com.mxbc.seckill.vo;

import java.io.Serializable;

public class SeckillResult implements Serializable{
	
	private static final long serialVersionUID = 567660725192200117L;
	/**
	 *  - code=0 ：秒杀成功
		- code=1 ：库存不存在
		- code=2 ：库存不足
		- code=3 ：已参与过（每人限购一件）
		- code=-1 ：操作失败（如未登录）
	 */
	private int code;
	private String message;
	private String orderId;
	private int stock;

	public SeckillResult() {
	}
	
	public SeckillResult(int code, String message) {
		this.code = code;
		this.message = message;
	}
	
	public SeckillResult(int code, String message, String orderId) {
		this.code = code;
		this.message = message;
		this.orderId = orderId;
	}
	
	public static SeckillResult failure(String msg) {
		return new SeckillResult(-1, msg);
	}
	
	public static SeckillResult success(String orderId) {
		return new SeckillResult(0, "秒杀成功", orderId);
	}
	
	public boolean isSuccess() {
		return code == 0;
	}
	
	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}
	
	public String getOrderId() {
		return orderId;
	}
	
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}
	
	public int getStock() {
		return stock;
	}
	
	public void setStock(int stock) {
		this.stock = stock;
	}
}
