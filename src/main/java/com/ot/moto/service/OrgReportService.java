package com.ot.moto.service;

import com.ot.moto.dao.OrgReportsDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.OrgReports;
import com.ot.moto.entity.Tam;
import com.ot.moto.repository.OrgReportsRepository;
import org.apache.poi.ss.usermodel.*;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

@Service
public class OrgReportService {

    @Autowired
    private OrgReportsDao orgReportsDao;

    @Autowired
    private OrgReportsRepository orgReportsRepository;

    private static final Logger logger = LoggerFactory.getLogger(OrgReportService.class);

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy hh:mm:ss a", Locale.ENGLISH);


    public ResponseEntity<ResponseStructure<Object>> uploadOrgReports(Sheet sheet) {
        List<OrgReports> orgReportsList = new ArrayList<>();
        try {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    logger.warn("Row {} is null, skipping...", i);
                    continue;
                }

                OrgReports orgReport = parseRowToOrgReport(row);

                if (orgReport == null || !isValidReport(orgReport)) {
                    logger.warn("Invalid or failed to parse row {}, skipping...", i);
                    continue;
                }

                if (isDuplicateReport(orgReport)) {
                    logger.info("Duplicate entry for DID: {} at dispatch time: {}", orgReport.getDid(), orgReport.getDispatchTime());
                    continue;
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

            return ResponseStructure.successResponse(null, "Successfully Parsed");

        } catch (Exception e) {
            logger.error("Error parsing Excel OrgReports", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private boolean isValidReport(OrgReports orgReport) {
        return orgReport.getDid() != null && orgReport.getDispatchTime() != null;
    }

    private OrgReports parseRowToOrgReport(Row row) {
        try {
            Long no = parseLong(row.getCell(0));
            Long did = parseLong(row.getCell(1));
            Long refId = parseLong(row.getCell(2));
            String driverName = parseString(row.getCell(3));
            String driverUsername = parseString(row.getCell(4));
            String driverId = parseString(row.getCell(5));
            Double amount = parseDouble(row.getCell(6));
            Double price = parseDouble(row.getCell(7));
            Double driverDebitAmount = parseDouble(row.getCell(8));
            Double driverCreditAmount = parseDouble(row.getCell(9));
            Boolean isFreeOrder = parseBoolean(row.getCell(10));
            LocalDateTime dispatchTime = parseDateTime(parseString(row.getCell(11)));
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

    private boolean isDuplicateReport(OrgReports orgReport) {
        return orgReportsRepository.findByDidAndDispatchTime(orgReport.getDid(), orgReport.getDispatchTime()) != null;
    }

    private OrgReports buildOrgReport(Long no, Long did, Long refId, String driverName, String driverUsername, String driverId,
                                      Double amount, Double price, Double driverDebitAmount, Double driverCreditAmount,
                                      Boolean isFreeOrder, LocalDateTime dispatchTime, String subscriber, Boolean driverPaidOrg,
                                      Boolean orgSettled, Boolean driverSettled) {

        OrgReports orgReport = new OrgReports();
        orgReport.setNo(no);
        orgReport.setDid(did);
        orgReport.setRefId(refId);
        orgReport.setDriverName(driverName);
        orgReport.setDriverUsername(driverUsername);
        orgReport.setDriverId(driverId);
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
        if (cell == null) return null;
        try {
            return cell.getCellType() == CellType.NUMERIC ? (long) cell.getNumericCellValue() : Long.parseLong(cell.getStringCellValue().trim());
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
        if (cell == null) return null;
        try {
            return cell.getCellType() == CellType.NUMERIC ? cell.getNumericCellValue() : Double.parseDouble(cell.getStringCellValue().trim());
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
        try {
            logger.info("Parsing date time: {}", dateTimeStr);
            return LocalDateTime.parse(dateTimeStr, FORMATTER);
        } catch (DateTimeParseException e) {
            logger.error("Failed to parse date time: {}", dateTimeStr, e);
            return null;
        }
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
            List<OrgReports> orgReports = orgReportsDao.findByDriverId(driverId);
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

    public ResponseEntity<ResponseStructure<List<OrgReports>>> findByDriverName(String name) {
        ResponseStructure<List<OrgReports>> responseStructure = new ResponseStructure<>();

        List<OrgReports> driverList = orgReportsDao.findByDriverName(name);
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
            String[] headers = {
                    "ID", "No", "DID", "Ref ID", "Driver Name", "Driver Username", "Driver ID",
                    "Amount", "Price", "Driver Debit Amount", "Driver Credit Amount", "Is Free Order",
                    "Dispatch Time", "Subscriber", "Driver Paid Org", "Org Settled", "Driver Settled"
            };
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

            return ResponseEntity.ok()
                    .headers(headers1)
                    .contentLength(outputStream.size())
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    public ResponseEntity<Object> getTopDriverWithHighestAmountForCurrentMonth() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endDate = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59).withNano(999999999);

        List<Object[]> results = orgReportsDao.findDriverWithHighestAmountForCurrentMonth(startDate, endDate);

        if (results.isEmpty()) {
            return ResponseEntity.ok("No data available for the current month.");
        }

        Object[] topDriver = results.get(0);
        String driverId = (String) topDriver[0];
        String driverName = (String) topDriver[1];
        Double totalAmount = (Double) topDriver[2];

        String responseMessage = String.format("Driver with ID: %s, Name: %s collected the highest amount: %s", driverId, driverName, totalAmount);

        return ResponseEntity.ok(responseMessage);
    }
}