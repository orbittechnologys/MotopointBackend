package com.ot.moto.dao;

import com.ot.moto.entity.OrgMetrics;
import com.ot.moto.entity.PaymentMetrics;
import com.ot.moto.entity.TamMetrics;
import com.ot.moto.repository.OrgMetricsRepository;
import com.ot.moto.repository.OrgReportsRepository;
import com.ot.moto.repository.PaymentMetricsRepository;
import com.ot.moto.repository.TamMetricsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public class MetricsDao {

    @Autowired
    private TamMetricsRepository tamMetricsRepository;

    @Autowired
    private PaymentMetricsRepository paymentMetricsRepository;

    @Autowired
    private OrgMetricsRepository orgMetricsRepository;


    public Page<OrgMetrics> findOrgMetricsByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return orgMetricsRepository.findAllByDateTimeBetween(startDate, endDate, PageRequest.of(page, size).withSort(Sort.by(field).ascending()));
    }

    public Page<PaymentMetrics> findPaymentMetricsByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return paymentMetricsRepository.findAllByDateTimeBetween(startDate, endDate, PageRequest.of(page, size).withSort(Sort.by(field).ascending()));
    }

    public Page<TamMetrics> findTamMetricsByDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, int page, int size, String field) {
        return tamMetricsRepository.findAllByDateTimeBetween(startDate, endDate, PageRequest.of(page, size).withSort(Sort.by(field).ascending()));
    }
}
