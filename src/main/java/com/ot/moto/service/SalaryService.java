package com.ot.moto.service;

import com.ot.moto.dao.SalaryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Salary;
import com.ot.moto.repository.SalaryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class SalaryService {

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private SalaryRepository salaryRepository;

    private static final Logger logger = LoggerFactory.getLogger(SalaryService.class);

    public ResponseEntity<ResponseStructure<Object>> getSalaryById(Long id) {
        try {
            Salary salary = salaryDao.getById(id);
            if (Objects.isNull(salary)) {
                logger.warn("No Salary found. Invalid ID:" + id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + id);
            }
            return ResponseStructure.successResponse(salary, "Salary found");
        } catch (Exception e) {
            logger.error("Error fetching Salary", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAll(int page, int size, String field) {
        try {

            Page<Salary> driverPage = salaryDao.findAll(page, size, field);
            if (driverPage.isEmpty()) {
                logger.warn("No Salary found.");
                return ResponseStructure.errorResponse(null, 404, "No Salary found");
            }
            return ResponseStructure.successResponse(driverPage, "Salary found");
        } catch (Exception e) {
            logger.error("Error fetching Salary", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}
