package com.ot.moto.service;

import com.ot.moto.dao.AssetsDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Assets;
import com.ot.moto.entity.Driver;
import com.ot.moto.repository.AssetsRepository;
import com.ot.moto.repository.DriverRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AssetsService {

    @Autowired
    private AssetsDao assetsDao;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private AssetsRepository assetsRepository;

    private static final Logger logger = LoggerFactory.getLogger(AssetsService.class);

    public ResponseEntity<ResponseStructure<Object>> saveAllAssetsForDriver(Long driverId,List<Assets> assetsList) {
        try {
            if (driverId == null) {
                logger.warn("Driver ID is null.");
                return ResponseStructure.errorResponse(null, 400, "Driver ID must be provided");
            }

            Driver driver = driverRepository.findById(driverId)
                    .orElseThrow(() -> new RuntimeException("Driver with ID " + driverId + " not found"));
            if (assetsList == null || assetsList.isEmpty()) {
                logger.warn("Empty assets list provided for Driver ID: " + driverId);
                return ResponseStructure.errorResponse(null, 400, "No assets provided for saving");
            }

            for (Assets asset : assetsList) {
                if (asset.getItem() == null || asset.getItem().trim().isEmpty()) {
                    logger.warn("Asset with missing or empty name found.");
                    return ResponseStructure.errorResponse(null, 400, "Asset name cannot be null or empty");
                }
                if (asset.getQuantity() <= 0) {
                    logger.warn("Asset with invalid quantity found: " + asset.getQuantity());
                    return ResponseStructure.errorResponse(null, 400, "Asset quantity must be greater than zero");
                }
                asset.setDriver(driver);
            }
            List<Assets> savedAssets = assetsRepository.saveAll(assetsList);

            if (savedAssets.isEmpty()) {
                logger.warn("No assets were saved for Driver ID: " + driverId);
                return ResponseStructure.errorResponse(null, 400, "Failed to save assets for driver");
            }

            return ResponseStructure.successResponse(savedAssets, "Assets saved successfully for Driver");

        } catch (RuntimeException e) {
            logger.error("Error saving assets for Driver ID: " + driverId, e);
            return ResponseStructure.errorResponse(null, 404, e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error saving assets for Driver ID: " + driverId, e);
            return ResponseStructure.errorResponse(null, 500, "An unexpected error occurred");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllAssets(int page, int size, String sortBy) {
        try {
            Page<Assets> assetsPage = assetsRepository.findAll(PageRequest.of(page, size, Sort.by(sortBy)));

            if (assetsPage.isEmpty()) {
                logger.warn("No Assets Data found.");
                return ResponseStructure.errorResponse(null, 404, "No Assets Data found");
            }

            return ResponseStructure.successResponse(assetsPage, "Assets Data found");
        } catch (Exception e) {
            logger.error("Error fetching Assets Data", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAssetById(Long id) {
        try {
            Optional<Assets> asset = assetsRepository.findById(id);

            if (asset.isEmpty()) {
                logger.warn("No Asset found with ID: " + id);
                return ResponseStructure.errorResponse(null, 404, "No Asset found");
            }

            return ResponseStructure.successResponse(asset.get(), "Asset found");
        } catch (Exception e) {
            logger.error("Error fetching Asset with ID: " + id, e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}
