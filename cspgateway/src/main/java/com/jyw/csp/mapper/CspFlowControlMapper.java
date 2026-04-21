package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspFlowControlEntity;

@Mapper
@Component
public interface CspFlowControlMapper {

	List<CspFlowControlEntity> selectAll();
	
	CspFlowControlEntity select(@Param("appid")String appid, 
			@Param("txcode")String txcode);
}
