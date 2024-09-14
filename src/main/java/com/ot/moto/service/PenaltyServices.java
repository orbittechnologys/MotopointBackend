package com.ot.moto.service;

import com.ot.moto.dao.FleetDao;
import com.ot.moto.dao.PenaltyDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreatePenaltyReq;
import com.ot.moto.dto.request.UpdatePenaltyReq;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.Penalty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PenaltyServices {

    @Autowired
    private PenaltyDao penaltyDao;

    @Autowired
    private FleetDao fleetDao;


    private static final Logger logger = LoggerFactory.getLogger(PenaltyServices.class);

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

    public ResponseEntity<ResponseStructure<Object>> updatePenalty(UpdatePenaltyReq updatePenaltyReq) {
        try {
            Penalty existingPenalty = penaltyDao.findById(updatePenaltyReq.getId());
            if (existingPenalty != null) {
                // Set new values from request DTO
                existingPenalty.setDescription(updatePenaltyReq.getDescription());
                existingPenalty.setAmount(updatePenaltyReq.getAmount());

                Penalty updatedPenalty = penaltyDao.save(existingPenalty);
                logger.info("Penalty updated successfully with ID: {}", updatedPenalty.getId());

                return ResponseStructure.successResponse(updatedPenalty, "Penalty updated successfully");
            } else {
                logger.warn("Penalty not found with ID: {}", updatePenaltyReq.getId());
                return ResponseStructure.errorResponse(null, 404, "Penalty not found with ID: " + updatePenaltyReq.getId());
            }
        } catch (Exception e) {
            logger.error("Error updating penalty with ID: {}", updatePenaltyReq.getId(), e);
            return ResponseStructure.errorResponse(null, 500, "Error updating penalty: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> savePenalty(CreatePenaltyReq createPenaltyReq) {
        try {
            // Fetch the fleet associated with the penalty
            Fleet fleet = fleetDao.getFleetById(createPenaltyReq.getFleetId());

            // Check if fleet exists
            if (fleet == null) {
                logger.warn("Fleet not found with ID: {}", createPenaltyReq.getFleetId());
                return ResponseStructure.errorResponse(null, 404, "Fleet does not exist with ID: " + createPenaltyReq.getFleetId());
            }

            // Create a new penalty and associate it with the fleet
            Penalty newPenalty = new Penalty();
            newPenalty.setDescription(createPenaltyReq.getDescription());
            newPenalty.setAmount(createPenaltyReq.getAmount());
            newPenalty.setFleet(fleet);  // Associate the penalty with the fleet

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
}
