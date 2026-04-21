package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspAppIpWhitelistEntity;

@Mapper
@Component
public interface CspAppIpWhitelistMapper {

	List<CspAppIpWhitelistEntity> selectAll();
	List<CspAppIpWhitelistEntity> select(@Param("appid")String appid);
}
