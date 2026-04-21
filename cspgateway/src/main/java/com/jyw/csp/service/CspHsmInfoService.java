package com.jyw.csp.service;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.jyw.csp.entity.CspHsmInfoEntity;
import com.jyw.csp.mapper.CspHsmInfoMapper;

@Service
public class CspHsmInfoService {
    @Resource
    private CspHsmInfoMapper hsmInfoMapper;

    public List<CspHsmInfoEntity> getAll() {
        return hsmInfoMapper.selectAll();
    }

    public List<CspHsmInfoEntity> getByGroupId(long groupId) {
        return hsmInfoMapper.selectByGroupId(groupId);
    }
}
