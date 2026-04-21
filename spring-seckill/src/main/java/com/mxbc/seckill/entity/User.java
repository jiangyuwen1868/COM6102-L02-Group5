package com.mxbc.seckill.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.mxbc.seckill.desensitize.DesensitizationTypeEnum;
import com.mxbc.seckill.desensitize.annotation.Desensitization;
import com.mxbc.seckill.edb.EncryptionConverter;

@Entity
@Table(name = "users")
public class User {
	@Id 
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private Long id;
	
	@Column(unique = true, nullable = false)
	@Convert(converter = EncryptionConverter.class)
	@Desensitization(type = DesensitizationTypeEnum.MY_RULE, startInclude = 1, endExclude = 10)
	private String username;
	
	private String password;
	
	private String salt;
	@Convert(converter = EncryptionConverter.class)
	@Desensitization(type = DesensitizationTypeEnum.EMAIL)
	private String email;
	@Convert(converter = EncryptionConverter.class)
	@Desensitization(type = DesensitizationTypeEnum.MOBILE_PHONE)
	private String phone;
	@Convert(converter = EncryptionConverter.class)
	@Desensitization(type = DesensitizationTypeEnum.MY_RULE, startInclude = 1, endExclude = 3)
	private String nickname;
	
	private Integer age;
	
	@Column(name = "create_time")
	private LocalDateTime createTime;
	
	@Column(name = "status")
	private Integer status = 1;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getSalt() {
		return salt;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public LocalDateTime getCreateTime() {
		return createTime;
	}
	public void setCreateTime(LocalDateTime createTime) {
		this.createTime = createTime;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
}