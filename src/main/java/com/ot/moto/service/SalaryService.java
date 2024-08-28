package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.SalaryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.SettleSalary;
import com.ot.moto.dto.request.SettleSalariesReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import com.ot.moto.repository.SalaryRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private DriverDao driverDao;

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

    public ResponseEntity<ResponseStructure<Object>> HighestBonus() {
        try {
            List<Salary> salaryList = salaryRepository.findHighestBonus();
            if (salaryList == null || salaryList.isEmpty()) {
                logger.warn("No Salary with a bonus found.");
                return ResponseStructure.errorResponse(null, 404, "No Salary with a bonus found.");
            }

            Double highestBonus = salaryList.get(0).getBonus();

            List<Salary> highestBonusSalaries = salaryList.stream()
                    .filter(salary -> highestBonus.equals(salary.getBonus()))
                    .collect(Collectors.toList());

            logger.info("Salaries with the highest bonus found. Number of drivers: {}", highestBonusSalaries.size());
            highestBonusSalaries.forEach(salary -> logger.info("Driver: {}, Bonus: {}",
                    salary.getDriver().getUsername(), salary.getBonus()));

            return ResponseStructure.successResponse(highestBonusSalaries, "Salaries with highest bonus found");
        } catch (Exception e) {
            logger.error("Error fetching salaries with highest bonus", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching salaries with highest bonus: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findByDriverUsernameContaining(String username) {
        try {
            List<Salary> salaries = salaryDao.findByDriverUsernameContaining(username);
            if (salaries.isEmpty()) {
                logger.warn("No salary records found for driver name : {}", username);
                return ResponseStructure.errorResponse(null, 404, "No salary records found for the given driver name.");
            }
            logger.info("Successfully retrieved salary records for driver name: {}", username);
            return ResponseStructure.successResponse(salaries, "Salary records found");
        } catch (Exception e) {
            logger.error("Error fetching salary records for driver name: {}", username, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching salary records: " + e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> generateExcelForSalary() {
        try {
            List<Salary> salaryList = salaryRepository.findAll();
            if (salaryList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Salary Data");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Month", "Year", "NoOfS1", "NoOfS2", "NoOfS3", "NoOfS4", "NoOfS5",
                    "TotalOrders", "S1Earnings", "S2Earnings", "S3Earnings", "S4Earnings", "S5Earnings",
                    "TotalEarnings", "TotalDeductions", "VisaCharges", "OtherCharges", "Bonus", "Incentives", "Status", "DriverUsername"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Salary salary : salaryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(salary.getId());
                row.createCell(1).setCellValue(salary.getMonth() != null ? salary.getMonth() : 0);
                row.createCell(2).setCellValue(salary.getYear() != null ? salary.getYear() : 0);
                row.createCell(3).setCellValue(salary.getNoOfS1() != null ? salary.getNoOfS1() : 0);
                row.createCell(4).setCellValue(salary.getNoOfS2() != null ? salary.getNoOfS2() : 0);
                row.createCell(5).setCellValue(salary.getNoOfS3() != null ? salary.getNoOfS3() : 0);
                row.createCell(6).setCellValue(salary.getNoOfS4() != null ? salary.getNoOfS4() : 0);
                row.createCell(7).setCellValue(salary.getNoOfS5() != null ? salary.getNoOfS5() : 0);
                row.createCell(8).setCellValue(salary.getTotalOrders() != null ? salary.getTotalOrders() : 0);
                row.createCell(9).setCellValue(salary.getS1Earnings() != null ? salary.getS1Earnings() : 0.0);
                row.createCell(10).setCellValue(salary.getS2Earnings() != null ? salary.getS2Earnings() : 0.0);
                row.createCell(11).setCellValue(salary.getS3Earnings() != null ? salary.getS3Earnings() : 0.0);
                row.createCell(12).setCellValue(salary.getS4Earnings() != null ? salary.getS4Earnings() : 0.0);
                row.createCell(13).setCellValue(salary.getS5Earnings() != null ? salary.getS5Earnings() : 0.0);
                row.createCell(14).setCellValue(salary.getTotalEarnings() != null ? salary.getTotalEarnings() : 0.0);
                row.createCell(15).setCellValue(salary.getTotalDeductions() != null ? salary.getTotalDeductions() : 0.0);
                row.createCell(16).setCellValue(salary.getVisaCharges() != null ? salary.getVisaCharges() : 0.0);
                row.createCell(17).setCellValue(salary.getOtherCharges() != null ? salary.getOtherCharges() : 0.0);
                row.createCell(18).setCellValue(salary.getBonus() != null ? salary.getBonus() : 0.0);
                row.createCell(19).setCellValue(salary.getIncentives() != null ? salary.getIncentives() : 0.0);
                row.createCell(20).setCellValue(salary.getStatus());
                row.createCell(21).setCellValue(salary.getDriver() != null ? salary.getDriver().getUsername() : "");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=salary_data.xlsx");
            headers1.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers1)
                    .contentLength(outputStream.size())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumOfNotSettledSalaries() {
        try {
            Double totalNotSettledSalaries = salaryDao.getSumOfNotSettledSalaries();

            if (totalNotSettledSalaries == null) {
                logger.warn("Sum of not settled salaries is null.");
                return ResponseStructure.errorResponse(null, 404, "Sum of not settled salaries not found.");
            }

            logger.info("Successfully calculated sum of not settled salaries: {}", totalNotSettledSalaries);
            return ResponseStructure.successResponse((Object) totalNotSettledSalaries, "Sum of not settled salaries calculated successfully");

        } catch (Exception e) {
            logger.error("Error calculating sum of not settled salaries", e);
            return ResponseStructure.errorResponse(null, 500, "Error calculating sum of not settled salaries: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumOfSettledSalaries() {
        try {
            Double totalSettledSalaries = salaryDao.getSumOfSettledSalaries();

            if (totalSettledSalaries == null) {
                logger.warn("Sum of settled salaries is null.");
                return ResponseStructure.errorResponse(null, 404, "Sum of settled salaries not found.");
            }

            logger.info("Successfully calculated sum of settled salaries: {}", totalSettledSalaries);
            return ResponseStructure.successResponse((Object) totalSettledSalaries, "Sum of settled salaries calculated successfully");

        } catch (Exception e) {
            logger.error("Error calculating sum of settled salaries", e);
            return ResponseStructure.errorResponse(null, 500, "Error calculating sum of settled salaries: " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> settleSalaries(SettleSalariesReq request) {
        try {
            List<Salary> settledSalaries = new ArrayList<>();

            for (SettleSalary sal : request.getSalaries()) {
                Salary salary = salaryDao.getById(sal.getId());
                if (salary != null) {

                    Double visaCharges = sal.getVisaCharges() != null ? sal.getVisaCharges() : 0.0;
                    Double otherCharges = sal.getOtherCharges() != null ? sal.getOtherCharges() : 0.0;
                    Double bonus = sal.getBonus() != null ? sal.getBonus() : 0.0;
                    Double incentives = sal.getIncentives() != null ? sal.getIncentives() : 0.0;


                    Double currentVisaCharges = salary.getVisaCharges() != null ? salary.getVisaCharges() : 0.0;
                    Double currentOtherCharges = salary.getOtherCharges() != null ? salary.getOtherCharges() : 0.0;
                    Double currentBonus = salary.getBonus() != null ? salary.getBonus() : 0.0;
                    Double currentIncentives = salary.getIncentives() != null ? salary.getIncentives() : 0.0;


                    salary.setVisaCharges(currentVisaCharges + visaCharges);
                    salary.setOtherCharges(currentOtherCharges + otherCharges);
                    salary.setBonus(currentBonus + bonus);
                    salary.setIncentives(currentIncentives + incentives);


                    Double settledAmount = salary.getTotalEarnings()
                            - (currentVisaCharges + visaCharges)
                            - (currentOtherCharges + otherCharges)
                            + (currentIncentives + incentives)
                            + (currentBonus + bonus);

                    salary.setStatus(Salary.status.SETTLED.name());
                    salary.setTotalEarnings(settledAmount);
                    salary.setTotalDeductions(
                            (currentVisaCharges + visaCharges) + (currentOtherCharges + otherCharges)
                    );

                    /*Set Driver Salary to Zero When Status is SETTLED*/
                    Driver driver = driverDao.getById(salary.getDriver().getId());
                    driver.setSalaryAmount(0.0);
                    driver.setBonus(driver.getBonus() + sal.getBonus());
                    driverDao.createDriver(driver);

                    settledSalaries.add(salary);
                } else {
                    logger.warn("No Salary found. Invalid ID: " + sal.getId());
                }
            }

            if (!settledSalaries.isEmpty()) {
                salaryDao.saveAll(settledSalaries);
            }

            return ResponseStructure.successResponse(settledSalaries, "Salaries settled successfully");
        } catch (Exception e) {
            logger.error("Error settling Salaries", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    @Transactional
    public void saveAllSalaries(List<Salary> salaries) {
        for (int i = 0; i < salaries.size(); i++) {
            salaryDao.saveSalary(salaries.get(i));
            if (i % 50 == 0) { // Adjust the batch size according to your need
                salaryDao.flush();
                salaryDao.clear();
            }
        }
    }
}