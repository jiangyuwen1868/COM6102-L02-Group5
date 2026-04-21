package com.mxbc.seckill.entity.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mxbc.seckill.entity.SystemMetrics;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {
    List<SystemMetrics> findByTimestampAfterOrderByTimestampAsc(LocalDateTime timestamp);
    SystemMetrics findTopByOrderByTimestampDesc();
}