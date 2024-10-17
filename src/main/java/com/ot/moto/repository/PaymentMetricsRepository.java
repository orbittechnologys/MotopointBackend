package com.ot.moto.repository;

import com.ot.moto.entity.PaymentMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface PaymentMetricsRepository extends JpaRepository<PaymentMetrics,Long> {

    public Page<PaymentMetrics> findAllByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    public Page<PaymentMetrics> findAllByDateTimeBetween(Pageable pageable);
}