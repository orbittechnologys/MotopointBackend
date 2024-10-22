package com.ot.moto.service;

import com.ot.moto.dao.*;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.response.UploadOrgResponse;
import com.ot.moto.entity.*;
import com.ot.moto.repository.DriverRepository;
import com.ot.moto.repository.OrgMetricsRepository;
import com.ot.moto.repository.OrgReportsRepository;
import jakarta.transaction.Transactional;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Service
public class OrgReportService {

    @Autowired
    private OrgReportsDao orgReportsDao;

    @Autowired
    private OrgReportsRepository orgReportsRepository;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private MasterDao masterDao;

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private BonusDao bonusDao;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private OrgMetricsRepository orgMetricsRepository;

    @Autowired
    private OtherDeductionDao otherDeductionDao;

    @Autowired
    private PenaltyDao penaltyDao;

    private static final Logger logger = LoggerFactory.getLogger(OrgReportService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);


    @Transactional
    public ResponseEntity<ResponseStructure<Object>> uploadOrgReports(Sheet sheet,String fileName) {
        List<OrgReports> orgReportsList = new ArrayList<>();

        HashMap<String, List<Double>> driverSlabMap = new HashMap<>(); // " jahezId | date " -> [total s1,total s2,total s3,total s4,total s5,totalCOD,totalDebit, totalCredit]
        try {
            Long noOfRowsParsed = 0L;
            Double totalCod = 0.0;
            Long totalDrivers = 0L;
            Double totalCredit = 0.0;
            Double totalDebit = 0.0;
            Long totalS1 = 0L;
            Long totalS2 = 0L;
            Long totalS3 = 0L;
            Long totalS4 = 0L;
            Long totalS5 = 0L;
            Double totalEarnings = 0.0;
            Double profit = 0.0;

            Set<Long> uniqueDrivers = new HashSet<>();

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    logger.warn("Row {} is null, skipping...", i);
                    continue;
                }

                logger.info("Parsing row {}", i);
                OrgReports orgReport = parseRowToOrgReport(row);
                logger.info("Successfully parsed row {}", i);
                logger.info("Org Reports: {}", orgReport);

                if (orgReport == null || !isValidReport(orgReport)) {
                    logger.warn("Invalid or failed to parse row {}, skipping...", i);
                    continue;
                }

                noOfRowsParsed++;
                Double price = orgReport.getPrice();
                Master master = masterDao.getMasterByJahezPaid(price);
                Double debit = orgReport.getDriverDebitAmount();
                Double credit = orgReport.getDriverCreditAmount();

                if (Objects.isNull(master)) {
                    logger.warn("Could not find master for price {}", price);
                    continue;
                }

                Long jahezId = orgReport.getDriverId();
                Driver driver = driverDao.findByJahezId(jahezId);

                // Check if the driver's status is false, if so skip processing the report
                if (driver != null && !driver.isStatus()) {
                    logger.warn("Driver with jahezId {} has status 'false', skipping...", jahezId);
                    continue;
                }

                if (driver != null) {
                    if(!uniqueDrivers.contains(driver.getId())){
                        uniqueDrivers.add(driver.getId());
                    }
                }

                if (Objects.isNull(driver)) {
                    logger.warn("Could not find driver by jahezId {}", jahezId);
                    continue;
                }

                LocalDate date = orgReport.getDispatchTime().toLocalDate();
                String dateStr = date.toString();

                if (isDuplicateReport(orgReport)) {
                    logger.info("Duplicate entry for DID: {} at dispatch time: {}", orgReport.getDid(), orgReport.getDispatchTime());
                    continue;
                }

                String key = jahezId + "|" + dateStr;
                Double codAmount = orgReport.getAmount();

                totalCod += codAmount;

                totalDebit += debit;
                totalCredit += credit;

                if (driverSlabMap.containsKey(key)) {
                    List<Double> slabList = driverSlabMap.get(key);
                    String masterSlab = master.getSlab();
                    slabList.set(5, slabList.get(5) + codAmount);
                    slabList.set(6, slabList.get(6) + debit);
                    slabList.set(7, slabList.get(7) + credit);
                    switch (masterSlab) {
                        case "S1":
                            slabList.set(0, slabList.get(0) + 1);
                            totalS1++;
                            break;
                        case "S2":
                            slabList.set(1, slabList.get(1) + 1);
                            totalS2++;
                            break;
                        case "S3":
                            slabList.set(2, slabList.get(2) + 1);
                            totalS3++;
                            break;
                        case "S4":
                            slabList.set(3, slabList.get(3) + 1);
                            totalS4++;
                            break;
                        case "S5":
                            slabList.set(4, slabList.get(4) + 1);
                            totalS5++;
                            break;
                    }
                } else {
                    List<Double> slabList = Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, codAmount, debit, credit);
                    String masterSlab = master.getSlab();
                    switch (masterSlab) {
                        case "S1":
                            slabList.set(0, 1.0);
                            totalS1++;
                            break;
                        case "S2":
                            slabList.set(1, 1.0);
                            totalS2++;
                            break;
                        case "S3":
                            slabList.set(2, 1.0);
                            totalS3++;
                            break;
                        case "S4":
                            slabList.set(3, 1.0);
                            totalS4++;
                            break;
                        case "S5":
                            slabList.set(4, 1.0);
                            totalS5++;
                            break;
                    }
                    driverSlabMap.put(key, slabList);
                }
                logger.info("Saving report for driver: {} at time: {}", orgReport.getDriverName(), orgReport.getDispatchTime());
                orgReportsList.add(orgReport);
            }

            if (!orgReportsList.isEmpty()) {
                orgReportsDao.saveAll(orgReportsList);
                logger.info("Successfully saved {} reports.", orgReportsList.size());
            } else {
                logger.info("No valid reports to save.");
            }

            processDriverSlabMap(driverSlabMap);

            Master s1Master = masterDao.getMasterBySlab("S1");
            Master s2Master = masterDao.getMasterBySlab("S2");
            Master s3Master = masterDao.getMasterBySlab("S3");
            Master s4Master = masterDao.getMasterBySlab("S4");
            Master s5Master = masterDao.getMasterBySlab("S5");

            Long totalOrders = totalS1 + totalS2 + totalS3 + totalS4 + totalS5;

            Double jahezPaidEarnings = totalS1 * s1Master.getJahezPaid() + totalS2 * s2Master.getJahezPaid()
                    + totalS3 * s3Master.getJahezPaid() + totalS4 * s4Master.getJahezPaid() + totalS5 * s5Master.getJahezPaid();
            Double driverEarnings = totalS1 * s1Master.getMotoPaid() + totalS2 * s2Master.getMotoPaid()
                    + totalS3 * s3Master.getMotoPaid() + totalS4 * s4Master.getMotoPaid() + totalS5 * s5Master.getMotoPaid();

            OrgMetrics orgMetrics = new OrgMetrics();
            orgMetrics.setNoOfRowsParsed(noOfRowsParsed);
            orgMetrics.setTotalCod(totalCod);
            orgMetrics.setTotalOrders(totalOrders);
            orgMetrics.setTotalDrivers((long) uniqueDrivers.size());
            orgMetrics.setTotalCredit(totalCredit);
            orgMetrics.setTotalDebit(totalDebit);
            orgMetrics.setTotalS1(totalS1);
            orgMetrics.setTotalS2(totalS2);
            orgMetrics.setTotalS3(totalS3);
            orgMetrics.setTotalS4(totalS4);
            orgMetrics.setTotalS5(totalS5);
            orgMetrics.setDateTime(LocalDateTime.now());
            orgMetrics.setFileName(fileName);
            orgMetrics.setProfit(jahezPaidEarnings - driverEarnings);
            orgMetrics.setTotalEarnings(driverEarnings);

            orgMetricsRepository.save(orgMetrics);

            UploadOrgResponse uploadOrgResponse = new UploadOrgResponse();
            uploadOrgResponse.setTotalDrivers((long) uniqueDrivers.size());
            uploadOrgResponse.setNoOfRowsParsed(noOfRowsParsed);
            uploadOrgResponse.setTotalCod(totalCod);
            uploadOrgResponse.setTotalCredit(totalCredit);
            uploadOrgResponse.setTotalDebit(totalDebit);
            uploadOrgResponse.setTotalS1(totalS1);
            uploadOrgResponse.setTotalS2(totalS2);
            uploadOrgResponse.setTotalS3(totalS3);
            uploadOrgResponse.setTotalS4(totalS4);
            uploadOrgResponse.setTotalS5(totalS5);
            uploadOrgResponse.setTotalOrders(totalOrders);
            uploadOrgResponse.setTotalEarnings(driverEarnings);
            uploadOrgResponse.setProfit(jahezPaidEarnings - driverEarnings);

            return ResponseStructure.successResponse(uploadOrgResponse, "Successfully Parsed");


        } catch (Exception e) {
            logger.error("Error parsing Excel OrgReports", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private void processDriverSlabMap(HashMap<String, List<Double>> driverSlabMap) {
        for (Map.Entry<String, List<Double>> entry : driverSlabMap.entrySet()) {
            String key = entry.getKey();
            List<Double> slabs = entry.getValue();

            String[] patternArray = key.split("\\|");
            Long jahezId = Long.parseLong(patternArray[0]);
            String dateStr = patternArray[1];

            Driver driver = driverDao.findByJahezId(jahezId);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate localDate = LocalDate.parse(dateStr, formatter);

            Double totalOrders = slabs.get(0) + slabs.get(1) + slabs.get(2) + slabs.get(3) + slabs.get(4);
            Orders orders = buildOrdersFromCellData(localDate, jahezId, driver.getUsername(), //
                    slabs.get(0).longValue(),  // S1 value as Long
                    slabs.get(1).longValue(),  // S2 value as Long
                    slabs.get(2).longValue(),  // S3 value as Long
                    slabs.get(3).longValue(),  // S4 value as Long
                    slabs.get(4).longValue(),  // S5 value as Long
                    totalOrders.longValue(),   // Total orders as Long
                    slabs.get(5),  // COD amount (as Double, assuming no conversion needed)
                    slabs.get(7), slabs.get(6));

        }

    }

    private Orders buildOrdersFromCellData(LocalDate date, Long jahezId, String driverName, Long noOfS1, Long noOfS2, Long noOfS3,
                                           Long noOfS4, Long noOfS5, Long deliveries, Double codAmount, Double credit, Double debit) {

        Driver driver = driverDao.findByJahezId(jahezId);

        if (Objects.isNull(driver)) {
            System.out.println("Driver not found for jahezId: " + jahezId);
            return null;
        }

        Orders existingOrder = orderDao.findByOrderAndDriver(driver.getUsername(), date);

        boolean newRecord = Objects.isNull(existingOrder);

        if (newRecord) {
            System.out.println("Creating a new order for driver: " + driver.getJahezId() + " on date: " + date);
            existingOrder = new Orders();
            existingOrder.setDate(date);
            existingOrder.setDriverName(driverName);
            existingOrder.setNoOfS1(noOfS1);
            existingOrder.setNoOfS2(noOfS2);
            existingOrder.setNoOfS3(noOfS3);
            existingOrder.setNoOfS4(noOfS4);
            existingOrder.setNoOfS5(noOfS5);
            existingOrder.setTotalOrders(deliveries);
            existingOrder.setCodAmount(codAmount);
            existingOrder.setDebit(debit);
            existingOrder.setCredit(credit);
            existingOrder.setDriver(driver);
        } else {
            System.out.println("Updating existing order for driver: " + driver.getJahezId() + " on date: " + date);
            existingOrder.setDate(date);
            existingOrder.setDriverName(driverName);
            existingOrder.setNoOfS1(existingOrder.getNoOfS1() + noOfS1);
            existingOrder.setNoOfS2(existingOrder.getNoOfS2() + noOfS2);
            existingOrder.setNoOfS3(existingOrder.getNoOfS3() + noOfS3);
            existingOrder.setNoOfS4(existingOrder.getNoOfS4() + noOfS4);
            existingOrder.setNoOfS5(existingOrder.getNoOfS5() + noOfS5);
            existingOrder.setTotalOrders(existingOrder.getTotalOrders() + deliveries);
            existingOrder.setCodAmount(existingOrder.getCodAmount() + codAmount);
            existingOrder.setDebit(existingOrder.getDebit() + debit);
            existingOrder.setCredit(existingOrder.getCredit() + credit);
        }

        addDriverDeliveries(codAmount, deliveries, driver);
        createSalaryFromOrders(existingOrder, driver);

        existingOrder = orderDao.save(existingOrder);

        long totalOrders = orderDao.getTotalOrdersForCurrentMonthByDriver(existingOrder.getDriver().getId());

        // Fetch the highest bonus based on delivery count
        Bonus bonusForCount = bonusDao.findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(totalOrders);

        // Fetch the bonus based on specialDate
        Bonus bonusForDate = bonusDao.findTopBySpecialDate(existingOrder.getDate());

        double totalBonus = 0.0;
        if (bonusForCount != null) {
            totalBonus += bonusForCount.getBonusAmount();
        }
        if (bonusForDate != null) {
            totalBonus += bonusForDate.getDateBonusAmount();
        }

        // Now, update the driver's bonus in the order or driver entity
        if (totalBonus > 0) {
            double preBonus = existingOrder.getDriver().getBonus() != null ? existingOrder.getDriver().getBonus() : 0.0;
            double currentBonus = preBonus + totalBonus;
            existingOrder.getDriver().setBonus(currentBonus);
            driverRepository.save(existingOrder.getDriver());
        }

        return existingOrder;
    }

    public Driver addDriverDeliveries(Double codAmount, Long deliveries, Driver driver) {
        driver.setAmountPending(driver.getAmountPending() + codAmount);
        driver.setCodAmount(Optional.ofNullable(driver.getCodAmount()).orElse(0.0) + codAmount);
        driver.setTotalOrders(driver.getTotalOrders() + deliveries);
        driver.setCurrentOrders(deliveries);
        return driverDao.createDriver(driver);
    }

    public Salary createSalaryFromOrders(Orders orders, Driver driver) {
        LocalDate localDate = orders.getDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();

        Salary salary = salaryDao.getSalaryByDriverAndDate(driver, localDate);

        boolean newRecord = Objects.isNull(salary);
        Master s1Master = masterDao.getMasterBySlab("S1");
        Master s2Master = masterDao.getMasterBySlab("S2");
        Master s3Master = masterDao.getMasterBySlab("S3");
        Master s4Master = masterDao.getMasterBySlab("S4");
        Master s5Master = masterDao.getMasterBySlab("S5");

        if (newRecord) {
            salary = new Salary();
            salary.setMonth((long) month);
            salary.setYear((long) year);
            salary.setDriver(orders.getDriver());

            salary.setNoOfS1(orders.getNoOfS1());
            salary.setNoOfS2(orders.getNoOfS2());
            salary.setNoOfS3(orders.getNoOfS3());
            salary.setNoOfS4(orders.getNoOfS4());
            salary.setNoOfS5(orders.getNoOfS5());

            long totalOrders = orders.getNoOfS1() + orders.getNoOfS2() + orders.getNoOfS3() + orders.getNoOfS4() + orders.getNoOfS5();

            salary.setTotalOrders(totalOrders);

            salary.setS1Earnings(s1Master.getMotoPaid() * salary.getNoOfS1());
            salary.setS2Earnings(s2Master.getMotoPaid() * salary.getNoOfS2());
            salary.setS3Earnings(s3Master.getMotoPaid() * salary.getNoOfS3());
            salary.setS4Earnings(s4Master.getMotoPaid() * salary.getNoOfS4());
            salary.setS5Earnings(s5Master.getMotoPaid() * salary.getNoOfS5());

            salary.setTotalEarnings(salary.getS1Earnings() + salary.getS2Earnings() + salary.getS3Earnings() + salary.getS4Earnings() + salary.getS5Earnings());

            Double jahezAmount = s1Master.getJahezPaid() * salary.getNoOfS1() + s2Master.getJahezPaid() * salary.getNoOfS2() + s3Master.getJahezPaid() * salary.getNoOfS3() + s4Master.getJahezPaid() * salary.getNoOfS4() + s5Master.getJahezPaid() * salary.getNoOfS5();
            salary.setProfit(jahezAmount - salary.getTotalEarnings());

            //increasing visaRecievedAmount & bikeRentRecievedAmount if not null

            //OD --> amount recieved + update) logic
            /*Double emiAmount =
                    (driver.getVisaAmountEmi() != null ? driver.getVisaAmountEmi() : 0.0) +
                            (driver.getBikeRentAmountEmi() != null ? driver.getBikeRentAmountEmi() : 0.0) +
                            (driver.getOtherDeductions() != null ? driver.getOtherDeductions().stream()
                                    .mapToDouble(OtherDeduction::getOtherDeductionAmountEmi)
                                    .sum() : 0.0);*/


            Double penaltyAmount = (driver.getPenalties() != null && !driver.getPenalties().isEmpty()) ?
                    driver.getPenalties().stream()
                            .filter(Objects::nonNull)  // Skip null penalties
                            .filter(penalty -> penalty.getStatus() == Penalty.PenaltyStatus.NOT_SETTLED)
                            .mapToDouble(penalty -> penalty.getAmount() != null ? penalty.getAmount() : 0.0)  // Handle null amount
                            .sum()
                    : 0.0;

            if(penaltyAmount > 0){
                settlePenalties(driver);
            }

            double totalEmi = updateDriverEmiAmounts(driver);

            Double payable = salary.getS1Earnings()
                    + salary.getS2Earnings()
                    + salary.getS3Earnings()
                    + salary.getS4Earnings()
                    + salary.getS5Earnings()
                    - totalEmi
                    - penaltyAmount
                    - orders.getCodAmount()
                    + salary.getCodCollected() // codCollectedAdded
                    + orders.getCredit()
                    - orders.getDebit();

            salary.setFleetPenalty(penaltyAmount);
            salary.setEmiPerDay(totalEmi);
            salary.setSalaryCreditDate(orders.getDate());
            salary.setTotalDeductions(totalEmi + penaltyAmount + orders.getCodAmount());
            salary.setBonus(0.0);
            salary.setIncentives(0.0);
            salary.setPayableAmount(payable);
            salary.setStatus("NOT_SETTLED");

        } else {
            salary.setNoOfS1(salary.getNoOfS1() + orders.getNoOfS1());
            salary.setNoOfS2(salary.getNoOfS2() + orders.getNoOfS2());
            salary.setNoOfS3(salary.getNoOfS3() + orders.getNoOfS3());
            salary.setNoOfS4(salary.getNoOfS4() + orders.getNoOfS4());
            salary.setNoOfS5(salary.getNoOfS5() + orders.getNoOfS5());

            long totalOrders = salary.getNoOfS1() + salary.getNoOfS2() + salary.getNoOfS3() + salary.getNoOfS4() + salary.getNoOfS5();


            salary.setTotalOrders(totalOrders);

            salary.setS1Earnings(salary.getS1Earnings() + s1Master.getMotoPaid() * orders.getNoOfS1());
            salary.setS2Earnings(salary.getS2Earnings() + s2Master.getMotoPaid() * orders.getNoOfS2());
            salary.setS3Earnings(salary.getS3Earnings() + s3Master.getMotoPaid() * orders.getNoOfS3());
            salary.setS4Earnings(salary.getS4Earnings() + s4Master.getMotoPaid() * orders.getNoOfS4());
            salary.setS5Earnings(salary.getS5Earnings() + s5Master.getMotoPaid() * orders.getNoOfS5());

            salary.setTotalEarnings(salary.getS1Earnings() + salary.getS2Earnings() + salary.getS3Earnings() + salary.getS4Earnings() + salary.getS5Earnings());

            salary.setSalaryCreditDate(orders.getDate());
            Double jahezAmount = s1Master.getJahezPaid() * salary.getNoOfS1() + s2Master.getJahezPaid() * salary.getNoOfS2() + s3Master.getJahezPaid() * salary.getNoOfS3() + s4Master.getJahezPaid() * salary.getNoOfS4() + s5Master.getJahezPaid() * salary.getNoOfS5();
            salary.setProfit(Optional.ofNullable(salary.getProfit()).orElse(0.0) + jahezAmount - salary.getTotalEarnings());
            Double penaltyAmount = (driver.getPenalties() != null && !driver.getPenalties().isEmpty()) ?
                    driver.getPenalties().stream()
                            .filter(Objects::nonNull)  // Skip null penalties
                            .filter(penalty -> penalty.getStatus() == Penalty.PenaltyStatus.NOT_SETTLED)
                            .mapToDouble(penalty -> penalty.getAmount() != null ? penalty.getAmount() : 0.0)  // Handle null amount
                            .sum()
                    : 0.0;
            salary.setFleetPenalty(penaltyAmount);
            if(penaltyAmount > 0){
                settlePenalties(driver);
            }

            Double payable = salary.getS1Earnings()
                    + salary.getS2Earnings()
                    + salary.getS3Earnings()
                    + salary.getS4Earnings()
                    + salary.getS5Earnings()
                    - salary.getEmiPerDay()
                    - penaltyAmount
                    - orders.getCodAmount()
                    + orders.getCredit()
                    - orders.getDebit();

            salary.setPayableAmount(payable);
        }

        long totalOrders = orderDao.getTotalOrdersForCurrentMonthByDriver(driver.getId());

        Bonus bonusForCount = bonusDao.findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(totalOrders);

        Bonus bonusForDate = bonusDao.findTopBySpecialDate(orders.getDate());

        double totalBonus = 0.0;

        if (bonusForCount != null) {
            totalBonus += bonusForCount.getBonusAmount();
        }
        if (bonusForDate != null) {
            totalBonus += bonusForDate.getDateBonusAmount();
        }
        if (totalBonus > 0) {
            double preBonus = salary.getBonus() != null ? salary.getBonus() : 0.0;
            double currentBonus = preBonus + totalBonus;
            salary.setBonus(currentBonus);
        }

        salary = salaryDao.saveSalary(salary);
        return salary;
    }

    public void settlePenalties(Driver driver){
        List<Penalty> penalties = driver.getPenalties();
        if(penalties.size() > 0){
            for (Penalty penalty : penalties) {
                penalty.setStatus(Penalty.PenaltyStatus.SETTLED); // Set each penalty as SETTLED
            }
            penaltyDao.saveAll(penalties); // Save all updated penalties in a single batch
        }
    }

    public double updateDriverEmiAmounts(Driver driver) {
        double totalEmi = 0.0;
        if(driver.getVisaAmountEmi() != null){
            driver.setVisaAmountReceived(driver.getVisaAmountReceived() + driver.getVisaAmountEmi());
            totalEmi += driver.getVisaAmountEmi();
        }

        if(driver.getBikeRentAmountEmi() != null){
            driver.setBikeRentAmountReceived(driver.getBikeRentAmountReceived() + driver.getBikeRentAmountEmi());
            totalEmi += driver.getBikeRentAmountEmi();
        }

        List<OtherDeduction> otherDeductionList = driver.getOtherDeductions();
        for(OtherDeduction otherDeduction : otherDeductionList){
            if(otherDeduction.getOtherDeductionReceived() < otherDeduction.getOtherDeductionAmount()){
                otherDeduction.setOtherDeductionReceived(otherDeduction.getOtherDeductionReceived() + otherDeduction.getOtherDeductionAmountEmi());
                totalEmi += otherDeduction.getOtherDeductionAmountEmi();
            }
        }
        otherDeductionDao.saveAll(otherDeductionList);
        driverDao.createDriver(driver);
        return totalEmi;
    }

    private boolean isValidReport(OrgReports report) {
        return report.getDriverId() != null && report.getDriverName() != null && report.getDispatchTime() != null;
    }

    private boolean isDuplicateReport(OrgReports orgReport) {
        return orgReportsRepository.findByDidAndDispatchTime(orgReport.getDid(), orgReport.getDispatchTime()) != null;
    }

    private OrgReports parseRowToOrgReport(Row row) {
        try {
            logger.info("parsing row {}", row);
            Long no = parseLong(row.getCell(0));
            Long did = parseLong(row.getCell(1));
            Long refId = parseLong(row.getCell(2));
            String driverName = parseString(row.getCell(3));
            String driverUsername = parseString(row.getCell(4));
            Long driverId = parseLong(row.getCell(5));
            Double amount = parseDouble(row.getCell(6));
            Double price = parseDouble(row.getCell(7));
            Double driverDebitAmount = parseDouble(row.getCell(8));
            Double driverCreditAmount = parseDouble(row.getCell(9));
            Boolean isFreeOrder = parseBoolean(row.getCell(10));
            LocalDateTime dispatchTime = parseDateTime(parseString(row.getCell(11)));
            logger.info("dispatch time {}", dispatchTime);

            String subscriber = parseString(row.getCell(12));
            Boolean driverPaidOrg = parseBoolean(row.getCell(13));
            Boolean orgSettled = parseBoolean(row.getCell(14));
            Boolean driverSettled = parseBoolean(row.getCell(15));

            return buildOrgReport(no, did, refId, driverName, driverUsername, driverId, amount, price, driverDebitAmount,
                    driverCreditAmount, isFreeOrder, dispatchTime, subscriber, driverPaidOrg, orgSettled, driverSettled);
        } catch (Exception e) {
            logger.error("Error parsing row: {}", row.getRowNum(), e);
            return null;
        }
    }

    private OrgReports buildOrgReport(Long no, Long did, Long refId, String driverName, String driverUsername, Long driverId,
                                      Double amount, Double price, Double driverDebitAmount, Double driverCreditAmount,
                                      Boolean isFreeOrder, LocalDateTime dispatchTime, String subscriber, Boolean driverPaidOrg,
                                      Boolean orgSettled, Boolean driverSettled) {

        OrgReports orgReport = new OrgReports();
        orgReport.setNo(no);
        orgReport.setDid(did);
        orgReport.setRefId(refId);
        orgReport.setDriverName(driverName);
        orgReport.setDriverUsername(driverUsername);
        orgReport.setDriverId(Long.valueOf(driverId));
        orgReport.setAmount(amount);
        orgReport.setPrice(price);
        orgReport.setDriverDebitAmount(driverDebitAmount);
        orgReport.setDriverCreditAmount(driverCreditAmount);
        orgReport.setIsFreeOrder(isFreeOrder);
        orgReport.setDispatchTime(dispatchTime);
        orgReport.setSubscriber(subscriber);
        orgReport.setDriverPaidOrg(driverPaidOrg);
        orgReport.setOrgSettled(orgSettled);
        orgReport.setDriverSettled(driverSettled);

        return orgReport;
    }

    private Long parseLong(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            logger.warn("Cell is null or blank, returning null for Long");
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return (long) cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
                return Long.parseLong(cell.getStringCellValue().trim());
            } else {
                logger.warn("Cell at row {} contains non-numeric or empty string, returning null", cell.getRowIndex());
                return null;
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing long from cell at row {}", cell.getRowIndex(), e);
            return null;
        }
    }

    private String parseString(Cell cell) {
        if (cell == null) return null;
        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                default:
                    return cell.toString().trim();
            }
        } catch (Exception e) {
            logger.error("Error parsing string from cell at row {}", cell.getRowIndex(), e);
            return null;
        }
    }

    private Double parseDouble(Cell cell) {
        if (cell == null || cell.getCellType() == CellType.BLANK) {
            logger.warn("Cell is null or blank, returning null");
            return null;
        }
        try {
            if (cell.getCellType() == CellType.NUMERIC) {
                return cell.getNumericCellValue();
            } else if (cell.getCellType() == CellType.STRING && !cell.getStringCellValue().trim().isEmpty()) {
                return Double.parseDouble(cell.getStringCellValue().trim());
            } else {
                logger.warn("Cell at row {} contains non-numeric or empty string", cell.getRowIndex());
                return null;
            }
        } catch (NumberFormatException e) {
            logger.error("Error parsing double from cell at row {}", cell.getRowIndex(), e);
            return null;
        }
    }

    private Boolean parseBoolean(Cell cell) {
        if (cell == null) return null;
        try {
            String cellValue = cell.toString().trim().toLowerCase();
            return "true".equals(cellValue) || "1".equals(cellValue) || "yes".equals(cellValue);
        } catch (Exception e) {
            logger.error("Error parsing boolean from cell at row {}", cell.getRowIndex(), e);
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            logger.warn("Date time string is null or empty");
            return null;
        }

        List<DateTimeFormatter> formatters = Arrays.asList(
                DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d/M/yyyy hh:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("d/M/yyyy HH:mm:ss", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MM-dd-yyyy hh:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMM dd, yyyy hh:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("MMMM d, yyyy hh:mm:ss a", Locale.ENGLISH),
                DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        );

        for (DateTimeFormatter formatter : formatters) {
            try {
                logger.info("Trying to parse date time: {} with format: {}", dateTimeStr, formatter);
                return LocalDateTime.parse(dateTimeStr, formatter);
            } catch (DateTimeParseException e) {
                logger.warn("Failed to parse with format: {}", formatter);
            }
        }

        logger.error("Failed to parse date time with all formats: {}", dateTimeStr);
        return null;
    }


    public ResponseEntity<ResponseStructure<Object>> getAllOrg(int page, int size, String field) {
        try {
            Page<OrgReports> orgReports = orgReportsDao.findAll(page, size, field);
            if (orgReports.isEmpty()) {
                logger.warn("No Staff found.");
                return ResponseStructure.errorResponse(null, 404, "No Driver found");
            }
            return ResponseStructure.successResponse(orgReports, "Driver found");
        } catch (Exception e) {
            logger.error("Error fetching Staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getOrgByDriverID(String driverId) {
        try {
            List<OrgReports> orgReports = orgReportsDao.findByDriverId(Long.valueOf(driverId));
            if (orgReports == null) {
                logger.warn("No OrgReports found for Driver ID: " + driverId);
                return ResponseStructure.errorResponse(null, 404, "No reports found for Driver ID: " + driverId);
            }
            return ResponseStructure.successResponse(orgReports, "Reports found for Driver ID: " + driverId);
        } catch (Exception e) {
            logger.error("Error fetching OrgReports for Driver ID: " + driverId, e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<List<OrgReports>>> findByDriverNameContaining(String name) {
        ResponseStructure<List<OrgReports>> responseStructure = new ResponseStructure<>();

        List<OrgReports> driverList = orgReportsDao.findByDriverNameContaining(name);
        if (driverList.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver Not Found in OrgReports ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Driver Found in OrgReports ");
            responseStructure.setData(driverList);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumForCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        try {
            Double sum = orgReportsDao.getSumOfCurrentMonth(startDateTime, endDateTime);
            return ResponseStructure.successResponse(sum, "Total sum for current month retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error fetching sum for current month: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);
        try {
            Double sum = orgReportsDao.getSumAmountOnDate(startOfDay, endOfDay);
            return ResponseStructure.successResponse(sum, "Total sum for yesterday retrieved successfully");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error fetching sum for yesterday: " + e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> generateExcelForOrgReports() {
        try {
            List<OrgReports> orgReportsList = orgReportsRepository.findAll();
            if (orgReportsList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Org Reports");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "No", "DID", "Ref ID", "Driver Name", "Driver Username", "Driver ID", "Amount", "Price", "Driver Debit Amount", "Driver Credit Amount", "Is Free Order", "Dispatch Time", "Subscriber", "Driver Paid Org", "Org Settled", "Driver Settled"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (OrgReports report : orgReportsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getNo() != null ? report.getNo() : 0);
                row.createCell(2).setCellValue(report.getDid() != null ? report.getDid() : 0);
                row.createCell(3).setCellValue(report.getRefId() != null ? report.getRefId() : 0);
                row.createCell(4).setCellValue(report.getDriverName());
                row.createCell(5).setCellValue(report.getDriverUsername());
                row.createCell(6).setCellValue(report.getDriverId());
                row.createCell(7).setCellValue(report.getAmount() != null ? report.getAmount() : 0.0);
                row.createCell(8).setCellValue(report.getPrice() != null ? report.getPrice() : 0.0);
                row.createCell(9).setCellValue(report.getDriverDebitAmount() != null ? report.getDriverDebitAmount() : 0.0);
                row.createCell(10).setCellValue(report.getDriverCreditAmount() != null ? report.getDriverCreditAmount() : 0.0);
                row.createCell(11).setCellValue(report.getIsFreeOrder() != null && report.getIsFreeOrder() ? "Yes" : "No");
                row.createCell(12).setCellValue(report.getDispatchTime() != null ? report.getDispatchTime().toString() : "");
                row.createCell(13).setCellValue(report.getSubscriber());
                row.createCell(14).setCellValue(report.getDriverPaidOrg() != null && report.getDriverPaidOrg() ? "Yes" : "No");
                row.createCell(15).setCellValue(report.getOrgSettled() != null && report.getOrgSettled() ? "Yes" : "No");
                row.createCell(16).setCellValue(report.getDriverSettled() != null && report.getDriverSettled() ? "Yes" : "No");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=org_reports.xlsx");
            headers1.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok().headers(headers1).contentLength(outputStream.size()).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTopDriverWithHighestAmountForCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);
        try {
            List<Object[]> results = orgReportsDao.findDriverWithHighestAmountForCurrentMonth(startDate, endDate);

            if (results.isEmpty()) {
                return ResponseStructure.errorResponse(null, 404, "No data available for the current month.");
            }
            Object[] topDriver = results.get(0);
            String driverId = (String) topDriver[0];
            String driverName = (String) topDriver[1];
            Double totalAmount = (Double) topDriver[2];

            Driver driver = driverDao.findByNameIgnoreCase(driverName);

            if (driver == null) {
                return ResponseStructure.errorResponse(null, 404, "The driver with name " + driverName + " does not exist in the system.");
            }
            Map<String, Object> responseData = new HashMap<>();
            responseData.put("driverId", driverId);
            responseData.put("driverName", driverName);
            responseData.put("totalAmount", totalAmount);
            responseData.put("profilePic", driver.getProfilePic());

            return ResponseStructure.successResponse(responseData, "Top driver with the highest amount for the current month retrieved successfully.");
        } catch (Exception e) {
            return ResponseStructure.errorResponse(null, 500, "Error fetching the top driver: " + e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> generateExcelForOrgReportsByDriverId(Long driverId) {
        try {
            // Find reports that have the matching driverId
            List<OrgReports> orgReportsList = orgReportsRepository.findByDriverId(driverId);
            if (orgReportsList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Org Reports");

            // Define headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "No", "DID", "Ref ID", "Driver Name", "Driver Username", "Driver ID", "Amount", "Price",
                    "Driver Debit Amount", "Driver Credit Amount", "Is Free Order", "Dispatch Time", "Subscriber",
                    "Driver Paid Org", "Org Settled", "Driver Settled"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            // Fill in data rows
            int rowNum = 1;
            for (OrgReports report : orgReportsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getNo() != null ? report.getNo() : 0);
                row.createCell(2).setCellValue(report.getDid() != null ? report.getDid() : 0);
                row.createCell(3).setCellValue(report.getRefId() != null ? report.getRefId() : 0);
                row.createCell(4).setCellValue(report.getDriverName());
                row.createCell(5).setCellValue(report.getDriverUsername());
                row.createCell(6).setCellValue(report.getDriverId() != null ? report.getDriverId() : 0); // Assuming driverId is part of the OrgReports
                row.createCell(7).setCellValue(report.getAmount() != null ? report.getAmount() : 0.0);
                row.createCell(8).setCellValue(report.getPrice() != null ? report.getPrice() : 0.0);
                row.createCell(9).setCellValue(report.getDriverDebitAmount() != null ? report.getDriverDebitAmount() : 0.0);
                row.createCell(10).setCellValue(report.getDriverCreditAmount() != null ? report.getDriverCreditAmount() : 0.0);
                row.createCell(11).setCellValue(report.getIsFreeOrder() != null && report.getIsFreeOrder() ? "Yes" : "No");
                row.createCell(12).setCellValue(report.getDispatchTime() != null ? report.getDispatchTime().toString() : "");
                row.createCell(13).setCellValue(report.getSubscriber());
                row.createCell(14).setCellValue(report.getDriverPaidOrg() != null && report.getDriverPaidOrg() ? "Yes" : "No");
                row.createCell(15).setCellValue(report.getOrgSettled() != null && report.getOrgSettled() ? "Yes" : "No");
                row.createCell(16).setCellValue(report.getDriverSettled() != null && report.getDriverSettled() ? "Yes" : "No");
            }

            // Write workbook to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=driver_org_reports.xlsx");
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


    public ResponseEntity<InputStreamResource> generateExcelForOrgReportsDateBetween(LocalDate startDate, LocalDate endDate) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay(); // Start of the day
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // End of the day

            List<OrgReports> orgReportsList = orgReportsRepository.findByDispatchTimeBetween(startDateTime, endDateTime);
            if (orgReportsList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Org Reports");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "No", "DID", "Ref ID", "Driver Name", "Driver Username", "Driver ID", "Amount", "Price", "Driver Debit Amount", "Driver Credit Amount", "Is Free Order", "Dispatch Time", "Subscriber", "Driver Paid Org", "Org Settled", "Driver Settled"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (OrgReports report : orgReportsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getNo() != null ? report.getNo() : 0);
                row.createCell(2).setCellValue(report.getDid() != null ? report.getDid() : 0);
                row.createCell(3).setCellValue(report.getRefId() != null ? report.getRefId() : 0);
                row.createCell(4).setCellValue(report.getDriverName());
                row.createCell(5).setCellValue(report.getDriverUsername());
                row.createCell(6).setCellValue(report.getDriverId());
                row.createCell(7).setCellValue(report.getAmount() != null ? report.getAmount() : 0.0);
                row.createCell(8).setCellValue(report.getPrice() != null ? report.getPrice() : 0.0);
                row.createCell(9).setCellValue(report.getDriverDebitAmount() != null ? report.getDriverDebitAmount() : 0.0);
                row.createCell(10).setCellValue(report.getDriverCreditAmount() != null ? report.getDriverCreditAmount() : 0.0);
                row.createCell(11).setCellValue(report.getIsFreeOrder() != null && report.getIsFreeOrder() ? "Yes" : "No");
                row.createCell(12).setCellValue(report.getDispatchTime() != null ? report.getDispatchTime().toString() : "");
                row.createCell(13).setCellValue(report.getSubscriber());
                row.createCell(14).setCellValue(report.getDriverPaidOrg() != null && report.getDriverPaidOrg() ? "Yes" : "No");
                row.createCell(15).setCellValue(report.getOrgSettled() != null && report.getOrgSettled() ? "Yes" : "No");
                row.createCell(16).setCellValue(report.getDriverSettled() != null && report.getDriverSettled() ? "Yes" : "No");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=org_reports.xlsx");
            headers1.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok().headers(headers1).contentLength(outputStream.size()).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<InputStreamResource> generateExcelForOrgReportsDateBetweenForParticularDriver(LocalDate startDate, LocalDate endDate, Long driverId) {
        try {
            LocalDateTime startDateTime = startDate.atStartOfDay(); // Start of the day
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // End of the day

            Driver driver = driverDao.getById(driverId);

            if (Objects.isNull(driver)) {
                logger.warn("Could not find driver by Id {}", driverId);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            List<OrgReports> orgReportsList = orgReportsRepository.findByDispatchTimeBetweenAndDriverId(startDateTime, endDateTime, driver.getJahezId());
            if (orgReportsList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Org Reports");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "No", "DID", "Ref ID", "Driver Name", "Driver Username", "Driver ID", "Amount", "Price", "Driver Debit Amount", "Driver Credit Amount", "Is Free Order", "Dispatch Time", "Subscriber", "Driver Paid Org", "Org Settled", "Driver Settled"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }
            int rowNum = 1;
            for (OrgReports report : orgReportsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(report.getId());
                row.createCell(1).setCellValue(report.getNo() != null ? report.getNo() : 0);
                row.createCell(2).setCellValue(report.getDid() != null ? report.getDid() : 0);
                row.createCell(3).setCellValue(report.getRefId() != null ? report.getRefId() : 0);
                row.createCell(4).setCellValue(report.getDriverName());
                row.createCell(5).setCellValue(report.getDriverUsername());
                row.createCell(6).setCellValue(report.getDriverId());
                row.createCell(7).setCellValue(report.getAmount() != null ? report.getAmount() : 0.0);
                row.createCell(8).setCellValue(report.getPrice() != null ? report.getPrice() : 0.0);
                row.createCell(9).setCellValue(report.getDriverDebitAmount() != null ? report.getDriverDebitAmount() : 0.0);
                row.createCell(10).setCellValue(report.getDriverCreditAmount() != null ? report.getDriverCreditAmount() : 0.0);
                row.createCell(11).setCellValue(report.getIsFreeOrder() != null && report.getIsFreeOrder() ? "Yes" : "No");
                row.createCell(12).setCellValue(report.getDispatchTime() != null ? report.getDispatchTime().toString() : "");
                row.createCell(13).setCellValue(report.getSubscriber());
                row.createCell(14).setCellValue(report.getDriverPaidOrg() != null && report.getDriverPaidOrg() ? "Yes" : "No");
                row.createCell(15).setCellValue(report.getOrgSettled() != null && report.getOrgSettled() ? "Yes" : "No");
                row.createCell(16).setCellValue(report.getDriverSettled() != null && report.getDriverSettled() ? "Yes" : "No");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=org_reports.xlsx");
            headers1.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok().headers(headers1).contentLength(outputStream.size()).contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")).body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getOrgReportsBetweenDates(LocalDate startDate, LocalDate endDate, int page, int size, String field) {
        try {
            // Convert LocalDate to LocalDateTime
            LocalDateTime startDateTime = startDate.atStartOfDay(); // Start of the day
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // End of the day

            // Create PageRequest with sorting
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(field));

            // Fetch the reports
            Page<OrgReports> orgReports = orgReportsRepository.findByDispatchTimeBetween(startDateTime, endDateTime, pageRequest);

            if (orgReports.isEmpty()) {
                logger.warn("No reports found between the specified dates.");
                return ResponseStructure.errorResponse(null, 404, "No reports found between the specified dates");
            }

            return ResponseStructure.successResponse(orgReports, "Reports found between specified dates");
        } catch (Exception e) {
            logger.error("Error fetching reports between dates", e);
            return ResponseStructure.errorResponse(null, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getOrgReportsForDriverBetweenDates(LocalDate startDate, LocalDate endDate, Long driverId, int page, int size, String field) {
        try {
            // Convert LocalDate to LocalDateTime
            LocalDateTime startDateTime = startDate.atStartOfDay(); // Start of the day
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX); // End of the day

            // Create PageRequest with sorting
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(field));

            Driver driver = driverDao.getById(driverId);

            // Fetch the reports for the specified driver
            Page<OrgReports> orgReports = orgReportsRepository.findByDispatchTimeBetweenAndDriverId(startDateTime, endDateTime, driver.getJahezId(), pageRequest);

            if (orgReports.isEmpty()) {
                logger.warn("No reports found for driver ID " + driverId + " between the specified dates.");
                return ResponseStructure.errorResponse(null, 404, "No reports found for the specified driver between the given dates");
            }

            return ResponseStructure.successResponse(orgReports, "Reports found for the specified driver between the given dates");
        } catch (Exception e) {
            logger.error("Error fetching reports for driver between dates", e);
            return ResponseStructure.errorResponse(null, 500, "Internal Server Error: " + e.getMessage());
        }
    }
}