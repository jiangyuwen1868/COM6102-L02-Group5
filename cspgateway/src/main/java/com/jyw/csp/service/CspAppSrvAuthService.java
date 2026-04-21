package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspAppSrvAuthEntity;
import com.jyw.csp.mapper.CspAppSrvAuthMapper;

@Service
public class CspAppSrvAuthService {

	@Autowired
	private CspAppSrvAuthMapper cspAppSrvAuthMapper;
	
	public List<CspAppSrvAuthEntity> getAll() {
		return cspAppSrvAuthMapper.selectAll();
	}
	
	public List<CspAppSrvAuthEntity> getList(String appid) {
		return cspAppSrvAuthMapper.select(appid);
	}
}
