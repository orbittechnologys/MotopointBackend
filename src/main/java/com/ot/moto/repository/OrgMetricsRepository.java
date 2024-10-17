package com.ot.moto.repository;

import com.ot.moto.entity.OrgMetrics;
import com.ot.moto.entity.TamMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface OrgMetricsRepository extends JpaRepository<OrgMetrics, Long> {
    public Page<OrgMetrics> findAllByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
