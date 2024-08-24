package com.ot.moto.service;

import com.ot.moto.dao.*;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.*;
import com.ot.moto.repository.DriverRepository;
import com.ot.moto.repository.PaymentRepository;
import com.ot.moto.util.StringUtil;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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


    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");


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

    public ResponseEntity<ResponseStructure<Object>> uploadJahezReport(Sheet sheet) {
        try {
            int rowStart = 1;
            int rowEnd = sheet.getLastRowNum();
            List<Orders> ordersList = new ArrayList<>();
            for (int i = rowStart; i <= rowEnd; i++) {
                Row row = sheet.getRow(i);

                String cellDate = row.getCell(0).toString();
                System.out.println(cellDate);
                String cellDriverName = row.getCell(1).toString();
                String cellNoOfS1 = row.getCell(2).toString();
                String cellNoOfS2 = row.getCell(3).toString();
                String cellNoOfS3 = row.getCell(4).toString();
                String cellNoOfS4 = row.getCell(5).toString();
                String cellNoOfS5 = row.getCell(6).toString();
                String cellDeliveries = row.getCell(7).toString();
                String cellCodAmount = row.getCell(8).toString();
                String cellCredit = row.getCell(9).toString();
                String cellDebit = row.getCell(10).toString();


                LocalDate cellLocalDate = LocalDate.parse(cellDate, formatter);
                Orders orders = orderDao.checkOrderValid(cellDriverName, cellLocalDate);
                if (Objects.nonNull(orders)) {
                    logger.info("Entry not valid for :" + cellDriverName + "," + cellDate);
                    continue;
                }
                orders = buildOrdersFromCellData(cellLocalDate, cellDriverName, StringUtil.getLong(cellNoOfS1),
                        StringUtil.getLong(cellNoOfS2), StringUtil.getLong(cellNoOfS3), StringUtil.getLong(cellNoOfS4),
                        StringUtil.getLong(cellNoOfS5), StringUtil.getLong(cellDeliveries), Double.parseDouble(cellCodAmount),
                        Double.parseDouble(cellCredit), Double.parseDouble(cellDebit));
                if (Objects.nonNull(orders)) {
                    logger.info("Saving order  :" + orders.getDriverName() + "," + orders.getDate().toString());
                    ordersList.add(orders);
                }
            }

            orderDao.createOrders(ordersList);

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
        createSalaryFromOrders(orders);

        return orders;
    }

    public Driver addDriverDeliveries(Double codAmount, Long deliveries, Driver driver) {
        driver.setAmountPending(driver.getAmountPending() + codAmount);
        driver.setTotalOrders(driver.getTotalOrders() + deliveries);
        driver.setCurrentOrders(deliveries);
        return driverDao.createDriver(driver);
    }

    public Salary createSalaryFromOrders(Orders orders) {
        LocalDate localDate = orders.getDate();
        int year = localDate.getYear();
        int month = localDate.getMonthValue();

        Salary salary = salaryDao.getSalaryByMonthAndYearAndDriver((long) month, (long) year, orders.getDriver());

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

            salary.setTotalEarnings(salary.getS1Earnings() + salary.getS2Earnings() + salary.getS3Earnings()
                    + salary.getS4Earnings() + salary.getS5Earnings());

            salary.setTotalDeductions(0.0);
            salary.setVisaCharges(0.0);
            salary.setOtherCharges(0.0);
            salary.setBonus(0.0);
            salary.setIncentives(0.0);
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

            salary.setTotalEarnings(salary.getS1Earnings() + salary.getS2Earnings() + salary.getS3Earnings()
                    + salary.getS4Earnings() + salary.getS5Earnings());
        }

        salary = salaryDao.saveSalary(salary);
        return salary;
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

                String dateStr = row.getCell(0).toString();
                LocalDate date = LocalDate.parse(dateStr, formatter);

                String phoneNumber = extractPhoneNumber(description);
                logger.info("Extracted phone number: " + phoneNumber);

                Driver driver = driverDao.findByPhoneNumber(phoneNumber);

                if (Objects.nonNull(driver)) {

                    if (paymentRecordExists(driver.getId(), date)) {
                        logger.info("Payment record already exists for driver: " + driver.getPhone() + " on date: " + date + ". Skipping this entry.");
                        continue;
                    }

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

        Pattern pattern = Pattern.compile("/PHONE/(\\d{10})");
        Matcher matcher = pattern.matcher(description);

        if (matcher.find()) {
            String phoneNumber = matcher.group(1);
            logger.info("Extracted phone number: " + phoneNumber);
            return phoneNumber;
        }

        logger.info("No phone number found in description: " + description);
        return null;
    }

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

        Optional<Driver> driverOpt = driverRepository.findByNameIgnoreCase(username);
        if (driverOpt.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver not found.");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }

        String driverPhoneNumber = driverOpt.get().getPhone();

        List<Payment> payments = paymentRepository.findPaymentsByDriverNameContaining(username);

        Pattern phonePattern = Pattern.compile("/PHONE/(\\d+)-");
        List<Payment> filteredPayments = payments.stream()
                .filter(payment -> {
                    Matcher matcher = phonePattern.matcher(payment.getDescription());
                    return matcher.find() && matcher.group(1).equals(driverPhoneNumber);
                })
                .collect(Collectors.toList());

        if (filteredPayments.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No payments found with the matching phone number in the description.");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        }

        responseStructure.setStatus(HttpStatus.OK.value());
        responseStructure.setMessage("Payments retrieved successfully.");
        responseStructure.setData(filteredPayments);
        return new ResponseEntity<>(responseStructure, HttpStatus.OK);
    }
}
