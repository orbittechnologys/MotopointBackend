package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.FleetDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Fleet;
import com.ot.moto.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FleetService {

    @Autowired
    private FleetDao fleetDao;

    @Autowired
    private DriverDao driverDao;

    private static final Logger logger = LoggerFactory.getLogger(FleetService.class);

    public ResponseEntity<ResponseStructure<Object>> createFleet(CreateFleetReq request) {
        try {
            if (fleetDao.getVehicleNumber(request.getVehicleNumber()) != null) {
                logger.warn("Vehicle Number already exists: {}", request.getVehicleName());
                return ResponseStructure.errorResponse(null, 409, "Vehicle Number already exists");
            }
            Fleet fleet = buildFleetFromRequest(request);
            fleetDao.createFleet(fleet);
            logger.info("Fleet created successfully: {}", fleet.getId());

            return ResponseStructure.successResponse(fleet, "Fleet created successfully");

        } catch (Exception e) {
            logger.error("Error creating fleet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Fleet buildFleetFromRequest(CreateFleetReq request) {
        Fleet fleet = new Fleet();
        fleet.setVehicleName(request.getVehicleName());
        fleet.setVehicleNumber(request.getVehicleNumber());
        fleet.setVehicleType(request.getVehicleType());
        fleet.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        fleet.setInsuranceDocument(request.getInsuranceDocument());

        Driver driver = driverDao.getById(request.getDriverId());
        fleet.setDriver(driver);

        return fleet;
    }

    private Fleet updateFleetFromRequest(UpdateFleetReq request, Fleet fleet) {
        if (!StringUtil.isEmpty(request.getVehicleName())) {
            fleet.setVehicleName(request.getVehicleName());
        }
        if (!StringUtil.isEmpty(request.getVehicleNumber())) {
            fleet.setVehicleType(request.getVehicleNumber());
        }
        if (!StringUtil.isEmpty(request.getVehicleType())) {
            fleet.setVehicleType(request.getVehicleType());
        }
        if (request.getInsuranceExpiryDate() != null) {
            fleet.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        }
        if (!StringUtil.isEmpty(request.getInsuranceDocument())) {
            fleet.setInsuranceDocument(request.getInsuranceDocument());
        }
        if (request.getDriverId() != null) {
            Driver driver = driverDao.getById(request.getDriverId());
            fleet.setDriver(driver);
        }
        return fleet;
    }

    public ResponseEntity<ResponseStructure<Object>> getFleet(Long id) {
        try {
            Fleet fleet = fleetDao.getFleetById(id);
            if (Objects.isNull(fleet)) {
                logger.warn("No Fleet found. Invalid ID: {}", id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id: " + id);
            }
            return ResponseStructure.successResponse(fleet, "Fleet found");
        } catch (Exception e) {
            logger.error("Error fetching single fleet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllFleets(int page, int size, String field) {
        try {
            Page<Fleet> fleetPage = fleetDao.findAll(page, size, field);
            if (fleetPage.isEmpty()) {
                logger.warn("No Fleet found.");
                return ResponseStructure.errorResponse(null, 404, "No Fleet found");
            }
            return ResponseStructure.successResponse(fleetPage, "Fleets found");
        } catch (Exception e) {
            logger.error("Error fetching fleets", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Fleet fetchFleet(Long id) {
        Fleet fleet = fleetDao.getFleetById(id);
        if (Objects.isNull(fleet)) {
            logger.warn("No Fleet found. Invalid ID: {}", id);
            return null;
        }
        return fleet;
    }

    public ResponseEntity<ResponseStructure<Object>> updateFleet(UpdateFleetReq request) {
        try {
            Fleet fleet = fetchFleet(request.getId());
            if (Objects.isNull(fleet)) {
                logger.warn("No fleet found with id: {}", request.getId());
                return ResponseStructure.errorResponse(null, 404, "Fleet not found with id: " + request.getId());
            }

            fleet = updateFleetFromRequest(request, fleet);
            fleetDao.createFleet(fleet);
            logger.info("Fleet updated successfully: {}", fleet.getId());

            return ResponseStructure.successResponse(fleet, "Fleet updated successfully");

        } catch (Exception e) {
            logger.error("Error updating fleet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}
