package com.ot.moto.service;

import com.ot.moto.dao.OrgReportsDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.OrgReports;
import com.ot.moto.repository.OrgReportsRepository;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
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
}
