package com.ot.moto.service;

import com.ot.moto.dao.DriverDao;
import com.ot.moto.dao.SummaryDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Salary;
import com.ot.moto.entity.Summary;
import com.ot.moto.entity.Tam;
import com.ot.moto.repository.*;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class SummaryService {

    @Autowired
    private SummaryDao summaryDao;

    @Autowired
    private DriverDao driverDao;

    @Autowired
    private DriverRepository driverRepository;

    @Autowired
    private SalaryRepository salaryRepository;

    @Autowired
    private TamRepository tamRepository;

    @Autowired
    private SummaryRepository summaryRepository;

    @Autowired
    private OrdersRepository ordersRepository;

    private static final Logger logger = LoggerFactory.getLogger(SummaryService.class);


    public ResponseEntity<ResponseStructure<Object>> getTotalPayToJahez() {
        logger.info("Fetching total 'payToJahez' value");

        try {
            Double payToJahez = summaryDao.findTotalPayToJahez();
            logger.info("Total 'payToJahez': {}", payToJahez);
            return ResponseStructure.successResponse(payToJahez, "Total 'payToJahez' retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching total 'payToJahez'", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching total 'payToJahez': " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTotalSalaryPaid() {
        logger.info("Fetching total 'salary' value");

        try {
            Double salaryPaid = summaryDao.findTotalSalaryPaid();
            logger.info("Total 'salary': {}", salaryPaid);
            return ResponseStructure.successResponse(salaryPaid, "Total 'salary' retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching total 'salary'", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching total 'salary': " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> getTotalProfit() {
        logger.info("Fetching total 'profit' value");

        try {
            Double profit = summaryDao.findTotalProfit();
            logger.info("Total 'profit': {}", profit);
            return ResponseStructure.successResponse(profit, "Total 'profit' retrieved successfully");
        } catch (Exception e) {
            logger.error("Error fetching total 'profit'", e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching total 'profit': " + e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findById(Long id) {
        try {
            Summary summary = summaryDao.findById(id);
            if (Objects.isNull(summary)) {
                logger.warn("No Summary Report found. Invalid ID:" + id);
                return ResponseStructure.errorResponse(null, 404, "Invalid Id:" + id);
            }
            return ResponseStructure.successResponse(summary, "Summary Report found");
        } catch (Exception e) {
            logger.error("Error fetching single Summary Report ", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAll(int page, int size, String field) {
        try {
            Page<Summary> summaryPage = summaryDao.findAll(page, size, field);
            if (summaryPage.isEmpty()) {
                logger.warn("No Summary Reports found.");
                return ResponseStructure.errorResponse(null, 404, "No Summary Reports found");
            }
            return ResponseStructure.successResponse(summaryPage, "All Summary Reports found");
        } catch (Exception e) {
            logger.error("Error fetching Summary Reports", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    public ResponseEntity<InputStreamResource> generateExcelForSummary() {
        try {
            List<Summary> summaryList = summaryRepository.findAll();
            if (summaryList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Summary Data");

            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "DriverName", "Deliveries", "Salary", "Bonus", "PayToJahez", "PaidByTam", "Profit"};
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Summary summary : summaryList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(summary.getId());
                row.createCell(1).setCellValue(summary.getDriverName());
                row.createCell(2).setCellValue(summary.getDeliveries() != null ? summary.getDeliveries() : 0);
                row.createCell(3).setCellValue(summary.getSalary() != null ? summary.getSalary() : 0);
                row.createCell(4).setCellValue(summary.getBonus() != null ? summary.getBonus() : 0.0);
                row.createCell(5).setCellValue(summary.getPayToJahez() != null ? summary.getPayToJahez() : 0.0);
                row.createCell(6).setCellValue(summary.getPaidByTam() != null ? summary.getPaidByTam() : 0.0);
                row.createCell(7).setCellValue(summary.getProfit() != null ? summary.getProfit() : 0.0);
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=summary_data.xlsx");
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

    @Transactional
    public void updateSummary(Driver driver) {
        try {
            logger.info("Updating summary for driver with ID: {}", driver.getId());

            // Fetch existing summary or create a new one
            Summary summary = summaryRepository.findByDriver(driver)
                    .orElseGet(() -> {
                        logger.info("No existing summary found for driver with ID: {}. Creating new summary.", driver.getId());
                        Summary newSummary = new Summary();
                        newSummary.setDriver(driver);
                        return newSummary;
                    });

            // Calculate new values
            Double newSalary = calculateSalary(driver);
            Double newBonus = calculateBonus(driver);
            Double newPayToJahez = calculatePayToJahez(driver);
            Double newPaidByTam = calculatePaidByTam(driver);
            Double newProfit = calculateProfit(driver);

            // Log calculated values to debug issues
            logger.debug("New Salary: {}", newSalary);
            logger.debug("New Bonus: {}", newBonus);
            logger.debug("New Pay to Jahez: {}", newPayToJahez);
            logger.debug("New Paid by Tam: {}", newPaidByTam);
            logger.debug("New Profit: {}", newProfit);

            // Set values conditionally
            summary.setDriverName(driver.getUsername());
            summary.setDeliveries(getTotalDeliveries(driver));
            summary.setSalary(newSalary != null ? newSalary : summary.getSalary());
            summary.setBonus(newBonus != null ? newBonus : summary.getBonus());
            summary.setPayToJahez(newPayToJahez != null ? newPayToJahez : summary.getPayToJahez());
            summary.setPaidByTam(newPaidByTam != null ? newPaidByTam : summary.getPaidByTam());
            summary.setProfit(newProfit != null ? newProfit : summary.getProfit());

            // Save the summary
            summaryRepository.save(summary);

            logger.info("Successfully updated summary for driver with ID: {}", driver.getId());

        } catch (Exception e) {
            logger.error("Error updating summary for driver with ID: {}", driver.getId(), e);
        }
    }


    private Long getTotalDeliveries(Driver driver) {
        try {
            logger.debug("Calculating total deliveries for driver with ID: {}", driver.getId());
            Long totalDeliveries = ordersRepository.countByDriver(driver);
            logger.debug("Total deliveries for driver with ID: {} is {}", driver.getId(), totalDeliveries);
            return totalDeliveries;
        } catch (Exception e) {
            logger.error("Error calculating total deliveries for driver with ID: {}", driver.getId(), e);
            throw new RuntimeException("Failed to calculate total deliveries", e);
        }
    }

    private Double calculateSalary(Driver driver) {
        try {
            logger.debug("Calculating salary for driver with ID: {}", driver.getId());
            Optional<Salary> salary = salaryRepository.findTopByDriverOrderByYearDescMonthDesc(driver);
            Double totalEarnings = salary.map(Salary::getTotalEarnings).orElse(0.0);
            logger.debug("Total salary earnings for driver with ID: {} is {}", driver.getId(), totalEarnings);
            return totalEarnings;
        } catch (Exception e) {
            logger.error("Error calculating salary for driver with ID: {}", driver.getId(), e);
            throw new RuntimeException("Failed to calculate salary", e);
        }
    }

    private Double calculateBonus(Driver driver) {
        try {
            logger.debug("Calculating bonus for driver with ID: {}", driver.getId());
            Double bonus = salaryRepository.findTopByDriverOrderByYearDescMonthDesc(driver)
                    .map(Salary::getBonus).orElse(0.0);
            logger.debug("Total bonus for driver with ID: {} is {}", driver.getId(), bonus);
            return bonus;
        } catch (Exception e) {
            logger.error("Error calculating bonus for driver with ID: {}", driver.getId(), e);
            throw new RuntimeException("Failed to calculate bonus", e);
        }
    }

    private Double calculatePayToJahez(Driver driver) {
        try {
            logger.debug("Calculating pay to Jahez for driver with ID: {}", driver.getId());
            Double payToJahez = tamRepository.findSumPayToJahezByDriver(driver);
            logger.debug("Total pay to Jahez for driver with ID: {} is {}", driver.getId(), payToJahez);
            return payToJahez;
        } catch (Exception e) {
            logger.error("Error calculating pay to Jahez for driver with ID: {}", driver.getId(), e);
            throw new RuntimeException("Failed to calculate pay to Jahez", e);
        }
    }

    private Double calculatePaidByTam(Driver driver) {
        try {
            logger.debug("Calculating paid by Tam for driver with ID: {}", driver.getId());
            Double paidByTam = tamRepository.findSumPaidByTamByDriver(driver);
            logger.debug("Total paid by Tam for driver with ID: {} is {}", driver.getId(), paidByTam);
            return paidByTam;
        } catch (Exception e) {
            logger.error("Error calculating paid by Tam for driver with ID: {}", driver.getId(), e);
            throw new RuntimeException("Failed to calculate paid by Tam", e);
        }
    }

    private Double calculateProfit(Driver driver) {
        try {
            logger.debug("Calculating profit for driver with ID: {}", driver.getId());
            Double salary = calculateSalary(driver);
            Double bonus = calculateBonus(driver);
            Double payToJahez = calculatePayToJahez(driver);
            Double paidByTam = calculatePaidByTam(driver);
            Double profit = salary + bonus - payToJahez - paidByTam;
            logger.debug("Total profit for driver with ID: {} is {}", driver.getId(), profit);
            return profit;
        } catch (Exception e) {
            logger.error("Error calculating profit for driver with ID: {}", driver.getId(), e);
            throw new RuntimeException("Failed to calculate profit", e);
        }
    }
}
