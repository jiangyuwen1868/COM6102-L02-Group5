package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspInitKeysEntity;

@Mapper
@Component
public interface CspInitKeysMapper {

	CspInitKeysEntity select(@Param("keyid")String keyid);
	
	List<CspInitKeysEntity> selectAll();
}
