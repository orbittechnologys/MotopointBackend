package com.ot.moto.service;

import com.ot.moto.dao.MetricsDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.OrgMetrics;
import com.ot.moto.entity.PaymentMetrics;
import com.ot.moto.entity.TamMetrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.core.Local;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
public class MetricService {

    @Autowired
    private MetricsDao metricsDao;

    private static final Logger logger = LoggerFactory.getLogger(MetricService.class);

    public ResponseEntity<ResponseStructure<Object>> findAllOrgMetricsByDateTimeBetween(LocalDate startDate, LocalDate endDate, int page, int size, String offset) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            Page<OrgMetrics> orgMetrics = metricsDao.findOrgMetricsByDateTimeBetween(startDateTime, endDateTime, page, size, offset);

            if (orgMetrics.isEmpty()) {
                logger.warn("no data found ");
                return ResponseStructure.errorResponse(null, 404, "No data found");
            }
            logger.info("data is being retrieved successfully");
            return ResponseStructure.successResponse(orgMetrics, "data successfully retrieved");

        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "unexpected error occured");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAllPaymentMetricsDateTimeBetween(LocalDate startDate, LocalDate endDate, int page, int size, String field) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            Page<PaymentMetrics> paymentMetrics = metricsDao.findPaymentMetricsByDateTimeBetween(startDateTime, endDateTime, page, size, field);

            if (paymentMetrics.isEmpty()) {
                logger.warn("no data found");
                return ResponseStructure.errorResponse(null, 404, "No data found");
            }
            logger.info("data is being retrieved successfully");
            return ResponseStructure.successResponse(paymentMetrics, "data successfully retrieved");

        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "unexpected error");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAllTamMtericsDateTimeBetween(LocalDate startDate, LocalDate endDate, int page, int size, String field) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            Page<TamMetrics> tamMetrics = metricsDao.findTamMetricsByDateTimeBetween(startDateTime, endDateTime, page, size, field);

            if (tamMetrics.isEmpty()) {
                logger.warn("no data found");
                return ResponseStructure.errorResponse(null, 404, "No data found");
            }
            logger.info("data is being retrieved successfully");
            return ResponseStructure.successResponse(tamMetrics, "data fetched successfully");

        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "unexpected error");
        }
    }
}