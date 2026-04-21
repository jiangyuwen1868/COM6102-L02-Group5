package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspSysConfigEntity;

@Mapper
@Component
public interface CspSysConfigMapper {

	List<CspSysConfigEntity> selectAll();
	
	CspSysConfigEntity selectByKey(@Param("config_key")String config_key);
}
