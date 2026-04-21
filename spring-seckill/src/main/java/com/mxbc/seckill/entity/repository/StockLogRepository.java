package com.mxbc.seckill.entity.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mxbc.seckill.entity.StockLog;

@Repository
public interface StockLogRepository extends JpaRepository<StockLog, Long> {
    List<StockLog> findByProduct_IdOrderByCreatedAtDesc(Long productId);
}