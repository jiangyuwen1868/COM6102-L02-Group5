package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspAppSrvAuthEntity;

@Mapper
@Component
public interface CspAppSrvAuthMapper {

	List<CspAppSrvAuthEntity> selectAll();
	
	List<CspAppSrvAuthEntity> select(@Param("appid")String appid);
}
