package com.mxbc.seckill.entity.repository;

import com.mxbc.seckill.entity.SeckillActivity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SeckillActivityRepository extends JpaRepository<SeckillActivity, Long> {

    List<SeckillActivity> findByStatusOrderByStartTimeAsc(SeckillActivity.ActivityStatus status);

    List<SeckillActivity> findByStartTimeAfterOrderByStartTimeAsc(LocalDateTime time);

    List<SeckillActivity> findByEndTimeBeforeOrderByEndTimeDesc(LocalDateTime time);

    List<SeckillActivity> findByStartTimeBeforeAndEndTimeAfter(LocalDateTime now1, LocalDateTime now2);

    @Query("SELECT a FROM SeckillActivity a WHERE a.product.id = :productId")
    List<SeckillActivity> findByProductId(@Param("productId") Long productId);
}
