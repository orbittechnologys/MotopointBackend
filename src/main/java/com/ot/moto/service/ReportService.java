package com.ot.moto.service;

import com.opencsv.CSVWriter;
import com.ot.moto.dao.*;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.response.DriverAnalysisSum;
import com.ot.moto.entity.*;
import com.ot.moto.repository.*;
import com.ot.moto.util.StringUtil;
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
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ot.moto.entity.Payment.PAYMENT_TYPE.BENEFIT;

@Service
public class ReportService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private SalaryDao salaryDao;

    @Autowired
    private MasterDao masterDao;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private BonusDao bonusDao;

    @Autowired
    private TamDao tamDao;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private TamRepository tamRepository;

    @Autowired
    private OrgReportsRepository orgReportsRepository;


    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");


    public ResponseEntity<ResponseStructure<Object>> uploadJahezReport(Sheet sheet) {
        try {
            int rowStart = 1;
            int rowEnd = sheet.getLastRowNum();
            List<Orders> ordersList = new ArrayList<>();
            for (int i = rowStart; i <= rowEnd; i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    logger.warn("Skipping null row at index: " + i);
                    continue;
                }

                // Check if the cell is empty before parsing
                Cell dateCell = row.getCell(0);
                if (dateCell == null || dateCell.toString().trim().isEmpty()) {
                    logger.warn("Empty date cell at row index: " + i);
                    continue; // Skip this row if the date is missing
                }

                String cellDate = dateCell.toString().trim();
                String cellDriverName = row.getCell(1).toString().trim();
                String cellNoOfS1 = row.getCell(2).toString().trim();
                String cellNoOfS2 = row.getCell(3).toString().trim();
                String cellNoOfS3 = row.getCell(4).toString().trim();
                String cellNoOfS4 = row.getCell(5).toString().trim();
                String cellNoOfS5 = row.getCell(6).toString().trim();
                String cellDeliveries = row.getCell(7).toString().trim();
                String cellCodAmount = row.getCell(8).toString().trim();
                String cellCredit = row.getCell(9).toString().trim();
                String cellDebit = row.getCell(10).toString().trim();

                try {
                    LocalDate cellLocalDate = LocalDate.parse(cellDate, formatter);
                    Orders orders = orderDao.checkOrderValid(cellDriverName, cellLocalDate);
                    if (Objects.nonNull(orders)) {
                        logger.info("Entry not valid for: " + cellDriverName + ", " + cellDate);
                        continue;
                    }
                    orders = buildOrdersFromCellData(cellLocalDate, cellDriverName, StringUtil.getLong(cellNoOfS1),
                            StringUtil.getLong(cellNoOfS2), StringUtil.getLong(cellNoOfS3), StringUtil.getLong(cellNoOfS4),
                            StringUtil.getLong(cellNoOfS5), StringUtil.getLong(cellDeliveries), Double.parseDouble(cellCodAmount),
                            Double.parseDouble(cellCredit), Double.parseDouble(cellDebit));

                    if (Objects.nonNull(orders)) {
                        logger.info("Saving order: " + orders.getDriverName() + ", " + orders.getDate().toString());
                        ordersList.add(orders);
                    }
                } catch (Exception e) {
                    logger.error("Error processing row at index: " + i + " with date: " + cellDate, e);
                    continue;
                }
            }

            ordersList = orderDao.createOrders(ordersList);

            for (Orders orders : ordersList) {
                long totalOrders = orderDao.getTotalOrdersForCurrentMonthByDriver(orders.getDriver().getId());

                // Fetch the highest bonus based on deliveryCount
                Bonus bonusForCount = bonusDao.findTopByDeliveryCountLessThanEqualOrderByDeliveryCountDesc(totalOrders);

                // Fetch the bonus based on specialDate
                Bonus bonusForDate = bonusDao.findTopBySpecialDate(orders.getDate());

                double totalBonus = 0.0;
                if (bonusForCount != null) {
                    totalBonus += bonusForCount.getBonusAmount();
                }
                if (bonusForDate != null) {
                    totalBonus += bonusForDate.getDateBonusAmount();
                }

                // Now, update the driver's bonus in the order or driver entity
                if (totalBonus > 0) {
                    double preBonus = orders.getDriver().getBonus() != null ? orders.getDriver().getBonus() : 0.0;
                    double currentBonus = preBonus + totalBonus;
                    orders.getDriver().setBonus(currentBonus);
                    driverRepository.save(orders.getDriver());
                }
            }

            return ResponseStructure.successResponse(null, "Successfully Parsed");

        } catch (Exception e) {
            logger.error("Error parsing Excel jahez", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private Orders buildOrdersFromCellData(LocalDate date, String driverName, Long noOfS1, Long noOfS2, Long noOfS3, Long noOfS4, Long noOfS5, Long deliveries, Double codAmount, Double credit, Double debit) {

        Driver driver = driverDao.findByNameIgnoreCase(driverName);
        if (Objects.isNull(driver)) {
            return null;
        }

        Orders orders = new Orders();
        orders.setDate(date);
        orders.setDriverName(driverName);
        orders.setNoOfS1(noOfS1);
        orders.setNoOfS2(noOfS2);
        orders.setNoOfS3(noOfS3);
        orders.setNoOfS4(noOfS4);
        orders.setNoOfS5(noOfS5);
        orders.setTotalOrders(deliveries);
        orders.setCodAmount(codAmount);
        orders.setDebit(debit);
        orders.setCredit(credit);
        orders.setDriver(driver);

        addDriverDeliveries(codAmount, deliveries, driver);
        /*createSalaryFromOrders(orders, driver);*/

        return orders;
    }

    public Driver addDriverDeliveries(Double codAmount, Long deliveries, Driver driver) {
        driver.setAmountPending(driver.getAmountPending() + codAmount);
        driver.setCodAmount(Optional.ofNullable(driver.getCodAmount()).orElse(0.0) + codAmount);
        driver.setTotalOrders(driver.getTotalOrders() + deliveries);
        driver.setCurrentOrders(deliveries);
        return driverDao.createDriver(driver);
    }

    public ResponseEntity<ResponseStructure<Object>> uploadBankStatement(Sheet sheet) {
        try {
            int rowStart = 1;
            int rowEnd = sheet.getLastRowNum();
            for (int i = rowStart; i <= rowEnd; i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    logger.warn("Row " + i + " is null. Skipping this row.");
                    continue;
                }

                String description = row.getCell(1).toString();
                double amount = row.getCell(2).getNumericCellValue();
                String paymentType = String.valueOf(BENEFIT);

                String dateStr = row.getCell(0).toString().trim();

                // Ensure the date string is not empty and valid
                if (dateStr.isEmpty()) {
                    logger.warn("Date is missing or empty in row " + i + ". Skipping this row.");
                    continue;
                }

                LocalDate date;
                try {
                    date = LocalDate.parse(dateStr, formatter);
                } catch (DateTimeParseException e) {
                    logger.error("Invalid date format in row " + i + ": " + dateStr + ". Skipping this row.", e);
                    continue;
                }

                String phoneNumber = extractPhoneNumber(description);
                logger.info("Extracted phone number: " + phoneNumber);

                Driver driver = driverDao.findByPhoneNumber(phoneNumber);

                if (Objects.nonNull(driver)) {

                    updateDriverPendingAmount(driver, amount, paymentType, date, description);

                } else {
                    logger.info("No driver found with phone number: " + phoneNumber);
                }
            }

        } catch (Exception e) {
            logger.error("Error parsing bank statement", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
        return ResponseStructure.successResponse(null, "Successfully Processed");
    }

    private boolean paymentRecordExists(Long driverId, LocalDate date) {
        return paymentDao.existsByDriverIdAndDate(driverId, date);
    }

    private String extractPhoneNumber(String description) {
        logger.info("Description being processed: " + description);

        Pattern pattern = Pattern.compile("/PHONE/(\\d{8})");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            String phoneNumber = matcher.group(1);
            logger.info("Extracted phone number: " + phoneNumber);
            return phoneNumber;
        }

        logger.info("No phone number found in description: " + description);
        return null;
    }

    /*To extract the Phone Number where the number start's with 973*/
    /*private String extractPhoneNumber(String description) {
        logger.info("Description being processed: " + description);

        // Update the pattern to capture an 11-digit phone number following /PHONE/
        Pattern pattern = Pattern.compile("/PHONE/(\\d{11})");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            String fullPhoneNumber = matcher.group(1);
            // Remove the first 3 digits and keep only the last 8 digits
            String phoneNumber = fullPhoneNumber.substring(3);
            logger.info("Extracted phone number: " + phoneNumber);
            return phoneNumber;
        }

        logger.info("No phone number found in description: " + description);
        return null;
    }*/

    private void updateDriverPendingAmount(Driver driver, double amount, String paymentType, LocalDate date, String description) {
        double newAmountPending = driver.getAmountPending() - amount;
        driver.setAmountPending(newAmountPending);
        driverDao.createDriver(driver);


        logger.info("Updated pending amount for driver: " + driver.getPhone() + ". New amount pending: " + newAmountPending);

        savePaymentRecord(driver, amount, paymentType, date, description);
    }

    private void savePaymentRecord(Driver driver, double amount, String paymentType, LocalDate date, String description) {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setType(paymentType);
        payment.setDate(date);
        payment.setDriver(driver);
        payment.setDescription(description);

        paymentDao.save(payment);

        logger.info("Saved payment record for driver: " + driver.getPhone() + ". Amount: " + amount + ", Description: " + description);
        updateSalary(driver, date, amount);
    }

    //TODO: to optimise it using hashmap
    public void updateSalary(Driver driver, LocalDate date, Double amount) {
        Salary salary = salaryDao.getSalaryByDriverAndDate(driver, date);

        Double emiAmount =
                (driver.getVisaAmountEmi() != null ? driver.getVisaAmountEmi() : 0.0) +
                        (driver.getBikeRentAmountEmi() != null ? driver.getBikeRentAmountEmi() : 0.0) +
                        (driver.getOtherDeductions() != null ? driver.getOtherDeductions().stream()
                                .mapToDouble(OtherDeduction::getOtherDeductionAmountEmi)
                                .sum() : 0.0);

        Double penaltyAmount = ((driver.getPenalties() != null && !driver.getPenalties().isEmpty()) ?
                driver.getPenalties().stream()
                        .filter(penalty -> penalty.getStatus() == Penalty.PenaltyStatus.NOT_SETTLED)
                        .mapToDouble(Penalty::getAmount)
                        .sum() : 0.0);

        if (Objects.nonNull(salary)) {
            salary.setCodCollected(salary.getCodCollected() + amount);
            salary.setPayableAmount(salary.getPayableAmount() + amount);
            salaryDao.saveSalary(salary);
        } else {
            salary = new Salary();
            salary.setSalaryCreditDate(date);
            salary.setCodCollected(amount);
            salary.setPayableAmount(salary.getPayableAmount() + amount - emiAmount - penaltyAmount);

            salary.setBonus(0.0);
            salary.setIncentives(0.0);
            salary.setMonth((long) date.getMonthValue());
            salary.setYear((long) date.getYear());
            salary.setNoOfS1(0l);
            salary.setNoOfS2(0l);
            salary.setNoOfS3(0l);
            salary.setNoOfS4(0l);
            salary.setNoOfS5(0l);

            salary.setS1Earnings(0.0);
            salary.setS2Earnings(0.0);
            salary.setS3Earnings(0.0);
            salary.setS4Earnings(0.0);
            salary.setS5Earnings(0.0);

            salary.setStatus(Salary.status.NOT_SETTLED.name());
            salary.setTotalDeductions(0.0);
            salary.setTotalEarnings(0.0);
            salary.setTotalOrders(0l);
            salary.setProfit(0.0);
            salary.setFleetPenalty(penaltyAmount);
            salary.setEmiPerDay(emiAmount);
            salary.setDriver(driver);

            salaryDao.saveSalary(salary);
        }
    }

    public Map<String, Double> getTotalAmountByPaymentType() {

        Map<String, Double> totalAmounts = new HashMap<>();
        for (Payment.PAYMENT_TYPE type : Payment.PAYMENT_TYPE.values()) {
            totalAmounts.put(type.name(), 0.0);
        }
        List<Object[]> results = paymentRepository.getTotalAmountByPaymentType();

        for (Object[] result : results) {
            String type = (String) result[0];
            Double total = (Double) result[1];

            try {
                Payment.PAYMENT_TYPE.valueOf(type);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid payment type: " + type);
            }
            totalAmounts.put(type, total);
        }
        return totalAmounts;
    }

    public ResponseEntity<ResponseStructure<List<Payment>>> findPaymentsByDriverUsernameContaining(String username) {
        ResponseStructure<List<Payment>> responseStructure = new ResponseStructure<>();

        List<Payment> payments = paymentRepository.findPaymentsByDriverNameContaining(username);

        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("Payments retrieved successfully.");
        responseStructure.setData(payments);
        return new ResponseEntity<>(responseStructure, HttpStatus.OK);
    }

    public ResponseEntity<InputStreamResource> generateExcelForPayments() {
        try {
            List<Payment> paymentsList = paymentRepository.findAll();
            if (paymentsList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Benefit Reports");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Amount", "Description", "Type", "Date", "Driver Name", "Driver PhoneNumber"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Payment payment : paymentsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(payment.getId());
                row.createCell(1).setCellValue(payment.getAmount());
                row.createCell(2).setCellValue(payment.getDescription());
                row.createCell(3).setCellValue(payment.getType());
                row.createCell(4).setCellValue(payment.getDate() != null ? payment.getDate().format(formatter) : "");
                row.createCell(5).setCellValue(payment.getDriver() != null ? payment.getDriver().getUsername() : "");
                row.createCell(6).setCellValue(payment.getDriver() != null ? payment.getDriver().getPhone() : "");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.xlsx");
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

    public ResponseEntity<ResponseStructure<Object>> getSumForCurrentMonth() {
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());

        logger.info("Fetching total sum for current month from {} to {}", startDate, endDate);

        try {
            Double sum = paymentDao.getSumOfCurrentMonth(startDate, endDate);
            logger.info("Total sum for current month from {} to {}: {}", startDate, endDate, sum);
            return ResponseStructure.successResponse(sum, "Total sum for current month retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching sum for current month from {} to {}", startDate, endDate, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching sum for current month: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getArrearsForToday() {
        LocalDate today = LocalDate.now();

        logger.info("Fetching total arrears for today ({})", today);

        try {
            Double arrearsSum = orderDao.getArrearsForToday();
            logger.info("Total arrears for today ({}): {}", today, arrearsSum);
            return ResponseStructure.successResponse(arrearsSum, "Total arrears for today retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching arrears for today ({})", today, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching arrears for today: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getSumAmountForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);

        logger.info("Fetching total amount for yesterday ({})", yesterday);

        try {
            Double amountSum = paymentDao.getSumAmountForYesterday();
            logger.info("Total amount for yesterday ({}): {}", yesterday, amountSum);
            return ResponseStructure.successResponse(amountSum, "Total amount for yesterday retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching amount for yesterday ({})", yesterday, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching amount for yesterday: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllReport(int page, int size, String field) {
        try {

            Page<Orders> ordersPage = orderDao.findAll(page, size, field);
            if (ordersPage.isEmpty()) {
                logger.warn("No jahez found ");
                return ResponseStructure.errorResponse(null, 404, "No jahez found");
            }
            return ResponseStructure.successResponse(ordersPage, "jahez found");
        } catch (Exception e) {
            logger.error("Error fetching jahez", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllBankstatement(int page, int size, String field) {
        try {
            Page<Payment> payments = paymentDao.findAll(page, size, field);
            if (payments.isEmpty()) {
                logger.warn("No Bank Statement found.");
                return ResponseStructure.errorResponse(null, 404, "No BankStatement found");
            }
            return ResponseStructure.successResponse(payments, "BankStatement  found");
        } catch (Exception e) {
            logger.error("Error fetching BankStatement ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllAnalysis(LocalDate startDate, LocalDate endDate, int page,
                                                                    int size, String field) {
        try {
            Page<Orders> orders = orderDao.findAllAnalysis(startDate, endDate, page, size, field);

            if (orders.isEmpty()) {
                return ResponseStructure.errorResponse(null, 404, "No orders found for the given date range.");
            }

            return ResponseStructure.successResponse(orders, "Orders fetched successfully.");
        } catch (Exception e) {
            logger.error("Error fetching All Orders ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<DriverAnalysisSum>> getAnalysisSum(
            LocalDate startDate, LocalDate endDate) {
        ResponseStructure<DriverAnalysisSum> responseStructure = new ResponseStructure<>();
        try {
            // Fetch paginated orders between date range
            List<Orders> ordersPage = orderDao.findAllAnalysis(startDate, endDate);

            if (ordersPage.isEmpty()) {
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No Order Found Between Date");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }

            // Initialize total sum object with default values
            DriverAnalysisSum totalSum = new DriverAnalysisSum();
            totalSum.setCodAmount(0.0);
            totalSum.setTotalOrders(0.0);
            totalSum.setBonus(0.0);
            totalSum.setPenalties(0.0);
            totalSum.setOther(0.0);
            totalSum.setDriverAmountPending(0.0);
            totalSum.setBike(0.0);
            totalSum.setVisa(0.0);

            // Iterate through all orders to sum driver details
            for (Orders order : ordersPage) {
                Driver driver = driverDao.getById(order.getDriver().getId());

                // Add the COD amount from the driver
                totalSum.setCodAmount(totalSum.getCodAmount() + driver.getCodAmount());

                // Add the total orders
                totalSum.setTotalOrders(totalSum.getTotalOrders() + driver.getTotalOrders());

                // Add the bonus
                totalSum.setBonus(totalSum.getBonus() + driver.getBonus());

                // Sum up penalties for the driver
                double totalPenalties = driver.getPenalties() != null ?
                        driver.getPenalties().stream()
                                .filter(penalty -> penalty.getStatus() == Penalty.PenaltyStatus.NOT_SETTLED)
                                .mapToDouble(Penalty::getAmount)
                                .sum() : 0.0;
                totalSum.setPenalties(totalSum.getPenalties() + totalPenalties);

                // Sum up other deductions
                double totalOtherDeductions = driver.getOtherDeductions().stream()
                        .mapToDouble(OtherDeduction::getOtherDeductionAmountEmi)
                        .sum();
                totalSum.setOther(totalSum.getOther() + totalOtherDeductions);

                // Add the pending amount for the driver
                totalSum.setDriverAmountPending(totalSum.getDriverAmountPending() + driver.getAmountPending());

                totalSum.setBike(totalSum.getBike() + driver.getBikeRentAmountEmi());

                totalSum.setVisa(totalSum.getVisa() + driver.getVisaAmountEmi());
            }

            // Set success response
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("All the Sum Of Driver");
            responseStructure.setData(totalSum);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);

        } catch (Exception e) {
            logger.error("Error fetching driver analysis sum: ", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<List<Orders>>> getDriverAnalysis(Long driverId, LocalDate startDate, LocalDate endDate) {
        ResponseStructure<List<Orders>> responseStructure = new ResponseStructure<>();
        try {
            List<Orders> orders = ordersRepository.findByDriverIdAndOrderDateBetween(driverId, startDate, endDate);

            if (orders.isEmpty()) {
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No orders found for the given date range for the driver");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }
            // Set success response
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Driver orders fetched successfully");
            responseStructure.setData(orders);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching driver analysis sum: ", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<DriverAnalysisSum>> getDriverAnalysisSum(Long driverId, LocalDate startDate, LocalDate endDate) {
        ResponseStructure<DriverAnalysisSum> responseStructure = new ResponseStructure<>();
        try {
            List<Orders> orders = ordersRepository.findByDriverIdAndOrderDateBetween(driverId, startDate, endDate);

            if (orders.isEmpty()) {
                responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
                responseStructure.setMessage("No orders found for the given date range for the driver");
                responseStructure.setData(null);
                return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
            }

            DriverAnalysisSum totalSum = new DriverAnalysisSum();
            totalSum.setCodAmount(0.0);
            totalSum.setTotalOrders(0.0);
            totalSum.setBonus(0.0);
            totalSum.setPenalties(0.0);
            totalSum.setOther(0.0);
            totalSum.setDriverAmountPending(0.0);

            for (Orders order : orders) {
                Driver driver = driverDao.getById(order.getDriver().getId());

                totalSum.setCodAmount(totalSum.getCodAmount() + (driver.getCodAmount() != null ? driver.getCodAmount() : 0.0));
                totalSum.setTotalOrders(totalSum.getTotalOrders() + (driver.getTotalOrders() != null ? driver.getTotalOrders() : 0.0));
                totalSum.setBonus(totalSum.getBonus() + (driver.getBonus() != null ? driver.getBonus() : 0.0));

                double totalPenalties = (driver.getPenalties() != null && !driver.getPenalties().isEmpty()) ?
                        driver.getPenalties().stream()
                                .filter(penalty -> penalty.getStatus() == Penalty.PenaltyStatus.NOT_SETTLED)
                                .mapToDouble(Penalty::getAmount)
                                .sum() : 0.0;
                totalSum.setPenalties(totalSum.getPenalties() + totalPenalties);

                double totalOtherDeductions = driver.getOtherDeductions() != null ? driver.getOtherDeductions().stream()
                        .mapToDouble(OtherDeduction::getOtherDeductionAmountEmi)
                        .sum() : 0.0;
                totalSum.setOther(totalSum.getOther() + totalOtherDeductions);

                totalSum.setDriverAmountPending(totalSum.getDriverAmountPending() + (driver.getAmountPending() != null ? driver.getAmountPending() : 0.0));
            }

            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("sum of Driver analysis fetched successfully.");
            responseStructure.setData(totalSum);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        } catch (Exception e) {
            logger.error("Error fetching sum of driver analysis : ", e);
            responseStructure.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            responseStructure.setMessage(e.getMessage());
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTotalCombinedAmountsForToday() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.plusDays(1).atStartOfDay();

        logger.info("Fetching total amounts for today ({})", today);

        try {
            Double totalAmountPayment = paymentRepository.getTotalAmountForToday(today);

            Double totalPayInAmountTam = tamRepository.getTotalPayInAmountForToday(startOfDay, endOfDay);

            Double totalAmountOrgReports = orgReportsRepository.getTotalAmountForToday(startOfDay, endOfDay);

            // Handle null values if there are no records
            totalAmountPayment = (totalAmountPayment == null) ? 0.0 : totalAmountPayment;
            totalPayInAmountTam = (totalPayInAmountTam == null) ? 0.0 : totalPayInAmountTam;
            totalAmountOrgReports = (totalAmountOrgReports == null) ? 0.0 : totalAmountOrgReports;

            // Add total amounts from Payment and Tam
            Double combinedPaymentAndTamTotal = totalAmountPayment + totalPayInAmountTam;

            // Subtract OrgReports total from the combined total of Payment and Tam
            Double finalResult = combinedPaymentAndTamTotal - totalAmountOrgReports;

            // Combine all amounts and the final result in the response
            Map<String, Double> result = new HashMap<>();
            result.put("totalAmountPayment", totalAmountPayment);
            result.put("totalPayInAmountTam", totalPayInAmountTam);
            result.put("totalAmountOrgReports", totalAmountOrgReports);
            result.put("combinedPaymentAndTamTotal", combinedPaymentAndTamTotal);
            result.put("finalResult", finalResult);

            return ResponseStructure.successResponse(result, "Total amounts for today retrieved and OrgReports subtracted successfully");
        } catch (Exception e) {
            logger.error("Error fetching total amounts for today ({})", today, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching total amounts for today: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findTotalBenefitAmountByDriver(Long driverId) {
        try {
            Driver driver = driverDao.getById(driverId);
            if (Objects.isNull(driver)) {
                return ResponseStructure.errorResponse(null, 404, "Driver Not Fund With ID " + driverId);
            }

                Double totalAmount = paymentDao.findTotalBenefitAmountByDriver(driverId);
                return ResponseStructure.successResponse(totalAmount, "Total Benefit Collected by a Driver Retrieved successfully");
            } catch(Exception e){
                return ResponseStructure.errorResponse(null, 500, "error fetching the total benefit collected by a driver ");
            }
    }

    public  ResponseEntity<ResponseStructure<Object>> findTotalBenefitAmount(){
        try{
            Double totalAmount = paymentDao.findTotalBenefitAmount();
            return ResponseStructure.successResponse(totalAmount,"Total Benefit Amount Fetched successfully ");
        }catch(Exception e){
            return ResponseStructure.errorResponse(null,500,"error fetching the total Benefit of all drivers ");
        }
    }

    public ResponseEntity<InputStreamResource> generateCsvForPaymentsByDriver(Long driverId) {
        try {
            // Fetch the driver by ID
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Fetch all payments for the driver
            List<Payment> payments = paymentRepository.findByDriverId(driverId);
            if (payments.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Create StringWriter and CSVWriter
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            // CSV header
            String[] header = {
                    "Payment ID", "Amount", "Description", "Payment Type", "Date"
            };

            csvWriter.writeNext(header);

            // Write data for each payment
            for (Payment payment : payments) {
                String[] data = {
                        String.valueOf(payment.getId()),
                        String.valueOf(payment.getAmount()),
                        payment.getDescription(),
                        payment.getType(),
                        payment.getDate() != null ? payment.getDate().toString() : ""
                };
                csvWriter.writeNext(data);
            }

            // Close writer and prepare the CSV content
            csvWriter.close();
            String csvContent = writer.toString();

            // Prepare the CSV file as an InputStreamResource for downloading
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(csvContent.getBytes());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvContent.getBytes().length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(resource);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public ResponseEntity<InputStreamResource> generateCsvForPaymentsByDriverAndDateRange(Long driverId, LocalDate startDate, LocalDate endDate) {
        try {
            // Fetch the driver by ID
            Driver driver = driverRepository.findById(driverId).orElse(null);
            if (driver == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Fetch all payments for the driver within the date range
            List<Payment> payments = paymentRepository.findAllByDriverIdAndDateBetween(driverId, startDate, endDate);
            if (payments.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            // Create StringWriter and CSVWriter
            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            // CSV header
            String[] header = {
                    "Payment ID", "Amount", "Description", "Payment Type", "Date"
            };

            csvWriter.writeNext(header);

            // Write data for each payment
            for (Payment payment : payments) {
                String[] data = {
                        String.valueOf(payment.getId()),
                        String.valueOf(payment.getAmount()),
                        payment.getDescription(),
                        payment.getType(),
                        payment.getDate() != null ? payment.getDate().toString() : ""
                };
                csvWriter.writeNext(data);
            }

            // Close writer and prepare the CSV content
            csvWriter.close();
            String csvContent = writer.toString();

            // Prepare the CSV file as an InputStreamResource for downloading
            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(csvContent.getBytes());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.csv");
            headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);

            return ResponseEntity.ok()
                    .headers(headers)
                    .contentLength(csvContent.getBytes().length)
                    .contentType(MediaType.parseMediaType("application/csv"))
                    .body(resource);

        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }


    public ResponseEntity<InputStreamResource> generateExcelForPaymentsDateBetween(LocalDate startDate,LocalDate endDate) {
        try {
            List<Payment> paymentsList = paymentRepository.findAllByDateBetween(startDate,endDate);
            if (paymentsList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Benefit Reports");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "Amount", "Description", "Type", "Date", "Driver Name", "Driver PhoneNumber"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            for (Payment payment : paymentsList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(payment.getId());
                row.createCell(1).setCellValue(payment.getAmount());
                row.createCell(2).setCellValue(payment.getDescription());
                row.createCell(3).setCellValue(payment.getType());
                row.createCell(4).setCellValue(payment.getDate() != null ? payment.getDate().format(formatter) : "");
                row.createCell(5).setCellValue(payment.getDriver() != null ? payment.getDriver().getUsername() : "");
                row.createCell(6).setCellValue(payment.getDriver() != null ? payment.getDriver().getPhone() : "");
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=payments.xlsx");
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

    public ResponseEntity<ResponseStructure<Object>> getAllBankstatementDateBetween(LocalDate startDate,LocalDate endDate,int page, int size, String field) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(field));
            Page<Payment> payments = paymentRepository.findAllByDateBetween(startDate,endDate,pageRequest);
            if (payments.isEmpty()) {
                logger.warn("No Bank Statement found.");
                return ResponseStructure.errorResponse(null, 404, "No BankStatement found");
            }
            return ResponseStructure.successResponse(payments, "BankStatement  found");
        } catch (Exception e) {
            logger.error("Error fetching BankStatement ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getAllBankStatementByDriverIdAndDateBetween(Long driverId, LocalDate startDate, LocalDate endDate, int page, int size, String field) {
        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(field));
            Page<Payment> payments = paymentRepository.findAllByDriverIdAndDateBetween(driverId,startDate,endDate,pageRequest);
            if (payments.isEmpty()) {
                logger.warn("No Bank Statement found.");
                return ResponseStructure.errorResponse(null, 404, "No BankStatement found");
            }
            return ResponseStructure.successResponse(payments, "BankStatement  found");
        } catch (Exception e) {
            logger.error("Error fetching BankStatement ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}
