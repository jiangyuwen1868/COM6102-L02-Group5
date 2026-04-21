package com.anydef.csp;

import org.jasypt.util.text.BasicTextEncryptor;

public class JasyptTest {

	public static void main(String[] args) {
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		//加密所需的salt(盐),自定义
		textEncryptor.setPassword("anydef@csp");
		//要加密的数据（数据库的用户名或密码）
		String username = textEncryptor.encrypt("root");
		String password = textEncryptor.encrypt("dceptsm%401234!");
		System.out.println("username:"+username);
		System.out.println("password:"+password);
	}
}
