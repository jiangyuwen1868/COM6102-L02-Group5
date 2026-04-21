package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspSysConfigEntity;
import com.jyw.csp.mapper.CspSysConfigMapper;

@Service
public class CspSysConfigService {

	@Autowired
	private CspSysConfigMapper cspSysConfigMapper;
	
	public List<CspSysConfigEntity> getAll() {
		return cspSysConfigMapper.selectAll();
	}
	
	public CspSysConfigEntity getByKey(String config_key) {
		return cspSysConfigMapper.selectByKey(config_key);
	}
}
