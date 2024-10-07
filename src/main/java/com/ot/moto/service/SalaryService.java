package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.PenaltyDao;
import com.ot.moto.dao.SalaryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.SettleSalV2;
import com.ot.moto.dto.request.SettleSalariesReq;
import com.ot.moto.dto.request.SettleSalary;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Penalty;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private PenaltyDao penaltyDao;

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

    /*@Transactional
    public ResponseEntity<ResponseStructure<Object>> settleSalaries(SettleSalariesReq request) {
        try {
            List<Salary> settledSalaries = new ArrayList<>();

            for (SettleSalary sal : request.getSalaries()) {
                Salary salary = salaryDao.getById(sal.getId());
                if (salary != null) {

                    Double bonus = sal.getBonus() != null ? sal.getBonus() : 0.0;
                    Double incentives = sal.getIncentives() != null ? sal.getIncentives() : 0.0;

                    Double currentBonus = salary.getBonus() != null ? salary.getBonus() : 0.0;
                    Double currentIncentives = salary.getIncentives() != null ? salary.getIncentives() : 0.0;
                    Double emiPerDayCharges = salary.getEmiPerDay() * sal.getNumberOfDaysSalarySettled();
                    Double penaltiesOfDriver = salary.getFleetPenalty();
                    Double driverCODAmount = salary.getDriver().getAmountPending();

                    salary.setBonus(currentBonus + bonus);
                    salary.setIncentives(currentIncentives + incentives);

                    Double settledAmount = salary.getTotalEarnings()
                            - emiPerDayCharges
                            - penaltiesOfDriver
                            - driverCODAmount
                            + (currentIncentives + incentives)
                            + (currentBonus + bonus);

                    salary.setStatus(Salary.status.SETTLED.name());
                    salary.setTotalEarnings(settledAmount);
                    salary.setTotalDeductions(emiPerDayCharges + penaltiesOfDriver + driverCODAmount);

                    Driver driver = driverDao.getById(salary.getDriver().getId());
                    driver.setBonus(Optional.ofNullable(driver.getBonus()).orElse(0.0) + sal.getBonus());
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
    }*/

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> settleSalaries(SettleSalariesReq request) {
        try {
            List<Salary> settledSalaries = new ArrayList<>();

            for (SettleSalary sal : request.getSalaries()) {
                Salary salary = salaryDao.getById(sal.getId());
                if (salary != null) {
                    // Get bonus and incentives from the request (if present) or use defaults
                    Double bonus = sal.getBonus() != null ? sal.getBonus() : 0.0;
                    Double incentives = sal.getIncentives() != null ? sal.getIncentives() : 0.0;

                    // Get the current values from the salary (if present) or default
                    Double currentBonus = salary.getBonus() != null ? salary.getBonus() : 0.0;
                    Double currentIncentives = salary.getIncentives() != null ? salary.getIncentives() : 0.0;

                    // Calculating deductions
                    Double emiPerDayCharges = salary.getEmiPerDay() * sal.getNumberOfDaysSalarySettled();
                    Double penaltiesOfDriver = salary.getFleetPenalty();
                    Double driverCODAmount = salary.getDriver().getAmountPending();

                    // Update bonus and incentives
                    salary.setBonus(currentBonus + bonus);
                    salary.setIncentives(currentIncentives + incentives);

                    // Calculate the total settled amount (after applying deductions)
                    Double settledAmount = salary.getS1Earnings()
                            + salary.getS2Earnings()
                            + salary.getS3Earnings()
                            + salary.getS4Earnings()
                            + salary.getS5Earnings()
                            - emiPerDayCharges
                            - penaltiesOfDriver
                            - driverCODAmount
                            + bonus // Add new bonus
                            + incentives; // Add new incentives

                    salary.setStatus(Salary.status.SETTLED.name());
                    /*salary.setTotalEarnings(settledAmount);*/
                    salary.setTotalDeductions(emiPerDayCharges + penaltiesOfDriver + driverCODAmount);
                    salary.setNumberOfDaysSalarySettled(sal.getNumberOfDaysSalarySettled());
                    salary.setPayableAmount(settledAmount);
                    salary.setSalarySettleDate(LocalDate.now());

                    // Update the driver's bonus
                    Driver driver = driverDao.getById(salary.getDriver().getId());
                    driver.setBonus(Optional.ofNullable(driver.getBonus()).orElse(0.0) + bonus);
                    driver.setAmountPending(0.0);
                    driver = driverDao.createDriver(driver);
                    List<Penalty> penalties = penaltyDao.findByDriverId(driver.getId());
                    if (penalties != null && !penalties.isEmpty()) {
                        for (Penalty penalty : penalties) {
                            penalty.setStatus(Penalty.PenaltyStatus.SETTLED); // Set each penalty as SETTLED
                        }
                        penaltyDao.saveAll(penalties); // Save all updated penalties in a single batch
                    }
                    // Add to the list of settled salaries
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
    public ResponseEntity<ResponseStructure<Object>> settleSalariesV2(SettleSalV2 request){
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();
        HashMap<Long,Integer> driverCountMap = new HashMap<>();

        List<Salary> salaryList = salaryRepository.findBySalaryCreditDateBetween(startDate,endDate);

        for(Salary salary : salaryList){
            Long driverId = salary.getDriver().getId();
            if(!driverCountMap.containsKey(driverId)){
                salary.setBonus(request.getBonus());
                salary.setIncentives(request.getIncentive());
                salary.setPayableAmount(salary.getPayableAmount() + request.getBonus() + request.getIncentive());

                Driver driver = driverDao.getById(salary.getDriver().getId());
                driver.setBonus(Optional.ofNullable(driver.getBonus()).orElse(0.0) + request.getBonus());
                driver.setAmountPending(0.0);
                driver = driverDao.createDriver(driver);

                List<Penalty> penalties = penaltyDao.findByDriverId(driver.getId());
                if (penalties != null && !penalties.isEmpty()) {
                    for (Penalty penalty : penalties) {
                        penalty.setStatus(Penalty.PenaltyStatus.SETTLED); // Set each penalty as SETTLED
                    }
                    penaltyDao.saveAll(penalties); // Save all updated penalties in a single batch
                }
            }
            driverCountMap.put(driverId,driverCountMap.getOrDefault(driverId,0) +1);
            salary.setStatus(Salary.status.SETTLED.name());
            salary.setSalarySettleDate(LocalDate.now());
        }
        salaryDao.saveAll(salaryList);

        return ResponseStructure.successResponse(driverCountMap,"All settled suiiiii");
    }

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> settleSalaryForDriver(Long driverId, SettleSalV2 request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Salary> salaryList = salaryRepository.findByDriverIdAndSalaryCreditDateBetween(driverId, startDate, endDate);

        if (salaryList.isEmpty()) {
            return ResponseStructure.errorResponse(null, 404, "No salaries found for the driver between " + startDate + " and " + endDate);
        }

        HashMap<Long, Integer> driverCountMap = new HashMap<>();

        for (Salary salary : salaryList) {
            salary.setBonus(request.getBonus());
            salary.setIncentives(request.getIncentive());
            salary.setPayableAmount(salary.getPayableAmount() + request.getBonus() + request.getIncentive());

            Driver driver = driverDao.getById(driverId);
            driver.setBonus(Optional.ofNullable(driver.getBonus()).orElse(0.0) + request.getBonus());
            driver.setAmountPending(0.0);  // Assuming we set amount pending to zero after settlement
            driver = driverDao.createDriver(driver);  // Save updated driver details

            List<Penalty> penalties = penaltyDao.findByDriverId(driver.getId());
            if (penalties != null && !penalties.isEmpty()) {
                for (Penalty penalty : penalties) {
                    penalty.setStatus(Penalty.PenaltyStatus.SETTLED);  // Mark each penalty as settled
                }
                penaltyDao.saveAll(penalties);  // Save all updated penalties in batch
            }

            salary.setStatus(Salary.status.SETTLED.name());
            salary.setSalarySettleDate(LocalDate.now());
            driverCountMap.put(driverId, driverCountMap.getOrDefault(driverId, 0) + 1);
        }
        salaryDao.saveAll(salaryList);

        return ResponseStructure.successResponse(driverCountMap, "Salary settled successfully for the driver.");
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

    public ResponseEntity<ResponseStructure<Object>> getAllSalariesBetweenDates(LocalDate startDate, LocalDate endDate, int offset, int pageSize, String field) {
        try {
            Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(field));

            // Fetch only salaries with status "NOT_SETTLED"
            Page<Salary> salaryPage = salaryRepository.findBySalaryCreditDateBetweenAndStatus(startDate, endDate, Salary.status.NOT_SETTLED.name(), pageable);

            if (salaryPage.isEmpty()) {
                logger.warn("No Salaries found between " + startDate + " and " + endDate);
                return ResponseStructure.errorResponse(null, 404, "No salaries found between " + startDate + " and " + endDate);
            }

            return ResponseStructure.successResponse(salaryPage, "salaries found between " + startDate + " and " + endDate);
        } catch (Exception e) {
            logger.error("Error fetching salaries between dates", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findSalariesOfParticularDriver(Long driverId, LocalDate startDate, LocalDate endDate) {
        try {

            List<Salary> salaries = salaryRepository.findByDriverIdAndSalaryCreditDateBetween(driverId, startDate, endDate);

            if (salaries.isEmpty()) {
                logger.warn("No salaries found for driver ID " + driverId + " between " + startDate + " and " + endDate);
                return ResponseStructure.errorResponse(null, 404, "No salaries found for the driver between " + startDate + " and " + endDate);
            }

            return ResponseStructure.successResponse(salaries, "Salaries found for driver between " + startDate + " and " + endDate);
        } catch (Exception e) {
            logger.error("Error fetching salaries for driver ID " + driverId + " between dates " + startDate + " and " + endDate, e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAllBetweenDates(LocalDate startDate, LocalDate endDate, int offset, int pageSize, String field) {
        try {
            Pageable pageable = PageRequest.of(offset, pageSize, Sort.by(field));
            Page<Salary> salaryPage = salaryRepository.findBySalaryCreditDateBetween(startDate, endDate, pageable);

            if (salaryPage.isEmpty()) {
                logger.warn("No Salaries found between " + startDate + " and " + endDate);
                return ResponseStructure.errorResponse(null, 404, "No salaries found between " + startDate + " and " + endDate);
            }

            return ResponseStructure.successResponse(salaryPage, "salaries found between " + startDate + " and " + endDate);
        } catch (Exception e) {
            logger.error("Error fetching salaries between dates", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTotalPayableAmountBetweenDates(
            LocalDate startDate, LocalDate endDate) {
        try {
            Double totalSettledPayableAmount = salaryDao.getTotalPayableAmountSettledBetweenDates(startDate, endDate);
            Double totalNotSettledPayableAmount = salaryDao.getTotalPayableAmountNotSettledBetweenDates(startDate, endDate);

            totalSettledPayableAmount = totalSettledPayableAmount != null ? totalSettledPayableAmount : 0.0;
            totalNotSettledPayableAmount = totalNotSettledPayableAmount != null ? totalNotSettledPayableAmount : 0.0;

            // Combine both results into a map
            Map<String, Object> response = new HashMap<>();
            response.put("totalSettledPayableAmount", totalSettledPayableAmount);
            response.put("totalNotSettledPayableAmount", totalNotSettledPayableAmount);

            if (totalSettledPayableAmount == 0.0 && totalNotSettledPayableAmount == 0.0) {
                return ResponseStructure.errorResponse(response, 404, "No salaries found between " + startDate + " and " + endDate);
            }

            return ResponseStructure.successResponse(response, "Total payable amounts calculated between " + startDate + " and " + endDate);

        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error calculating total payable amounts: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTotalPayableAmountBetweenDatesForParticularDriver(
            Long driverId, LocalDate startDate, LocalDate endDate) {
        try {

            Driver driver = driverDao.getById(driverId);
            Double totalSettledPayableAmount = salaryRepository.getTotalPayableAmountNotSettledForDriverBetweenSalaryCreditedDate(driver, startDate, endDate);
            Double totalNotSettledPayableAmount = salaryRepository.getTotalPayableAmountSettledForDriverSalaryCreditedDate(driver, startDate, endDate);

            totalSettledPayableAmount = totalSettledPayableAmount != null ? totalSettledPayableAmount : 0.0;
            totalNotSettledPayableAmount = totalNotSettledPayableAmount != null ? totalNotSettledPayableAmount : 0.0;

            // Combine both results into a map
            Map<String, Object> response = new HashMap<>();
            response.put("totalSettledPayableAmount", totalSettledPayableAmount);
            response.put("totalNotSettledPayableAmount", totalNotSettledPayableAmount);

            if (totalSettledPayableAmount == 0.0 && totalNotSettledPayableAmount == 0.0) {
                return ResponseStructure.errorResponse(response, 404, "No salaries found between " + startDate + " and " + endDate);
            }

            return ResponseStructure.successResponse(response, "Total payable amounts calculated between " + startDate + " and " + endDate);

        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error calculating total payable amounts: " + e.getMessage());
        }
    }
}