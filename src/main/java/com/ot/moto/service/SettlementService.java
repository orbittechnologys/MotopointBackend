package com.ot.moto.service;

import com.ot.moto.dao.SettlementDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.Settlement;
import com.ot.moto.repository.SettlementRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

@Service
public class SettlementService {

    @Autowired
    private SettlementDao settlementDao;

    @Autowired
    private SettlementRepository settlementRepository;

    public ResponseEntity<ResponseStructure<Object>> save(Settlement settlement) {
        try {
            Settlement existingSettlement = settlementDao.findById(settlement.getId());
            if (Objects.isNull(existingSettlement)) {
                settlementDao.save(settlement);
                return ResponseStructure.successResponse(settlement, "Settlement saved successfully");
            }
            return ResponseStructure.errorResponse(null, 404, "Settlement already exists");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Unexpected error occurred");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findById(Long id) {
        try {
            Settlement settlement = settlementDao.findById(id);
            if (settlement != null) {
                return ResponseStructure.successResponse(settlement, "Settlement found");
            }
            return ResponseStructure.errorResponse(null, 404, "Settlement not found");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Unexpected error occurred");
        }
    }


    public ResponseEntity<ResponseStructure<Object>> findAll(int page, int size, String field) {
        try {
            Page<Settlement> settlements = settlementDao.findAll(page, size, field);
            if (settlements != null ) {
                return ResponseStructure.successResponse(settlements, "Settlements found");
            }
            return ResponseStructure.errorResponse(null, 404, "No settlements found");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Unexpected error occurred");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAllBySettleDateTimeBetween(LocalDate startDate, LocalDate endDate, int page, int size, String field) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            Page<Settlement> settlements = settlementDao.findAllBySettleDateTimeBetween(startDateTime, endDateTime, page, size, field);
            if (settlements != null) {
                return ResponseStructure.successResponse(settlements, "Settlements found within the date range");
            }
            return ResponseStructure.errorResponse(null, 404, "No settlements found within the date range");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Unexpected error occurred");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAllSettlementByDriverId(Long driverId, int page, int size, String field) {
        try {
            Page<Settlement> settlements = settlementDao.findAllSettlementByDriverId(driverId, page, size, field);
            if (settlements != null) {
                return ResponseStructure.successResponse(settlements, "Settlements found for the driver");
            }
            return ResponseStructure.errorResponse(null, 404, "No settlements found for the driver");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Unexpected error occurred");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAllSettlementByDriverIdDateTimeBetween(Long driverId, LocalDate startDate, LocalDate endDate, int page, int size, String field) {
        try {
            LocalDateTime startdDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);

            Page<Settlement> settlements = settlementDao.findAllSettlementByDriverIdDateTimeBetween(driverId, startdDateTime, endDateTime, page, size, field);
            if (settlements != null) {
                return ResponseStructure.successResponse(settlements, "Settlements found for the driver within the date range" );
            }
            return ResponseStructure.errorResponse(null, 404, "No settlements found for the driver within the date range");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Unexpected error occurred");
        }
    }
}
