package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspErrorInfoEntity;
import com.jyw.csp.mapper.CspErrorInfoMapper;

@Service
public class CspErrorInfoService {

	@Autowired
	private CspErrorInfoMapper cspErrorInfoMapper;
	
	public List<CspErrorInfoEntity> getAll() {
		return cspErrorInfoMapper.selectAll();
	}
}
