package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jyw.csp.entity.CspHsmTypeEntity;

@Mapper
public interface CspHsmTypeMapper {
    /**
     * 查询所有加密机类型信息
     * 
     * @return 加密机类型信息列表
     */
    public List<CspHsmTypeEntity> selectAll();

    /**
     * 根据加密机类型ID查询加密机类型信息
     * 
     * @param typeId 加密机类型ID
     * @return 加密机类型信息
     */
    public CspHsmTypeEntity selectById(long typeId);
}
