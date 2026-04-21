package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspAppInfoEntity;
import com.jyw.csp.mapper.CspAppInfoMapper;

@Service
public class CspAppInfoService {

	@Autowired
	private CspAppInfoMapper cspAppinfoMapper;
	
	
	public int count() {
		return cspAppinfoMapper.count();
	}
	
	public List<CspAppInfoEntity> getAll() {
		return cspAppinfoMapper.selectAll();
	}
	
	public CspAppInfoEntity get(String appid) {
		return cspAppinfoMapper.select(appid);
	}
}
