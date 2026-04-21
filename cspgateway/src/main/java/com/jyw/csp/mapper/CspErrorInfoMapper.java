package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

import com.jyw.csp.entity.CspErrorInfoEntity;

@Mapper
@Component
public interface CspErrorInfoMapper {

	List<CspErrorInfoEntity> selectAll();
}
