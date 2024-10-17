package com.ot.moto.repository;

import com.ot.moto.entity.TamMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TamMetricsRepository extends JpaRepository<TamMetrics,Long> {

    public Page<TamMetrics> findAllByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    public Page<TamMetrics> findAllByDateTimeBetween(Pageable pageable);
}
