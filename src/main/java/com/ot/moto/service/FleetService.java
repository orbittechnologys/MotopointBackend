package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.FleetDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AssignFleet;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Fleet;
import com.ot.moto.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class FleetService {

    @Autowired
    private FleetDao fleetDao;

    @Autowired
    private DriverDao driverDao;

    private static final Logger logger = LoggerFactory.getLogger(FleetService.class);


    public ResponseEntity<ResponseStructure<Object>> getFleetCounts() {
        try {
            long selfOwnedCount = fleetDao.getSelfOwnedFleetCount(Fleet.OWN_TYPE.SELF_OWNED);
            long motoPointCount = fleetDao.getMotoPointFleetCount(Fleet.OWN_TYPE.MOTO_POINT);

            logger.info("Fetched fleet counts - SELF_OWNED: {}, MOTO_POINT: {}", selfOwnedCount, motoPointCount);

            Map<String, Long> counts = new HashMap<>();
            counts.put("SELF_OWNED", selfOwnedCount);
            counts.put("MOTO_POINT", motoPointCount);

            return ResponseStructure.successResponse(counts, "Fleet counts retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching fleet counts", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching fleet counts: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> createFleet(CreateFleetReq request) {
        try {
            if (fleetDao.getVehicleNumber(request.getVehicleNumber()) != null) {
                logger.warn("Vehicle Number already exists: {}", request.getVehicleNumber());
                return ResponseStructure.errorResponse(null, 409, "Vehicle Number already exists");
            }

            Fleet.VEHICLE_TYPE vehicleType;
            try {
                vehicleType = Fleet.VEHICLE_TYPE.valueOf(request.getVehicleType().toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseStructure.errorResponse(null, 400, "Invalid vehicle type. Allowed values: TWO_WHEELER, FOUR_WHEELER");
            }

            Fleet fleet = buildFleetFromRequest(request, vehicleType);
            fleetDao.createFleet(fleet);
            logger.info("Fleet created successfully: {}", fleet.getId());

            return ResponseStructure.successResponse(fleet, "Fleet created successfully");

        } catch (Exception e) {
            logger.error("Error creating fleet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Fleet buildFleetFromRequest(CreateFleetReq request, Fleet.VEHICLE_TYPE vehicleType) {
        Fleet fleet = new Fleet();
        fleet.setVehicleName(request.getVehicleName());
        fleet.setVehicleNumber(request.getVehicleNumber());
        fleet.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        fleet.setInsuranceDocument(request.getInsuranceDocument());
        fleet.setVehicleType(vehicleType);
        fleet.setImage(request.getImage());
        Driver driver = driverDao.getById(request.getDriverId());
        fleet.setDriver(driver);

        return fleet;
    }

    private Fleet updateFleetFromRequest(UpdateFleetReq request, Fleet fleet) {
        if (!StringUtil.isEmpty(request.getVehicleName())) {
            fleet.setVehicleName(request.getVehicleName());
        }
        if (!StringUtil.isEmpty(request.getVehicleNumber())) {
            fleet.setVehicleNumber(request.getVehicleNumber());
        }
        if (!StringUtil.isEmpty(request.getVehicleType())) {

            fleet.setVehicleType(Fleet.VEHICLE_TYPE.valueOf(request.getVehicleType().toUpperCase()));
        }
        if (request.getInsuranceExpiryDate() != null) {
            fleet.setInsuranceExpiryDate(request.getInsuranceExpiryDate());
        }
        if (request.getImage() != null) {
            fleet.setImage(request.getImage());
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

    public long countTwoWheelers() {
        return fleetDao.countByVehicleType(Fleet.VEHICLE_TYPE.TWO_WHEELER);
    }

    public long countFourWheelers() {
        return fleetDao.countByVehicleType(Fleet.VEHICLE_TYPE.FOUR_WHEELER);
    }

    public ResponseEntity<ResponseStructure<Object>> assignFleet(AssignFleet assignFleet) {
        try {
            Fleet fleet = fetchFleet(assignFleet.getId());
            if (Objects.isNull(fleet)) {
                logger.warn("No fleet found with id: {}", assignFleet.getId());
                return ResponseStructure.errorResponse(null, 404, "Fleet not found with id: " + assignFleet.getId());
            }
            Driver driver = driverDao.getById(assignFleet.getDriverId());
            fleet.setDriver(driver);
            fleet.setFleetAssignDate(LocalDate.now());
            fleetDao.createFleet(fleet);
            logger.info("Fleet updated successfully: {}", fleet.getId());
            return ResponseStructure.successResponse(fleet, "Fleet Assigned successfully on Date " + fleet.getFleetAssignDate());
        } catch (Exception e) {
            logger.error("Error updating fleet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<List<Fleet>>> searchFleetByVehicleNumber(String vehicleNumberSubstring) {
        ResponseStructure responseStructure = new ResponseStructure();
        try {
            List<Fleet> fleets = fleetDao.findFleetsByVehicleNumber(vehicleNumberSubstring);

            if (fleets.isEmpty()) {
                logger.warn("No fleets found with vehicle number containing: {}", vehicleNumberSubstring);
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No fleets found with vehicle number containing: " + vehicleNumberSubstring);
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }

            logger.info("Fleets found with vehicle number containing: {}", vehicleNumberSubstring);
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Fleets found successfully");
            responseStructure.setData(fleets);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error searching for fleets by vehicle number", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}