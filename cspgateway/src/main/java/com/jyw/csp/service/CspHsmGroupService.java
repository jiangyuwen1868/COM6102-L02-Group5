package com.jyw.csp.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspHsmGroupEntity;
import com.jyw.csp.entity.CspHsmTypeEntity;
import com.jyw.csp.mapper.CspHsmGroupMapper;

@Service
public class CspHsmGroupService {
    @Resource
    private CspHsmGroupMapper hsmGroupMapper;

    public List<CspHsmGroupEntity> getAll() {
        return hsmGroupMapper.selectAll();
    }

    public CspHsmGroupEntity getById(long groupId) {
        return hsmGroupMapper.selectById(groupId);
    }

    public List<CspHsmGroupEntity> getListByCategory(String category) {
        return hsmGroupMapper.selectByCategory(category);
    }
    
    public CspHsmTypeEntity getHsmTypeByGroupId(long groupId) {
    	return hsmGroupMapper.selectHsmTypeByGroupId(groupId);
    }
    
    public CspHsmTypeEntity getHsmTypeByIpPort(String hsm_ip, int hsm_port) {
    	return hsmGroupMapper.selectHsmTypeByIpPort(hsm_ip, hsm_port);
    }
}
