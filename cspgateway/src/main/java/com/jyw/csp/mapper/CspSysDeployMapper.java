package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspSysDeployEntity;

@Mapper
@Component
public interface CspSysDeployMapper {

	List<CspSysDeployEntity> selectAll();
	
	CspSysDeployEntity select(String deployid);
	
	List<CspSysDeployEntity> selectByWhere(CspSysDeployEntity entity);
	
	int insert(CspSysDeployEntity entity);
	
	int update(CspSysDeployEntity entity);
}
