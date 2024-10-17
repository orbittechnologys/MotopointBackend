package com.ot.moto.service;

import com.ot.moto.dao.*;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.SettleSalV2;
import com.ot.moto.dto.request.SettleSalariesReq;
import com.ot.moto.dto.request.SettleSalary;
import com.ot.moto.entity.*;
import com.ot.moto.repository.SalaryRepository;
import com.ot.moto.repository.SettlementRepository;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SalaryService {

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private MasterDao masterDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private  TamDao tamDao;

    @Autowired
    private SettlementRepository settlementRepository;

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
    public ResponseEntity<ResponseStructure<Object>> settleSalariesV2(SettleSalV2 request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Driver> driverList = salaryRepository.findUniqueDriversBetweenDatesWithStatusNotSettled(startDate,endDate);

        List<Settlement> settlementList = new ArrayList<>();

        for(Driver driver : driverList){
            Settlement settlement = settleSalaryForDriverHelper(driver.getId(), request);
            settlementList.add(settlement);
        }

        return ResponseStructure.successResponse(settlementList, "All settled suiiiii");
    }

    public Settlement settleSalaryForDriverHelper(Long driverId, SettleSalV2 request){
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        HashMap<Long, Settlement.OdDeductions> odMap = new HashMap<>();

        Settlement settlement = new Settlement();

        settlement.setStartDate(request.getStartDate());
        settlement.setEndDate(request.getEndDate());
        settlement.setSettleDateTime(LocalDateTime.now());

        double totalCod = 0.0;
        double totalBenefit = 0.0;
        double totalTam = 0.0;
        double totalOD = 0.0;

        Driver driver = driverDao.getById(driverId);

        settlement.setDriver(driver);

        List<Salary> salaryList = salaryRepository.findByDriverIdAndStatusAndSalaryCreditDateBetween(driverId,"NOT_SETTLED",startDate,endDate);

        if (salaryList.isEmpty()) {
            return null;
        }

        HashMap<Long, Integer> driverCountMap = new HashMap<>();

        List<OtherDeduction> otherDeductionList = driver.getOtherDeductions();


        for (Salary salary : salaryList) {

            if (!driverCountMap.containsKey(driverId)) {
                salary.setBonus(request.getBonus());
                salary.setIncentives(request.getIncentive());
                salary.setPayableAmount(salary.getPayableAmount() + request.getBonus() + request.getIncentive());

                driver.setBonus(Optional.ofNullable(driver.getBonus()).orElse(0.0) + request.getBonus());
                //            driver.setAmountPending(0.0);  // Assuming we set amount pending to zero after settlement
                driver = driverDao.createDriver(driver);  // Save updated driver details

            }
            settlement = incrementSettlement(settlement,salary);
            Orders orders = orderDao.findByDriverAndDate(driver,salary.getSalaryCreditDate());
            if(Objects.nonNull(orders)){
                totalCod +=orders.getCodAmount();
            }

            Payment payment = paymentDao.findByDriverAndDate(driver,salary.getSalaryCreditDate());
            if(Objects.nonNull(payment)){
                totalBenefit += payment.getAmount();
            }


            double tamForTheDay = tamDao.getSumByDriverAndDate(driver,salary.getSalaryCreditDate());
            totalTam+=tamForTheDay;

            LocalDate salaryCreditDate = salary.getSalaryCreditDate();

            if(otherDeductionList.size() > 0 ){
                for(OtherDeduction otherDeduction : otherDeductionList){
                    LocalDate deductionStartDate = otherDeduction.getOtherDeductionAmountStartDate();
                    LocalDate deductionEndDate = otherDeduction.getOtherDeductionAmountEndDate();
                    Settlement.OdDeductions odDeductions = null;
                    if(odMap.containsKey(otherDeduction.getId())){
                        odDeductions = odMap.get(otherDeduction.getId());
                    }else{
                        odDeductions  = new Settlement.OdDeductions();
                    }

                    if ((salaryCreditDate.isAfter(deductionStartDate) || salaryCreditDate.isEqual(deductionStartDate))
                            && (salaryCreditDate.isBefore(deductionEndDate) || salaryCreditDate.isEqual(deductionEndDate))) {
                        totalOD += otherDeduction.getOtherDeductionAmountEmi();
                        odDeductions.setDeductionsPerDay(otherDeduction.getOtherDeductionAmountEmi());
                        odDeductions.setNoOfDays(odDeductions.getNoOfDays() + 1);
                        odDeductions.setDeductionsTotal(odDeductions.getDeductionsPerDay() * odDeductions.getNoOfDays());
                    }
                    if(odDeductions.getDeductionsTotal() > 0 ){
                        odMap.put(otherDeduction.getId(),odDeductions);
                    }
                }
            }

            salary.setStatus(Salary.status.SETTLED.name());
            salary.setSalarySettleDate(LocalDate.now());
            driverCountMap.put(driverId, driverCountMap.getOrDefault(driverId, 0) + 1);
        }
        Master s1Master = masterDao.getMasterBySlab("S1");
        Master s2Master = masterDao.getMasterBySlab("S2");
        Master s3Master = masterDao.getMasterBySlab("S3");
        Master s4Master = masterDao.getMasterBySlab("S4");
        Master s5Master = masterDao.getMasterBySlab("S5");

        double totalEarnings = settlement.getTotalS1() * s1Master.getMotoPaid() +
                settlement.getTotalS2() * s2Master.getMotoPaid() +
                settlement.getTotalS3() * s3Master.getMotoPaid() +
                settlement.getTotalS4() * s4Master.getMotoPaid() +
                settlement.getTotalS5() * s5Master.getMotoPaid() ;

        settlement.setTotalEarnings(totalEarnings);
        settlement.setTotalCod(totalCod);
        settlement.setTotalBenefit(totalBenefit);
        settlement.setTotalTam(totalTam);
        settlement.setTotalOtherDeductions(totalOD);

        List<Settlement.OdDeductions> odDeductions = (List<Settlement.OdDeductions>) odMap.values();
        if(odDeductions.size() > 0){
            settlement.setOdDeductionsList(odDeductions);
        }

        long noOfDaysNotSettled = salaryList.size();

        settlement.setTotalVisaDeductions(driver.getVisaAmountEmi() * noOfDaysNotSettled);
        settlement.setTotalBikeRentDeductions(driver.getBikeRentAmountEmi() * noOfDaysNotSettled);
        salaryDao.saveAll(salaryList);

        settlement = settlementRepository.save(settlement);
        return settlement;
    }

    @Transactional
    public ResponseEntity<ResponseStructure<Object>> settleSalaryForDriver(Long driverId, SettleSalV2 request) {
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        HashMap<Long, Settlement.OdDeductions> odMap = new HashMap<>();

        Settlement settlement = new Settlement();

        settlement.setStartDate(request.getStartDate());
        settlement.setEndDate(request.getEndDate());
        settlement.setSettleDateTime(LocalDateTime.now());

        double totalCod = 0.0;
        double totalBenefit = 0.0;
        double totalTam = 0.0;
        double totalOD = 0.0;

        Driver driver = driverDao.getById(driverId);

        settlement.setDriver(driver);

        List<Salary> salaryList = salaryRepository.findByDriverIdAndStatusAndSalaryCreditDateBetween(driverId,"NOT_SETTLED",startDate,endDate);

        if (salaryList.isEmpty()) {
            return ResponseStructure.errorResponse(null, 404, "No salaries found for the driver between " + startDate + " and " + endDate);
        }

        HashMap<Long, Integer> driverCountMap = new HashMap<>();

        List<OtherDeduction> otherDeductionList = driver.getOtherDeductions();


        for (Salary salary : salaryList) {

            if (!driverCountMap.containsKey(driverId)) {
                salary.setBonus(request.getBonus());
                salary.setIncentives(request.getIncentive());
                salary.setPayableAmount(salary.getPayableAmount() + request.getBonus() + request.getIncentive());

                driver.setBonus(Optional.ofNullable(driver.getBonus()).orElse(0.0) + request.getBonus());
    //            driver.setAmountPending(0.0);  // Assuming we set amount pending to zero after settlement
                driver = driverDao.createDriver(driver);  // Save updated driver details

            }
            settlement = incrementSettlement(settlement,salary);
            Orders orders = orderDao.findByDriverAndDate(driver,salary.getSalaryCreditDate());
            if(Objects.nonNull(orders)){
                totalCod +=orders.getCodAmount();
            }

            Payment payment = paymentDao.findByDriverAndDate(driver,salary.getSalaryCreditDate());
            if(Objects.nonNull(payment)){
                totalBenefit += payment.getAmount();
            }


            double tamForTheDay = tamDao.getSumByDriverAndDate(driver,salary.getSalaryCreditDate());
            totalTam+=tamForTheDay;

            LocalDate salaryCreditDate = salary.getSalaryCreditDate();

            if(otherDeductionList.size() > 0 ){
                for(OtherDeduction otherDeduction : otherDeductionList){
                    LocalDate deductionStartDate = otherDeduction.getOtherDeductionAmountStartDate();
                    LocalDate deductionEndDate = otherDeduction.getOtherDeductionAmountEndDate();
                    Settlement.OdDeductions odDeductions = null;
                    if(odMap.containsKey(otherDeduction.getId())){
                        odDeductions = odMap.get(otherDeduction.getId());
                    }else{
                        odDeductions  = new Settlement.OdDeductions();
                    }

                    if ((salaryCreditDate.isAfter(deductionStartDate) || salaryCreditDate.isEqual(deductionStartDate))
                            && (salaryCreditDate.isBefore(deductionEndDate) || salaryCreditDate.isEqual(deductionEndDate))) {
                        totalOD += otherDeduction.getOtherDeductionAmountEmi();
                        odDeductions.setDeductionsPerDay(otherDeduction.getOtherDeductionAmountEmi());
                        odDeductions.setNoOfDays(odDeductions.getNoOfDays() + 1);
                        odDeductions.setDeductionsTotal(odDeductions.getDeductionsPerDay() * odDeductions.getNoOfDays());
                    }
                    if(odDeductions.getDeductionsTotal() > 0 ){
                        odMap.put(otherDeduction.getId(),odDeductions);
                    }
                }
            }

            salary.setStatus(Salary.status.SETTLED.name());
            salary.setSalarySettleDate(LocalDate.now());
            driverCountMap.put(driverId, driverCountMap.getOrDefault(driverId, 0) + 1);
        }
        Master s1Master = masterDao.getMasterBySlab("S1");
        Master s2Master = masterDao.getMasterBySlab("S2");
        Master s3Master = masterDao.getMasterBySlab("S3");
        Master s4Master = masterDao.getMasterBySlab("S4");
        Master s5Master = masterDao.getMasterBySlab("S5");

        double totalEarnings = settlement.getTotalS1() * s1Master.getMotoPaid() +
                settlement.getTotalS2() * s2Master.getMotoPaid() +
                settlement.getTotalS3() * s3Master.getMotoPaid() +
                settlement.getTotalS4() * s4Master.getMotoPaid() +
                settlement.getTotalS5() * s5Master.getMotoPaid() ;

        settlement.setTotalEarnings(totalEarnings);
        settlement.setTotalCod(totalCod);
        settlement.setTotalBenefit(totalBenefit);
        settlement.setTotalTam(totalTam);
        settlement.setTotalOtherDeductions(totalOD);

        List<Settlement.OdDeductions> odDeductions = (List<Settlement.OdDeductions>) odMap.values();
        if(odDeductions.size() > 0){
            settlement.setOdDeductionsList(odDeductions);
        }

        long noOfDaysNotSettled = salaryList.size();

        settlement.setTotalVisaDeductions(driver.getVisaAmountEmi() * noOfDaysNotSettled);
        settlement.setTotalBikeRentDeductions(driver.getBikeRentAmountEmi() * noOfDaysNotSettled);
        salaryDao.saveAll(salaryList);

        settlement = settlementRepository.save(settlement);

        return ResponseStructure.successResponse(settlement, "Salary settled successfully for the driver.");
    }

    public Settlement incrementSettlement(Settlement settlement, Salary salary){

        settlement.setTotalS1(settlement.getTotalS1() + salary.getNoOfS1());
        settlement.setTotalS2(settlement.getTotalS2() + salary.getNoOfS2());
        settlement.setTotalS3(settlement.getTotalS3() + salary.getNoOfS3());
        settlement.setTotalS4(settlement.getTotalS4() + salary.getNoOfS4());
        settlement.setTotalS5(settlement.getTotalS5() + salary.getNoOfS5());
        settlement.setTotalCashCollected(settlement.getTotalCashCollected() + salary.getCodCollected());
        settlement.setTotalOrders(settlement.getTotalS1() + settlement.getTotalS2() + settlement.getTotalS3()
                + settlement.getTotalS4() + settlement.getTotalS5());

        return settlement;
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
            Double totalNotSettledPayableAmount = salaryRepository.getTotalPayableAmountNotSettledForDriverBetweenSalaryCreditedDate(driver, startDate, endDate);
            Double totalSettledPayableAmount = salaryRepository.getTotalPayableAmountSettledForDriverSalaryCreditedDate(driver, startDate, endDate);

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

    public ResponseEntity<InputStreamResource> generateExcelForSalaries() {
        try {
            List<Salary> salaryList = salaryRepository.findAll();
            if (salaryList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Salary Reports");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Month", "Year", "No. of S1", "No. of S2", "No. of S3", "No. of S4", "No. of S5",
                    "Total Orders", "S1 Earnings", "S2 Earnings", "S3 Earnings", "S4 Earnings", "S5 Earnings",
                    "Total Earnings", "Total Deductions", "Bonus", "Incentives", "Status", "Profit", "EMI per Day",
                    "Fleet Penalty", "Salary Credit Date", "Salary Settle Date", "No. of Days Salary Settled",
                    "Payable Amount", "COD Collected", "Driver Name", "Driver Phone"
            };

            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Fill data rows
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Salary salary : salaryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(salary.getId());
                row.createCell(1).setCellValue(salary.getMonth());
                row.createCell(2).setCellValue(salary.getYear());
                row.createCell(3).setCellValue(salary.getNoOfS1());
                row.createCell(4).setCellValue(salary.getNoOfS2());
                row.createCell(5).setCellValue(salary.getNoOfS3());
                row.createCell(6).setCellValue(salary.getNoOfS4());
                row.createCell(7).setCellValue(salary.getNoOfS5());
                row.createCell(8).setCellValue(salary.getTotalOrders());
                row.createCell(9).setCellValue(salary.getS1Earnings());
                row.createCell(10).setCellValue(salary.getS2Earnings());
                row.createCell(11).setCellValue(salary.getS3Earnings());
                row.createCell(12).setCellValue(salary.getS4Earnings());
                row.createCell(13).setCellValue(salary.getS5Earnings());
                row.createCell(14).setCellValue(salary.getTotalEarnings());
                row.createCell(15).setCellValue(salary.getTotalDeductions());
                row.createCell(16).setCellValue(salary.getBonus());
                row.createCell(17).setCellValue(salary.getIncentives());
                row.createCell(18).setCellValue(salary.getStatus());
                row.createCell(19).setCellValue(salary.getProfit());
                row.createCell(20).setCellValue(salary.getEmiPerDay());
                row.createCell(21).setCellValue(salary.getFleetPenalty());
                row.createCell(22).setCellValue(salary.getSalaryCreditDate() != null ? salary.getSalaryCreditDate().format(formatter) : "");
                row.createCell(23).setCellValue(salary.getSalarySettleDate() != null ? salary.getSalarySettleDate().format(formatter) : "");
                row.createCell(24).setCellValue(salary.getNumberOfDaysSalarySettled());
                row.createCell(25).setCellValue(salary.getPayableAmount());
                row.createCell(26).setCellValue(salary.getCodCollected());
                row.createCell(27).setCellValue(salary.getDriver() != null ? salary.getDriver().getUsername() : "");
                row.createCell(28).setCellValue(salary.getDriver() != null ? salary.getDriver().getPhone() : "");
            }

            // Write workbook to ByteArrayOutputStream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=salaries.xlsx");
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


    public ResponseEntity<ResponseStructure<Object>> getTotalPayableAmountByDriverAndDateRange(Long driverId, LocalDate startDate, LocalDate endDate) {
        try {
            // Fetch all salary records for the specified driver within the date range
            List<Salary> salaryList = salaryRepository.findAllByDriverIdAndSalaryCreditDateBetween(driverId, startDate, endDate);

            if (salaryList == null || salaryList.isEmpty()) {
                logger.warn("No salary records found for driver ID " + driverId + " between " + startDate + " and " + endDate);
                return ResponseStructure.errorResponse(null, 404, "No salary records found for driver between " + startDate + " and " + endDate);
            }

            // Calculate the total payable amount
            double totalPayableAmount = salaryList.stream()
                    .mapToDouble(Salary::getPayableAmount)
                    .sum();

            // Prepare detailed information for each day in the date range, sorted by salaryCreditDate
            List<Map<String, Object>> salaryDetailsList = salaryList.stream()
                    .sorted(Comparator.comparing(Salary::getSalaryCreditDate)) // Sort by salaryCreditDate
                    .map(salary -> {
                        Map<String, Object> salaryDetails = new HashMap<>();
                        salaryDetails.put("date", salary.getSalaryCreditDate());
                        salaryDetails.put("bonus", salary.getBonus());
                        salaryDetails.put("incentives", salary.getIncentives());
                        salaryDetails.put("emiPerDay", salary.getEmiPerDay());
                        salaryDetails.put("fleetPenalty", salary.getFleetPenalty());
                        salaryDetails.put("totalEarnings", salary.getTotalEarnings());
                        salaryDetails.put("status", salary.getStatus());
                        salaryDetails.put("payableAmount", salary.getPayableAmount());
                        return salaryDetails;
                    })
                    .collect(Collectors.toList()); // Collect to list after sorting

            // Construct the response with total payable amount and detailed salary information
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("totalPayableAmount", totalPayableAmount);
            responseData.put("salaryDetails", salaryDetailsList);

            return ResponseStructure.successResponse(responseData, "Total payable amount and salary details found for driver between " + startDate + " and " + endDate);
        } catch (Exception e) {
            logger.error("Error fetching salary details for driver between dates", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching salary details: " + e.getMessage());
        }
    }

}