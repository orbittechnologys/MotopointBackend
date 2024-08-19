package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.OrderDao;
import com.ot.moto.dao.PaymentDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateStaffReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Orders;
import com.ot.moto.entity.Payment;
import com.ot.moto.entity.Staff;
import com.ot.moto.repository.PaymentRepository;
import com.ot.moto.util.StringUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class ReportService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private PaymentDao paymentDao;

    @Autowired
    private PaymentRepository paymentRepository;


    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");


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
                String paymentType = row.getCell(3).toString();

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

                    updateDriverPendingAmount(driver, amount, paymentType, date);

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


    private void updateDriverPendingAmount(Driver driver, double amount, String paymentType, LocalDate date) {
        double newAmountPending = driver.getAmountPending() - amount;
        driver.setAmountPending(newAmountPending);
        driverDao.createDriver(driver);

        logger.info("Updated pending amount for driver: " + driver.getPhone() + ". New amount pending: " + newAmountPending);

        savePaymentRecord(driver, amount, paymentType, date);
    }

    private void savePaymentRecord(Driver driver, double amount, String paymentType, LocalDate date) {
        Payment payment = new Payment();
        payment.setAmount(amount);
        payment.setType(paymentType);
        payment.setDate(date);
        payment.setDriver(driver);

        paymentDao.save(payment);
        logger.info("Saved payment record for driver: " + driver.getPhone() + ". Amount: " + amount);
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

        return orders;
    }

    public Driver addDriverDeliveries(Double codAmount, Long deliveries, Driver driver) {
        driver.setAmountPending(driver.getAmountPending() + codAmount);
        driver.setTotalOrders(driver.getTotalOrders() + deliveries);
        driver.setCurrentOrders(deliveries);
        return driverDao.createDriver(driver);
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
            totalAmounts.put(type, total);
        }
        return totalAmounts;
    }
}