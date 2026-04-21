package com.jyw.csp.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspTxCodeEntity;
import com.jyw.csp.mapper.CspTxCodeMapper;

@Service
public class CspTxCodeService {

	@Autowired
	private CspTxCodeMapper cspTxCodeMapper;
	
	public List<CspTxCodeEntity> getAll() {
		return cspTxCodeMapper.selectAll();
	}
}
