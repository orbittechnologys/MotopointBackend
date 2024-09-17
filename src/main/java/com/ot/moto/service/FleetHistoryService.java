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

@Service
public class FleetHistoryService {

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;

    @Autowired
    private FleetHistoryDao fleetHistoryDao;

    private static final Logger logger = LoggerFactory.getLogger(FleetHistoryService.class);



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
}
