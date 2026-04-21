package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jyw.csp.entity.CspHsmInfoEntity;

@Mapper
public interface CspHsmInfoMapper {
    /**
     * 查询所有加密机信息
     * 
     * @return 加密机信息列表
     */
    public List<CspHsmInfoEntity> selectAll();

    /**
     * 根据加密机分组ID查询加密机信息
     * 
     * @param groupId 加密机分组ID
     * @return 加密机信息列表
     */
    public List<CspHsmInfoEntity> selectByGroupId(long groupId);
}
