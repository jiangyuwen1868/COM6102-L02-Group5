package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspAppInfoEntity;

@Mapper
@Component
public interface CspAppInfoMapper {

	int count();
	
	List<CspAppInfoEntity> selectAll();
	
	CspAppInfoEntity select(@Param("appid")String appid);
	
}
