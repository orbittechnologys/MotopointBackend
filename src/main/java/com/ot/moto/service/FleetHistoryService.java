package com.ot.moto.service;

import com.ot.moto.dao.FleetHistoryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.FleetHistory;
import com.ot.moto.repository.FleetHistoryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FleetHistoryService {

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;

    @Autowired
    private FleetHistoryDao fleetHistoryDao;

    private static final Logger logger = LoggerFactory.getLogger(FleetHistoryService.class);


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryById(Long id) {
        try {
            FleetHistory fleetHistory = fleetHistoryDao.findById(id);
            if (fleetHistory == null) {
                logger.warn("FleetHistory not found with id: {}", id);
                return ResponseStructure.errorResponse(null, 404, "FleetHistory not found with id: " + id);
            }
            return ResponseStructure.successResponse(fleetHistory, "FleetHistory found");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by id", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by id: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByFleetId(Long fleetId) {
        try {
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByFleetId(fleetId);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for fleetId: {}", fleetId);
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found for fleetId: " + fleetId);
            }
            return ResponseStructure.successResponse(fleetHistories, "FleetHistory found for fleetId");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by fleetId", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by fleetId: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByDriverId(Long driverId) {
        try {
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByDriverId(driverId);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for driverId: {}", driverId);
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found for driverId: " + driverId);
            }
            return ResponseStructure.successResponse(fleetHistories, "FleetHistory found for driverId");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by driverId", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by driverId: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getFleetHistoryByFleetIdAndDriverId(Long fleetId, Long driverId) {
        try {
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByFleetIdAndDriverId(fleetId, driverId);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for fleetId: {} and driverId: {}", fleetId, driverId);
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found for fleetId: " + fleetId + " and driverId: " + driverId);
            }
            return ResponseStructure.successResponse(fleetHistories, "FleetHistory found for fleetId and driverId");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by fleetId and driverId", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching FleetHistory by fleetId and driverId: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getAllFleetHistory(int page, int size, String field) {
        try {
            Page<FleetHistory> fleetHistorypage = fleetHistoryDao.findAll(page, size, field);
            if (fleetHistorypage.isEmpty()) {
                logger.warn("No FleetHistory found.");
                return ResponseStructure.errorResponse(null, 404, "No FleetHistory found");
            }
            return ResponseStructure.successResponse(fleetHistorypage, "FleetHistory found");
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public List<FleetHistory> findByFleetIdAndDateRange(Long fleetId, LocalDateTime startDate, LocalDateTime endDate) {
        try {
            // Fetch fleet history records by fleetId and date range
            List<FleetHistory> fleetHistories = fleetHistoryDao.findByFleetIdAndDateRange(fleetId, startDate, endDate);
            if (fleetHistories.isEmpty()) {
                logger.warn("No FleetHistory found for fleetId: {} between {} and {}", fleetId, startDate, endDate);
            } else {
                logger.info("FleetHistory found for fleetId: {} between {} and {}", fleetId, startDate, endDate);
            }
            return fleetHistories;
        } catch (Exception e) {
            logger.error("Error fetching FleetHistory by fleetId and date range", e);
            throw new RuntimeException("Error fetching FleetHistory by fleetId and date range: " + e.getMessage());
        }
    }

}
