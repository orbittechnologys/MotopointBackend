package com.ot.moto.service;

import com.opencsv.CSVWriter;
import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.OrderDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Orders;
import com.ot.moto.repository.OrdersRepository;
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
import java.io.StringWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrdersRepository ordersRepository;

    @Autowired
    private DriverDao driverDao;

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);


    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        logger.info("Fetching total order count for yesterday ({})", yesterday);

        try {
            long orderCount = orderDao.sumTotalOrdersOnDate(yesterday);
            logger.info("Total orders for yesterday: {}", orderCount);
            return ResponseStructure.successResponse(orderCount, "Total orders for yesterday retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching order count for yesterday", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching order count for yesterday: " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForMonth(int year, int month) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        logger.info("Fetching total order count for the month ({}) of year ({}) from ({}) to ({})", month, year, startOfMonth, endOfMonth);

        try {
            long orderCount = orderDao.sumTotalOrdersBetweenDates(startOfMonth, endOfMonth);
            logger.info("Total orders for month {} of year {}: {}", month, year, orderCount);
            return ResponseStructure.successResponse(orderCount, "Total orders for month retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching order count for month {} of year {}", month, year, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching order count for month " + month + " of year " + year + ": " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAll(int page, int size, String field) {
        logger.info("Fetching orders with page number {}, page size {}, sorted by field {}", page, size, field);

        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(field));
            Page<Orders> ordersPage = ordersRepository.findAll(pageRequest);

            if (ordersPage.isEmpty()) {
                logger.warn("No Orders found for page number {} and page size {}", page, size);
                return ResponseStructure.errorResponse(null, 404, "No Orders found");
            }
            logger.info("Successfully fetched orders for page number {} and page size {}", page, size);
            return ResponseStructure.successResponse(ordersPage, "Orders found successfully");
        } catch (Exception e) {
            logger.error("Error fetching orders: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching orders: " + e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> generateCsvForOrders() {
        try {
            List<Orders> allOrders = ordersRepository.findAll();
            if (allOrders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }

            StringWriter writer = new StringWriter();
            CSVWriter csvWriter = new CSVWriter(writer);

            String[] header = {"Order ID", "Driver Name", "Date", "No of S1", "No of S2", "No of S3", "No of S4", "No of S5",
                    "Total Orders", "COD Amount", "Credit", "Debit", "Driver ID"};

            csvWriter.writeNext(header);

            for (Orders order : allOrders) {
                String[] data = {
                        String.valueOf(order.getId()),
                        order.getDriverName(),
                        order.getDate() != null ? order.getDate().toString() : "",
                        String.valueOf(order.getNoOfS1()),
                        String.valueOf(order.getNoOfS2()),
                        String.valueOf(order.getNoOfS3()),
                        String.valueOf(order.getNoOfS4()),
                        String.valueOf(order.getNoOfS5()),
                        String.valueOf(order.getTotalOrders()),
                        String.valueOf(order.getCodAmount()),
                        String.valueOf(order.getCredit()),
                        String.valueOf(order.getDebit()),
                        order.getDriver() != null ? String.valueOf(order.getDriver().getId()) : ""
                };
                csvWriter.writeNext(data);
            }

            csvWriter.close();
            String csvContent = writer.toString();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(csvContent.getBytes());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=orders.csv");
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

    public ResponseEntity<ResponseStructure<List<Orders>>> findByDriverNameContaining(String letter) {
        ResponseStructure<List<Orders>> responseStructure = new ResponseStructure<>();
        List<Orders> orders = orderDao.findByDriverNameContaining(letter);
        if (orders.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("No orders found with name containing letter '" + letter + "'");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("orders found with name containing letter '" + letter + "'");
            responseStructure.setData(orders);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }

/*    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForAllDrivers() {
        try {
            List<Object[]> results = ordersRepository.findTotalOrdersForAllDrivers();

            if (results.isEmpty()) {
                logger.warn("No data available for total orders of drivers.");
                return ResponseStructure.errorResponse(null, 404, "No data available for total orders of drivers.");
            }
            Map<String, Object> responseData = new HashMap<>();
            for (Object[] result : results) {
                Long driverId = (Long) result[0];
                String driverName = (String) result[1];
                String profilePic = (String) result[2];
                Long totalOrders = (Long) result[3];

                Map<String, Object> driverData = new HashMap<>();
                driverData.put("driverId", driverId);
                driverData.put("driverName", driverName);
                driverData.put("profilePic", profilePic);
                driverData.put("totalOrders", totalOrders);

                responseData.put(driverId.toString(), driverData);
            }

            if (responseData.isEmpty()) {
                logger.warn("No drivers found in the system.");
                return ResponseStructure.errorResponse(null, 404, "No drivers found in the system.");
            }

            logger.info("Total orders for all drivers retrieved successfully.");
            return ResponseStructure.successResponse(responseData, "Total orders for all drivers retrieved successfully.");

        } catch (Exception e) {
            logger.error("Error fetching total orders for all drivers: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching total orders for all drivers: " + e.getMessage());
        }
    }*/

    public ResponseEntity<ResponseStructure<Object>> getTotalOrdersForAllDrivers() {
        try {
            List<Object[]> results = ordersRepository.findTotalOrdersForAllDrivers();

            if (results.isEmpty()) {
                logger.warn("No data available for total orders of drivers.");
                return ResponseStructure.errorResponse(null, 404, "No data available for total orders of drivers.");
            }

            // Change the responseData to a list instead of a map
            List<Map<String, Object>> responseData = new ArrayList<>();
            for (Object[] result : results) {
                Long driverId = (Long) result[0];
                String driverName = (String) result[1];
                String profilePic = (String) result[2];
                Long totalOrders = (Long) result[3];

                Map<String, Object> driverData = new HashMap<>();
                driverData.put("driverId", driverId);
                driverData.put("driverName", driverName);
                driverData.put("profilePic", profilePic);
                driverData.put("totalOrders", totalOrders);

                responseData.add(driverData);  // Add each driver's data to the list
            }

            if (responseData.isEmpty()) {
                logger.warn("No drivers found in the system.");
                return ResponseStructure.errorResponse(null, 404, "No drivers found in the system.");
            }

            logger.info("Total orders for all drivers retrieved successfully.");
            return ResponseStructure.successResponse(responseData, "Total orders for all drivers retrieved successfully.");

        } catch (Exception e) {
            logger.error("Error fetching total orders for all drivers: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching total orders for all drivers: " + e.getMessage());
        }
    }



    public ResponseEntity<ResponseStructure<Object>> getTopDriverWithHighestLifetimeOrders() {
        try {
            List<Object[]> results = orderDao.findDriverWithHighestTotalOrders();

            if (results.isEmpty()) {
                logger.warn("No driver data available.");
                return ResponseStructure.errorResponse(null, 404, "No driver data available.");
            }

            Object[] topDriver = results.get(0);
            Long driverId = (Long) topDriver[0];
            String driverName = (String) topDriver[1];
            Long totalOrders = (Long) topDriver[2];

            Driver driver = driverDao.getById(driverId);

            if (driver == null) {
                logger.warn("The driver with ID " + driverId + " does not exist in the system.");
                return ResponseStructure.errorResponse(null, 404, "The driver with ID " + driverId + " does not exist in the system.");
            }

            Map<String, Object> responseData = new HashMap<>();
            responseData.put("driverId", driverId);
            responseData.put("driverName", driverName);
            responseData.put("totalOrders", totalOrders);
            responseData.put("profilePic", driver.getProfilePic());

            logger.info("Top driver with the highest lifetime orders retrieved successfully.");
            return ResponseStructure.successResponse(responseData, "Top driver with the highest lifetime orders retrieved successfully.");
        } catch (Exception e) {
            logger.error("Error fetching the top driver: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching the top driver: " + e.getMessage());
        }
    }

}