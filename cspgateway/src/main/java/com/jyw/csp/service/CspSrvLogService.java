package com.jyw.csp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspSrvLogEntity;
import com.jyw.csp.mapper.CspSrvLogMapper;

@Service
public class CspSrvLogService {

	@Autowired
	private CspSrvLogMapper cspSrvLogMapper;
	
	public int save(CspSrvLogEntity entity) {
		return cspSrvLogMapper.insert(entity);
	}
	
	public Integer count() {
		return cspSrvLogMapper.count();
	}
}
