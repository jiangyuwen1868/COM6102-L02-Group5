package com.jyw.csp.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.jyw.csp.entity.CspHsmGroupEntity;
import com.jyw.csp.entity.CspHsmTypeEntity;

@Mapper
public interface CspHsmGroupMapper {
    /**
     * 查询所有加密机分组信息
     * 
     * @return 加密机分组信息列表
     */
    public List<CspHsmGroupEntity> selectAll();

    /**
     * 根据加密机分组ID查询加密机分组信息
     * 
     * @param groupId 加密机分组ID
     * @return 加密机分组信息
     */
    public CspHsmGroupEntity selectById(long groupId);

    /**
     * 根据加密机类别查询加密机分组信息
     * 
     * @param category
     * @return
     */
    public List<CspHsmGroupEntity> selectByCategory(String category);
    
    /**
     * 根据密码机分组ID查询密码机所对应的密码机类型
     * @param groupId
     * @return
     */
    public CspHsmTypeEntity selectHsmTypeByGroupId(long groupId);
    
    /**
     * 根据密码机IP、Port查询密码机类型
     * @param hsm_ip
     * @param hsm_port
     * @return
     */
    public CspHsmTypeEntity selectHsmTypeByIpPort(String hsm_ip, int hsm_port);
}
