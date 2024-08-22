package com.ot.moto.service;

import com.ot.moto.dao.OrderDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Orders;
import com.ot.moto.repository.OrdersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

@Service
public class OrderService {

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private OrdersRepository ordersRepository;

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
}