package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspSysDeployEntity;
import com.jyw.csp.mapper.CspSysDeployMapper;

@Service
public class CspSysDeployService {

	@Autowired
	private CspSysDeployMapper cspSysDeployMapper;
	
	public List<CspSysDeployEntity> getAll() {
		return cspSysDeployMapper.selectAll();
	}
	
	public int initMyDeploy(CspSysDeployEntity entity) {
		List<CspSysDeployEntity> list = cspSysDeployMapper.selectByWhere(entity);
		if(list==null || list.isEmpty()) {
			return cspSysDeployMapper.insert(entity);
		} else {
			return cspSysDeployMapper.update(entity);
		}
	}
}
