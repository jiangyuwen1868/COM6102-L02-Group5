package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspAppIpWhitelistEntity;
import com.jyw.csp.mapper.CspAppIpWhitelistMapper;

@Service
public class CspAppIpWhitelistService {

	@Autowired
	private CspAppIpWhitelistMapper appIpWhitelistMapper;
	
	public List<CspAppIpWhitelistEntity> getAll() {
		return appIpWhitelistMapper.selectAll();
	}
	
	public List<CspAppIpWhitelistEntity> getList(String appid) {
		return appIpWhitelistMapper.select(appid);
	}
}
