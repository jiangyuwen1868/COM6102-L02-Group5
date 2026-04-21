package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspInitKeysEntity;
import com.jyw.csp.mapper.CspInitKeysMapper;

@Service
public class CspInitKeysService {

	@Autowired
	private CspInitKeysMapper cspInitKeysMapper;
	
	public List<CspInitKeysEntity> getAll() {
		return cspInitKeysMapper.selectAll();
	}
	
	public CspInitKeysEntity get(String keyid) {
		return cspInitKeysMapper.select(keyid);
	}
}
