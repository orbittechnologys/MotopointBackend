package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.OrderDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.dto.request.CreateStaffReq;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Orders;
import com.ot.moto.entity.Staff;
import com.ot.moto.util.StringUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ReportService {

    private static final Logger logger = LoggerFactory.getLogger(ReportService.class);

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private DriverDao driverDao;

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


    public ResponseEntity<ResponseStructure<Object>> getAllReport(int page, int size, String field) {
        try {

            Page<Orders> ordersPage = orderDao.findAll(page, size, field);
            if (ordersPage.isEmpty()) {
                logger.warn("No Staff found.");
                return ResponseStructure.errorResponse(null, 404, "No Driver found");
            }
            return ResponseStructure.successResponse(ordersPage, "Driver found");
        } catch (Exception e) {
            logger.error("Error fetching Staff", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }
}