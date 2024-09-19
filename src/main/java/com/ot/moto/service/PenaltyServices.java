package com.ot.moto.service;

import com.ot.moto.dao.FleetDao;
import com.ot.moto.dao.PenaltyDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreatePenaltyReq;
import com.ot.moto.dto.request.UpdatePenaltyReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.Penalty;
import com.ot.moto.repository.DriverRepository;
import com.ot.moto.repository.FleetRepository;
import com.ot.moto.repository.PenaltyRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.api.OpenApiResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PenaltyServices {

    @Autowired
    private PenaltyDao penaltyDao;

    @Autowired
    private FleetDao fleetDao;

    @Autowired
    private PenaltyRepository penaltyRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private FleetRepository fleetRepository;

    private static final Logger logger = LoggerFactory.getLogger(PenaltyServices.class);


    public ResponseEntity<ResponseStructure<Object>> savePenalty(CreatePenaltyReq createPenaltyReq) {
        try {
            // Fetch the fleet associated with the penalty
            Fleet fleet = fleetDao.getFleetById(createPenaltyReq.getFleetId());

            // Check if fleet exists
            if (fleet == null) {
                logger.warn("Fleet not found with ID: {}", createPenaltyReq.getFleetId());
                return ResponseStructure.errorResponse(null, 404, "Fleet does not exist with ID: " + createPenaltyReq.getFleetId());
            }

            // Fetch the driver associated with the fleet
            Driver driver = fleet.getDriver(); // Assuming Fleet has a getDriver() method

            // Create a new penalty and associate it with the fleet and driver
            Penalty newPenalty = new Penalty();
            newPenalty.setDescription(createPenaltyReq.getDescription());
            newPenalty.setAmount(createPenaltyReq.getAmount());
            newPenalty.setFleet(fleet);  // Associate the penalty with the fleet
            newPenalty.setDriver(driver); // Associate the penalty with the driver
            newPenalty.setStatus(Penalty.PenaltyStatus.NOT_SETTLED); // Set status to NOT_SETTLED by default


            // Save the penalty
            Penalty savedPenalty = penaltyDao.save(newPenalty);
            logger.info("Penalty saved successfully with ID: {}", savedPenalty.getId());

            // Return a success response
            return ResponseStructure.successResponse(savedPenalty, "Penalty saved successfully");

        } catch (Exception e) {
            logger.error("Error saving penalty", e);
            return ResponseStructure.errorResponse(null, 500, "Error saving penalty: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> deletePenaltiesByFleetIdAndDriverId(Long fleetId, Long driverId) {
        try {
            // Fetch penalties associated with the fleetId and driverId
            List<Penalty> penalties = penaltyDao.getPenaltiesByFleetIdAndDriverId(fleetId, driverId);

            // Check if any penalties exist
            if (penalties.isEmpty()) {
                logger.warn("No penalties found for Fleet ID: {} and Driver ID: {}", fleetId, driverId);
                return ResponseStructure.errorResponse(null, 404, "No penalties found for Fleet ID: " + fleetId + " and Driver ID: " + driverId);
            }

            // Delete the penalties
            penaltyRepository.deleteAll(penalties);
            logger.info("Penalties deleted successfully for Fleet ID: {} and Driver ID: {}", fleetId, driverId);

            // Return a success response
            return ResponseStructure.successResponse("Penalties deleted successfully", "Penalties deleted successfully for Fleet ID: " + fleetId + " and Driver ID: " + driverId);

        } catch (Exception e) {
            logger.error("Error deleting penalties for Fleet ID: {} and Driver ID: {}", fleetId, driverId, e);
            return ResponseStructure.errorResponse(null, 500, "Error deleting penalties: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> updatePenaltyByFleetIdAndDriverId(UpdatePenaltyReq updatePenaltyReq) {
        try {
            Long fleetId = updatePenaltyReq.getFleetId();
            Long driverId = updatePenaltyReq.getDriverId();

            // Fetch the penalty by fleetId and driverId
            List<Penalty> penalties = penaltyDao.getPenaltiesByFleetIdAndDriverId(fleetId, driverId);

            if (penalties.isEmpty()) {
                logger.warn("No penalties found for Fleet ID: {} and Driver ID: {}", fleetId, driverId);
                return ResponseStructure.errorResponse(null, 404, "No penalties found for Fleet ID: " + fleetId + " and Driver ID: " + driverId);
            }

            // Assuming you're updating the first matched penalty
            Penalty existingPenalty = penalties.get(0);

            // Update fields only if provided
            if (updatePenaltyReq.getDescription() != null && !updatePenaltyReq.getDescription().isEmpty()) {
                existingPenalty.setDescription(updatePenaltyReq.getDescription());
            }

            if (updatePenaltyReq.getAmount() != null) {
                existingPenalty.setAmount(updatePenaltyReq.getAmount());
            }

            // Set status to NOT_SETTLED by default if not provided
            if (updatePenaltyReq.getStatus() == null) {
                existingPenalty.setStatus(Penalty.PenaltyStatus.NOT_SETTLED);
            } else {
                existingPenalty.setStatus(updatePenaltyReq.getStatus());
            }

            // Save the updated penalty
            penaltyDao.save(existingPenalty);
            logger.info("Penalty updated successfully for Fleet ID: {} and Driver ID: {}", fleetId, driverId);

            return ResponseStructure.successResponse(existingPenalty, "Penalty updated successfully");

        } catch (Exception e) {
            logger.error("Error updating penalty for Fleet ID: {} and Driver ID: {}", updatePenaltyReq.getFleetId(), updatePenaltyReq.getDriverId(), e);
            return ResponseStructure.errorResponse(null, 500, "Error updating penalty: " + e.getMessage());
        }
    }



    public ResponseEntity<ResponseStructure<Object>> getPenaltyById(long id) {
        try {
            Penalty penalty = penaltyDao.findById(id);
            if (penalty != null) {
                logger.info("Penalty retrieved successfully with ID: {}", penalty.getId());
                return ResponseStructure.successResponse(penalty, "Penalty retrieved successfully");
            } else {
                logger.warn("Penalty not found with ID: {}", id);
                return ResponseStructure.errorResponse(null, 404, "Penalty not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error fetching penalty with ID: {}", id, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching penalty: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> deletePenaltyById(long id) {
        try {
            Penalty penalty = penaltyDao.findById(id);
            if (penalty != null) {
                penaltyDao.delete(penalty);
                logger.info("Penalty deleted successfully with ID: {}", id);
                return ResponseStructure.successResponse(null, "Penalty deleted successfully");
            } else {
                logger.warn("Penalty not found with ID: {}", id);
                return ResponseStructure.errorResponse(null, 404, "Penalty not found with ID: " + id);
            }
        } catch (Exception e) {
            logger.error("Error deleting penalty with ID: {}", id, e);
            return ResponseStructure.errorResponse(null, 500, "Error deleting penalty: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllPenalties() {
        try {
            List<Penalty> penalties = penaltyDao.findAll();
            logger.info("Retrieved all penalties, total count: {}", penalties.size());

            return ResponseStructure.successResponse(penalties, "Penalties retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching penalties", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching penalties: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getPenaltiesByDriverId(Long driverId, int offset, int pageSize, String field) {
        try {
            Optional<Driver> driver = driverRepository.findById(driverId);
            if (!driver.isPresent()) {
                logger.warn("Driver not found with ID: {}", driverId);
                return ResponseStructure.errorResponse(null, 404, "Driver not found with ID: " + driverId);
            }

            Page<Penalty> penalties = penaltyDao.findByDriverId(driverId, offset, pageSize, field);

            if (penalties.hasContent()) {
                logger.info("Penalties retrieved successfully for driver ID: {}", driverId);
                return ResponseStructure.successResponse(penalties, "Penalties retrieved successfully");
            } else {
                logger.warn("No penalties found for driver ID: {}", driverId);
                return ResponseStructure.errorResponse(null, 404, "No penalties found for driver ID: " + driverId);
            }
        } catch (Exception e) {
            logger.error("Error fetching penalties for driver ID: {}", driverId, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching penalties: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getPenaltiesByFleetId(Long fleetId, int offset, int pageSize, String field) {
        try {
            Optional<Fleet> fleet = fleetRepository.findById(fleetId);
            if (!fleet.isPresent()) {
                logger.warn("Fleet not found with ID: {}", fleetId);
                return ResponseStructure.errorResponse(null, 404, "Fleet not found with ID: " + fleetId);
            }

            Page<Penalty> penalties = penaltyDao.findByFleetId(fleetId, offset, pageSize, field);

            if (penalties.hasContent()) {
                List<Map<String, Object>> penaltyDetails = penalties.getContent().stream().map(penalty -> {
                    Map<String, Object> penaltyMap = new HashMap<>();
                    penaltyMap.put("id", penalty.getId());
                    penaltyMap.put("description", penalty.getDescription());
                    penaltyMap.put("amount", penalty.getAmount());
                    penaltyMap.put("status", penalty.getStatus().name()); // Convert enum to string

                    if (penalty.getFleet() != null) {
                        Map<String, Object> fleetMap = new HashMap<>();
                        fleetMap.put("id", penalty.getFleet().getId());
                        fleetMap.put("vehicleName", penalty.getFleet().getVehicleName());
                        fleetMap.put("vehicleNumber", penalty.getFleet().getVehicleNumber());
                        // Add other fleet fields as needed
                        penaltyMap.put("fleet", fleetMap);
                    }

                    if (penalty.getDriver() != null) {
                        Map<String, Object> driverMap = new HashMap<>();
                        driverMap.put("id", penalty.getDriver().getId());
                        driverMap.put("username", penalty.getDriver().getUsername());
                        driverMap.put("cprNumber", penalty.getDriver().getCprNumber()); // Ensure getter exists
                        driverMap.put("phoneNumber", penalty.getDriver().getPhone()); // Ensure getter exists
                        driverMap.put("jahezId", penalty.getDriver().getJahezId()); // Ensure getter exists
                        // Add other driver fields as needed
                        penaltyMap.put("driver", driverMap);
                    }

                    return penaltyMap;
                }).collect(Collectors.toList());

                logger.info("Penalties retrieved successfully for fleet ID: {}", fleetId);
                return ResponseStructure.successResponse(penaltyDetails, "Penalties retrieved successfully");
            } else {
                logger.warn("No penalties found for fleet ID: {}", fleetId);
                return ResponseStructure.errorResponse(null, 404, "No penalties found for fleet ID: " + fleetId);
            }
        } catch (Exception e) {
            logger.error("Error fetching penalties for fleet ID: {}", fleetId, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching penalties: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> settlePenaltyByDriver(long penaltyId, long driverId) {
        try {
            // Fetch the penalty by its ID using orElseThrow
            Penalty penalty = penaltyRepository.findById(penaltyId)
                    .orElseThrow(() -> new OpenApiResourceNotFoundException("Penalty not found with ID: " + penaltyId));

            // Fetch the driver by its ID using orElseThrow
            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new OpenApiResourceNotFoundException("Driver not found with ID: " + driverId));

            // Check if the penalty is associated with the given driver
            if (!penalty.getDriver().equals(driver)) {
                logger.warn("Driver ID: {} is not associated with Penalty ID: {}", driverId, penaltyId);
                return ResponseStructure.errorResponse(null, 403, "Driver is not authorized to settle this penalty");
            }

            // Update the penalty status to SETTLED
            penalty.setStatus(Penalty.PenaltyStatus.SETTLED);

            // Save the updated penalty
            Penalty updatedPenalty = penaltyRepository.save(penalty);
            logger.info("Penalty settled successfully by Driver ID: {} with Penalty ID: {}", driverId, updatedPenalty.getId());

            // Return a success response
            return ResponseStructure.successResponse(updatedPenalty, "Penalty settled successfully");

        } catch (OpenApiResourceNotFoundException e) {
            logger.warn(e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 404, e.getMessage());

        } catch (Exception e) {
            logger.error("Error settling penalty", e);
            return ResponseStructure.errorResponse(null, 500, "Error settling penalty: " + e.getMessage());
        }
    }
}