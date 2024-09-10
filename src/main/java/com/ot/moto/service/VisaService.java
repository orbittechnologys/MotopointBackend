package com.ot.moto.service;

import com.ot.moto.dao.VisaDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.VisaRequest;
import com.ot.moto.dto.request.VisaUpdateReq;
import com.ot.moto.entity.Fleet;
import com.ot.moto.entity.Visa;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class VisaService {

    private static final Logger logger = LoggerFactory.getLogger(VisaService.class);

    @Autowired
    private VisaDao visaDao;

    public ResponseEntity<ResponseStructure<Object>> createVisa(VisaRequest request) {
        Visa visa = visaDao.findByVisaName(request.getVisaName());
        if (Objects.isNull(visa)) {
            visa = new Visa();
            visa.setVisaName(request.getVisaName());
            visa = visaDao.save(visa);
            return ResponseStructure.successResponse(visa, "Visa created successfully");
        } else {
            return ResponseStructure.errorResponse(null, 409, "Visa Name already exists");
        }
    }

    public ResponseEntity<ResponseStructure<Object>> updateVisa(VisaUpdateReq request) {

        Visa visa = visaDao.findById(request.getId());
        if (Objects.isNull(visa)) {
            return ResponseStructure.errorResponse(null, 404, "Visa not found");
        }
        Visa existingVisa = visaDao.findByVisaName(request.getVisaName());
        if (existingVisa != null && existingVisa.getId() != visa.getId()) {
            return ResponseStructure.errorResponse(null, 409, "Visa Name already exists");
        }
        visa.setVisaName(request.getVisaName());
        visa = visaDao.save(visa);
        return ResponseStructure.successResponse(visa, "Visa updated successfully");
    }


    public ResponseEntity<ResponseStructure<Object>> findAll() {
        try {
            List<Visa> fleetPage = visaDao.findAll();
            if (fleetPage.isEmpty()) {
                logger.warn("No visa found.");
                return ResponseStructure.errorResponse(null, 404, "No visa found");
            }
            return ResponseStructure.successResponse(fleetPage, "visa found");
        } catch (Exception e) {
            logger.error("Error fetching visas", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> findById(Long id) {
        try {
            Visa visa = visaDao.findById(id);
            if (Objects.isNull(visa)) {
                logger.warn("No visa found. Invalid ID: {}", id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id: " + id);
            }
            return ResponseStructure.successResponse(visa, "visa found");
        } catch (Exception e) {
            logger.error("Error fetching single visa", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getVisaByName(String visaName) {
        try {
            Visa visa = visaDao.findByVisaName(visaName);
            if (visa == null) {
                logger.warn("No Visa found with name: " + visaName);
                return ResponseStructure.errorResponse(null, 404, "Visa not found");
            }
            return ResponseStructure.successResponse(visa, "Visa found");
        } catch (Exception e) {
            logger.error("Error fetching visa by name: " + visaName, e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}