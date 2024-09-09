package com.ot.moto.service;

import com.ot.moto.dao.TamDao;
import com.ot.moto.dto.ResponseStructure;
import com.ot.moto.entity.Driver;
import com.ot.moto.entity.Tam;
import com.ot.moto.repository.DriverRepository;
import com.ot.moto.repository.TamRepository;
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
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TamService {

    @Autowired
    private TamDao tamDao;

    @Autowired
    private TamRepository tamRepository;

    @Autowired
    private DriverRepository driverRepository;


    private static final Logger logger = LoggerFactory.getLogger(TamService.class);

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // Adjust pattern as needed


    public ResponseEntity<ResponseStructure<Object>> uploadTamSheet(Sheet sheet) {
        logger.info("Starting upload of Tam sheet.");
        List<Tam> tamList = new ArrayList<>();

        try {
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);

                if (row == null) {
                    logger.warn("Row " + i + " is null, skipping...");
                    continue;
                }

                Tam tam = parseRowToTam(row);

                if (tam != null && isValidTam(tam)) {
                    if (isDuplicateTam(tam)) {
                        logger.info("Duplicate entry for Key Session Id: " + tam.getKeySessionId() + " at datetime: " + tam.getDateTime());
                        continue;
                    }

                    logger.info("Saving Tam record: " + tam.getDriverName() + " at time: " + tam.getDateTime());
                    deductAmountPending(tam.getMobileNumber(), tam.getPayInAmount());
                    tamList.add(tam);

                    Driver driver = driverRepository.findByPhone((tam.getMobileNumber()));

                } else {
                    logger.warn("Invalid or failed to parse row " + i + ", skipping...");
                }
            }

            if (!tamList.isEmpty()) {
                tamRepository.saveAll(tamList);
                logger.info("Successfully saved " + tamList.size() + " Tam records.");
            } else {
                logger.info("No valid Tam records to save.");
            }

            return ResponseStructure.successResponse(null, "Successfully Parsed");

        } catch (Exception e) {
            logger.error("Error parsing Excel Tam Sheet", e);
            return ResponseStructure.errorResponse(null, 500, e.getMessage());
        }
    }

    private boolean isValidTam(Tam tam) {
        return tam.getSTAN() != null && tam.getDateTime() != null; // Add more validation if needed
    }

    private Tam parseRowToTam(Row row) {
        try {
            LocalDateTime dateTime = parseLocalDateTime(row.getCell(1));
            String keySessionId = parseString(row.getCell(2));
            String status = parseString(row.getCell(3));
            String serviceName = parseString(row.getCell(4));
            String merchantName = parseString(row.getCell(5));
            String terminalId = parseString(row.getCell(6));
            String location = parseString(row.getCell(7));
            String branchName = parseString(row.getCell(8));
            String customerPhone = parseString(row.getCell(12));
            Long rpnPayment = parseLong(row.getCell(13));
            Long STAN = parseLong(row.getCell(14));
            Double amountToPay = parseDouble(row.getCell(15));
            Double payInAmount = parseDouble(row.getCell(16));
            String payInAmountData = parseString(row.getCell(17));
            String paymentMode = parseString(row.getCell(20));
            Long usedVoucherNumber = parseLong(row.getCell(21));
            Long confirmationId = parseLong(row.getCell(22));
            Boolean vouchered = parseBoolean(row.getCell(23));
            LocalDateTime holdTrxnDateTime = parseLocalDateTime(row.getCell(24));
            LocalDateTime confTrxnDateTime = parseLocalDateTime(row.getCell(25));
            String responseMessage = parseString(row.getCell(32));
            Long merchantId = parseLong(row.getCell(33));
            Long jahezRiderId = parseLong(row.getCell(34));
            Long cprNumber = parseLong(row.getCell(35));
            String driverCompanyName = parseString(row.getCell(36));
            String driverCompanyJahezId = parseString(row.getCell(37));
            Long mobileNumber = parseLong(row.getCell(38));
            String driverName = parseString(row.getCell(39));

            return buildTam(dateTime, keySessionId, status, serviceName, merchantName, terminalId, location,
                    branchName, customerPhone, rpnPayment, STAN, amountToPay, payInAmount, payInAmountData, paymentMode,
                    usedVoucherNumber, confirmationId, vouchered, holdTrxnDateTime, confTrxnDateTime, responseMessage,
                    merchantId, jahezRiderId, cprNumber, driverCompanyName, driverCompanyJahezId, mobileNumber, driverName);
        } catch (Exception e) {
            logger.error("Error parsing row: " + row.getRowNum(), e);
            return null;
        }
    }

    private boolean isDuplicateTam(Tam tam) {
        return tamRepository.findBykeySessionId(tam.getKeySessionId()) != null;
    }

    private Tam buildTam(LocalDateTime dateTime, String keySessionId, String status, String serviceName,
                         String merchantName, String terminalId, String location, String branchName,
                         String customerPhone, Long rpnPayment, Long STAN, Double amountToPay, Double payInAmount,
                         String payInAmountData, String paymentMode, Long usedVoucherNumber, Long confirmationId,
                         Boolean vouchered, LocalDateTime holdTrxnDateTime, LocalDateTime confTrxnDateTime,
                         String responseMessage, Long merchantId, Long jahezRiderId, Long cprNumber,
                         String driverCompanyName, String driverCompanyJahezId, Long mobileNumber, String driverName) {

        Tam tam = new Tam();
        tam.setDateTime(dateTime);
        tam.setKeySessionId(keySessionId);
        tam.setStatus(status);
        tam.setServiceName(serviceName);
        tam.setMerchantName(merchantName);
        tam.setTerminalId(terminalId);
        tam.setLocation(location);
        tam.setBranchName(branchName);
        tam.setCustomerPhone(customerPhone);
        tam.setRpnPayment(rpnPayment);
        tam.setSTAN(STAN);
        tam.setAmountToPay(amountToPay);
        tam.setPayInAmount(payInAmount);
        tam.setPayInAmountData(payInAmountData);
        tam.setPaymentMode(paymentMode);
        tam.setUsedVoucherNumber(usedVoucherNumber);
        tam.setConfirmationId(confirmationId);
        tam.setVouchered(vouchered);
        tam.setHoldTrxnDateTime(holdTrxnDateTime);
        tam.setConfTrxnDateTime(confTrxnDateTime);
        tam.setResponseMessage(responseMessage);
        tam.setMerchantId(merchantId);
        tam.setJahezRiderId(jahezRiderId);
        tam.setCprNumber(cprNumber);
        tam.setDriverCompanyName(driverCompanyName);
        tam.setDriverCompanyJahezId(driverCompanyJahezId);
        tam.setMobileNumber(mobileNumber);
        tam.setDriverName(driverName);

        return tam;
    }

    private Long parseLong(Cell cell) {
        try {
            if (cell == null) return null;
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (long) cell.getNumericCellValue();
                case STRING:
                    return Long.parseLong(cell.getStringCellValue().trim());
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing long from cell: " + cell, e);
            return null;
        }
    }

    private Double parseDouble(Cell cell) {
        try {
            if (cell == null) return null;
            switch (cell.getCellType()) {
                case NUMERIC:
                    return (Double) cell.getNumericCellValue();
                case STRING:
                    return Double.parseDouble(cell.getStringCellValue().trim());
                default:
                    return null;
            }
        } catch (Exception e) {
            logger.error("Error parsing long from cell: " + cell, e);
            return null;
        }
    }

    private String parseString(Cell cell) {
        try {
            if (cell == null) return null;
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    return String.valueOf(cell.getNumericCellValue());
                default:
                    return cell.toString().trim();
            }
        } catch (Exception e) {
            logger.error("Error parsing string from cell: " + cell, e);
            return null;
        }
    }

    private Boolean parseBoolean(Cell cell) {
        try {
            if (cell == null) return null;
            String cellValue = cell.toString().trim().toLowerCase();
            return "true".equals(cellValue) || "1".equals(cellValue);
        } catch (Exception e) {
            logger.error("Error parsing boolean from cell: " + cell, e);
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(Cell cell) {
        try {
            if (cell == null) return null;
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getLocalDateTimeCellValue();
            } else if (cell.getCellType() == CellType.STRING) {
                String cellString = cell.getStringCellValue();
                return LocalDateTime.parse(cellString.trim(), dateTimeFormatter);
            }
            return null;
        } catch (Exception e) {
            logger.error("Error parsing datetime from cell: " + cell, e);
            return null;
        }
    }

    private void deductAmountPending(Long mobileNumber, double amount) {
        Driver driver = driverRepository.findByPhone(mobileNumber);
        if (Objects.nonNull(driver)) {
            driver.setPaidByTam(Optional.ofNullable(driver.getPaidByTam()).orElse(0.0) + amount);
            driver.setPayToJahez(driver.getCodAmount() - amount);
            driver.setAmountPending(driver.getAmountPending() - amount);
            driverRepository.save(driver);
        }
    }

    public ResponseEntity<ResponseStructure<Object>> findAll(int page, int size, String field) {
        logger.info("Fetching tam with page number {}, page size {}, sorted by field {}", page, size, field);

        try {
            PageRequest pageRequest = PageRequest.of(page, size, Sort.by(field));
            Page<Tam> ordersPage = tamRepository.findAll(pageRequest);

            if (ordersPage.isEmpty()) {
                logger.warn("No tam found for page number {} and page size {}", page, size);
                return ResponseStructure.errorResponse(null, 404, "No tam found");
            }
            logger.info("Successfully fetched tam for page number {} and page size {}", page, size);
            return ResponseStructure.successResponse(ordersPage, "Tam found successfully");
        } catch (Exception e) {
            logger.error("Error fetching tam: {}", e.getMessage(), e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching tam: " + e.getMessage());
        }
    }


    public ResponseEntity<ResponseStructure<Object>> getByJahezRiderId(Long jahezRiderId) {
        logger.info("Fetching tam with jahezRiderId: {}", jahezRiderId);

        try {
            if (jahezRiderId == null) {
                logger.warn("JahezRiderId is null");
                return ResponseStructure.errorResponse(null, 400, "JahezRiderId cannot be null");
            }

            List<Tam> tam = tamRepository.findByJahezRiderId(jahezRiderId);

            if (tam == null) {
                logger.warn("No Jahez rider found with ID: {}", jahezRiderId);
                return ResponseStructure.errorResponse(null, 404, "No Jahez rider found with ID: " + jahezRiderId);
            }

            logger.info("Successfully fetched Jahez rider with ID: {}", jahezRiderId);
            return ResponseStructure.successResponse(tam, "Jahez rider found successfully");
        } catch (Exception e) {
            logger.error("Error fetching Jahez rider with ID: {}", jahezRiderId, e);
            return ResponseStructure.errorResponse(null, 500, "Error fetching Jahez rider with ID: " + jahezRiderId);
        }
    }

    public ResponseEntity<ResponseStructure<List<Tam>>> findByDriverNameContaining(String name) {
        ResponseStructure<List<Tam>> responseStructure = new ResponseStructure<>();

        List<Tam> driverList = tamDao.findByDriverNameContaining(name);
        if (driverList.isEmpty()) {
            responseStructure.setStatus(HttpStatus.NOT_FOUND.value());
            responseStructure.setMessage("Driver Not Found in TAM  ");
            responseStructure.setData(null);
            return new ResponseEntity<>(responseStructure, HttpStatus.NOT_FOUND);
        } else {
            responseStructure.setStatus(HttpStatus.OK.value());
            responseStructure.setMessage("Driver Found in TAM ");
            responseStructure.setData(driverList);
            return new ResponseEntity<>(responseStructure, HttpStatus.OK);
        }
    }


    public ResponseEntity<InputStreamResource> generateExcelForAll() {
        try {
            List<Tam> tamList = tamRepository.findAll();
            if (tamList.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            XSSFWorkbook workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Tam Data");

            Row headerRow = sheet.createRow(0);
            String[] headers = {
                    "ID", "DateTime", "KeySessionId", "Status", "ServiceName", "MerchantName", "TerminalId",
                    "Location", "BranchName", "Product", "Quantity", "Phone", "CustomerPhone", "RpnPayment",
                    "STAN", "AmountToPay", "PayInAmount", "PayInAmountData", "PayOutAmount", "PayOutAmountData",
                    "PaymentMode", "UsedVoucherNumber", "ConfirmationId", "IsVouchered", "HoldTrxnDateTime",
                    "ConfTrxnDateTime", "PayInVoucher", "PayoutVoucher", "AuthCode", "CPR", "VoucherPhoneNumber",
                    "ResponseMessage", "MerchantId", "JahezRiderId", "CprNumber", "DriverCompanyName",
                    "DriverCompanyJahezId", "MobileNumber", "DriverName"
            };
            for (int i = 0; i < headers.length; i++) {
                headerRow.createCell(i).setCellValue(headers[i]);
            }

            int rowNum = 1;
            for (Tam tam : tamList) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(tam.getId());
                row.createCell(1).setCellValue(tam.getDateTime() != null ? tam.getDateTime().toString() : "");
                row.createCell(2).setCellValue(tam.getKeySessionId());
                row.createCell(3).setCellValue(tam.getStatus());
                row.createCell(4).setCellValue(tam.getServiceName());
                row.createCell(5).setCellValue(tam.getMerchantName());
                row.createCell(6).setCellValue(tam.getTerminalId());
                row.createCell(7).setCellValue(tam.getLocation());
                row.createCell(8).setCellValue(tam.getBranchName());
                row.createCell(9).setCellValue(tam.getProduct());
                row.createCell(10).setCellValue(tam.getQuantity() != null ? tam.getQuantity() : 0);
                row.createCell(11).setCellValue(tam.getPhone());
                row.createCell(12).setCellValue(tam.getCustomerPhone());
                row.createCell(13).setCellValue(tam.getRpnPayment() != null ? tam.getRpnPayment() : 0);
                row.createCell(14).setCellValue(tam.getSTAN() != null ? tam.getSTAN() : 0);
                row.createCell(15).setCellValue(tam.getAmountToPay() != null ? tam.getAmountToPay() : 0.0);
                row.createCell(16).setCellValue(tam.getPayInAmount() != null ? tam.getPayInAmount() : 0.0);
                row.createCell(17).setCellValue(tam.getPayInAmountData());
                row.createCell(18).setCellValue(tam.getPayOutAmount() != null ? tam.getPayOutAmount() : 0.0);
                row.createCell(19).setCellValue(tam.getPayOutAmountData());
                row.createCell(20).setCellValue(tam.getPaymentMode());
                row.createCell(21).setCellValue(tam.getUsedVoucherNumber() != null ? tam.getUsedVoucherNumber() : 0);
                row.createCell(22).setCellValue(tam.getConfirmationId() != null ? tam.getConfirmationId() : 0);
                row.createCell(23).setCellValue(tam.isVouchered());
                row.createCell(24).setCellValue(tam.getHoldTrxnDateTime() != null ? tam.getHoldTrxnDateTime().toString() : "");
                row.createCell(25).setCellValue(tam.getConfTrxnDateTime() != null ? tam.getConfTrxnDateTime().toString() : "");
                row.createCell(26).setCellValue(tam.getPayInVoucher());
                row.createCell(27).setCellValue(tam.getPayoutVoucher());
                row.createCell(28).setCellValue(tam.getAuthCode() != null ? tam.getAuthCode() : 0);
                row.createCell(29).setCellValue(tam.getCPR() != null ? tam.getCPR() : 0);
                row.createCell(30).setCellValue(tam.getVoucherPhoneNumber() != null ? tam.getVoucherPhoneNumber() : 0);
                row.createCell(31).setCellValue(tam.getResponseMessage());
                row.createCell(32).setCellValue(tam.getMerchantId() != null ? tam.getMerchantId() : 0);
                row.createCell(33).setCellValue(tam.getJahezRiderId() != null ? tam.getJahezRiderId() : 0);
                row.createCell(34).setCellValue(tam.getCprNumber() != null ? tam.getCprNumber() : 0);
                row.createCell(35).setCellValue(tam.getDriverCompanyName());
                row.createCell(36).setCellValue(tam.getDriverCompanyJahezId());
                row.createCell(37).setCellValue(tam.getMobileNumber() != null ? tam.getMobileNumber() : 0);
                row.createCell(38).setCellValue(tam.getDriverName());
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            workbook.close();

            ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            InputStreamResource resource = new InputStreamResource(byteArrayInputStream);

            HttpHeaders headers1 = new HttpHeaders();
            headers1.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=tam_data.xlsx");
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


    public Double getSumPayInAmountForCurrentMonth() {
        LocalDateTime startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth()).atTime(23, 59, 59);

        try {
            return tamDao.getSumPayInAmountForCurrentMonth(startOfMonth, endOfMonth);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching sum for current month: " + e.getMessage(), e);
        }
    }

    public Double getSumPayInAmountForYesterday() {
        LocalDate yesterday = LocalDate.now().minusDays(1);
        LocalDateTime startOfDay = yesterday.atStartOfDay();
        LocalDateTime endOfDay = yesterday.atTime(23, 59, 59);

        try {
            return tamDao.getSumPayInAmountForDateRange(startOfDay, endOfDay);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching sum for yesterday: " + e.getMessage(), e);
        }
    }
}