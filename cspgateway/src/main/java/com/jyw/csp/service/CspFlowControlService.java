package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspFlowControlEntity;
import com.jyw.csp.mapper.CspFlowControlMapper;

@Service
public class CspFlowControlService {

	@Autowired
	private CspFlowControlMapper cspFlowControlMapper;
	
	public List<CspFlowControlEntity> getAll() {
		return cspFlowControlMapper.selectAll();
	}
	
	public CspFlowControlEntity get(String appid, String txcode) {
		return cspFlowControlMapper.select(appid, txcode);
	}
}
