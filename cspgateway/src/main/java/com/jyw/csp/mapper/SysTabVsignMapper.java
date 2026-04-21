package com.jyw.csp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.SysTabVsignEntity;

@Mapper
@Component
public interface SysTabVsignMapper {
	
	SysTabVsignEntity select(SysTabVsignEntity entity);

	int insert(SysTabVsignEntity entity);
	
	int update(SysTabVsignEntity entity);
}
