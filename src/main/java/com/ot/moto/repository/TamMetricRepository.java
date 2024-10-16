package com.ot.moto.repository;

import com.ot.moto.entity.TamMetrics;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TamMetricRepository extends JpaRepository<TamMetrics,Long> {
}
