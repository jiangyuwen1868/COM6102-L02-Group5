package com.jyw.csp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jyw.csp.entity.SysTabVsignEntity;
import com.jyw.csp.mapper.SysTabVsignMapper;

@Service
public class SysTabVsignService {

	@Autowired
	private SysTabVsignMapper sysTabVsignMapper;
	
	public int save(SysTabVsignEntity entity) {
		return sysTabVsignMapper.insert(entity);
	}
	
	public int update(SysTabVsignEntity entity) {
		return sysTabVsignMapper.update(entity);
	}
	
	public int saveOrUpdate(SysTabVsignEntity entity) {
		SysTabVsignEntity e = sysTabVsignMapper.select(entity);
		if(e==null) {
			return sysTabVsignMapper.insert(entity);
		} else {
			return sysTabVsignMapper.update(entity);
		}
	}
}
