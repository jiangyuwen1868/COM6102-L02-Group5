package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspTxCodeEntity;

@Mapper
@Component
public interface CspTxCodeMapper {

	List<CspTxCodeEntity> selectAll();
}
