package com.mxbc.seckill.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mxbc.seckill.entity.SeckillRecord;

@Repository
public interface SeckillRecordRepository extends JpaRepository<SeckillRecord, Long> {
    List<SeckillRecord> findByUser_IdOrderByCreatedAtDesc(Long userId);
    List<SeckillRecord> findByActivity_IdOrderByCreatedAtDesc(Long activityId);
    SeckillRecord findByUser_IdAndActivity_Id(Long userId, Long activityId);
}