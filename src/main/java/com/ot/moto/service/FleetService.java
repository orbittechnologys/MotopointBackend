package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.FleetDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.AssignFleet;
import com.ot.moto.dto.request.CreateFleetReq;
import com.ot.moto.dto.request.UpdateFleetReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.FleetHistory;
import com.ot.moto.repository.FleetHistoryRepository;
import com.ot.moto.util.StringUtil;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class FleetService {

    @Autowired
    private FleetDao fleetDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private FleetHistoryRepository fleetHistoryRepository;

    private static final Logger logger = LoggerFactory.getLogger(FleetService.class);


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
        fleet.setRegistrationCertificate(request.getRegistrationCertificate());
        fleet.setDateOfPurchase(request.getDateOfPurchase());
        Driver driver = driverDao.getById(request.getDriverId());
        fleet.setDriver(driver);

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
        if (!StringUtil.isEmpty(request.getInsuranceDocument())) {
            fleet.setInsuranceDocument(request.getInsuranceDocument());
        }
        if (request.getRegistrationCertificate() != null) {
            fleet.setRegistrationCertificate(request.getRegistrationCertificate());
        }
        if (request.getDateOfPurchase() != null) {
            fleet.setDateOfPurchase(request.getDateOfPurchase());
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

    public long countTwoWheelers() {
        return fleetDao.countByVehicleType(Fleet.VEHICLE_TYPE.TWO_WHEELER);
    }

    public long countFourWheelers() {
        return fleetDao.countByVehicleType(Fleet.VEHICLE_TYPE.FOUR_WHEELER);
    }

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> assignFleet(AssignFleet assignFleet) {
        try {
            Fleet fleet = fetchFleet(assignFleet.getId());
            if (Objects.isNull(fleet)) {
                logger.warn("No fleet found with id: {}", assignFleet.getId());
                return ResponseStructure.errorResponse(null, 404, "Fleet not found with id: " + assignFleet.getId());
            }

            // Unassign fleet from current driver if already assigned to a different driver
            if (fleet.getDriver() != null && !fleet.getDriver().getId().equals(assignFleet.getDriverId())) {
                Driver currentDriver = fleet.getDriver();
                currentDriver.setFleet(null);  // Unlink the fleet from the current driver

                // Update FleetHistory for the current driver (Unassigning)
                FleetHistory lastHistory = fleetHistoryRepository
                        .findByFleetIdAndDriverId(fleet.getId(), currentDriver.getId())
                        .stream()
                        .max(Comparator.comparing(FleetHistory::getFleetAssignDateTime))
                        .orElseThrow(() -> new EntityNotFoundException("History not found"));

                lastHistory.setFleetUnAssignDateTime(LocalDateTime.now());  // Set unassign date and time
                fleetHistoryRepository.save(lastHistory);
            }

            // Fetch the new driver for assignment
            Driver newDriver = driverDao.getById(assignFleet.getDriverId());
            if (newDriver == null) {
                logger.warn("No driver found with id: {}", assignFleet.getDriverId());
                return ResponseStructure.errorResponse(null, 404, "Driver not found with id: " + assignFleet.getDriverId());
            }

            // Assign fleet to the new driver
            fleet.setDriver(newDriver);
            fleet.setFleetAssignDateTime(LocalDateTime.now());  // Set assign date and time

            // Save the new FleetHistory entry
            FleetHistory newHistory = new FleetHistory();
            newHistory.setFleet(fleet);
            newHistory.setDriver(newDriver);
            newHistory.setFleetAssignDateTime(LocalDateTime.now());  // Record the assignment date and time
            newHistory.setProfit("0");  // Profit can be calculated based on your logic
            fleetHistoryRepository.save(newHistory);

            // Save the updated fleet
            fleetDao.createFleet(fleet);

            logger.info("Fleet assigned successfully: {}", fleet.getId());
            return ResponseStructure.successResponse(fleet, "Fleet assigned successfully on Date " + fleet.getFleetAssignDateTime());
        } catch (Exception e) {
            logger.error("Error assigning fleet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    @Transactional
    public ResponseEntity<ResponseStructure<Object>> unassignFleet(Long fleetId) {
        try {
            // Fetch the fleet by its ID
            Fleet fleet = fetchFleet(fleetId);
            if (Objects.isNull(fleet)) {
                logger.warn("No fleet found with id: {}", fleetId);
                return ResponseStructure.errorResponse(null, 404, "Fleet not found with id: " + fleetId);
            }

            // Check if the fleet is already unassigned
            if (fleet.getDriver() == null) {
                logger.warn("Fleet with id {} is already unassigned.", fleetId);
                return ResponseStructure.errorResponse(null, 400, "Fleet is already unassigned.");
            }

            // Get the current driver assigned to the fleet
            Driver currentDriver = fleet.getDriver();

            // Unassign the fleet from the current driver
            fleet.setDriver(null);  // Remove the driver from the fleet
            fleet.setFleetUnAssignDateTime(LocalDateTime.now());  // Set the unassign date and time

            // Save the updated fleet
            fleetDao.createFleet(fleet);

            // Update FleetHistory for the current driver (Unassigning)
            FleetHistory lastHistory = fleetHistoryRepository
                    .findByFleetIdAndDriverId(fleet.getId(), currentDriver.getId())
                    .stream()
                    .max(Comparator.comparing(FleetHistory::getFleetUnAssignDateTime))
                    .orElseThrow(() -> new EntityNotFoundException("History not found"));

            lastHistory.setFleetUnAssignDateTime(LocalDateTime.now());  // Set unassign date and time
            fleetHistoryRepository.save(lastHistory);

            logger.info("Fleet unassigned successfully: {}", fleet.getId());
            return ResponseStructure.successResponse(fleet, "Fleet unassigned successfully on Date " + fleet.getFleetUnAssignDateTime());
        } catch (Exception e) {
            logger.error("Error unassigning fleet", e);
            return ResponseStructure.errorResponse(null, 500, "Error unassigning fleet: " + e.getMessage());
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

    public ResponseEntity<ResponseStructure<Long>> countAssignedTwoWheeler() {
        ResponseStructure<Long> responseStructure = new ResponseStructure<>();
        try {
            long count = fleetDao.countAssignedTwoWheeler();

            if (count == 0) {
                logger.warn("No assigned TWO_WHEELER fleets found");
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No assigned TWO_WHEELER fleets found");
                responseStructure.setData(count);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }

            logger.info("Count of assigned TWO_WHEELER fleets retrieved successfully");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Count retrieved successfully");
            responseStructure.setData(count);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error counting assigned TWO_WHEELER fleets", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<Long>> countAssignedFourWheeler() {
        ResponseStructure<Long> responseStructure = new ResponseStructure<>();
        try {
            long count = fleetDao.countAssignedFourWheeler();

            if (count == 0) {
                logger.warn("No assigned FOUR_WHEELER fleets found");
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No assigned FOUR_WHEELER fleets found");
                responseStructure.setData(count);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }

            logger.info("Count of assigned FOUR_WHEELER fleets retrieved successfully");
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Count retrieved successfully");
            responseStructure.setData(count);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error counting assigned FOUR_WHEELER fleets", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getAllAssignedFleets(int page, int size, String field) {
        try {
            List<Fleet> assignedFleets = fleetDao.findAllAssignedFleets(page, size, field);

            if (assignedFleets.isEmpty()) {
                logger.info("No assigned fleets found.");
                return ResponseStructure.successResponse(assignedFleets, "No assigned fleets found.");
            }

            logger.info("Assigned fleets retrieved successfully. Count: {}", assignedFleets.size());
            return ResponseStructure.successResponse(assignedFleets, "Assigned fleets retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error retrieving assigned fleets", e);
            return ResponseStructure.errorResponse(null, 500, "Error retrieving assigned fleets: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllUnAssignedFleets(int page, int size, String field) {
        try {
            List<Fleet> assignedFleets = fleetDao.findAllUnAssignedFleets(page, size, field);

            if (assignedFleets.isEmpty()) {
                logger.info("No Unassigned fleets found.");
                return ResponseStructure.successResponse(assignedFleets, "No Unassigned fleets found.");
            }

            logger.info("Unassigned fleets retrieved successfully. Count: {}", assignedFleets.size());
            return ResponseStructure.successResponse(assignedFleets, "Unassigned fleets retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error retrieving Unassigned fleets", e);
            return ResponseStructure.errorResponse(null, 500, "Error retrieving Unassigned fleets: " + e.getMessage());
        }
    }

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
}