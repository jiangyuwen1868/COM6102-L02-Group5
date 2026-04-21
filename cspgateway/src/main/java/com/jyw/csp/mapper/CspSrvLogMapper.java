package com.jyw.csp.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspSrvLogEntity;

@Mapper
@Component
public interface CspSrvLogMapper {

	int insert(CspSrvLogEntity entity);
	
	Integer count();
}
