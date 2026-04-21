package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspResourceGroupEntity;
import com.jyw.csp.mapper.CspResourceGroupMapper;

@Service
public class CspResourceGroupService {

	@Autowired
	private CspResourceGroupMapper cspResourceGroupMapper;
	
	public List<CspResourceGroupEntity> getAll() {
		return cspResourceGroupMapper.selectAll();
	}
}
